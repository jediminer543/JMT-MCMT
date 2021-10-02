package org.jmt.mcmt.paralelised;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.NotImplementedException;

/*
 * DO NOT USE; IS BROKEN
 */
public class ConcurrentLinkedList<E> implements List<E> {

	final AtomicInteger size;
	
	public ConcurrentLinkedList() {
		this.size = new AtomicInteger();
	}
	
	Link head;
	Link tail;
	
	@Override
	public int size() {
		return size.get();
	}

	@Override
	public boolean isEmpty() {
		return size.get() == 0;
	}

	@Override
	public boolean contains(Object o) {
		Link current = head;
		while (current != null) {
			if (current.element == o || (current.element != null && current.element.equals(o))) {
				return true;
			}
			current = current.next;
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public Object[] toArray() {
		throw new NotImplementedException("TODO; Implement this");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new NotImplementedException("TODO; Implement this");
	}

	@Override
	public synchronized boolean add(E e) {
		if (head == null) {
			head = new Link(e, null);
			tail = head;
			return true;
		}
		Link l = new Link(e, tail);
		tail.next = l;
		head = l;
		size.incrementAndGet();
		return true;
	}
	
	private synchronized void int_remove(Link current) {
		size.decrementAndGet();
		Link prev = current.prev;
		Link next = current.next;
		if (prev != null) {
			prev.next = next;
		}
		if (next != null) {
			next.prev = prev;
		}
		if (current == head) {
			head = next;
		}
		if (current == tail) {
			head = prev;
		}
	}

	@Override
	public synchronized boolean remove(Object o) {
		Link current = head;
		while (current != null) {
			if (current.element == o || (current.element != null && current.element.equals(o))) {
				int_remove(current);
				return true;
			}
			current = current.next;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (indexOf(o) == -1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E i : c) {
			add(i);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object i : c) {
			remove(i);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public synchronized void clear() {
		tail = null;
		head = null;
	}

	@Override
	public E get(int index) {
		Link current = head;
		for (; index > 0; index--) {
			current = current.next;
			if (current == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		return current.element;
	}

	@Override
	public E set(int index, E element) {
		Link current = head;
		for (; index > 0; index--) {
			current = current.next;
			if (current == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		E old = current.element;
		current.element = element;
		return old;
	}

	@Override
	public void add(int index, E element) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public E remove(int index) {
		Link current = head;
		for (; index > 0; index--) {
			current = current.next;
			if (current == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		int_remove(current);
		return current.element;
	}

	@Override
	public int indexOf(Object o) {
		Link current = head;
		int i = 0;
		while (current != null) {
			if (current.element == o || (current.element != null && current.element.equals(o))) {
				return i;
			}
			i++;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		Link current = head;
		int lastHit = -1;
		int i = 0;
		while (current != null) {
			if (current.element == o || (current.element != null && current.element.equals(o))) {
				lastHit = i;
			}
			i++;
		}
		return lastHit;
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListIterator<E>() {

			int index = 0;
			Link prev = null;
			Link current = head;
			Link next = null;
			
			@Override
			public boolean hasNext() {
				next = current.next;
				return next != null;
			}

			@Override
			public E next() {
				current = next;
				next = null;
				prev = null;
				index++;
				return current.element;
			}

			@Override
			public boolean hasPrevious() {
				prev = current.prev;
				return prev != null;
			}

			@Override
			public E previous() {
				current = prev;
				next = null;
				prev = null;
				index--;
				return current.element;
			}

			@Override
			public int nextIndex() {
				return index+1;
			}

			@Override
			public int previousIndex() {
				return Math.min(index-1, 0);
			}

			@Override
			public void remove() {
				int_remove(current);
			}

			@Override
			public void set(E e) {
				current.element = e;
			}

			@Override
			public void add(E e) {
				throw new NotImplementedException("TODO");
			}
		};
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new NotImplementedException("TODO");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		forEach(e -> sb.append(e+", "));
		sb.append("]");
		return sb.toString();
	}
	
	class Link {
		
		E element;
		Link prev, next;
		
		public Link(E element, Link prev) {
			this.element = element;
			this.prev = prev;
			this.next = null;
		}
	}

}
