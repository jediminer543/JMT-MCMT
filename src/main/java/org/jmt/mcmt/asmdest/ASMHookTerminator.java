package org.jmt.mcmt.asmdest;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.jmt.mcmt.paralelised.GatedLock;
import org.jmt.mcmt.serdes.SerDesHookTypes;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.filter.ISerDesFilter;
import org.jmt.mcmt.serdes.pools.PostExecutePool;

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
	public static ConcurrentHashMap<String, Runnable> worldExecutionStack = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Runnable> entityExecutionStack = new ConcurrentHashMap<>();
	
	public static Set<String> tracerStack = null;
	
	private static GatedLock stackLock = new GatedLock();
	public static ExecutorService exec;
	static MinecraftServer mcServer;
	static AtomicBoolean isTicking = new AtomicBoolean();
	static AtomicInteger threadID = new AtomicInteger();

	// Statistics
	public static AtomicInteger currentWorlds = new AtomicInteger();
	public static AtomicInteger currentEnts = new AtomicInteger();
	public static AtomicInteger currentTEs = new AtomicInteger();
	public static AtomicInteger currentEnvs = new AtomicInteger();
	
	

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

	public static void regThread(String poolName, Thread thread) {
		mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
	}

	public static boolean isThreadPooled(String poolName, Thread t) {
		return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
	}

	public static boolean serverExecutionThreadPatch(MinecraftServer ms) {
		return isThreadPooled("MCMT", Thread.currentThread());
	}

	// Add a Runnable to the execution stack
	private static void execute(String taskName, Runnable task, ConcurrentHashMap<String, Runnable> stack) {
		// ensure there is no accidental key collision
		while (stack.containsKey(taskName)) taskName = taskName + "+";
		String finalTaskName = taskName;
		// add a CompletableFuture to the execution stack that runs the given task
		stack.put(finalTaskName, () -> {
			task.run();
			stack.remove(finalTaskName);
		});
	}

	private static void awaitCompletion(ConcurrentHashMap<String, Runnable> waitOn) {
		if (waitOn.size() == 0) return; // avoid hefty operations if we don't need them
		// don't re-execute if code is already running
		if (stackLock.isLocked(waitOn)) {
			stackLock.waitForUnlock(waitOn);
			return;
		}
		stackLock.lockOn(waitOn);

		if (GeneralConfig.opsTracing)
			tracerStack = new HashSet<>(waitOn.keySet());

		// loop
		while (!waitOn.isEmpty()) {
			// execute every queued tick
			List<CompletableFuture<Void>> allTasks = new ArrayList<>(waitOn.size());
			for (Entry<String, Runnable> x : waitOn.entrySet()) {
				allTasks.add(CompletableFuture.runAsync(x.getValue(), exec));
			}
			// convert all outstanding ticks to one CompletableFuture to wait on
			CompletableFuture<Void> tickSum = CompletableFuture.allOf(allTasks.toArray(new CompletableFuture[allTasks.size()]));
			try {
				// wait on all executing ticks for up to 1 second (20 ticks)
				tickSum.get(1, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				LOGGER.error("This tick has taken longer than 1 second, investigating...");
				LOGGER.error("Tick status: " + (tickSum.isDone() ? "done" : "not done"));
				LOGGER.error("Initial queue size: " + allTasks.size());
				
				if (GeneralConfig.opsTracing) {
					// get all ticks still in queue that were also in the starting queue
					tracerStack.retainAll(waitOn.keySet());
					
					LOGGER.error("Current stuck ticks in queue:");
					StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
					for (String taskName : tracerStack) sj.add(taskName);
					LOGGER.error(sj.toString());
					LOGGER.error("=====");
				}
				
				LOGGER.error("Current queue:");
				StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
				for (String taskName : waitOn.keySet()) sj.add(taskName);
				LOGGER.error(sj.toString());

				if (GeneralConfig.continueAfterStuckTick) {
					LOGGER.fatal("CONTINUING AFTER STUCK TICK! I REALLY hope you have backups...");
					tickSum.cancel(true); // cancel combined CompletableFuture
				} else {
					LOGGER.error("Continuing to wait for tick to complete... (don't hold your breath)");
					try {
						tickSum.get(); // wait for this tick to complete (but if we're here, it probably won't)
						for (CompletableFuture<Void> i : allTasks) i.get();
					} catch (InterruptedException | ExecutionException e1) {
						LOGGER.fatal("Failed to wait for tick: ", e1);
					} 
				}
			} catch (ExecutionException | InterruptedException e) {
				LOGGER.fatal("Tick execution failed: ", e);
				tickSum.cancel(true);
			}

			// debug data for how long the tick took
			//			if (GeneralConfig.opsTracing)
			//				LOGGER.info("This tick took " + Instant.now().minusMillis(before.toEpochMilli()).toEpochMilli() + " ms.");
		}

		if (waitOn.size() > 0) {
			LOGGER.fatal("Execution stack was not empty before continuing to next tick! " + waitOn.size());

			StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
			for (String taskName : waitOn.keySet()) sj.add(taskName);
			LOGGER.fatal(sj.toString());
		}

		stackLock.unlock(waitOn);
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
			String taskName =  "WorldTick: " + serverworld.toString() + "@" +
					// append world's dimension name to world tick task
					/* 1.16.1 code; AKA the only thing that changed  */
					serverworld.func_234923_W_().func_240901_a_().toString();
					/* */
					/* 1.15.2 code; AKA the only thing that changed  
					serverworld.getDimension().getType().getRegistryName().toString();
					/* */

			execute(taskName, () -> {
				try {
					currentWorlds.incrementAndGet();
					serverworld.tick(hasTimeLeft);
					if (!GeneralConfig.disableWorldPostTick) {
						// execute world post-tick asynchronously
						execute(taskName + "|PostTick", () -> {
							// synchronized (net.minecraftforge.fml.hooks.BasicEventHooks.class) {
							net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
							// }
						}, worldExecutionStack);
					} else {
						net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
					}
				} finally {
					currentWorlds.decrementAndGet();
				}
			}, worldExecutionStack);
		}
	}

	public static void callEntityTick(Entity entityIn, ServerWorld serverworld) {
		if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
			entityIn.tick();
			return;
		}
		String taskName = "EntityTick: " + entityIn.toString() + "@" + entityIn.hashCode();
		Runnable r = () -> {
			try {
				currentEnts.incrementAndGet();
				awaitCompletion(worldExecutionStack); // force world ticks to complete first
				entityIn.tick();
			} finally {
				currentEnts.decrementAndGet();
			}
		};

		final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.EntityTick, entityIn.getClass());
		if (filter != null) {
			filter.serialise(entityIn::tick, entityIn, entityIn.getPosition(), serverworld, task -> {
				execute(taskName, r, entityExecutionStack);
			}, SerDesHookTypes.EntityTick);
		} else {
			execute(taskName, r, entityExecutionStack);
		}
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
		}, worldExecutionStack);
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
		String taskName = "TETick: " + tte.toString()  + "@" + tte.hashCode();
		final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.TETick, tte.getClass());
		if (filter != null) {
			filter.serialise(tte::tick, tte, ((TileEntity)tte).getPos(), world, (task) -> {
				execute(taskName, () -> {
					try {
						currentTEs.incrementAndGet();
						awaitCompletion(worldExecutionStack); // force world ticks to complete first
						task.run();
					} finally {
						currentTEs.decrementAndGet();
					}
				}, entityExecutionStack);
			}, SerDesHookTypes.TETick);
		} else {
			execute(taskName, () -> {
				awaitCompletion(worldExecutionStack); // force world ticks to complete first
				tte.tick();
			}, entityExecutionStack);
		}
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
			awaitCompletion(worldExecutionStack); // this should be empty, but run it just in case
			awaitCompletion(entityExecutionStack);
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
			for (String s : entityExecutionStack.keySet()) {
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
	
	public static boolean shouldThreadChunks() {
		return GeneralConfig.disableMultiChunk;
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

