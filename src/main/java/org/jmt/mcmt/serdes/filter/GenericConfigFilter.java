package org.jmt.mcmt.serdes.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.jmt.mcmt.config.SerDesConfig;
import org.jmt.mcmt.serdes.ISerDesHookType;
import org.jmt.mcmt.serdes.SerDesRegistry;
import org.jmt.mcmt.serdes.pools.ISerDesPool;
import org.jmt.mcmt.serdes.pools.ISerDesPool.ISerDesOptions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GenericConfigFilter implements ISerDesFilter {

	SerDesConfig.FilterConfig cfg;
	
	public GenericConfigFilter(SerDesConfig.FilterConfig cfg) {
		this.cfg = cfg;
	}
	
	ISerDesPool primePool;
	ISerDesOptions primeOpts;
	
	// Target lists
	Set<Class<?>> whitelist;
	Set<Class<?>> blacklist;
	List<String>  wcWhitelist;
	List<String>  wcBlacklist;
	
	// PatternMatchers
	Pattern regexWhitelist;
	Pattern regexBlacklist;
	
	@Override
	public void init() {
		primePool = SerDesRegistry.getPool(cfg.getPool());
		primeOpts = primePool.compileOptions(cfg.getPoolParams());
		if (cfg.getWhitelist() != null) {
			whitelist   = ConcurrentHashMap.newKeySet();
			wcWhitelist = new ArrayList<String>();
			for (String s : cfg.getWhitelist()) {
				try {
					Class<?> clz = Class.forName(s);
					whitelist.add(clz);
				} catch (Exception e) {
					wcWhitelist.add("^" + s.replace(".", "\\.").replace("**", "+-/").replace("*", "[A-Za-z0-9$]*").replace("+-/", ".*") + "$");
				}
			}
			if (wcWhitelist.size() > 0)
			regexWhitelist = Pattern.compile(String.join("|", wcWhitelist));
		}
		if (cfg.getBlacklist() != null) {
			blacklist   = ConcurrentHashMap.newKeySet();
			wcBlacklist = new ArrayList<String>();
			for (String s : cfg.getBlacklist()) {
				try {
					Class<?> clz = Class.forName(s);
					blacklist.add(clz);
				} catch (Exception e) {
					wcBlacklist.add("^" + s.replace(".", "\\.").replace("**", "+-/").replace("*", "[A-Za-z0-9$]*").replace("+-/", ".*") + "$");
				}
			}
			if (wcBlacklist.size() > 0)
			regexBlacklist = Pattern.compile(String.join("|", wcBlacklist));
		}
	}
	
	@Override
	public Set<Class<?>> getWhitelist() {
		return whitelist;
	}
	
	@Override
	public Set<Class<?>> getTargets() {
		return blacklist;
	}
	
	@Override
	public ClassMode getModeOnline(Class<?> c) {
		if (regexBlacklist != null) {
			if (regexBlacklist.matcher(c.getName()).find()) {
				return ClassMode.CHUNKLOCK;
			}
		}
		if (regexWhitelist != null) {
			if (regexWhitelist.matcher(c.getName()).find()) {
				return ClassMode.WHITELIST;
			}
		}
		return ClassMode.UNKNOWN;
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w,
			Consumer<Runnable> executeMultithreaded, ISerDesHookType hookType) {
		primePool.serialise(task, hookType, bp, w, executeMultithreaded, primeOpts);
	}

}
