package org.jmt.mcmt.serdes.pools;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Run on main thread.
 */
public class SingleExecutionPool implements ISerDesPool {

	private Lock l = new ReentrantLock();

	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w,
			Consumer<Runnable> executeMultithreaded, @Nullable ISerDesOptions options) {
		try {
			l.lock();
			task.run();
		} finally {
			l.unlock();
		}
	}

}
