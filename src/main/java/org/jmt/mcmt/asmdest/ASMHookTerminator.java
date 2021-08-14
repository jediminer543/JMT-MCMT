package org.jmt.mcmt.asmdest;

import java.time.Instant;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.MCMT;
import org.jmt.mcmt.commands.StatsCommand;
import org.jmt.mcmt.config.GeneralConfig;
import org.jmt.mcmt.serdes.SerDesHookTypes;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.filter.ISerDesFilter;

import net.minecraft.block.BlockEventData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.CrashReportExtender;

/**
 * This is where all current ASM hooks are terminated
 * 
 * So DON'T rename this file (Or there will be a lot of other work todo)
 * 
 * Fun point: So because this is hooking into a lot of the stuff, be careful what you reference here
 * I attempted to reference a function on {@link GeneralConfig} and it got VERY angry at me with a "class refuses to load" error
 * So remember that if you start getting class loading errors
 * 
 * 
 * TODO: Add more docs
 * 
 * @author jediminer543
 *
 */
public class ASMHookTerminator {

	private static final Logger LOGGER = LogManager.getLogger();

	//	static Phaser phaser;
	static ConcurrentHashMap<String, CompletableFuture<Void>> executionStack = new ConcurrentHashMap<>();
	static ExecutorService exec;
	static MinecraftServer mcServer;
	static AtomicBoolean isTicking = new AtomicBoolean();
	static AtomicInteger threadID = new AtomicInteger();


	public static void setupThreadpool(int parallelism) {
		threadID = new AtomicInteger();
		final ClassLoader cl = MCMT.class.getClassLoader();
		ForkJoinWorkerThreadFactory fjpf = p -> {
			ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
			fjwt.setName("MCMT-Pool-Thread-"+threadID.getAndIncrement());
			regThread("MCMT", fjwt);
			fjwt.setContextClassLoader(cl);
			return fjwt;
		};

		exec = new ForkJoinPool(
				parallelism,
				fjpf,	
				null, false);
	}

	/**
	 * Creates and sets up the thread pool
	 */
	static {
		// Must be static here due to class loading shenanagins
		setupThreadpool(4);
	}

	static Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<String, Set<Thread>>();

	// Statistics
	public static AtomicInteger currentWorlds = new AtomicInteger();
	public static AtomicInteger currentEnts = new AtomicInteger();
	public static AtomicInteger currentTEs = new AtomicInteger();
	public static AtomicInteger currentEnvs = new AtomicInteger();


	public static void regThread(String poolName, Thread thread) {
		mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
	}

	public static boolean isThreadPooled(String poolName, Thread t) {
		return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
	}

	public static boolean serverExecutionThreadPatch(MinecraftServer ms) {
		return isThreadPooled("MCMT", Thread.currentThread());
	}

	// create a CompletableFuture, run it, and add it to the execution stack
	private static void execute(String taskName, Runnable task) {
		// ensure there is no accidental key collision
		while (executionStack.containsKey(taskName)) taskName = taskName + "+";
		String finalTaskName = taskName;
		// add a CompletableFuture to the execution stack that runs the given task, then removes itself when done
		executionStack.put(finalTaskName, CompletableFuture.runAsync(task, exec).thenRun(() -> {executionStack.remove(finalTaskName);}));
	}

