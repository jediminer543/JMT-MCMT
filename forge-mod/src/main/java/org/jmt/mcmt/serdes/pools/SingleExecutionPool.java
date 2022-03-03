package org.jmt.mcmt.serdes.pools;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SingleExecutionPool implements ISerDesPool {

	private Lock l = new ReentrantLock();
	
	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, Level w, @Nullable ISerDesOptions options) {
		try {
			l.lock();
			task.run();
		} finally {
			l.unlock();
		}
	}
	
}
