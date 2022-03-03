package org.jmt.mcmt.paralelised.pooling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

// This is highly WIP, please ignore for now
@SuppressWarnings("unused")
public class LockAwareThreadPool extends AbstractExecutorService {

	private volatile boolean isShutdown;
	private AtomicInteger liveThreads;
	private AtomicInteger blockedThreads;
	private ConcurrentLinkedDeque<Runnable> taskQueue;
	private Map<LockAwareThread, LockAwareThreadState> threadSet = new ConcurrentHashMap<>();
	
	public enum LockAwareThreadState {
		PARK,
		RUN,
		BLOCK
	}
	
	public class LockAwareThread extends Thread {
		
		@Override
		public void run() {
			while (!isShutdown()) {
				LockAwareThreadState state = threadSet.get(this);
				if (state == LockAwareThreadState.BLOCK) {
					//SHOULDN'T BE HERE SOMETHING FRACKED UP
					threadSet.put(this, state = LockAwareThreadState.PARK);
					blockedThreads.decrementAndGet();
				}
				if (state == LockAwareThreadState.PARK) {
					
				}
			}
		}
		
	}
	
	
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
		long waitnanos = unit.toNanos(timeout);
		long deadline = System.nanoTime() + waitnanos;
		while (System.nanoTime() < deadline) {
			if (isTerminated()) return true;
			LockSupport.parkUntil(deadline);
		}
		return false;
	}

	@Override
	public void execute(Runnable command) {
		taskQueue.add(command);
	}

}
