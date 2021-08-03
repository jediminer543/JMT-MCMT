package org.jmt.mcmt.serdes.pools;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SingleExecutionPool implements ISerDesPool {

	Lock l = new ReentrantLock();
	
	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w, ISerDesOptions options) {
		try {
			l.lock();
			task.run();
		} finally {
			l.unlock();
		}
	}
	
}
