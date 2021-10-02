package org.jmt.mcmt.paralelised.fastutil.sync;

import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

public class SyncLongLinkedOpenHashSet extends LongLinkedOpenHashSet {

	private static final long serialVersionUID = -5532128240738069111L;
	
	public SyncLongLinkedOpenHashSet() {
		super();
	}
	
	public SyncLongLinkedOpenHashSet(final int initial) {
		super(initial);
	}
	
	public SyncLongLinkedOpenHashSet(final int initial, final float dnc) {
		this(initial);
	}
	
	public SyncLongLinkedOpenHashSet(final LongCollection c) {
		this(c.size());
		addAll(c);
	}
	
	public SyncLongLinkedOpenHashSet(final LongCollection c, final float f) {
		this(c.size(), f);
		addAll(c);
	}
	
	public SyncLongLinkedOpenHashSet(final LongIterator i, final float f) {
		this(16, f);
		while (i.hasNext())
			add(i.nextLong());
	}
	
	public SyncLongLinkedOpenHashSet(final LongIterator i) {
		this(i, -1);
	}
	
	public SyncLongLinkedOpenHashSet(final Iterator<?> i, final float f) {
		this(LongIterators.asLongIterator(i), f);
	}
	
	public SyncLongLinkedOpenHashSet(final Iterator<?> i) {
		this(LongIterators.asLongIterator(i));
	}
	
	public SyncLongLinkedOpenHashSet(final long[] a, final int offset, final int length, final float f) {
		this(length < 0 ? 0 : length, f);
		LongArrays.ensureOffsetLength(a, offset, length);
		for (int i = 0; i < length; i++)
			add(a[offset + i]);
	}

	public SyncLongLinkedOpenHashSet(final long[] a, final int offset, final int length) {
		this(a, offset, length, DEFAULT_LOAD_FACTOR);
	}

	public SyncLongLinkedOpenHashSet(final long[] a, final float f) {
		this(a, 0, a.length, f);
	}

	public SyncLongLinkedOpenHashSet(final long[] a) {
		this(a, -1);
	}
	
	@Override
	public synchronized boolean add(final long k) {
		return super.add(k);
	}
	
	@Override
	public synchronized boolean addAll(LongCollection c) {
		return super.addAll(c);
	}
	
	@Override
	public synchronized boolean addAll(Collection<? extends Long> c) {
		return super.addAll(c);
	}
	
	@Override
	public synchronized boolean addAndMoveToFirst(final long k) {
		return super.addAndMoveToFirst(k);
	}
	
	@Override
	public synchronized boolean addAndMoveToLast(final long k) {
		return super.addAndMoveToFirst(k);
	}
	
	@Override
	public synchronized void clear() {
		super.clear();
	}
	
	@Override
	public synchronized LongLinkedOpenHashSet clone() {
		return new SyncLongLinkedOpenHashSet(this);
	}
	
	@Override
	public synchronized LongComparator comparator() {
		return super.comparator();
	}
	
	@Override
	public synchronized boolean contains(final long k) {
		return super.contains(k);
	}
	
	@Override
	public synchronized long firstLong() {
		return super.firstLong();
	}
	
	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public synchronized LongSortedSet headSet(long to) {
		return super.headSet(to);
	}
	
	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}
	
	@Override
	public synchronized LongListIterator iterator() {
		return super.iterator();
	}
	
	@Override
	public synchronized LongListIterator iterator(long from) {
		return super.iterator(from);
	}
	
	@Override
	public synchronized long lastLong() {
		return super.lastLong();
	}
	
	@Override
	public synchronized boolean remove(final long k) {
		return super.remove(k);
	}
	
	@Override
	public synchronized long removeFirstLong() {
		return super.removeFirstLong();
	}
	
	@Override
	public synchronized long removeLastLong() {
		return super.removeLastLong();
	}
	
	@Override
	public synchronized int size() {
		return super.size();
	}
	
	@Override
	public synchronized LongSortedSet subSet(long from, long to) {
		return super.subSet(from, to);
	}
	
	@Override
	public synchronized LongSortedSet tailSet(long from) {
		return super.tailSet(from);
	}
	
	@Override
	public synchronized boolean trim() {
		return super.trim();
	}
	
	@Override
	public synchronized boolean trim(final int n) {
		return super.trim(n);
	}
}
