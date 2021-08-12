package org.jmt.mcmt.serdes.filter;

import org.jmt.mcmt.serdes.ISerDesHookType;

import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VanillaFilter implements ISerDesFilter {

	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
		task.run();
	}

	@Override
	public ClassMode getModeOnline(Class<?> c) {
		if (c.getName().startsWith("net.minecraft") && !c.equals(PistonTileEntity.class)) {
			return ClassMode.WHITELIST;
		}
		return ClassMode.UNKNOWN;
	}
	
}
