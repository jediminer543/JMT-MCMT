package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.NotImplementedException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Int2ObjectConcurrentHashMap<V> implements Int2ObjectMap<V> {

	Map<Integer, V> backing;
	
	public Int2ObjectConcurrentHashMap() {
		backing = new ConcurrentHashMap<Integer, V>();
	}
	
	@Override
	public V get(int key) {
		return backing.get(key);
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
	public void putAll(Map<? extends Integer, ? extends V> m) {
		backing.putAll(m);
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public void defaultReturnValue(V rv) {
		throw new NotImplementedException("MCMT - Not implemented");
	}

	@Override
	public V defaultReturnValue() {
		return null;
	}

	@Override
	public ObjectSet<Entry<V>> int2ObjectEntrySet() {
		return FastUtilHackUtil.entrySetIntWrap(backing);
	}

		
	@Override
	public IntSet keySet() {
		return FastUtilHackUtil.wrapIntSet(backing.keySet());
	}

	@Override
	public ObjectCollection<V> values() {
		return FastUtilHackUtil.wrap(backing.values());
	}

	@Override
	public boolean containsKey(int key) {
		return backing.containsKey(key);
	}

	@Override
	public V put(int key, V value) {
		return backing.put(key, value);
	}
	
	@Override
	public V put(Integer key, V value) {
		return backing.put(key, value);
	}
	
	@Override
	public V remove(int key) {
		return backing.remove(key);
	}
}
