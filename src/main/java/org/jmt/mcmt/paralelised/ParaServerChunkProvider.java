package org.jmt.mcmt.paralelised;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.datafixers.DataFixer;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
// 1.16.1
import net.minecraft.world.storage.SaveFormat.LevelSave;

// 1.15.2
//import java.io.File;

public class ParaServerChunkProvider extends ServerChunkProvider {

	protected Map<Long, ChunkCacheLine> chunkCache = new ConcurrentHashMap<Long, ParaServerChunkProvider.ChunkCacheLine>();
	protected int access = Integer.MIN_VALUE;
	protected static final int CACHE_SIZE = 64;
	protected Thread cacheThread;

	/* 1.16.1 code; AKA the only thing that changed  */
	public ParaServerChunkProvider(ServerWorld worldIn, LevelSave worldDirectory, DataFixer dataFixer,
			TemplateManager templateManagerIn, Executor executorIn, ChunkGenerator chunkGeneratorIn, int viewDistance,
			boolean spawnHostiles, IChunkStatusListener p_i51537_8_, Supplier<DimensionSavedDataManager> p_i51537_9_) {
		super(worldIn, worldDirectory, dataFixer, templateManagerIn, executorIn, chunkGeneratorIn, viewDistance,
				spawnHostiles, p_i51537_8_, p_i51537_9_);
		cacheThread = new Thread(this::chunkCacheCleanup, "Chunk Cache Cleaner " + worldIn.func_234923_W_().func_240901_a_().getPath());
		cacheThread.start();
	}
	/* */
	
	/* 1.15.2 code; AKA the only thing that changed 
	public ParaServerChunkProvider(ServerWorld worldIn, File worldDirectory, DataFixer dataFixer,
			TemplateManager templateManagerIn, Executor executorIn, ChunkGenerator<?> chunkGeneratorIn,
			int viewDistance, IChunkStatusListener p_i51537_8_, Supplier<DimensionSavedDataManager> p_i51537_9_) {
		super(worldIn, worldDirectory, dataFixer, templateManagerIn, executorIn, chunkGeneratorIn, viewDistance, p_i51537_8_,
				p_i51537_9_);
		cacheThread = new Thread(this::chunkCacheCleanup, "Chunk Cache Cleaner " + worldIn.dimension.getType().getId());
		cacheThread.start();
	}
	/* */
	
	@Override
	@Nullable
	public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
		if (GeneralConfig.disabled || GeneralConfig.disableChunkProvider) {
			if (ASMHookTerminator.isThreadPooled("Main", Thread.currentThread())) {
				return CompletableFuture.supplyAsync(() -> {
		            return this.getChunk(chunkX, chunkZ, requiredStatus, load);
		         }, this.executor).join();
			}
			return super.getChunk(chunkX, chunkZ, requiredStatus, load);
		}
		long i = ChunkPos.asLong(chunkX, chunkZ);

		IChunk c = lookupChunk(i, requiredStatus);
		if (c != null) {
			return c;
		}
		
		if (ASMHookTerminator.isThreadPooled("Main", Thread.currentThread())) {
			return CompletableFuture.supplyAsync(() -> {
	            return this.getChunk(chunkX, chunkZ, requiredStatus, load);
	         }, this.executor).join();
		}
		
		IChunk cl;
		synchronized (this) {
			cl = super.getChunk(chunkX, chunkZ, requiredStatus, load);
		}
		cacheChunk(i, cl, requiredStatus);
		return cl;
	}

	/* 1.15.2/1.16.2 */
	@Override
	@Nullable
	public Chunk getChunkNow(int chunkX, int chunkZ) {
		if (GeneralConfig.disabled) {
			return super.getChunkNow(chunkX, chunkZ);
		}
		long i = ChunkPos.asLong(chunkX, chunkZ);

		IChunk c = lookupChunk(i, ChunkStatus.FULL);
		if (c != null) {
			return (Chunk) c;
		}

		Chunk cl = super.getChunkNow(chunkX, chunkZ);
		cacheChunk(i, cl, ChunkStatus.FULL);
		return cl;
	}
	/* */
	
	/* 1.16.2 because the bad mappings are bad
	@Override
	@Nullable
	public Chunk getChunkWithoutLoading(int chunkX, int chunkZ) {
		if (GeneralConfig.disabled) {
			return super.getChunkWithoutLoading(chunkX, chunkZ);
		}
		long i = ChunkPos.asLong(chunkX, chunkZ);

		IChunk c = lookupChunk(i, ChunkStatus.FULL);
		if (c != null) {
			return (Chunk) c;
		}

		Chunk cl = super.getChunkWithoutLoading(chunkX, chunkZ);
		cacheChunk(i, cl, ChunkStatus.FULL);
		return cl;
	}
	*/

	public IChunk lookupChunk(long p_225315_1_, ChunkStatus p_225315_4_) {
		int oldaccess = access++;
		if (access < oldaccess) {
			// Long Rollover so super rare
			chunkCache.clear();
			return null;
		}
		ChunkCacheLine ccl = chunkCache.get(p_225315_1_);
		if (ccl != null && ccl.status == p_225315_4_) {
			ccl.updateLastAccess();
			return ccl.getChunk();
		}
		return null;
	}

	public void cacheChunk(long chunkPos, IChunk chunk, ChunkStatus status) {
		long oldaccess = access++;
		if (access < oldaccess) {
			// Long Rollover so super rare
			chunkCache.clear();
		}
		ChunkCacheLine ccl;
		if ((ccl = chunkCache.get(chunkPos)) != null) {
			ccl.updateLastAccess();
			ccl.updateChunkRef(chunk);
		}
		ccl = new ChunkCacheLine(chunk, status);
		chunkCache.put(chunkPos, ccl);
	}

	Logger log = LogManager.getLogger();

	public void chunkCacheCleanup() {
		while (world == null || world.getServer() == null) {
			log.debug("ChunkCleaner Waiting for startup");
		}
		while (world.getServer().isServerRunning()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int size = chunkCache.size();
			if (size < CACHE_SIZE)
				continue;
			// System.out.println("CacheFill: " + size);
			long maxAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).max().orElseGet(() -> access);
			long minAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).min()
					.orElseGet(() -> Integer.MIN_VALUE);
			long cutoff = minAccess + (long) ((maxAccess - minAccess) / ((float) size / ((float) CACHE_SIZE)));
			for (Entry<Long, ChunkCacheLine> l : chunkCache.entrySet()) {
				if (l.getValue().getLastAccess() < cutoff | l.getValue().getChunk() == null) {
					chunkCache.remove(l.getKey());
				}
			}
		}
		log.debug("ChunkCleaner terminating");
	}

	protected class ChunkCacheLine {
		WeakReference<IChunk> chunk;
		ChunkStatus status;
		int lastAccess;

		public ChunkCacheLine(IChunk chunk, ChunkStatus status) {
			this(chunk, status, access);
		}

		public ChunkCacheLine(IChunk chunk, ChunkStatus status, int lastAccess) {
			this.chunk = new WeakReference<>(chunk);
			this.status = status;
			this.lastAccess = lastAccess;
		}

		public IChunk getChunk() {
			return chunk.get();
		}

		public ChunkStatus getStatus() {
			return status;
		}

		public int getLastAccess() {
			return lastAccess;
		}

		public void updateLastAccess() {
			lastAccess = access;
		}

		public void updateChunkRef(IChunk c) {
			if (chunk.get() == null) {
				chunk = new WeakReference<>(c);
			}
		}
	}
}
