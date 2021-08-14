package org.jmt.mcmt.serdes.pools;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISerDesPool {

	public interface ISerDesOptions {}
	
	public void serialise(Runnable task, Object o, BlockPos bp, World w, 
			Consumer<Runnable> executeMultithreaded, @Nullable ISerDesOptions options);
	
	public default ISerDesOptions compileOptions(Map<String, Object> config) {
		return null;
	}
	
	public default void init(String name, Map<String, Object> config) {
		
	}
	
}
