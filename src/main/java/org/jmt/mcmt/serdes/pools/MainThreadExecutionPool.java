package org.jmt.mcmt.serdes.pools;

import java.util.function.Consumer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MainThreadExecutionPool implements ISerDesPool {

	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w, Consumer<Runnable> executeMultithreaded,
			ISerDesOptions options) {
		task.run();
	}
}
