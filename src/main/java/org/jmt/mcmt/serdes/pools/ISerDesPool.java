package org.jmt.mcmt.serdes.pools;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISerDesPool {

	public interface ISerDesOptions {}
	
	public void serialise(Runnable task, Object o, BlockPos bp, World w, @Nullable ISerDesOptions options);
	
	public default ISerDesOptions compileOptions(Map<String, String> config) {
		return null;
	}
	
	public default void init(String name, Map<String, String> config) {
		
	}
	
}