	private static void awaitCompletion() {
		Instant before = Instant.now();
		// convert all outstanding ticks to one CompletableFuture to wait on
		CompletableFuture<Void> allTicks = CompletableFuture.allOf(executionStack.values().toArray(new CompletableFuture[executionStack.size()]));
		try {
			// wait on all executing ticks for up to 1 second (20 ticks)
			allTicks.get(1, TimeUnit.SECONDS);
			LOGGER.info(executionStack.size());
		} catch (TimeoutException e) {
			LOGGER.error("This tick has taken longer than 1 second, investigating...");
			LOGGER.error("Current stuck tasks:");
			StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
			for (String taskName : executionStack.keySet()) sj.add(taskName);
			LOGGER.error(sj.toString());

			if (GeneralConfig.continueAfterStuckTick) {
				LOGGER.fatal("CONTINUING AFTER STUCK TICK! I REALLY hope you have backups...");
				allTicks.cancel(true); // cancel combined CompletableFuture
				executionStack.clear(); // empty the execution stack for another go-around
			} else {
				LOGGER.error("Continuing to wait for tick to complete... (don't hold your breath)");
				try {
					allTicks.get(); // wait for this tick to complete (but if we're here, it probably won't)
				} catch (InterruptedException | ExecutionException e1) {
					LOGGER.fatal("Failed to wait for tick: ", e1);
				} 
			}
		} catch (ExecutionException | InterruptedException e) {
			LOGGER.fatal("Tick execution failed: ", e);
			allTicks.cancel(true);
			executionStack.clear(); // clear execution stack without prompt, we're in uncharted waters anyways
		}

		// debug data for how long the tick took
		LOGGER.info("This tick took " + Instant.now().minusMillis(before.toEpochMilli()).toEpochMilli() + " ms.");

		if (executionStack.size() > 0) {
			LOGGER.fatal("Execution stack was not empty before continuing to next tick! " + executionStack.size());
			
			StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
			for (String taskName : executionStack.keySet()) sj.add(taskName);
			LOGGER.fatal(sj.toString());
			
			executionStack.clear();
		}
	}

	public static void preTick(MinecraftServer server) {
		isTicking.set(true);
		mcServer = server;
		StatsCommand.setServer(mcServer);
	}

	public static void callTick(ServerWorld serverworld, BooleanSupplier hasTimeLeft, MinecraftServer server) {
		if (GeneralConfig.disabled || GeneralConfig.disableWorld) {
			try {
				serverworld.tick(hasTimeLeft);
			} catch (Exception e) {
				throw e;
			} finally {
				net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
			}
			return;
		}

		if (mcServer != server) {
			LOGGER.warn("Multiple servers?");
			GeneralConfig.disabled = true;
			serverworld.tick(hasTimeLeft);
			net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
			return;
		} else {
			String taskName =  "WorldTick: " + serverworld.toString() + "@" + serverworld.hashCode();
			execute(taskName, () -> {
				try {
					currentWorlds.incrementAndGet();
					serverworld.tick(hasTimeLeft);
					if (!GeneralConfig.disableWorldPostTick) {
						exec.execute(() -> {
							//ForkJoinPool.managedBlock(
							//		new RunnableManagedBlocker(() ->  { 
							synchronized (net.minecraftforge.fml.hooks.BasicEventHooks.class) {
								net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
							}
							//		}));
							//} catch (InterruptedException e) {
							//	e.printStackTrace();
						});
					} else {
						net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
					}
				} finally {
					currentWorlds.decrementAndGet();
				}
			});
		}
	}

