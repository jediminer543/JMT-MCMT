package org.jmt.mcmt.asmdest;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.commands.StatsCommand;
import org.jmt.mcmt.config.GeneralConfig;
import org.jmt.mcmt.paralelised.ChunkLock;

import net.minecraft.block.BlockEventData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;

public class ASMHookTerminator {

	private static final Logger LOGGER = LogManager.getLogger();
	
	static Phaser p;
	static ExecutorService ex = Executors.newWorkStealingPool();
	static MinecraftServer mcs;
	static AtomicBoolean isTicking = new AtomicBoolean();
	
	// Statistics
	public static AtomicInteger currentWorlds = new AtomicInteger();
	public static AtomicInteger currentEnts = new AtomicInteger();
	public static AtomicInteger currentTEs = new AtomicInteger();
	public static AtomicInteger currentEnvs = new AtomicInteger();

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
		if (mcs != server) {
			LOGGER.warn("Multiple servers?");
			GeneralConfig.disabled = true;
			serverworld.tick(hasTimeLeft);
			return;
		} else {
			p.register();
			ex.execute(() -> {
				try {
					currentWorlds.incrementAndGet();
					serverworld.tick(hasTimeLeft);
					net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverworld);
				} finally {
					p.arriveAndDeregister();
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
		p.register();
		ex.execute(() -> {
			try {
				currentEnts.incrementAndGet();
				entityIn.tick();
			} finally {
				currentEnts.decrementAndGet();
				p.arriveAndDeregister();
			}
		});
	}
	
	public static void callTickEnvironment(ServerWorld world, Chunk chunk, int k, ServerChunkProvider scp) {
		if (GeneralConfig.disabled  || GeneralConfig.disableEnvironment) {
			world.tickEnvironment(chunk, k);
			return;
		}
		p.register();
		ex.execute(() -> {
			try {
				currentEnvs.incrementAndGet();
				world.tickEnvironment(chunk, k);
			} finally {
				currentEnvs.decrementAndGet();
				p.arriveAndDeregister();
			}
		});
	}
	
	static List<ITickableTileEntity> tteList = new CopyOnWriteArrayList<ITickableTileEntity>();
	
	public static void callTileEntityTick(ITickableTileEntity tte, World world) {
		if (GeneralConfig.disabled  || GeneralConfig.disableTileEntity || !(world instanceof ServerWorld)) {
			tte.tick();
			return;
		}
		if (tteList.contains(tte)) {
			LOGGER.warn("Re-Ticking TTE: " + tte);
		}
		tteList.add(tte);
		boolean isLocking = false;
		if (tte instanceof PistonTileEntity) {
			isLocking = true;
		}
		if (GeneralConfig.chunkLockModded && !tte.getClass().getPackage().equals(Package.getPackage("net.minecraft.tileentity"))) {
			isLocking = true;
		}
		final boolean doLock = isLocking;
		p.register();
		ex.execute(() -> {
			try {
				if (doLock) {
					BlockPos bp = ((TileEntity) tte).getPos();
					long[] locks = ChunkLock.lock(bp, 1);
					try {
						currentTEs.incrementAndGet();
						tte.tick();
					} finally {
						ChunkLock.unlock(locks);
					}
				} else {
					currentTEs.incrementAndGet();
					tte.tick();
				}
			} catch (Exception e) {
				System.err.println("Exception ticking TE at " + ((TileEntity) tte).getPos());
				e.printStackTrace();
			} finally {
				currentTEs.decrementAndGet();
				p.arriveAndDeregister();
			}
		});
	}
	
	public static void sendQueuedBlockEvents(Deque<BlockEventData> d, ServerWorld sw) {
		Iterator<BlockEventData> bed = d.iterator();
		while (bed.hasNext()) {
			BlockEventData blockeventdata = bed.next();
			if (sw.fireBlockEvent(blockeventdata)) {
				// 1.16.1
				sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.func_234923_W_(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
				// 1.15.2
				//sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, sw.getDimension().getType(), new SBlockActionPacket(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
			}
			if (!isTicking.get()) {
				LOGGER.fatal("Block updates outside of tick");
			}
			bed.remove();
		}
	}
	
	public static void postTick(MinecraftServer server) {
		if (mcs != server) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			p.arriveAndAwaitAdvance();
			isTicking.set(false);
			tteList.clear();
			p = null;
		}
	}
	
	Deque<BlockEventData> test;
	
	public static <T> void fixSTL(ServerTickList<T> stl) {
		LOGGER.debug("FixSTL Called");
		stl.pendingTickListEntriesTreeSet.addAll(stl.pendingTickListEntriesHashSet);
	}
}

