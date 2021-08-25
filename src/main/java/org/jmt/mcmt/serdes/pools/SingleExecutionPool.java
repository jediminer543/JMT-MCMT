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
		// Push to the executeMultithreaded as it's allowed to execute off main thread, just not more than one at a time
		// The idea for this pool is global stuff like enderchests/tanks or equiv where you have a globally accessible single data element that will probably break
		executeMultithreaded.accept(() -> {
			try {
				l.lock();
				task.run();
			} finally {
				l.unlock();
			}
		});
	}

}
