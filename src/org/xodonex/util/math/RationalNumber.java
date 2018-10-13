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
package org.xodonex.util.math;

/**
 *
 * @author Henrik Lauritzen
 */
public class RationalNumber extends Number
        implements Comparable, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public static long gcd(long m, long n) {
        // Euclid's algorithm
        if (n == 0L) {
            return 1L;
        }

        long r;
        do {
            r = m % n;
            m = n;
            n = r;
        } while (r != 0L);
        return m;
    }

    /*
     * public static long gcd(long m, long n) { // J. Stein's algorithm, Knuth 2
     * p. 321 int fac = 1; // fac = 2^k while ((m & 1) == 0 && (n & 1) == 0) {
     * fac <<= 1; m >>= 1; n >>= 1; } long t = (m & 1) == 1 ? -n : m;
     *
     * do { while ((t & 1) == 0) { t >>= 1; } if (t > 0) { m = t; } else { n =
     * -t; } t = m - n; } while (t != 0);
     *
     * return fac * m; }
     */

    protected long _num, _denom;

    public RationalNumber(int nom, int denom) {
        this((long)nom, (long)denom);
    }

    public RationalNumber(long nom, long denom) {
        _num = nom;
        _denom = denom;
        normalize();
    }

    public boolean isNaN() {
        return (_num == 0L) && (_denom == 0L);
    }

    public boolean isInfinite() {
        return (_denom == 0L) && (_num != 0L);
    }

    public boolean isNegative() {
        return _num < 0L;
    }

    protected void normalize() {
        if (_denom != 0) {
            if (_num == 0) {
                // zero representation
                _denom = 1;
            }
            else {
                // cancel out the greatest common divisor
                long gcd = gcd(_num, _denom);
                _num /= gcd;
                _denom /= gcd;
            }
            if (_denom < 0) {
                // ensure that the denominator is nonnegative
                _num = -_num;
                _denom = -_denom;
            }
        }
        else {
            _num = _num < 0 ? -1l : _num > 0 ? 1l : 0l;
        }
    }

    protected void setValue(long num, long denom) {
        _num = num;
        _denom = denom;
        normalize();
    }

    protected void add(long num, long denom) {
        // check for denormals
        if (_denom == 0L) {
            if (denom == 0L) {
                // NaN if different signs (or NaN)
                if (_num != num) {
                    _num = 0L;
                }
            }
            else {
                // ignore
            }
            return;
        }
        else if (denom == 0L) {
            _num = num;
            _denom = denom;
            return;
        }

        // Knuth vol. 2, p. 313
        long d1 = gcd(_denom, denom);
        if (d1 == 1) {
            _num = _num * denom + _denom * num;
            _denom *= denom;
        }
        else {
            long t = _num * (denom / d1) + num * (_denom / d1);
            long d2 = gcd(t, d1);
            _num = t / d2;
            _denom = (_denom / d1) * (denom / d2);
        }
    }

    protected void sub(long num, long denom) {
        add(-num, denom);
    }

    protected void mul(long num, long denom) {
        // check for denormals
        if (_denom == 0L) {
            if (_num == 0 || num == 0L) {
                // NaN * x || (-)Inf * 0 -> NaN
                _num = 0L;
            }
            else if (denom == 0L) {
                // NaN if different signs (or NaN)
                if (_num != num) {
                    _num = 0L;
                }
            }
            else {
                if (num < 0) {
                    _num = -_num;
                }
            }
            return;
        }
        else if (denom == 0L) {
            if (_num == 0L) {
                // 0 * denormal -> NaN
                _denom = 0L;
            }
            else {
                // x * denormal -> denormal
                _num = _num < 0 ? -num : num;
                _denom = denom;
            }
            return;
        }

        // Knuth vol. 2, p. 313
        long d1 = gcd(_num, denom);
        long d2 = gcd(_denom, num);
        _num = (_num / d1) * (num / d2);
        _denom = (_denom / d2) * (denom / d1);
        if (_denom < 0L) {
            _num = -_num;
            _denom = -_denom;
        }
    }

    protected void div(long num, long denom) {
        // check for denormals
        if (_denom == 0L) {
            if (denom == 0L) {
                // denormal / denormal -> NaN
                _num = 0L;
            }
            else if (_num == 0L) {
                // NaN / x -> NaN : do nothing
            }
            else if (num == 0L) {
                // (-)Inf / 0 : do nothing
            }
            else {
                // (-)Inf / x -> (-)Inf
                _num = (_num != num) ? -1 : 1;
            }
            return;
        }
        else if (denom == 0L) {
            if (num == 0L) {
                // x / NaN -> NaN
                _num = 0L;
                _denom = 0L;
            }
            else {
                // x / (-)Inf -> 0
                _num = 0L;
                _denom = 1L;
            }
            return;
        }
        else if (num == 0L) {
            // divide by zero
            _num = _num > 0L ? 1L : -1L;
            _denom = 0L;
            return;
        }

        // Knuth vol. 2, p. 592
        long d1 = gcd(_num, num);
        long d2 = gcd(_denom, denom);
        _num = (_num / d1) * (denom / d2);
        if (num < 0L) {
            _num = -_num;
        }
        _denom = Math.abs((_denom / d2) * (num / d1));
    }

    protected void invert() {
        if (_denom == 0L) {
            if (_num != 0L) {
                // 1 / (-)Inf -> 0
                _num = 0L;
                _denom = 1L;
            }
            // else 1 / NaN -> NaN
        }
        else if (_num == 0L) {
            // 1 / 0 -> Inf
            _num = 1L;
            _denom = 0L;
        }
        else {
            // reciprocate, and ensure that the denominator is positive
            long tmp = _num;
            if (tmp < 0L) {
                _num = -_denom;
                _denom = -tmp;
            }
            else {
                _num = _denom;
                _denom = tmp;
            }
        }
    }

    @Override
    public int intValue() {
        return (_denom == 1L) ? (int)_num : (int)(_num / _denom);
    }

    @Override
    public long longValue() {
        return (_denom == 1L) ? _num : _num / _denom;
    }

    @Override
    public float floatValue() {
        return (_denom == 1L) ? (float)_num : (float)_num / _denom;
    }

    @Override
    public double doubleValue() {
        return (_denom == 1) ? (double)_num : (double)_num / _denom;
    }

    @Override
    public String toString() {
        if (_denom == 0L) {
            if (_num == 0L) {
                return "NaN";
            }
            else {
                return _num < 0L ? "-Infinity" : "Infinity";
            }
        }
        else if (_denom == 1L) {
            return "" + _num;
        }
        else {
            return "" + _num + "/" + _denom;
        }
    }

    @Override
    public int hashCode() {
        return (int)((_num >>> 32) * (_denom >>> 32) +
                (_num & 0xffffffff) * (_denom & 0xffffffff));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RationalNumber)) {
            return false;
        }
        RationalNumber r = (RationalNumber)obj;
        return r._num == _num && r._denom == _denom;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }

        RationalNumber r = (RationalNumber)obj;
        if (_denom == r._denom && _num == r._num) {
            // equal
            return 0;
        }
        else if (r._denom == 0 && r._num == 0) {
            // x.compareTo(NaN), x <> NaN
            return -1;
        }

        if (_denom == 0L) {
            if (_num == 0L) {
                // NaN.compareTo(r), r <> NaN
                return 1;
            }
            else {
                // (-)Inf.compareTo(r)
                return _num < 0L ? -1 : 1;
            }
        }
        else if (r._denom == 0) {
            // x.compareTo((-)Inf)
            return r._num < 0L ? 1 : -1;
        }
        else {
            // compare two normal, different fractions :
            // save old value, substract, compare, restore
            long n = _num, d = _denom;
            sub(r._num, r._denom);
            int result = _num < 0 ? -1 : 1;
            _num = n;
            _denom = d;
            return result;
        }
    }

}
