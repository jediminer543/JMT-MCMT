package org.jmt.mcmt.serdes.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jmt.mcmt.config.GeneralConfig;
import org.jmt.mcmt.serdes.ISerDesHookType;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.pools.ChunkLockPool;
import org.jmt.mcmt.serdes.pools.ISerDesPool;
import org.jmt.mcmt.serdes.pools.ISerDesPool.ISerDesOptions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LegacyFilter implements ISerDesFilter {

	ISerDesPool clp;
	ISerDesOptions config;
	
	@Override
	public void init() {
		clp = SerDesRegistry.getOrCreatePool("LEGACY", ChunkLockPool::new);
		Map<String, Object> cfg = new HashMap<>();
		cfg.put("range", "1");
		config = clp.compileOptions(cfg);
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
		clp.serialise(task, obj, bp, w, config);
	}
	
	@Override
	public Set<Class<?>> getTargets() {
		return GeneralConfig.teBlackList;
	}
	
	@Override
	public Set<Class<?>> getWhitelist() {
		return GeneralConfig.teWhiteList;
	}

}
