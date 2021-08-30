package org.jmt.mcmt.asmdest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.datafixers.util.Either;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkHolder.IChunkLoadingError;
import net.minecraft.world.server.ServerChunkProvider;

/**
 * Handles chunk forcing in scenarios where world corruption has occurred
 * 
 * @author jediminer543
 *
 */
public class ChunkRepairHookTerminator {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static boolean bypassLoadTarget = false;
	
	public static class BrokenChunkLocator {
		long chunkPos;
		CompletableFuture<?> maincf;
		CompletableFuture<?> brokecf;
		public BrokenChunkLocator(long chunkPos, CompletableFuture<?> maincf, CompletableFuture<?> brokecf) {
			super();
			this.chunkPos = chunkPos;
			this.maincf = maincf;
			this.brokecf = brokecf;
		}
	}
	
	public static List<BrokenChunkLocator> breaks = new ArrayList<>();
	
	public static boolean isBypassLoadTarget() {
		return bypassLoadTarget;
	}
		
	public static void chunkLoadDrive(
			ServerChunkProvider.ChunkExecutor executor,
			BooleanSupplier isDone,
			ServerChunkProvider scp, 
			CompletableFuture<Either<IChunk, IChunkLoadingError>> completableFuture, 
			long chunkpos) {
		
		if (!GeneralConfig.enableChunkTimeout) {
			bypassLoadTarget = false;
			executor.driveUntil(isDone);
			return;
		}
		
		int failcount = 0;
		while (!isDone.getAsBoolean()) {
			
			if (!executor.driveOne()) {
				
				if(isDone.getAsBoolean()) {
					break; // Nothing more to execute
				}
				
				if (failcount++ < GeneralConfig.timeoutCount) {
					Thread.yield();
					LockSupport.parkNanos("THE END IS ~~NEVER~~ LOADING", 100000L);
				} else {
					LOGGER.error("", new TimeoutException("Error fetching chunk " + chunkpos));	
					bypassLoadTarget = true;
					if (GeneralConfig.enableTimeoutRegen) {
						try {
							CompoundNBT cnbt = scp.chunkManager.readChunk(new ChunkPos(chunkpos));
							if (cnbt != null) {
								ChunkPrimer cp = ChunkSerializer.read(scp.world, scp.chunkManager.templateManager, scp.chunkManager.pointOfInterestManager, new ChunkPos(chunkpos), cnbt);
								completableFuture.complete(Either.left(new Chunk(scp.getWorld(), cp)));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						completableFuture.complete(ChunkHolder.MISSING_CHUNK);
					} else {
						System.err.println(completableFuture.toString());
						ChunkHolder chunkholder = scp.func_217213_a(chunkpos);
						CompletableFuture<?> firstBroke = null;
						for (ChunkStatus cs : ChunkStatus.getAll()) {
							CompletableFuture<Either<IChunk, IChunkLoadingError>> cf = chunkholder.func_219301_a(cs);
							if (cf == ChunkHolder.MISSING_CHUNK_FUTURE) {
								System.out.println("Status: " + cs.toString() + " is not yet loaded");
							} else {
								System.out.println("Status: " + cs.toString() + " is " + cf.toString());
								if (firstBroke == null && !cf.toString().contains("Completed normally")) {
									firstBroke = cf;
								}
							}
						}
						breaks.add(new BrokenChunkLocator(chunkpos, completableFuture, firstBroke));
						completableFuture.complete(Either.right(new IChunkLoadingError() {
							@Override
							public String toString() {
								return "TIMEOUT";
							}
						}));
					}
				}
			}
		}
	}
	
	public static void checkNull(Object o) {
		if (o == null) {
			System.out.println("Null warning:");
			new Throwable("Null trace").printStackTrace();
		}
	}
}
