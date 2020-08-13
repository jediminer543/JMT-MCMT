package org.jmt.mcmt.paralelised.fastutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmt.mcmt.asmdest.ObjectIteratorHack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
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

	public static <T> ObjectSet<Int2ObjectMap.Entry<T>> entrySetIntWrap(Map<Integer, T> map) {
		return new ConvertingObjectSet<Map.Entry<Integer, T>, Int2ObjectMap.Entry<T>>(map.entrySet(), FastUtilHackUtil::intEntryForwards, FastUtilHackUtil::intEntryBackwards);
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

	public static IntSet wrap(Set<Integer> intset) {
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
			return ObjectIteratorHack.itrWrap(backing);
		}
		
	}

	public static <K> ObjectCollection<K> wrap(Collection<K> c) {
		return new WrappingObjectCollection<K>(c);
	}
}
