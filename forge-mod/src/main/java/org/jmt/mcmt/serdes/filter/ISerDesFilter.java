package org.jmt.mcmt.serdes.filter;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jmt.mcmt.serdes.ISerDesHookType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ISerDesFilter {

	public void serialise(Runnable task, Object obj, BlockPos bp, Level w, ISerDesHookType hookType);
	
	@Nullable
	public default Set<Class<?>> getTargets() {
		return null;
	}
	
	/**
	 * Perform initialisation; this may include optimisation steps like looking up 
	 * pools pre-emptively, generating pook configs, etc.
	 * 
	 * As such it is invoked after pools are initialised
	 */
	public default void init() {
		
	}
	
	@Nullable
	public default Set<Class<?>> getWhitelist() {
		return null;
	}
	
	public static enum ClassMode {
		BLACKLIST,
		WHITELIST,
		UNKNOWN;
	}
	
	@Nonnull
	public default ClassMode getModeOnline(Class<?> c) {
		return ClassMode.UNKNOWN;
	}
}
