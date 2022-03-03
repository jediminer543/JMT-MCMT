package org.jmt.mcmt.paralelised;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class ChunkLock {
	
	@Deprecated
	public static final ChunkLock INSTANCE = new ChunkLock(); 
	
	Map<Long, Lock> chunkLockCache = new ConcurrentHashMap<>();
	
	//TODO Add cleanup thread
	
	public void cleanup() {
		chunkLockCache = new ConcurrentHashMap<>();
	}
	
	public long[] lock(BlockPos bp, int radius) {
		long cp = new ChunkPos(bp).toLong();
		return lock(cp, radius);
	}
	
	public long[] lock(long cp, int radius) {
		long[] targets = new long[(1+radius*2)*(1+radius*2)];
		int pos = 0;
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				long curr = cp + ChunkPos.asLong(i, j); // Can error at the boundaries but eh
				targets[pos++] = curr;
			}
		}
		Arrays.sort(targets);
		for (long l : targets) {
			chunkLockCache.computeIfAbsent(l, x -> new ReentrantLock()).lock();
		}
		return targets;
	}
	
	public void unlock(long[] locks) {
		ArrayUtils.reverse(locks);
		for (long l : locks) {
			chunkLockCache.get(l).unlock();
		}
	}
	
	
	
}
