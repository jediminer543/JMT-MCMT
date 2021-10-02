package org.jmt.mcmt.paralelised;

import java.util.concurrent.ForkJoinPool;
import java.util.function.BooleanSupplier;

public class RunnableManagedBlocker implements ForkJoinPool.ManagedBlocker {

	BooleanSupplier runMe;
	boolean isDone = false;
	
	public RunnableManagedBlocker(BooleanSupplier task) {
		runMe = task;
	}
	
	public RunnableManagedBlocker(Runnable task) {
		runMe = () -> { task.run(); return true; };
	}
	
	@Override
	public boolean block() throws InterruptedException {
		if (!isDone) {
			isDone = runMe.getAsBoolean();
		}
		return isDone;
	}

	@Override
	public boolean isReleasable() {
		return isDone;
	}
}
