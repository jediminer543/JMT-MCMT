package org.jmt.mcmt.paralelised.fastutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class FastUtilHackUtil {

	public static class ConvertingObjectSet<E,T> implements ObjectSet<T> {

		Set<E> backing;
		Function<E, T> forward; 
		Function<T, E> back;
		
		public ConvertingObjectSet(Set<E> backing, Function<E, T> forward, Function<T, E> back) {
			this.backing = backing;
			this.forward = forward;
			this.back = back;
		}
		
		@Override
		public int size() {
			return backing.size();
		}

		@Override
		public boolean isEmpty() {
			return backing.isEmpty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object o) {
			try {
				return backing.contains(back.apply((T)o));
			} catch (ClassCastException cce) {
				return false;
			}
		}

		@Override
		public Object[] toArray() {
			return backing.stream().map(forward).toArray();
		}

		@Override
		public <R> R[] toArray(R[] a) {
			return backing.stream().map(forward).collect(Collectors.toSet()).toArray(a);
		}

		@Override
		public boolean add(T e) {
			return backing.add(back.apply(e));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			try {
				return backing.remove(back.apply((T)o));
			} catch (ClassCastException cce) {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean containsAll(Collection<?> c) {
			try {
				return backing.containsAll(c.stream().map(i -> back.apply((T) i)).collect(Collectors.toSet()));
			} catch (ClassCastException cce) {
				return false;
			}
			
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			return backing.addAll(c.stream().map(i -> back.apply(i)).collect(Collectors.toSet()));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean removeAll(Collection<?> c) {
			try {
				return backing.removeAll(c.stream().map(i -> back.apply((T) i)).collect(Collectors.toSet()));
			} catch (ClassCastException cce) {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean retainAll(Collection<?> c) {
			try {
				return backing.retainAll(c.stream().map(i -> back.apply((T) i)).collect(Collectors.toSet()));
			} catch (ClassCastException cce) {
				return false;
			}
		}

		@Override
		public void clear() {
			backing.clear();
			
		}

		@Override
		public ObjectIterator<T> iterator() {
			final Iterator<E> backg = backing.iterator();
			return new ObjectIterator<T>() {

				@Override
				public boolean hasNext() {
					return backg.hasNext();
				}

				@Override
				public T next() {
					return forward.apply(backg.next());
				}
				
				@Override
				public void remove() {
					backg.remove();
				}
			};
		}

		
	}
	
	private static <T> Int2ObjectMap.Entry<T> intEntryForwards(Map.Entry<Integer, T> entry) {
		return new Int2ObjectMap.Entry<T>() {

			@Override
			public T getValue() {
				return entry.getValue();
			}

			@Override
			public T setValue(T value) {
				return entry.setValue(value);
			}

			@Override
			public int getIntKey() {
				return entry.getKey();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj == entry) {
					return true;
				}
				return super.equals(obj);
			}
			
			@Override
			public int hashCode() {
				return entry.hashCode();
			}
		};
	}
	
	private static <T> Map.Entry<Integer, T> intEntryBackwards(Int2ObjectMap.Entry<T> entry) {
		return entry;
	}
	
	private static <T> Long2ObjectMap.Entry<T> longEntryForwards(Map.Entry<Long, T> entry) {
		return new Long2ObjectMap.Entry<T>() {

			@Override
			public T getValue() {
				return entry.getValue();
			}

			@Override
			public T setValue(T value) {
				return entry.setValue(value);
			}

			@Override
			public long getLongKey() {
				return entry.getKey();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj == entry) {
					return true;
				}
				return super.equals(obj);
			}
			
			@Override
			public int hashCode() {
				return entry.hashCode();
			}
		};
	}
	
	private static <T> Map.Entry<Long, T> longEntryBackwards(Long2ObjectMap.Entry<T> entry) {
		return entry;
	}
	
	private static Long2ByteMap.Entry longByteEntryForwards(Map.Entry<Long, Byte> entry) {
		return new Long2ByteMap.Entry() {

			@Override
			public Byte getValue() {
				return entry.getValue();
			}

			@Override
			public byte setValue(byte value) {
				return entry.setValue(value);
			}
			
			@Override
			public byte getByteValue() {
				return entry.getValue();
			}

			@Override
			public long getLongKey() {
				return entry.getKey();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj == entry) {
					return true;
				}
				return super.equals(obj);
			}
			
			@Override
			public int hashCode() {
				return entry.hashCode();
			}

		};
	}
	
	private static <T> Map.Entry<Long, Byte> longByteEntryBackwards(Long2ByteMap.Entry entry) {
		return entry;
	}

	public static <T> ObjectSet<Int2ObjectMap.Entry<T>> entrySetIntWrap(Map<Integer, T> map) {
		return new ConvertingObjectSet<Map.Entry<Integer, T>, Int2ObjectMap.Entry<T>>(map.entrySet(), FastUtilHackUtil::intEntryForwards, FastUtilHackUtil::intEntryBackwards);
	}
	
	public static <T> ObjectSet<Long2ObjectMap.Entry<T>> entrySetLongWrap(Map<Long, T> map) {
		return new ConvertingObjectSet<Map.Entry<Long, T>, Long2ObjectMap.Entry<T>>(map.entrySet(), FastUtilHackUtil::longEntryForwards, FastUtilHackUtil::longEntryBackwards);
	}
	
	public static ObjectSet<Long2ByteMap.Entry> entrySetLongByteWrap(Map<Long, Byte> map) {
		return new ConvertingObjectSet<Map.Entry<Long, Byte>, Long2ByteMap.Entry>(map.entrySet(), FastUtilHackUtil::longByteEntryForwards, FastUtilHackUtil::longByteEntryBackwards);
	}
	
	
	static class WrappingIntIterator implements IntIterator {

		Iterator<Integer> backing;
		
		public WrappingIntIterator(Iterator<Integer> backing) {
			this.backing = backing;
		}
		
		@Override
		public boolean hasNext() {
			return backing.hasNext();
		}

		@Override
		public int nextInt() {
			return backing.next();
		}
		
		@Override
		public Integer next() {
			return backing.next();
		}
		
		@Override
		public void remove() {
			backing.remove();
		}
		
	}
	
	static class WrappingLongIterator implements LongIterator {

		Iterator<Long> backing;
		
		public WrappingLongIterator(Iterator<Long> backing) {
			this.backing = backing;
		}
		
		@Override
		public boolean hasNext() {
			return backing.hasNext();
		}

		@Override
		public long nextLong() {
			return backing.next();
		}
		
		@Override
		public Long next() {
			return backing.next();
		}
		
		@Override
		public void remove() {
			backing.remove();
		}
		
	}
	
	public static class WrappingIntSet implements IntSet {

		Set<Integer> backing;
		
		public WrappingIntSet(Set<Integer> backing) {
			this.backing = backing;
		}
		
		@Override
		public boolean add(int key) {
			return backing.add(key);
		}

		@Override
		public boolean contains(int key) {
			return backing.contains(key);
		}

		@Override
		public int[] toIntArray() {
			return backing.stream().mapToInt(i -> i).toArray();
		}

		@Override
		public int[] toIntArray(int[] a) {
			if (a.length >= size()) {
				return null;
			} else {
				return toIntArray();
			}
		}

		@Override
		public int[] toArray(int[] a) {
			return toIntArray(a);
		}

		@Override
		public boolean addAll(IntCollection c) {
			return backing.addAll(c);
		}

		@Override
		public boolean containsAll(IntCollection c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean removeAll(IntCollection c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(IntCollection c) {
			return backing.retainAll(c);
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
		public Object[] toArray() {
			return backing.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return backing.toArray(a);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Integer> c) {
			return backing.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return backing.retainAll(c);
		}

		@Override
		public void clear() {
			backing.clear();
		}

		@Override
		public IntIterator iterator() {
			return new WrappingIntIterator(backing.iterator());
		}

		@Override
		public boolean remove(int k) {
			return backing.remove(k);
		}
		
	}

	public static LongSet wrapLongSet(Set<Long> longset) {
		return new WrappingLongSet(longset);
	}
	
	public static class WrappingLongSet implements LongSet {

		Set<Long> backing;
		
		public WrappingLongSet(Set<Long> backing) {
			this.backing = backing;
		}
		
		@Override
		public boolean add(long key) {
			return backing.add(key);
		}

		@Override
		public boolean contains(long key) {
			return backing.contains(key);
		}

		@Override
		public long[] toLongArray() {
			return backing.stream().mapToLong(i -> i).toArray();
		}

		@Override
		public long[] toLongArray(long[] a) {
			if (a.length >= size()) {
				return null;
			} else {
				return toLongArray();
			}
		}

		@Override
		public long[] toArray(long[] a) {
			return toLongArray(a);
		}

		@Override
		public boolean addAll(LongCollection c) {
			return backing.addAll(c);
		}

		@Override
		public boolean containsAll(LongCollection c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean removeAll(LongCollection c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(LongCollection c) {
			return backing.retainAll(c);
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
		public Object[] toArray() {
			return backing.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return backing.toArray(a);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Long> c) {
			return backing.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return backing.retainAll(c);
		}

		@Override
		public void clear() {
			backing.clear();
		}

		@Override
		public LongIterator iterator() {
			return new WrappingLongIterator(backing.iterator());
		}

		@Override
		public boolean remove(long k) {
			return backing.remove(k);
		}
		
	}

	public static IntSet wrapIntSet(Set<Integer> intset) {
		return new WrappingIntSet(intset);
	}
	
	public static class WrappingObjectCollection<V> implements ObjectCollection<V> {

		Collection<V> backing;
		
		public WrappingObjectCollection(Collection<V> backing) {
			this.backing = backing;
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
		public boolean contains(Object o) {
			return backing.contains(o);
		}

		@Override
		public Object[] toArray() {
			return backing.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return backing.toArray(a);
		}

		@Override
		public boolean add(V e) {
			return backing.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return backing.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			return backing.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return backing.retainAll(c);
		}

		@Override
		public void clear() {
			backing.clear();
		}

		@Override
		public ObjectIterator<V> iterator() {
			return FastUtilHackUtil.itrWrap(backing);
		}
		
	}

	public static <K> ObjectCollection<K> wrap(Collection<K> c) {
		return new WrappingObjectCollection<K>(c);
	}
	
	public static class WrappingByteCollection implements ByteCollection {

		Collection<Byte> backing;
		
		public WrappingByteCollection(Collection<Byte> backing) {
			this.backing = backing;
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
		public boolean contains(byte o) {
			return backing.contains(o);
		}

		@Override
		public Object[] toArray() {
			return backing.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return backing.toArray(a);
		}

		@Override
		public boolean add(byte e) {
			return backing.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return backing.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return backing.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Byte> c) {
			return backing.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return backing.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return backing.retainAll(c);
		}

		@Override
		public void clear() {
			backing.clear();
		}

		@Override
		public ByteIterator iterator() {
			return FastUtilHackUtil.itrByteWrap(backing);
		}

		@Override
		public boolean rem(byte key) {
			return this.remove(key);
		}

		@Override
		public byte[] toByteArray() {
			return null;
		}

		@Override
		public byte[] toByteArray(byte[] a) {
			return toArray(a);
		}

		@Override
		public byte[] toArray(byte[] a) {
			return ArrayUtils.toPrimitive(backing.toArray(new Byte[0]));
		}

		@Override
		public boolean addAll(ByteCollection c) {
			return addAll((Collection<Byte>)c);
		}

		@Override
		public boolean containsAll(ByteCollection c) {
			return containsAll((Collection<Byte>)c);
		}

		@Override
		public boolean removeAll(ByteCollection c) {
			return removeAll((Collection<Byte>)c);
		}

		@Override
		public boolean retainAll(ByteCollection c) {
			return retainAll((Collection<Byte>)c);
		}
		
	}
	
	public static ByteCollection wrapBytes(Collection<Byte> c) {
		return new WrappingByteCollection(c);
	}


	public static class WrappingLongListIterator implements LongListIterator {
		
		ListIterator<Long> backing;
		
		public WrappingLongListIterator(ListIterator<Long> backing) {
			this.backing = backing;
		}

		@Override
		public long previousLong() {
			return backing.previous();
		}

		@Override
		public long nextLong() {
			return backing.next();
		}

		@Override
		public boolean hasNext() {
			return backing.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return backing.hasPrevious();
		}

		@Override
		public int nextIndex() {
			return backing.nextIndex();
		}

		@Override
		public int previousIndex() {
			return backing.previousIndex();
		}
		
		@Override
		public void add(long k) {
			backing.add(k);
		}
		
		@Override
		public void remove() {
			backing.remove();
		}
		
		@Override
		public void set(long k) {
			backing.set(k);
		}
	}
	
	public static class SlimWrappingLongListIterator implements LongListIterator {
		
		Iterator<Long> backing;
		
		public SlimWrappingLongListIterator(Iterator<Long> backing) {
			this.backing = backing;
		}

		@Override
		public long previousLong() {
			throw new IllegalStateException();
		}

		@Override
		public long nextLong() {
			return backing.next();
		}

		@Override
		public boolean hasNext() {
			return backing.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			throw new IllegalStateException();
		}

		@Override
		public int nextIndex() {
			throw new IllegalStateException();
		}

		@Override
		public int previousIndex() {
			throw new IllegalStateException();
		}
		
		@Override
		public void add(long k) {
			throw new IllegalStateException();
		}
		
		@Override
		public void remove() {
			backing.remove();
		}
		
		@Override
		public void set(long k) {
			throw new IllegalStateException();
		}
	}
	
	public static LongListIterator wrap(ListIterator<Long> c) {
		return new WrappingLongListIterator(c);
	}
	
	public static LongListIterator wrap(Iterator<Long> c) {
		return new SlimWrappingLongListIterator(c);
	}
	
	public static class WrappingByteIterator implements ByteIterator {

		Iterator<Byte> parent;
		
		public WrappingByteIterator(Iterator<Byte> parent) {
			this.parent = parent;
		}
		
		@Override
		public boolean hasNext() {
			return parent.hasNext();
		}

		@Override
		public Byte next() {
			return parent.next();
		}
		
		@Override
		public void remove() {
			parent.remove();
		}

		@Override
		public byte nextByte() {
			return next();
		}
		
	}
	
	public static ByteIterator itrByteWrap(Iterator<Byte> backing) {
		return new WrappingByteIterator(backing);
	}
	
	public static ByteIterator itrByteWrap(Iterable<Byte> backing) {
		return new WrappingByteIterator(backing.iterator());
	}
	
	public static class WrapperObjectIterator<T> implements ObjectIterator<T> {

		Iterator<T> parent;
		
		public WrapperObjectIterator(Iterator<T> parent) {
			this.parent = parent;
		}
		
		@Override
		public boolean hasNext() {
			return parent.hasNext();
		}

		@Override
		public T next() {
			return parent.next();
		}
		
		@Override
		public void remove() {
			parent.remove();
		}
		
	}
	
	public static class IntWrapperEntry<T> implements Entry<T> {

		java.util.Map.Entry<Integer, T> parent;
		
		public IntWrapperEntry(java.util.Map.Entry<Integer, T> parent) {
			this.parent = parent;
		}
		
		@Override
		public T getValue() {
			return parent.getValue();
		}

		@Override
		public T setValue(T value) {
			return parent.setValue(value);
		}

		@Override
		public int getIntKey() {
			return parent.getKey();
		}
		
		@Override
		public Integer getKey() {
			return parent.getKey();
		}
		
	}
	
	public static class WrapperIntEntryObjectIterator<T> implements ObjectIterator<Entry<T>> {

		Iterator<Map.Entry<Integer, T>> parent;
		
		public WrapperIntEntryObjectIterator(Iterator<Map.Entry<Integer, T>> parent) {
			this.parent = parent;
		}
		
		@Override
		public boolean hasNext() {
			return parent.hasNext();
		}

		@Override
		public Entry<T> next() {
			Map.Entry<Integer, T> val = parent.next();
			if (val == null) return null;
			return new IntWrapperEntry<T>(val);
		}
		
		@Override
		public void remove() {
			parent.remove();
		}
		
	}
	
	public static <T> ObjectIterator<Entry<T>> intMapItrFake(Map<Integer, T> in) {
		return new WrapperIntEntryObjectIterator<T>(in.entrySet().iterator());
	}
	
	public static <T> ObjectIterator<T> itrWrap(Iterator<T> in) {
		return new WrapperObjectIterator<T>(in);
	}
	
	public static <T> ObjectIterator<T> itrWrap(Iterable<T> in) {
		return new WrapperObjectIterator<T>(in.iterator());
	}
}
