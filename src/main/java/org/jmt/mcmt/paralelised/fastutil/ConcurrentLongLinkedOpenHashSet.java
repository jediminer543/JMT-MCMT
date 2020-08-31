package org.jmt.mcmt.paralelised.fastutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

public class ConcurrentLongLinkedOpenHashSet extends LongLinkedOpenHashSet {

	private static final long serialVersionUID = -5532128240738069111L;

	private final ConcurrentSkipListSet<Long> backing;
	
	public ConcurrentLongLinkedOpenHashSet() {
		//backing = new ConcurrentLinkedDeque<Long>();
		backing = new ConcurrentSkipListSet <Long>();
	}
	
	public ConcurrentLongLinkedOpenHashSet(final int initial) {
		//backing = new ConcurrentLinkedDeque<Long>();
		backing = new ConcurrentSkipListSet <Long>();
	}
	
	public ConcurrentLongLinkedOpenHashSet(final int initial, final float dnc) {
		this(initial);
	}
	
	public ConcurrentLongLinkedOpenHashSet(final LongCollection c) {
		this(c.size());
		addAll(c);
	}
	
	public ConcurrentLongLinkedOpenHashSet(final LongCollection c, final float f) {
		this(c.size(), f);
		addAll(c);
	}
	
	public ConcurrentLongLinkedOpenHashSet(final LongIterator i, final float f) {
		this(16, f);
		while (i.hasNext())
			add(i.nextLong());
	}
	
	public ConcurrentLongLinkedOpenHashSet(final LongIterator i) {
		this(i, -1);
	}
	
	public ConcurrentLongLinkedOpenHashSet(final Iterator<?> i, final float f) {
		this(LongIterators.asLongIterator(i), f);
	}
	
	public ConcurrentLongLinkedOpenHashSet(final Iterator<?> i) {
		this(LongIterators.asLongIterator(i));
	}
	
	public ConcurrentLongLinkedOpenHashSet(final long[] a, final int offset, final int length, final float f) {
		this(length < 0 ? 0 : length, f);
		LongArrays.ensureOffsetLength(a, offset, length);
		for (int i = 0; i < length; i++)
			add(a[offset + i]);
	}

	public ConcurrentLongLinkedOpenHashSet(final long[] a, final int offset, final int length) {
		this(a, offset, length, DEFAULT_LOAD_FACTOR);
	}

	public ConcurrentLongLinkedOpenHashSet(final long[] a, final float f) {
		this(a, 0, a.length, f);
	}

	public ConcurrentLongLinkedOpenHashSet(final long[] a) {
		this(a, -1);
	}
	
	@Override
	public boolean add(final long k) {
		boolean out = backing.add(k);
		/*
		if (!firstDef) {
			first = k;
			firstDef = true;
		}
		last = k;
		*/
		return out;
	}
	
	@Override
	public boolean addAll(LongCollection c) {
		return addAll((Collection<Long>)c);
	}
	
	@Override
	public boolean addAll(Collection<? extends Long> c) {
		return backing.addAll(c);
	}
	
	@Override
	public boolean addAndMoveToFirst(final long k) {
		boolean out = backing.add(k);
		//first = k;
		return out;
	}
	
	@Override
	public boolean addAndMoveToLast(final long k) {
		boolean out = backing.add(k);
		//last = k;
		return out;
	}
	
	@Override
	public void clear() {
		backing.clear();
	}
	
	@Override
	public LongLinkedOpenHashSet clone() {
		return new ConcurrentLongLinkedOpenHashSet(backing.iterator());
	}
	
	@Override
	public LongComparator comparator() {
		return null;
	}
	
	@Override
	public boolean contains(final long k) {
		return backing.contains(k);
	}
	
	@Override
	public long firstLong() {
		/*
		if (backing.size() == 0) throw new NoSuchElementException();
		return first;
		*/
		return backing.first();
	}
	
	@Override
	public int hashCode() {
		return backing.hashCode();
	}
	
	@Override
	public LongSortedSet headSet(long to) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}
	
	@Override
	public LongListIterator iterator() {
		return FastUtilHackUtil.wrap(backing.iterator());
	}
	
	@Override
	public LongListIterator iterator(long from) {
		throw new IllegalStateException();
		//return FastUtilHackUtil.wrap(backing.iterator());
	}
	
	@Override
	public long lastLong() {
		/*
		if (backing.size() == 0) throw new NoSuchElementException();
		return last;
		*/
		return backing.last();
	}
	
	@Override
	public boolean remove(final long k) {
		/*
		if (k == first) {
			first = backing.iterator().next();
		}
		if (k == last) {
			last = backing.iterator().next();
		}
		*/
		return backing.remove(k);
	}
	
	@Override
	public long removeFirstLong() {
		long fl = this.firstLong();
		this.remove(fl);
		//first = backing.iterator().next();
		return fl;
	}
	
	@Override
	public long removeLastLong() {
		long fl = this.lastLong();
		this.remove(fl);
		//last = backing.iterator().next();
		return fl;
	}
	
	@Override
	public int size() {
		return backing.size();
	}
	
	@Override
	public LongSortedSet subSet(long from, long to) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public LongSortedSet tailSet(long from) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean trim() {
		return true;
	}
	
	@Override
	public boolean trim(final int n) {
		return true;
	}
}
