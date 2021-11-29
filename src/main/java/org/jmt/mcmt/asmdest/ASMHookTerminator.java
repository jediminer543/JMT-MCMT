package org.jmt.mcmt.asmdest;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Phaser;
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
import org.jmt.mcmt.serdes.pools.PostExecutePool;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.hooks.BasicEventHooks;

/**
 * This is where all current ASM hooks are terminated
 * 
 * So DON'T rename this file (Or there will be a lot of other work todo)
 * 
 * Fun point: So because this is hooking into a lot of the stuff, be careful what you reference here
 * I attempted to reference a function on {@link GeneralConfig} and it got VERY angery at me with a "class refuses to load" error
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
	
	static Phaser p;
	static ExecutorService ex;
	static MinecraftServer mcs;
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
		ex = new ForkJoinPool(
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
	
	//Operation logging
	public static Set<String> currentTasks = ConcurrentHashMap.newKeySet(); 
	
	
	public static void regThread(String poolName, Thread thread) {
		mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
	}
	
	public static boolean isThreadPooled(String poolName, Thread t) {
		return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
	}
	
	public static boolean serverExecutionThreadPatch(MinecraftServer ms) {
		return isThreadPooled("MCMT", Thread.currentThread());
	}

	public static void preTick(MinecraftServer server) {
		if (p != null) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			isTicking.set(true);
			p = new Phaser();
			p.register();
			mcs = server;
			StatsCommand.setServer(mcs);
		}
	}
	
	public static void callTick(ServerLevel serverworld, BooleanSupplier hasTimeLeft, MinecraftServer server) {
		if (GeneralConfig.disabled || GeneralConfig.disableWorld) {
			try {
				serverworld.tick(hasTimeLeft);
			} catch (Exception e) {
				throw e;
			} finally {
				BasicEventHooks.onPostWorldTick(serverworld);
			}
			return;
		}
		if (mcs != server) {
			LOGGER.warn("Multiple servers?");
			GeneralConfig.disabled = true;
			serverworld.tick(hasTimeLeft);
			BasicEventHooks.onPostWorldTick(serverworld);
			return;
		} else {
			String taskName = null;
			if (GeneralConfig.opsTracing) {
				 taskName =  "WorldTick: " + serverworld.toString() + "@" + serverworld.hashCode();
				currentTasks.add(taskName);
			}
			String finalTaskName = taskName;
			p.register();
			ex.execute(() -> {
				try {
					currentWorlds.incrementAndGet();
					serverworld.tick(hasTimeLeft);
					if (GeneralConfig.disableWorldPostTick) {
						synchronized (BasicEventHooks.class) {
							BasicEventHooks.onPostWorldTick(serverworld);
						}
					} else {
						TickEvent.WorldTickEvent event = new TickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, serverworld);
						ListenerList ll = EventListenerHelper.getListenerList(TickEvent.WorldTickEvent.class);
						//TODO find better way to locate listeners
						IEventListener[] listeners = ll.getListeners(0);
						//TODO Add some way to cache listeners because this is
						//Janky and slow
						Map<EventPriority, List<IEventListener>> prioritymap = new HashMap<EventPriority, List<IEventListener>>();
						EventPriority current = EventPriority.HIGHEST;
						prioritymap.computeIfAbsent(current, i->new ArrayList<>());
						for (IEventListener iel : listeners) {
							if (iel instanceof EventPriority) {
								EventPriority newcurrent = (EventPriority) iel;
								// Shouldn't be absent but if exists then drop
								prioritymap.computeIfAbsent(newcurrent, i->new ArrayList<>());
								//List<IEventListener> iell = prioritymap.computeIfAbsent(newcurrent, i->new ArrayList<>());
								//iell.add(current); May break stuff so avoided;
								current = newcurrent;
							} else {
								prioritymap.get(current).add(iel);
							}
						}
						for (EventPriority ep : EventPriority.values()) {
							List<IEventListener> iell = prioritymap.get(ep);
							if (iell != null) {
								ep.invoke(event);
								for (IEventListener iel : iell) {
									p.register();
									ex.execute(() -> {
										try {
											synchronized (iel) {
												iel.invoke(event);
											}
										} finally {
											p.arriveAndDeregister();
										}
									});
								}
							}
						}
					}
				} finally {
					p.arriveAndDeregister();
					currentWorlds.decrementAndGet();
					if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
				}
			});
		}
		
	}
	
	public static void callEntityTick(Entity entityIn, ServerLevel serverworld) {
		if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
			entityIn.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EntityTick: " + entityIn.toString() + "@" + entityIn.hashCode();
			currentTasks.add(taskName);
		}
		String finalTaskName = taskName;
		p.register();
		ex.execute(() -> {
			try {
				//currentEnts.incrementAndGet();
				//entityIn.tick();
				final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.EntityTick, entityIn.getClass());
				currentTEs.incrementAndGet();
				if (filter != null) {
					filter.serialise(entityIn::tick, entityIn, entityIn.getOnPos(), serverworld, SerDesHookTypes.EntityTick);
				} else {
					entityIn.tick();
				}
			} finally {
				currentEnts.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
	
	public static void callTickEnvironment(ServerLevel world, LevelChunk chunk, int k, ServerChunkCache scp) {
		if (GeneralConfig.disabled  || GeneralConfig.disableEnvironment) {
			world.tickChunk(chunk, k);
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EnvTick: " + chunk.toString() + "@" + chunk.hashCode();
			currentTasks.add(taskName);
		}
		String finalTaskName = taskName;
		p.register();
		ex.execute(() -> {
			try {
				currentEnvs.incrementAndGet();
				world.tickChunk(chunk, k);
			} finally {
				currentEnvs.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
	
	public static boolean filterTE(TickingBlockEntity tte) {
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
		if (tte instanceof PistonMovingBlockEntity) {
			isLocking = true;
		}
		return isLocking;
	}
		
	public static void callTileEntityTick(TickingBlockEntity tte, Level world) {
		if (GeneralConfig.disabled  || GeneralConfig.disableTileEntity || !(world instanceof ServerLevel)) {
			tte.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "TETick: " + tte.toString()  + "@" + tte.hashCode();
			currentTasks.add(taskName);
		}
		p.register();
		String finalTaskName = taskName;
		ex.execute(() -> {
			try {
				//final boolean doLock = filterTE(tte);
				final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.TETick, tte.getClass());
				currentTEs.incrementAndGet();
				if (filter != null) {
					filter.serialise(tte::tick, tte, tte.getPos(), world, SerDesHookTypes.TETick);
				} else {
					tte.tick();
				}
			} catch (Exception e) {
				System.err.println("Exception ticking TE at " + tte.getPos());
				e.printStackTrace();
			} finally {
				currentTEs.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
	
	/*
	//TODO FIXME
	public static void sendQueuedBlockEvents(Deque<BlockEventData> d, ServerLevel sw) {
		Iterator<BlockEventData> bed = d.iterator();
		while (bed.hasNext()) {
			BlockEventData blockeventdata = bed.next();
			if (sw.fireBlockEvent(blockeventdata)) {
				//1.16.1 code; AKA the only thing that changed
				//sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.func_234923_W_(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
				//1.15.2 code; AKA the only thing that changed  
				//sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.getDimension().getType(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
			}
			if (!isTicking.get()) {
				LOGGER.fatal("Block updates outside of tick");
			}
			bed.remove();
		}
	}
	*/
	
	public static void postTick(MinecraftServer server) {
		if (mcs != server) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			p.arriveAndAwaitAdvance();
			isTicking.set(false);
			p = null;
			//PostExecute logic
			Deque<Runnable> queue = PostExecutePool.POOL.getQueue();
			Iterator<Runnable> qi = queue.iterator();
			while (qi.hasNext()) {
				Runnable r = qi.next();
				r.run();
				qi.remove();
			}
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
			for (String s : currentTasks) {
				confInfo.append("\t\t"); confInfo.append("\t"); confInfo.append(s); confInfo.append("\n");
			}
			confInfo.append("\t\t"); confInfo.append("-- Running Operations End -- "); confInfo.append("\n");
		}
		return confInfo.toString();
	}
	
	/*
	static {
		net.minecraftforge.fmllegacy.CrashReportExtender.registerCrashCallable("MCMT", ASMHookTerminator::populateCrashReport);
	}
	*/
	
	public static <T> void fixSTL(ServerTickList<T> stl) {
		LOGGER.debug("FixSTL Called");
		//stl.pendingTickListEntriesTreeSet.addAll(stl.pendingTickListEntriesHashSet);
		stl.tickNextTickList.addAll(stl.tickNextTickSet);
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

