// Copyright 1998,1999,2000,2001,2018, Henrik Lauritzen.
/*
    This file is part of the Hojo interpreter & toolkit.

    The Hojo interpreter & toolkit is free software: you can redistribute it
    and/or modify it under the terms of the GNU Affero General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    The Hojo interpreter & toolkit is distributed in the hope that it will
    be useful or (at least have historical interest),
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.xodonex.util.struct;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class implements a priority queue using <code>long</code> values as
 * keys.
 */
public class LongPriorityQueue extends AbstractCollection
        implements Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The default size of the queue.
     */
    public final static int DEFAULT_SIZE = 16;

    /**
     * This value indicates whether the priority is least-first.
     */
    public final static boolean LEAST_FIRST = true;

    /**
     * This value indicates whether the priority is largest-first.
     */
    public final static boolean LARGEST_FIRST = false;

    /**
     * A <code>LongOrder</code> is used to generate a priority code for the
     * objects added to this a priority queue.
     */
    public static interface LongOrder {
        public long priorityOf(Object obj);
    }

    private int size;
    private long[] heap;
    private Object[] elements;
    private final LongOrder pOrder;
    private final boolean swapOrder; // swapped means that least elements are
                                     // first
    private int modCount; // modification counter (for iterators)

    /* ************************ Constructors ************************* */

    /**
     * Constructor.
     */
    public LongPriorityQueue() {
        this(DEFAULT_SIZE, LARGEST_FIRST, null);
    }

    /**
     * Constructor.
     *
     * @param c
     *            A collection which should be included in the queue.
     */
    public LongPriorityQueue(Collection c) {
        this(DEFAULT_SIZE, LARGEST_FIRST, null);
        addAll(c);
    }

    /**
     * Constructor.
     *
     * @param size
     *            The size of the queue.
     */
    public LongPriorityQueue(int size) {
        this(size, LARGEST_FIRST);
    }

    /**
     * Constructor.
     *
     * @param size
     *            The size of the queue.
     * @param pOrder
     *            THe prio function.
     */
    public LongPriorityQueue(int size, LongOrder pOrder) {
        this(size, LARGEST_FIRST, pOrder);
    }

    /**
     * Constructor.
     *
     * @param leastFirst
     *            Indicator of priority (<code>true</code> if the priority is
     *            least-first).
     */
    public LongPriorityQueue(boolean leastFirst) {
        this(DEFAULT_SIZE, leastFirst, null);
    }

    /**
     * Constructor.
     *
     * @param leastFirst
     *            Indicator of priority (<code>true</code> if the priority is
     *            least-first).
     * @param pOrder
     *            The prio function.
     */
    public LongPriorityQueue(boolean leastFirst, LongOrder pOrder) {
        this(DEFAULT_SIZE, leastFirst, pOrder);
    }

    /**
     * Constructor.
     *
     * @param size
     *            The size of the queue.
     * @param leastFirst
     *            Indicator of priority (<code>true</code> if the priority is
     *            least-first).
     */
    public LongPriorityQueue(int size, boolean leastFirst) {
        this(size, leastFirst, null);
    }

    /**
     * Constructor.
     *
     * @param size
     *            The size of the queue.
     * @param leastFirst
     *            Indicator of priority (<code>true</code> if the priority is
     *            least-first).
     * @param pOrder
     *            The prio function.
     */
    public LongPriorityQueue(int size, boolean leastFirst, LongOrder pOrder) {
        if (size <= 1) {
            size = DEFAULT_SIZE;
        }
        this.size = 0;
        heap = new long[size + 1];
        elements = new Object[size + 1];
        modCount = 0;
        swapOrder = leastFirst;
        this.pOrder = pOrder;
    }

    // for iterator().next()
    private Object getElement(int index, int expected)
            throws ArrayIndexOutOfBoundsException {
        if (expected != modCount) {
            throw new ConcurrentModificationException();
        }
        if ((index < 1) || (index > size)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return elements[index];
    }

    // for contains(). This should already be synchronized.
    private int findElement(Object o, long prio, int index) {
        if (index > size) {
            return -1;
        }

        long h = heap[index];

        if ((h == prio) && (o.equals(elements[index]))) {
            // found it
            return index;
        }
        else if ((h < prio) ^ swapOrder) {
            // only elements of lower (higher) priority exist below
            return -1;
        }
        else {
            // search the left branch
            int result = findElement(o, prio, index << 1);
            if (result >= 0) {
                // found it
                return result;
            }
            else {
                // search the right branch
                return findElement(o, prio, (index << 1) + 1);
            }
        }
    }

    // for contains(). This should already be synchronized.
    private int findElement(Object o) {
        if (o == null) {
            return -1;
        }
        return findElement(o,
                (pOrder == null) ? o.hashCode() : pOrder.priorityOf(o), 1);
    }

    // double the heap size. This should already be synchronized.
    private void inflate() {
        int newSize = (heap.length << 1) - 1; // 2*extra - 1 = 1 extra for index
                                              // 0

        long[] newHeap = new long[newSize];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;

        Object[] newElems = new Object[newSize];
        System.arraycopy(elements, 0, newElems, 0, elements.length);
        elements = newElems;
    }

    // insert an element into the heap. This should already be synchronized.
    private void heapInsert(Object o) {
        long prio = (pOrder == null) ? o.hashCode() : pOrder.priorityOf(o);

        modCount++;
        if (size >= heap.length - 1) {
            inflate();
        }

        int parent;
        int i = ++size;
        while ((i > 1) && ((heap[parent = i >> 1] < prio) ^ swapOrder)) {
            heap[i] = heap[parent];
            elements[i] = elements[parent];
            i = parent;
        }

        heap[i] = prio;
        elements[i] = o;
    }

    // remove a specific element from the heap. This should already be
    // synchronized.
    private Object heapRemove(int index) {
        Object result = elements[index];

        int s = size--;
        if (index < s) {
            elements[index] = elements[s];
            heap[index] = heap[s];
            heapify(index);
        }

        return result;
    }

    // fix up the heap after a removal operation. This should already be
    // synchronized.
    private void heapify(int i) {
        int l = i << 1;
        int r = l + 1;

        int best;
        best = ((l <= size) && ((heap[l] > heap[i]) ^ swapOrder)) ? l : i;
        if ((r <= size) && ((heap[r] > heap[best]) ^ swapOrder)) {
            best = r;
        }

        if (best != i) {
            // swap the elements at indices best and i.
            long tmp = heap[best];
            heap[best] = heap[i];
            heap[i] = tmp;
            Object o = elements[best];
            elements[best] = elements[i];
            elements[i] = o;

            // fix up the rest
            heapify(best);
        }
    }

    private Object extractElementAt(int index) throws NoSuchElementException {
        if (index > size) {
            throw new NoSuchElementException();
        }
        modCount++;
        return heapRemove(index);
    }

    /* ******************************* Queue ******************************* */

    public boolean isLowestFirst() {
        return swapOrder;
    }

    /**
     * Extracts an object from the queue.
     *
     * @return The element in the queue with highest priority.
     * @exception NoSuchElementException
     *                if the queue is empty
     */
    public Object extract() throws NoSuchElementException {
        if (size < 1) {
            throw new NoSuchElementException();
        }
        return extractElementAt(1);
    }

    /**
     * Returns the element from the queue with highest priority without removing
     * it from the queue.
     *
     * @return The element from the queue with highest priority
     */
    public Object peek() {
        if (size < 1) {
            return null;
        }
        return elements[1];
    }

    /**
     * Returns the priority of the next element in queue.
     *
     * @return The priority of the next element in queue.
     */
    public long peekPriority() {
        if (size < 1) {
            return -1;
        }
        return heap[1];
    }

    /*
     * ******************************* Collection
     * *******************************
     */

    /**
     * Returns the size of the queue.
     *
     * @return The size of the queue.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checks whether the queue is empty.
     *
     * @return <code>true</code> if the queue is empty, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks whether the queue contains the specified element.
     *
     * @param o
     *            The element.
     * @return <code>true</code> if the queue contains the specified element,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean contains(Object o) {
        return findElement(o) > 0;
    }

    /**
     * Returns an iterator for the queue.
     *
     * @return An iterator for the queue.
     */
    @Override
    public Iterator iterator() {
        return new Iterator() {
            private int cursor = 1;
            private boolean allowRemove = false;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                return (cursor < size());
            }

            @Override
            public Object next() throws NoSuchElementException {
                Object o = getElement(cursor++, expectedModCount);
                allowRemove = true;
                return o;
            }

            @Override
            public void remove() throws IllegalStateException {
                if (!allowRemove) {
                    throw new IllegalStateException();
                }
                else if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                else {
                    extractElementAt(cursor - 1);
                    expectedModCount++;
                }
            }
        };
    }

    /**
     * Returns an array representation of the queue.
     *
     * @return An array representation of the queue.
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(elements, 1, result, 0, result.length);
        return result;
    }

    /**
     * Returns an array representation of the queue.
     *
     * @param a
     *            The array which should hold the elements of the queue.
     * @return An array representation of the queue.
     */
    @Override
    public Object[] toArray(Object a[]) {
        if (a.length < size) {
            a = (Object[])Array.newInstance(a.getClass().getComponentType(),
                    size);
        }
        System.arraycopy(elements, 1, a, 0, size);
        return a;
    }

    /**
     * Adds an element to the queue.
     *
     * @param o
     *            The element to be added.
     * @return <code>true</code>.
     */
    @Override
    public boolean add(Object o) {
        heapInsert(o);
        return true;
    }

    /**
     * Remove an element from the queue.
     *
     * @param o
     *            The element to be removed.
     * @return <code>true</code> if the element was in the queue,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean remove(Object o) {
        int i = findElement(o);
        if (i < 0) {
            return false;
        }
        else {
            modCount++;
            heapRemove(i);
            return true;
        }
    }

    /**
     * Clears the queue.
     */
    @Override
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /*
     * ******************************* Cloneable *******************************
     */

    /**
     * Clones this instance of PriorityQueue.
     *
     * @return A clone of this instance of PriorityQueue.
     */
    @Override
    public Object clone() {
        try {
            LongPriorityQueue q = (LongPriorityQueue)super.clone();
            q.heap = new long[heap.length];
            System.arraycopy(heap, 0, q.heap, 0, heap.length);
            q.elements = new Object[elements.length];
            System.arraycopy(elements, 0, q.elements, 0, elements.length);
            q.modCount = 0;
            return q;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /* ******************************* Object ******************************* */

    /**
     * Compares this object to another object.
     *
     * @param o
     *            The object to be compared with this object.
     * @return <code>true</code> if this object and o are equal,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LongPriorityQueue)) {
            return false;
        }

        LongPriorityQueue p = (LongPriorityQueue)o;

        if (p.swapOrder != swapOrder || p.size != size) {
            return false; // different size or ordering
        }
        if (pOrder == null) {
            if (p.pOrder != null) {
                return false;
            }
        }
        else if (!pOrder.equals(p.pOrder)) {
            return false;
        }

        for (int i = size; i > 0;) {
            // check all elements, using the hashcode first for improved speed
            if ((p.heap[i] != heap[i])
                    || !p.elements[i].equals(elements[i--])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the hashCode for this object.
     *
     * @return The hashCode for this object.
     */
    @Override
    public int hashCode() {
        long hcode = size;

        for (int i = size; i > 0;) {
            hcode = (hcode * 3) + heap[i--];
        }
        return (int)hcode;
    }

}
