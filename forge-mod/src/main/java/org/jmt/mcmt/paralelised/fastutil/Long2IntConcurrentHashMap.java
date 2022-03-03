package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Long2IntConcurrentHashMap implements Long2IntMap {
	
	public Map<Long, Integer> backing = new ConcurrentHashMap<Long, Integer>();
	int defaultRV = 0;

	@Override
	public int get(long key) {
		if (backing.containsKey(key)) {
			return backing.get(key);
		} else return defaultRV;
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public void putAll(Map<? extends Long, ? extends Integer> m) {
		backing.putAll(m);
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public void defaultReturnValue(int rv) {
		defaultRV = rv;
	}

	@Override
	public int defaultReturnValue() {
		return defaultRV;
	}

	@Override
	public ObjectSet<Entry> long2IntEntrySet() {
		return null;
	}

	@Override
	public LongSet keySet() {
		return FastUtilHackUtil.wrapLongSet(backing.keySet());
	}

	@Override
	public IntCollection values() {
		return FastUtilHackUtil.wrapInts(backing.values());
	}

	@Override
	public boolean containsKey(long key) {
		return backing.containsKey(key);
	}

	@Override
	public boolean containsValue(int value) {
		return backing.containsValue(value);
	}


}
