package org.jmt.mcmt.paralelised;

import java.util.concurrent.CompletableFuture;

public class TickTask {
	private String name;
	private CompletableFuture<Void> future;
	
	public TickTask(String name, CompletableFuture<Void> future) {
		this.name = name;
		this.future = future;
	}

	public String getName() {
		return name;
	}

	public CompletableFuture<Void> getFuture() {
		return future;
	}
}
