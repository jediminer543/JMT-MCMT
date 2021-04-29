package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntSortedMap;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

public class Long2IntConcurrentNonLinkedOpenMap extends Long2IntLinkedOpenHashMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2082212127278131631L;
	
	public Map<Long, Integer> backing = new ConcurrentHashMap<Long, Integer>();
	int defaultRV = 0;

	public Long2IntConcurrentNonLinkedOpenMap(final int expected, final float f) {

	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 *
	 * @param expected the expected number of elements in the hash map.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final int expected) {
	}

	/**
	 * Creates a new hash map with initial expected
	 * {@link Hash#DEFAULT_INITIAL_SIZE} entries and
	 * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 */
	public Long2IntConcurrentNonLinkedOpenMap() {
	}

	/**
	 * Creates a new hash map copying a given one.
	 *
	 * @param m a {@link Map} to be copied into the new hash map.
	 * @param f the load factor.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final Map<? extends Long, ? extends Integer> m, final float f) {
		putAll(m);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given one.
	 *
	 * @param m a {@link Map} to be copied into the new hash map.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final Map<? extends Long, ? extends Integer> m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map copying a given type-specific one.
	 *
	 * @param m a type-specific map to be copied into the new hash map.
	 * @param f the load factor.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final Long2IntMap m, final float f) {
		this(m.size(), f);
		putAll(m);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given type-specific one.
	 *
	 * @param m a type-specific map to be copied into the new hash map.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final Long2IntMap m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @param f the load factor.
	 * @throws IllegalArgumentException if {@code k} and {@code v} have different
	 *                                  lengths.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final long[] k, final int[] v, final float f) {
		if (k.length != v.length)
			throw new IllegalArgumentException(
					"The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
		for (int i = 0; i < k.length; i++)
			this.put(k[i], v[i]);
	}

	/**
	 * Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @throws IllegalArgumentException if {@code k} and {@code v} have different
	 *                                  lengths.
	 */
	public Long2IntConcurrentNonLinkedOpenMap(final long[] k, final int[] v) {
		this(k, v, DEFAULT_LOAD_FACTOR);
	}

	public void putAll(Map<? extends Long, ? extends Integer> m) {
		backing.putAll(m);
	}

	public int put(final long k, final int v) {
		return backing.put(k, v);
	}

	public int addTo(final long k, final int incr) {
		return backing.put(k, this.get(k)+incr);
	}

	public int remove(final long k) {
		return backing.remove(k);
	}

	public int removeFirstInt() {
		return backing.remove(backing.keySet().stream().findAny().get());
	}

	public int removeLastInt() {
		return backing.remove(backing.keySet().stream().findAny().get());
	}


	public int getAndMoveToFirst(final long k) {
		return backing.get(k);
	}

	public int getAndMoveToLast(final long k) {
		return backing.get(k);
	}

	public int putAndMoveToFirst(final long k, final int v) {
		return backing.put(k, v);
	}

	public int putAndMoveToLast(final long k, final int v) {
		return backing.put(k, v);
	}

	public int get(final long k) {
		return backing.get(k);
	}

	public boolean containsKey(final long k) {
		return backing.containsKey(k);
	}

	public boolean containsValue(final int v) {
		return backing.containsValue(v);
	}

	public int getOrDefault(final long k, final int defaultValue) {
		return backing.getOrDefault(k, defaultValue);
	}

	public int putIfAbsent(final long k, final int v) {
		return backing.putIfAbsent(k, v);
	}


	public boolean remove(final long k, final int v) {
		return backing.remove(k, v);
	}


	public boolean replace(final long k, final int oldValue, final int v) {
		return backing.replace(k, oldValue, v);
	}


	public int replace(final long k, final int v) {
		return backing.replace(k, v);
	}


	public int computeIfAbsent(final long k, final java.util.function.LongToIntFunction mappingFunction) {
		return backing.computeIfAbsent(k, (l) -> mappingFunction.applyAsInt(l));
	}


	public int computeIfAbsentNullable(final long k,
			final java.util.function.LongFunction<? extends Integer> mappingFunction) {
		return backing.computeIfAbsent(k, (l) -> mappingFunction.apply(l));
	}


	public int computeIfPresent(final long k,
			final java.util.function.BiFunction<? super Long, ? super Integer, ? extends Integer> remappingFunction) {
		if (this.containsKey(k)) {
			return backing.put(k, remappingFunction.apply(k, backing.get(k)));
		}		
		return defaultReturnValue();
	}

	@Override
	public int compute(final long k,
			final java.util.function.BiFunction<? super Long, ? super Integer, ? extends Integer> remappingFunction) {
		return backing.compute(k, remappingFunction);
	}

	@Override
	public int merge(final long k, final int v,
			final java.util.function.BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction) {
		return backing.merge(k, v, remappingFunction);
	}

	@Override
	public void clear() {
		backing.clear();
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public long firstLongKey() {
		throw new NoSuchElementException();
	}

	@Override
	public long lastLongKey() {
		throw new NoSuchElementException();
	}

	@Override
	public Long2IntSortedMap tailMap(long from) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long2IntSortedMap headMap(long to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long2IntSortedMap subMap(long from, long to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LongComparator comparator() {
		return null;
	}

	@Override
	public FastSortedEntrySet long2IntEntrySet() {
		return entries;
	}

	

	@Override
	public LongSortedSet keySet() {
		return FastUtilHackUtil.wrapLongSortedSet(backing.keySet());
	}


	@Override
	public IntCollection values() {
		return FastUtilHackUtil.wrapInts(backing.values());
	}

	public boolean trim() {
		return true;
	}

	public boolean trim(final int n) {
		return true;
	}


	@Override
	public Long2IntConcurrentNonLinkedOpenMap clone() {
		return new Long2IntConcurrentNonLinkedOpenMap(backing);
	}

	@Override
	public int hashCode() {
		return backing.hashCode();
	}
	

}
