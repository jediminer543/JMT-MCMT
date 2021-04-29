package org.jmt.mcmt.paralelised.fastutil.sync;

import java.util.Map;

import org.jmt.mcmt.paralelised.fastutil.FastUtilHackUtil;
import org.jmt.mcmt.paralelised.fastutil.Long2IntConcurrentNonLinkedOpenMap;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntSortedMap;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.Long2IntSortedMap.FastSortedEntrySet;

public class SyncLong2IntLinkedOpenHashMap extends Long2IntLinkedOpenHashMap {

	public SyncLong2IntLinkedOpenHashMap(final int expected, final float f) {
		super(expected, f);
	}

	public SyncLong2IntLinkedOpenHashMap(final int expected) {
		super(expected);
	}

	public SyncLong2IntLinkedOpenHashMap() {
		super();
	}

	public SyncLong2IntLinkedOpenHashMap(final Map<? extends Long, ? extends Integer> m, final float f) {
		super(m, f);
	}

	public SyncLong2IntLinkedOpenHashMap(final Map<? extends Long, ? extends Integer> m) {
		super(m);
	}

	public SyncLong2IntLinkedOpenHashMap(final Long2IntMap m, final float f) {
		super(m, f);
	}

	public SyncLong2IntLinkedOpenHashMap(final Long2IntMap m) {
		super(m);
	}

	public SyncLong2IntLinkedOpenHashMap(final long[] k, final int[] v, final float f) {
		super(k, v, f);
	}

	public SyncLong2IntLinkedOpenHashMap(final long[] k, final int[] v) {
		super(k, v);
	}

	public synchronized void putAll(Map<? extends Long, ? extends Integer> m) {
		super.putAll(m);
	}

	public synchronized int put(final long k, final int v) {
		return super.put(k, v);
	}

	public synchronized int addTo(final long k, final int incr) {
		return super.addTo(k, incr);
	}

	public synchronized int remove(final long k) {
		return super.remove(k);
	}

	public synchronized int removeFirstInt() {
		return super.removeFirstInt();
	}

	public synchronized int removeLastInt() {
		return super.removeLastInt();
	}


	public synchronized int getAndMoveToFirst(final long k) {
		return super.getAndMoveToFirst(k);
	}

	public synchronized int getAndMoveToLast(final long k) {
		return super.getAndMoveToLast(k);
	}

	public synchronized int putAndMoveToFirst(final long k, final int v) {
		return super.putAndMoveToFirst(k, v);
	}

	public synchronized int putAndMoveToLast(final long k, final int v) {
		return super.putAndMoveToLast(k, v);
	}
 
	public synchronized int get(final long k) {
		return super.get(k);
	}

	public synchronized boolean containsKey(final long k) {
		return super.containsKey(k);
	}

	public synchronized boolean containsValue(final int v) {
		return super.containsValue(v);
	}

	public synchronized int getOrDefault(final long k, final int defaultValue) {
		return super.getOrDefault(k, defaultValue);
	}

	public synchronized int putIfAbsent(final long k, final int v) {
		return super.putIfAbsent(k, v);
	}


	public synchronized boolean remove(final long k, final int v) {
		return super.remove(k, v);
	}


	public synchronized boolean replace(final long k, final int oldValue, final int v) {
		return super.replace(k, oldValue, v);
	}


	public synchronized int replace(final long k, final int v) {
		return super.replace(k, v);
	}


	public synchronized int computeIfAbsent(final long k, final java.util.function.LongToIntFunction mappingFunction) {
		return super.computeIfAbsent(k, mappingFunction);
	}


	public synchronized int computeIfAbsentNullable(final long k,
			final java.util.function.LongFunction<? extends Integer> mappingFunction) {
		return super.computeIfAbsentNullable(k, mappingFunction);
	}


	public synchronized int computeIfPresent(final long k,
			final java.util.function.BiFunction<? super Long, ? super Integer, ? extends Integer> remappingFunction) {
		return super.computeIfPresent(k, remappingFunction);
	}

	@Override
	public synchronized int compute(final long k,
			final java.util.function.BiFunction<? super Long, ? super Integer, ? extends Integer> remappingFunction) {
		return super.compute(k, remappingFunction);
	}

	@Override
	public synchronized int merge(final long k, final int v,
			final java.util.function.BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction) {
		return super.merge(k, v, remappingFunction);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized long firstLongKey() {
		return super.firstLongKey();
	}

	@Override
	public synchronized long lastLongKey() {
		return super.lastLongKey();
	}

	@Override
	public synchronized Long2IntSortedMap tailMap(long from) {
		return super.tailMap(from);
	}

	@Override
	public synchronized Long2IntSortedMap headMap(long to) {
		return super.headMap(to);
	}

	@Override
	public synchronized Long2IntSortedMap subMap(long from, long to) {
		return super.subMap(from, to);
	}

	@Override
	public synchronized LongComparator comparator() {
		return super.comparator();
	}
	
	
	@Override
	public synchronized FastSortedEntrySet long2IntEntrySet() {
		return super.long2IntEntrySet();
	}

	@Override
	public synchronized LongSortedSet keySet() {
		return super.keySet();
	}


	@Override
	public synchronized IntCollection values() {
		return super.values();
	}

	@Override
	public synchronized boolean trim() {
		return super.trim();
	}
	
	@Override
	public synchronized boolean trim(final int n) {
		return super.trim(n);
	}


	@Override
	public synchronized Long2IntLinkedOpenHashMap clone() {
		return super.clone();
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}
	

}
