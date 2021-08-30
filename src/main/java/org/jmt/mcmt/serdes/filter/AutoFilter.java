package org.jmt.mcmt.serdes.filter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.jmt.mcmt.config.SerDesConfig;
import org.jmt.mcmt.serdes.ISerDesHookType;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.pools.ISerDesPool;
import org.jmt.mcmt.serdes.pools.MainThreadExecutionPool;

import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * @author Hunter Hancock (meta1203)
 * This, and any other code I submit to jediminer543's JMT-MCMT project, is licensed under the 2-Clause BSD License.
 * (https://opensource.org/licenses/BSD-2-Clause)
 */
public class AutoFilter implements ISerDesFilter {
	private static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
	private static AutoFilter SINGLETON;
	
	private ISerDesPool pool;
	private Set<Class<?>> filtered = ConcurrentHashMap.newKeySet();
	
	public static AutoFilter singleton() {
		if (SINGLETON == null) SINGLETON = new AutoFilter();
		return SINGLETON;
	}
	
	@Override
	public void init() {
		pool = SerDesRegistry.getOrCreatePool("MAIN", MainThreadExecutionPool::new);
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, 
			Consumer<Runnable> multi, ISerDesHookType hookType) {
		pool.serialise(task, obj, bp, w, multi, null);
	}

	@Override
	public Set<Class<?>> getFiltered() {
		return filtered;
	}

	@Override
	public ClassMode getModeOnline(Class<?> c) {
		return ClassMode.UNKNOWN;
	}
	
	public void addClassToBlacklist(Class<?> c) {
		addClassToBlacklist(c, "CHUNK_LOCK");
	}
	
	public void addClassToBlacklist(Class<?> c, String pool) {
		LOGGER.error("Adding " + c.getName() + " to blacklist.");
		SerDesConfig.createFilterConfig(
				"auto-" + c.getName(),
				10,
				Lists.newArrayList(),
				Lists.newArrayList(c.getName()),
				pool
				);
		filtered.add(c);
	}
}