	public static void callEntityTick(Entity entityIn, ServerWorld serverworld) {
		if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
			entityIn.tick();
			return;
		}
		String taskName = "EntityTick: " + entityIn.toString() + "@" + entityIn.hashCode();
		execute(taskName, () -> {
			try {
				//currentEnts.incrementAndGet();
				//entityIn.tick();
				final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.EntityTick, entityIn.getClass());
				currentTEs.incrementAndGet();
				if (filter != null) {
					filter.serialise(entityIn::tick, entityIn, entityIn.getPosition(), serverworld, SerDesHookTypes.EntityTick);
				} else {
					entityIn.tick();
				}
			} finally {
				currentEnts.decrementAndGet();
			}
		});
	}

	public static void callTickEnvironment(ServerWorld world, Chunk chunk, int k, ServerChunkProvider scp) {
		if (GeneralConfig.disabled  || GeneralConfig.disableEnvironment) {
			world.tickEnvironment(chunk, k);
			return;
		}
		String taskName = "EnvTick: " + chunk.toString() + "@" + chunk.hashCode();
		execute(taskName, () -> {
			try {
				currentEnvs.incrementAndGet();
				world.tickEnvironment(chunk, k);
			} finally {
				currentEnvs.decrementAndGet();
			}
		});
	}

	public static boolean filterTickableEntity(ITickableTileEntity tte) {
		boolean isLocking = false;
		if (GeneralConfig.teBlackList.contains(tte.getClass())) {
			isLocking = true;
		}
		// Apparently a string starts with check is faster than Class.getPackage; who knew (I didn't)
		if (!isLocking && GeneralConfig.chunkLockModded && !tte.getClass().getName().startsWith("net.minecraft.tileentity.")) {
			isLocking = true;
		}
		if (isLocking && GeneralConfig.teWhiteList.contains(tte.getClass())) {
			isLocking = false;
		}
		if (tte instanceof PistonTileEntity) {
			isLocking = true;
		}
		return isLocking;
	}

	public static void callTileEntityTick(ITickableTileEntity tte, World world) {
		if (GeneralConfig.disabled  || GeneralConfig.disableTileEntity || !(world instanceof ServerWorld)) {
			tte.tick();
			return;
		}
		String taskName = null;
		taskName = "TETick: " + tte.toString()  + "@" + tte.hashCode();
		execute(taskName, () -> {
			try {
				//final boolean doLock = filterTE(tte);
				final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.TETick, tte.getClass());
				currentTEs.incrementAndGet();
				if (filter != null) {
					filter.serialise(tte::tick, tte, ((TileEntity)tte).getPos(), world, SerDesHookTypes.TETick);
				} else {
					tte.tick();
				}
			} catch (Exception e) {
				System.err.println("Exception ticking TE at " + ((TileEntity) tte).getPos());
				e.printStackTrace();
			} finally {
				currentTEs.decrementAndGet();
			}
		});
	}

	public static void sendQueuedBlockEvents(Deque<BlockEventData> d, ServerWorld sw) {
		Iterator<BlockEventData> bed = d.iterator();
		while (bed.hasNext()) {
			BlockEventData blockeventdata = bed.next();
			if (sw.fireBlockEvent(blockeventdata)) {
				/* 1.16.1 code; AKA the only thing that changed  */
				sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.func_234923_W_(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
				/* */
				/* 1.15.2 code; AKA the only thing that changed  
				sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.getDimension().getType(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
				/* */
			}
			if (!isTicking.get()) {
				LOGGER.fatal("Block updates outside of tick");
			}
			bed.remove();
		}
	}

	public static void postTick(MinecraftServer server) {
		if (mcServer != server) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			awaitCompletion();
		}
	}

	public static String populateCrashReport() {
		StringBuilder confInfo = new StringBuilder();
		confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("Config Info:"); confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("\t- Disabled: "); 
		confInfo.append(GeneralConfig.disabled); confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("\t- World Disabled: "); 
		confInfo.append(GeneralConfig.disableWorld); 
		confInfo.append("(onPostTick Disabled: "); confInfo.append(GeneralConfig.disableWorldPostTick); confInfo.append(")\n");
		confInfo.append("\t\t"); confInfo.append("\t- Entity Disabled: "); 
		confInfo.append(GeneralConfig.disableEntity); confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("\t- Env Disabled: "); 
		confInfo.append(GeneralConfig.disableEnvironment); confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("\t- TE Disabled: "); 
		confInfo.append(GeneralConfig.disableTileEntity); confInfo.append("\n");
		confInfo.append("\t\t"); confInfo.append("\t- SCP Disabled: "); 
		confInfo.append(GeneralConfig.disableChunkProvider); confInfo.append("\n");
		//TODO expand on TE settings
		if (GeneralConfig.opsTracing) {
			confInfo.append("\t\t"); confInfo.append("-- Running Operations Begin -- "); confInfo.append("\n");
			for (String s : executionStack.keySet()) {
				confInfo.append("\t\t"); confInfo.append("\t"); confInfo.append(s); confInfo.append("\n");
			}
			confInfo.append("\t\t"); confInfo.append("-- Running Operations End -- "); confInfo.append("\n");
		}
		return confInfo.toString();
	}

	static {
		CrashReportExtender.registerCrashCallable("MCMT", ASMHookTerminator::populateCrashReport);
	}

	public static <T> void fixSTL(ServerTickList<T> stl) {
		LOGGER.debug("FixSTL Called");
		stl.pendingTickListEntriesTreeSet.addAll(stl.pendingTickListEntriesHashSet);
	}

	//Below is debug code for science reasons
	/*
	 * 	static Random debugRand = new Random();
	//Debug section begin
	if (true) {
		try {
			LOGGER.error("Locking");
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	//End Debug Section
	 */
}

