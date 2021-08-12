package org.jmt.mcmt.serdes.filter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jmt.mcmt.serdes.ISerDesHookType;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.pools.ChunkLockPool;
import org.jmt.mcmt.serdes.pools.ISerDesPool;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoFilter implements ISerDesFilter {
	private static AutoFilter SINGLETON;
	
	private ISerDesPool pool;
	private Set<Class<?>> blacklist = ConcurrentHashMap.newKeySet();
	
	public static AutoFilter singleton() {
		if (SINGLETON == null) SINGLETON = new AutoFilter();
		return SINGLETON;
	}
	
	@Override
	public void init() {
		pool = SerDesRegistry.getOrCreatePool("AUTO", ChunkLockPool::new);
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
		pool.serialise(task, obj, bp, w, null);
	}

	@Override
	public Set<Class<?>> getTargets() {
		return blacklist;
	}

	@Override
	public ClassMode getModeOnline(Class<?> c) {
		return ClassMode.BLACKLIST;
	}
	
	public void addClassToBlacklist(Class<?> c) {
		blacklist.add(c);
	}
}