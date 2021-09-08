package org.jmt.mcmt.serdes.pools;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PostExecutePool implements ISerDesPool {

	public static final PostExecutePool POOL = new PostExecutePool();
	
	private PostExecutePool() {}

	Deque<Runnable> runnables = new ConcurrentLinkedDeque<Runnable>();
	
	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w, ISerDesOptions options) {
		runnables.add(task);
	}
	
	public Deque<Runnable> getQueue() {
		return runnables;
	}
	
	
}
