package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Long2ObjectConcurrentHashMap<V> implements Long2ObjectMap<V> {

	Map<Long, V> backing;
	V defaultReturn = null;
	
	public Long2ObjectConcurrentHashMap() {
		backing = new ConcurrentHashMap<Long, V>();
	}
	
	@Override
	public V get(long key) {
		V out = backing.get(key);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : out;
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return backing.containsValue(value);
	}

	@Override
	public void putAll(Map<? extends Long, ? extends V> m) {
		backing.putAll(m);
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public void defaultReturnValue(V rv) {
		defaultReturn = rv;
	}

	@Override
	public V defaultReturnValue() {
		return defaultReturn;
	}

	@Override
	public ObjectSet<Entry<V>> long2ObjectEntrySet() {
		return FastUtilHackUtil.entrySetLongWrap(backing);
	}

		
	@Override
	public LongSet keySet() {
		return FastUtilHackUtil.wrapLongSet(backing.keySet());
	}

	@Override
	public ObjectCollection<V> values() {
		return FastUtilHackUtil.wrap(backing.values());
	}

	@Override
	public boolean containsKey(long key) {
		return backing.containsKey(key);
	}

	@Override
	public V put(long key, V value) {
		return put((Long)key, value);
	}
	
	@Override
	public V put(Long key, V value) {
		V out = backing.put(key, value);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : backing.put(key, value);
	}
	
	@Override
	public V remove(long key) {
		V out = backing.remove(key);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : out;
	}
}
