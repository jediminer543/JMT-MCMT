package org.jmt.mcmt.paralelised.pooling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// This is highly WIP, please ignore for now
@SuppressWarnings("unused")
public class LockAwareThreadPool extends AbstractExecutorService {

	private volatile boolean isShutdown;
	private AtomicInteger liveThreads;
	private AtomicInteger runningThreads;
	private AtomicInteger nextIdx;
	private ConcurrentLinkedDeque<Runnable> taskQueue;
	
	
	@Override
	public void shutdown() {
		isShutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		// Prevent any new tasks from being fetched
		shutdown();
		// Get all tasks from task queue that haven't been executed yet
		List<Runnable> out =  new ArrayList<Runnable>(taskQueue);
		// Empty task queue
		taskQueue.clear();
		// Return result
		return out;
	}

	@Override
	public boolean isShutdown() {
		return isShutdown;
	}

	@Override
	public boolean isTerminated() {
		return taskQueue.isEmpty();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		//NYI
		return false;
	}

	@Override
	public void execute(Runnable command) {
		taskQueue.add(command);
	}

}
