package org.jmt.mcmt.asmdest;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PatchHookTerminator {

	static Set<Class<?>> onCollisionFixBypassClass = new HashSet<>();
	
	static {
		try {
			onCollisionFixBypassClass.add(Class.forName("blusunrize.immersiveengineering.common.blocks.IETileProviderBlock"));
		} catch (ClassNotFoundException cnfe) {}
	}
	
	//TODO Add config for this
	@SuppressWarnings("deprecation")
	public static boolean OnCollisionFix(BlockState bs, World w, BlockPos p, Entity e) {
		if (onCollisionFixBypassClass.contains(bs.getBlock().getClass())) {
			synchronized (bs.getBlock()) {			
				bs.getBlock().onEntityCollision(bs, w, p, e);
			}
			return true;
		}
		return false;
	}
	
}
