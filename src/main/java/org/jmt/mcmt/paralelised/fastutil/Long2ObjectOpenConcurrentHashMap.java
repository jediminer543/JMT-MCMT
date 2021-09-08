package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Long2ObjectOpenConcurrentHashMap<V> extends Long2ObjectOpenHashMap<V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -121514116954680057L;
	
	Map<Long, V> backing;
	V defaultReturn = null;
	
	public Long2ObjectOpenConcurrentHashMap() {
		backing = new ConcurrentHashMap<Long, V>();
	}
	
	@Override
	public V get(long key) {
		V out = backing.get(key);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : out;
	}
	
	@Override
	public V get(Object key) {
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
	public FastEntrySet<V> long2ObjectEntrySet() {
		return FastUtilHackUtil.entrySetLongWrapFast(backing);
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
	
	@Override
	public boolean trim() { return true; }
	
	@Override
	public boolean trim(final int n) { return true; }
	
	@Override
	public boolean replace(final long k, final V oldValue, final V v) {
		return backing.replace(k, oldValue, v);
	}
	
	@Override
	public V replace(final long k, final V v) {
		return backing.replace(k, v);	
	}
	
	@Override
	public boolean replace(final Long k, final V oldValue, final V v) {
		return backing.replace(k, oldValue, v);
	}
	
	@Override
	public V replace(final Long k, final V v) {
		return backing.replace(k, v);	
	}
	
	@Override
	public boolean remove(final long k, final Object v) {
		return backing.remove(k, v);
	}
	
	@Override
	public V putIfAbsent(final long k, final V v) {
		return backing.putIfAbsent(k, v);
	}
	
	@Override
	public V putIfAbsent(final Long k, final V v) {
		return backing.putIfAbsent(k, v);
	}
	
	@Override
	public V merge(final long k, final V v, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return backing.merge(k, v, remappingFunction);
	}
	
	@Override
	public V merge(Long k, final V v, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return backing.merge(k, v, remappingFunction);
	}
	
	@Override
	public int hashCode() {
		return backing.hashCode();
	}
	
	@Override
	public V getOrDefault(final long k, final V defaultValue) {
		return backing.getOrDefault(k, defaultValue);
	}
	
	@Override
	public V getOrDefault(Object k, final V defaultValue) {
		return backing.getOrDefault(k, defaultValue);
	}
	
	@Override
	public V computeIfPresent(final long k, final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
		return backing.computeIfPresent(k, remappingFunction);
	}
	
	@Override
	public V computeIfPresent(final Long k, final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
		return backing.computeIfPresent(k, remappingFunction);
	}
	
	@Override
	public V computeIfAbsent(final long k, final java.util.function.LongFunction<? extends V> mappingFunction) {
		return backing.computeIfAbsent(k, (llong) -> mappingFunction.apply(llong));
	}
	
	public V computeIfAbsent(final Long k, final java.util.function.LongFunction<? extends V> mappingFunction) {
		return backing.computeIfAbsent(k, (llong) -> mappingFunction.apply(llong));
	}
	
	@Override
	public V computeIfAbsentPartial(final long key, final Long2ObjectFunction<? extends V> mappingFunction) {
		if (!mappingFunction.containsKey(key))
			return defaultReturn;
		return backing.computeIfAbsent(key, (llong) -> mappingFunction.apply(llong));
	}
	
	@Override
	public V compute(final long k, final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
		return backing.compute(k, remappingFunction);
	}
	
	@Override
	public V compute(final Long k, final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
		return backing.compute(k, remappingFunction);
	}
	
	@Override
	public Long2ObjectOpenHashMap<V> clone() {
		throw new IllegalArgumentException();
	}
	
	public void clear() {
		backing.clear();
	}
	
	@Override
	public ObjectSet<java.util.Map.Entry<Long, V>> entrySet() {
		return new FastUtilHackUtil.ConvertingObjectSet<java.util.Map.Entry<Long, V>, java.util.Map.Entry<Long, V>>(backing.entrySet(), Function.identity(), Function.identity());
	}
	
	@Override
	public V remove(Object key) {
		return backing.remove(key);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		return backing.remove(key, value);
	}
	
}
