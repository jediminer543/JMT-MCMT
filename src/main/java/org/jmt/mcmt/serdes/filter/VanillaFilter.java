package org.jmt.mcmt.serdes.filter;

import org.jmt.mcmt.serdes.ISerDesHookType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class VanillaFilter implements ISerDesFilter {

	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, Level w, ISerDesHookType hookType) {
		task.run();
	}

	@Override
	public ClassMode getModeOnline(Class<?> c) {
		if (c.getName().startsWith("net.minecraft") /*&& !c.equals(PistonTileEntity.class)*/) {
			return ClassMode.WHITELIST;
		}
		return ClassMode.UNKNOWN;
	}
	
}
