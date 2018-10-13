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
package org.xodonex.util.text.lexer;

/**
 *
 * @author Henrik Lauritzen
 */
class CharStack {

    protected int size = 0;
    protected char[] stack;

    public CharStack() {
        this(16);
    }

    public CharStack(int initialSize) {
        stack = new char[initialSize <= 0 ? 16 : initialSize];
    }

    public int size() {
        return size;
    }

    public void setMinCapacity(int capacity) {
        if (capacity <= stack.length) {
            return;
        }
        char[] newStack = new char[capacity];
        System.arraycopy(stack, 0, newStack, 0, size);
        stack = newStack;
    }

    public void ensureCapacity(int capacity) {
        int newC = stack.length;
        while (newC < capacity) {
            newC <<= 1;
        }
        setMinCapacity(newC);
    }

    public int pop() {
        if (size <= 0) {
            return -1;
        }
        return stack[--size];
    }

    public CharStack push(char c) {
        if (size >= stack.length) {
            setMinCapacity(stack.length * 2);
        }
        stack[size++] = c;
        return this;
    }

    public final CharStack push(char[] cs) {
        return push(cs, 0, cs.length);
    }

    public CharStack push(char[] cs, int off, int len) {
        if (len <= 0) {
            return this;
        }

        ensureCapacity(size + len);
        System.arraycopy(cs, off, stack, size, len);
        size += len;
        return this;
    }

    public final CharStack push(String s) {
        return push(s.toCharArray());
    }

    public final CharStack pushRev(char[] cs) {
        return pushRev(cs, 0, cs.length);
    }

    public CharStack pushRev(char[] cs, int off, int len) {
        if (len <= 0) {
            return this;
        }

        ensureCapacity(size + len);
        for (int i = off + len - 1; i >= off;) {
            stack[size++] = cs[i--];
        }
        return this;
    }

    public final CharStack pushRev(String s) {
        return pushRev(s.toCharArray());
    }

    public CharStack insert(char c) {
        if (size >= stack.length) {
            setMinCapacity(stack.length * 2);
        }
        System.arraycopy(stack, 0, stack, 1, size);
        stack[0] = c;
        size++;
        return this;
    }

    public final CharStack insert(char[] cs) {
        return insert(cs, 0, cs.length);
    }

    public CharStack insert(char[] cs, int off, int len) {
        ensureCapacity(size + len);
        System.arraycopy(stack, 0, stack, len, size);
        System.arraycopy(cs, off, stack, 0, len);
        size += len;
        return this;
    }

    public final CharStack insert(String s) {
        return insert(s.toCharArray());
    }

    public void clear() {
        size = 0;
    }

    @Override
    public String toString() {
        char[] tmp = new char[size];
        for (int i = size, idx = 0; i > 0;) {
            tmp[idx++] = stack[--i];
        }
        return new String(tmp);
    }

    public char[] getChars() {
        char[] result = new char[size];
        System.arraycopy(stack, 0, result, 0, size);
        return result;
    }

}
