package org.jmt.mcmt.asmdest;

public class PatchHookTerminator {
/*
 * WATERFILLS YOUR FECKING HOUSE
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
*/
}
