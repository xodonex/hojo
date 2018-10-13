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
package org.xodonex.util;

import java.lang.reflect.Array;

/**
 *
 * @author Henrik Lauritzen
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /* --------------------------- boolean --------------------------- */

    public static boolean[] enlarge(boolean[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        boolean[] newArr = new boolean[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static boolean[] removeRange(boolean[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        boolean[] newArr = new boolean[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static boolean[] insert(boolean[] arr, int index, boolean val) {
        if (index >= arr.length) {
            boolean[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        boolean[] newArr = new boolean[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static boolean[] insert(boolean[] arr, int index, boolean[] val) {
        if (index >= arr.length) {
            boolean[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        boolean[] newArr = new boolean[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static boolean[] reverseRange(boolean[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        boolean temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static boolean[] fill(boolean[] arr, int lo, int hi, boolean value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static boolean[] merge(boolean[] arr1, boolean[] arr2) {
        boolean[] result = new boolean[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static boolean[] merge(boolean[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        boolean[] result = new boolean[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(boolean[] arr1, boolean[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(boolean[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(boolean[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }

        int result = 1;
        for (int i = ofs; len > 0; i++, ofs--) {
            result = (result << 3) ^ (arr[i] ? 10 : 0);
        }
        return result;
    }

    public static String toString(boolean[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- byte --------------------------- */

    public static byte[] enlarge(byte[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        byte[] newArr = new byte[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static byte[] removeRange(byte[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        byte[] newArr = new byte[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static byte[] insert(byte[] arr, int index, byte val) {
        if (index >= arr.length) {
            byte[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        byte[] newArr = new byte[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static byte[] insert(byte[] arr, int index, byte[] val) {
        if (index >= arr.length) {
            byte[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        byte[] newArr = new byte[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static byte[] reverseRange(byte[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        byte temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static byte[] fill(byte[] arr, int lo, int hi, byte value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static byte[] merge(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static byte[] merge(byte[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        byte[] result = new byte[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(byte[] arr1, byte[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(byte[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(byte[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (result << 3) ^ arr[i];
        }
        return result;
    }

    public static String toString(byte[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- short --------------------------- */

    public static short[] enlarge(short[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        short[] newArr = new short[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static short[] removeRange(short[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        short[] newArr = new short[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static short[] insert(short[] arr, int index, short val) {
        if (index >= arr.length) {
            short[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        short[] newArr = new short[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static short[] insert(short[] arr, int index, short[] val) {
        if (index >= arr.length) {
            short[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        short[] newArr = new short[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static short[] reverseRange(short[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        short temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static short[] fill(short[] arr, int lo, int hi, short value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static short[] merge(short[] arr1, short[] arr2) {
        short[] result = new short[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static short[] merge(short[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        short[] result = new short[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(short[] arr1, short[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(short[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(short[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (result << 3) ^ arr[i];
        }
        return result;
    }

    public static String toString(short[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- char --------------------------- */

    public static char[] enlarge(char[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        char[] newArr = new char[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static char[] removeRange(char[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        char[] newArr = new char[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static char[] insert(char[] arr, int index, char val) {
        if (index >= arr.length) {
            char[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        char[] newArr = new char[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static char[] insert(char[] arr, int index, char[] val) {
        if (index >= arr.length) {
            char[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        char[] newArr = new char[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static char[] reverseRange(char[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        char temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static char[] fill(char[] arr, int lo, int hi, char value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static char[] merge(char[] arr1, char[] arr2) {
        char[] result = new char[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static char[] merge(char[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        char[] result = new char[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(char[] arr1, char[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(char[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(char[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (result << 3) ^ arr[i];
        }
        return result;
    }

    public static String toString(char[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- int --------------------------- */

    public static int[] enlarge(int[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        int[] newArr = new int[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static int[] removeRange(int[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        int[] newArr = new int[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static int[] insert(int[] arr, int index, int val) {
        if (index >= arr.length) {
            int[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        int[] newArr = new int[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static int[] insert(int[] arr, int index, int[] val) {
        if (index >= arr.length) {
            int[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        int[] newArr = new int[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static int[] reverseRange(int[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        int temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static int[] fill(int[] arr, int lo, int hi, int value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static int[] merge(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static int[] merge(int[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        int[] result = new int[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(int[] arr1, int[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(int[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(int[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (result << 3) ^ arr[i];
        }
        return result;
    }

    public static String toString(int[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- long --------------------------- */

    public static long[] enlarge(long[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        long[] newArr = new long[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static long[] removeRange(long[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        long[] newArr = new long[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static long[] insert(long[] arr, int index, long val) {
        if (index >= arr.length) {
            long[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        long[] newArr = new long[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static long[] insert(long[] arr, int index, long[] val) {
        if (index >= arr.length) {
            long[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        long[] newArr = new long[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static long[] reverseRange(long[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        long temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static long[] fill(long[] arr, int lo, int hi, long value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static long[] merge(long[] arr1, long[] arr2) {
        long[] result = new long[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static long[] merge(long[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        long[] result = new long[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(long[] arr1, long[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(long[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(long[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (((result << 3) ^ (int)arr[i]) << 3)
                    ^ ((int)(arr[i] >>> 32));
        }
        return result;
    }

    public static String toString(long[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- float --------------------------- */

    public static float[] enlarge(float[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        float[] newArr = new float[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static float[] removeRange(float[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        float[] newArr = new float[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static float[] insert(float[] arr, int index, float val) {
        if (index >= arr.length) {
            float[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        float[] newArr = new float[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static float[] insert(float[] arr, int index, float[] val) {
        if (index >= arr.length) {
            float[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        float[] newArr = new float[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static float[] reverseRange(float[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        float temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static float[] fill(float[] arr, int lo, int hi, float value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }

        return arr;
    }

    public static float[] merge(float[] arr1, float[] arr2) {
        float[] result = new float[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static float[] merge(float[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        float[] result = new float[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(float[] arr1, float[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(float[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(float[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - 1;
        }
        int result = 1;
        for (int i = ofs; len > 0; i++, len--) {
            result = (result << 3) ^ Float.floatToIntBits(arr[i]);
        }
        return result;
    }

    public static String toString(float[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- double --------------------------- */

    public static double[] enlarge(double[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        double[] newArr = new double[arr.length + xtra];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static double[] removeRange(double[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        double[] newArr = new double[arr.length - hi + lo];
        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static double[] insert(double[] arr, int index, double val) {
        if (index >= arr.length) {
            double[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        double[] newArr = new double[arr.length + 1];
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static double[] insert(double[] arr, int index, double[] val) {
        if (index >= arr.length) {
            double[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        double[] newArr = new double[arr.length + val.length];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static double[] reverseRange(double[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        double temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static double[] fill(double[] arr, int lo, int hi, double value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static double[] merge(double[] arr1, double[] arr2) {
        double[] result = new double[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static double[] merge(double[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        double[] result = new double[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(double[] arr1, double[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(double[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(double[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }

        int result = 1;
        long l;
        for (int i = ofs; len > 0; i++, len--) {
            l = Double.doubleToLongBits(arr[i]);
            result = (((result << 3) ^ (int)l) << 3) ^ ((int)(l >>> 32));
        }
        return result;
    }

    public static String toString(double[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;
        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    /* --------------------------- Object --------------------------- */

    public static Object[] enlarge(Object[] arr, int xtra) {
        if (xtra <= 0) {
            xtra = arr.length;
        }
        if (xtra == 0) {
            return arr;
        }

        Object[] newArr = (Object[])Array.newInstance(
                arr.getClass().getComponentType(), arr.length + xtra);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        return newArr;
    }

    public static Object[] removeRange(Object[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        if (hi <= lo) {
            return arr;
        }

        Object[] newArr = (Object[])Array.newInstance(
                arr.getClass().getComponentType(), arr.length - hi + lo);

        if (lo > 0) {
            System.arraycopy(arr, 0, newArr, 0, lo);
        }
        if (hi < arr.length) {
            System.arraycopy(arr, hi, newArr, lo, arr.length - hi);
        }

        return newArr;
    }

    public static Object[] insert(Object[] arr, int index, Object val) {
        if (index >= arr.length) {
            Object[] newArr = enlarge(arr, index + 1 - arr.length);
            newArr[index] = val;
            return newArr;
        }

        Object[] newArr = (Object[])Array.newInstance(
                arr.getClass().getComponentType(), arr.length + 1);
        newArr[index] = val;
        System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static Object[] insert(Object[] arr, int index, Object[] val) {
        if (index >= arr.length) {
            Object[] newArr = enlarge(arr, index + val.length - arr.length);
            System.arraycopy(val, 0, newArr, index, val.length);
            return newArr;
        }

        Object[] newArr = (Object[])Array.newInstance(
                arr.getClass().getComponentType(), arr.length + val.length);
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(val, 0, newArr, index, val.length);
        if (index < arr.length) {
            System.arraycopy(arr, index, newArr, index + val.length,
                    arr.length - index);
        }
        return newArr;
    }

    public static Object[] reverseRange(Object[] arr, int lo, int hi) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }

        hi--;
        Object temp;
        while (hi > lo) {
            temp = arr[hi];
            arr[hi--] = arr[lo];
            arr[lo++] = temp;
        }

        return arr;
    }

    public static Object[] fill(Object[] arr, int lo, int hi, Object value) {
        if (lo < 0) {
            lo = 0;
        }
        if (hi > arr.length || hi < lo) {
            hi = arr.length;
        }
        for (int i = lo; i < hi; i++) {
            arr[i] = value;
        }
        return arr;
    }

    public static Object[] merge(Object[] arr1, Object[] arr2) {
        Object[] result = new Object[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static Object[] merge(Object[][] arrs) {
        int newLength = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] != null) {
                newLength += arrs[i].length;
            }
        }

        Object[] result = new Object[newLength];
        int ofs = 0;

        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == null) {
                continue;
            }

            System.arraycopy(arrs[i], 0, result, ofs, arrs[i].length);
            ofs += arrs[i].length;
        }

        return result;
    }

    public static boolean equals(Object[] arr1, Object[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        else if (arr2 == null) {
            return false;
        }
        else if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == null) {
                if (arr2[i] != null) {
                    return false;
                }
            }
            else if (!arr1[i].equals(arr2[i])) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(Object[] arr) {
        return hashCode(arr, 0, -1);
    }

    public static int hashCode(Object[] arr, int ofs, int len) {
        if (arr == null) {
            return 0;
        }
        if (len < 0) {
            len = arr.length - ofs;
        }
        int result = 1;
        for (int i = ofs; ofs > 0; i++, len--) {
            result <<= 3;
            if (arr[i] != null) {
                result ^= arr[i].hashCode();
            }
        }
        return result;
    }

    public static String toString(Object[] arr) {
        StringBuffer b = new StringBuffer().append('(');
        int max = arr.length - 1;

        for (int i = 0; i <= max; i++) {
            b.append(arr[i]);
            if (i < max) {
                b.append(", ");
            }
        }

        return b.append(')').toString();
    }

}
