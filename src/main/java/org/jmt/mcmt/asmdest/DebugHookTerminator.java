package org.jmt.mcmt.asmdest;

import java.io.IOException;
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
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkHolder.IChunkLoadingError;
import net.minecraft.world.server.ServerChunkProvider;

// TODO Should be renamed ChunkRepairHookTerminator (Note requres coremod edit)
/**
 * Handles chunk forcing in scenarios where world corruption has occured
 * 
 * @author jediminer543
 *
 */
public class DebugHookTerminator {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static boolean bypassLoadTarget = false;
	
	public static boolean isBypassLoadTarget() {
		return bypassLoadTarget;
	}
		
	public static void chunkLoadDrive(ServerChunkProvider.ChunkExecutor executor, BooleanSupplier isDone, ServerChunkProvider scp, 
			CompletableFuture<Either<IChunk, IChunkLoadingError>> completableFuture, long chunkpos) {
		if (!GeneralConfig.enableChunkTimeout) {
			bypassLoadTarget = false;
			executor.driveUntil(isDone);
			return;
		}
		int failcount = 0;
		while (!isDone.getAsBoolean()) {
			if (!executor.driveOne()) {
				if(isDone.getAsBoolean()) {
					break;
				}
				// Nothing more to execute
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
}
