package org.jmt.mcmt.paralelised;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.datafixers.DataFixer;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
/* */

/* 1.15.2 code; AKA the only thing that changed  
import java.io.File;
/* */

public class ParaServerChunkProvider extends ServerChunkCache {

	protected Map<ChunkCacheAddress, ChunkCacheLine> chunkCache = new ConcurrentHashMap<ChunkCacheAddress, ChunkCacheLine>();
	protected AtomicInteger access = new AtomicInteger(Integer.MIN_VALUE);
	protected static final int CACHE_SIZE = 512;
	protected Thread cacheThread;
	protected ChunkLock loadingChunkLock = new ChunkLock();
	Logger log = LogManager.getLogger();
	Marker chunkCleaner = MarkerManager.getMarker("ChunkCleaner");

	public ParaServerChunkProvider(ServerLevel worldIn, LevelStorageSource.LevelStorageAccess worldDirectory, DataFixer dataFixer, StructureManager templateManagerIn, Executor executorIn, ChunkGenerator chunkGeneratorIn, int viewDistance, boolean spawnHostiles, ChunkProgressListener p_143236_, ChunkStatusUpdateListener p_143237_, Supplier<DimensionDataStorage> p_143238_) {
		super(worldIn, worldDirectory, dataFixer, templateManagerIn, executorIn, chunkGeneratorIn, viewDistance,
				spawnHostiles, p_143236_, p_143237_, p_143238_);
		cacheThread = new Thread(this::chunkCacheCleanup, "Chunk Cache Cleaner " + worldIn.dimension().location().getPath());
		cacheThread.start();
	}
	
	
	@Override
	@Nullable
	public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
		if (GeneralConfig.disabled || GeneralConfig.disableChunkProvider) {
			if (ASMHookTerminator.isThreadPooled("Main", Thread.currentThread())) {
				return CompletableFuture.supplyAsync(() -> {
		            return this.getChunk(chunkX, chunkZ, requiredStatus, load);
		         }, this.mainThreadProcessor).join();
			}
			return super.getChunk(chunkX, chunkZ, requiredStatus, load);
		}
		if (ASMHookTerminator.isThreadPooled("Main", Thread.currentThread())) {
			return CompletableFuture.supplyAsync(() -> {
	            return this.getChunk(chunkX, chunkZ, requiredStatus, load);
	         }, this.mainThreadProcessor).join();
		}
		
		long i = ChunkPos.asLong(chunkX, chunkZ);

		ChunkAccess c = lookupChunk(i, requiredStatus, false);
		if (c != null) {
			return c;
		}
		
		//log.debug("Missed chunk " + i + " on status "  + requiredStatus.toString());
		
		ChunkAccess cl;
		if (ASMHookTerminator.shouldThreadChunks()) {
			// Multithread but still limit to 1 load op per chunk
			long[] locks = loadingChunkLock.lock(i, 0);
			try {
				if ((c = lookupChunk(i, requiredStatus, false)) != null) {
					return c;
				}
				cl = super.getChunk(chunkX, chunkZ, requiredStatus, load);
			} finally {
				loadingChunkLock.unlock(locks);
			}
		} else {
			synchronized (this) {
				if (chunkCache.containsKey(new ChunkCacheAddress(i, requiredStatus)) && (c = lookupChunk(i, requiredStatus, false)) != null) {
					return c;
				}
				cl = super.getChunk(chunkX, chunkZ, requiredStatus, load);
			}
		}
		cacheChunk(i, cl, requiredStatus);
		return cl;
	}

	@Override
	@Nullable
	public LevelChunk getChunkNow(int chunkX, int chunkZ) {
		if (GeneralConfig.disabled) {
			return super.getChunkNow(chunkX, chunkZ);
		}
		long i = ChunkPos.asLong(chunkX, chunkZ);

		ChunkAccess c = lookupChunk(i, ChunkStatus.FULL, false);
		if (c != null) {
			return (LevelChunk) c;
		}
		
		//log.debug("Missed chunk " + i + " now");

		LevelChunk cl = super.getChunkNow(chunkX, chunkZ);
		cacheChunk(i, cl, ChunkStatus.FULL);
		return cl;
	}

	public ChunkAccess lookupChunk(long chunkPos, ChunkStatus status, boolean compute) {
		int oldaccess = access.getAndIncrement();
		if (access.get() < oldaccess) {
			// Long Rollover so super rare
			chunkCache.clear();
			return null;
		}
		ChunkCacheLine ccl;
		ccl = chunkCache.get(new ChunkCacheAddress(chunkPos, status));
		if (ccl != null) {
			ccl.updateLastAccess();
			return ccl.getChunk();
		}
		return null;
		
	}

	public void cacheChunk(long chunkPos, ChunkAccess chunk, ChunkStatus status) {
		long oldaccess = access.getAndIncrement();
		if (access.get() < oldaccess) {
			// Long Rollover so super rare
			chunkCache.clear();
		}
		ChunkCacheLine ccl;
		if ((ccl = chunkCache.get(new ChunkCacheAddress(chunkPos, status))) != null) {
			ccl.updateLastAccess();
			ccl.updateChunkRef(chunk);
		}
		ccl = new ChunkCacheLine(chunk);
		chunkCache.put(new ChunkCacheAddress(chunkPos, status), ccl);
	}

	public void chunkCacheCleanup() {
		while (getLevel() == null || getLevel().getServer() == null) {
			log.debug(chunkCleaner, "ChunkCleaner Waiting for startup");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while (getLevel().getServer().isRunning()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int size = chunkCache.size();
			if (size < CACHE_SIZE)
				continue;
			// System.out.println("CacheFill: " + size);
			long maxAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).max().orElseGet(() -> access.get());
			long minAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).min()
					.orElseGet(() -> Integer.MIN_VALUE);
			long cutoff = minAccess + (long) ((maxAccess - minAccess) / ((float) size / ((float) CACHE_SIZE)));
			for (Entry<ChunkCacheAddress, ChunkCacheLine> l : chunkCache.entrySet()) {
				if (l.getValue().getLastAccess() < cutoff | l.getValue().getChunk() == null) {
					chunkCache.remove(l.getKey());
				}
			}
		}
		log.debug(chunkCleaner, "ChunkCleaner terminating");
	}
	
	protected class ChunkCacheAddress {
		
		protected long chunk;
		protected ChunkStatus status;
		
		public ChunkCacheAddress(long chunk, ChunkStatus status) {
			super();
			this.chunk = chunk;
			this.status = status;
		}
		
		@Override
		public int hashCode() {
			return Long.hashCode(chunk) ^ status.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ChunkCacheAddress) {
				if ((((ChunkCacheAddress) obj).chunk == chunk) && (((ChunkCacheAddress) obj).status.equals(status))) {
					return true;
				}
			}
			return false;
		}
	}

	protected class ChunkCacheLine {
		WeakReference<ChunkAccess> chunk;
		int lastAccess;

		public ChunkCacheLine(ChunkAccess chunk) {
			this(chunk, access.get());
		}

		public ChunkCacheLine(ChunkAccess chunk, int lastAccess) {
			this.chunk = new WeakReference<>(chunk);
			this.lastAccess = lastAccess;
		}

		public ChunkAccess getChunk() {
			return chunk.get();
		}

		public int getLastAccess() {
			return lastAccess;
		}

		public void updateLastAccess() {
			lastAccess = access.get();
		}

		public void updateChunkRef(ChunkAccess c) {
			if (chunk.get() == null) {
				chunk = new WeakReference<>(c);
			}
		}
	}
}
