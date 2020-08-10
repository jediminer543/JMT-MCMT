package org.jmt.mcmt.asmdest;

import java.util.Iterator;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class ObjectIteratorHack {

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
