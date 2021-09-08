package org.jmt.mcmt.paralelised;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GatedLock {
	private ConcurrentHashMap<Object, ReentrantLock> locks = new ConcurrentHashMap<>();
	
	public boolean isLocked(Object on) {
		if (!locks.containsKey(on)) return false;
		return locks.get(on).isLocked();
	}
	
	public void lockOn(Object on) {
		locks.computeIfAbsent(on, (x) -> new ReentrantLock()).lock();
		return;
	}
	
	public void waitForUnlock(Object on) {
		if (!this.isLocked(on)) return;
		locks.computeIfAbsent(on, (x) -> new ReentrantLock()).lock();
		locks.get(on).unlock();
	}
	
	public void unlock(Object on) {
		if (!this.locks.contains(on)) return;
		locks.get(on).unlock();
	}
}
