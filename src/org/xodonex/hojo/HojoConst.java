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
package org.xodonex.hojo;

import java.lang.reflect.Modifier;

import org.xodonex.util.ReflectUtils;
import org.xodonex.util.text.lexer.LexerTokens;

/**
 * Various constants for the compiler and type system.
 */
public interface HojoConst extends LexerTokens {

    public int NUM_PRI_BAD = ReflectUtils.NUM_PRI_BAD,
            NUM_PRI_BYTE = ReflectUtils.NUM_PRI_BYTE,
            NUM_PRI_SHORT = ReflectUtils.NUM_PRI_SHORT,
            NUM_PRI_INT = ReflectUtils.NUM_PRI_INT,
            NUM_PRI_LONG = ReflectUtils.NUM_PRI_LONG,
            NUM_PRI_FLOAT = ReflectUtils.NUM_PRI_FLOAT,
            NUM_PRI_DOUBLE = ReflectUtils.NUM_PRI_DOUBLE,
            NUM_PRI_BINT = ReflectUtils.NUM_PRI_BINT,
            NUM_PRI_BDEC = ReflectUtils.NUM_PRI_BDEC;

    public int MOD_PUBLIC = Modifier.PUBLIC,
            MOD_FINAL = Modifier.FINAL,
            MOD_SYNCHRONIZED = Modifier.SYNCHRONIZED,
            MOD_ALL = MOD_PUBLIC | MOD_FINAL | MOD_SYNCHRONIZED;

    // token code base size
    int BASE = 100000;
    int SIZE_BASE = 1000;

    // base token code for meta keywords
    public int META_BASE_ID = BASE;

    // meta keywords
    public int META_ANS = META_BASE_ID + 0,

            META_DEFINE = META_ANS + 1,
            META_UNDEF = META_DEFINE + 1,
            META_IMPORT = META_UNDEF + 1,
            META_EXPORT = META_IMPORT + 1,
            META_PACKAGE = META_EXPORT + 1,
            META_NOPACKAGE = META_PACKAGE + 1,
            META_DECLARE = META_NOPACKAGE + 1,
            META_UNDECLARE = META_DECLARE + 1,
            META_LOAD = META_UNDECLARE + 1,
            META_UNLOAD = META_LOAD + 1,
            META_OP = META_UNLOAD + 1,
            META_LEFT = META_OP + 1,
            META_RIGHT = META_LEFT + 1,
            META_NOP = META_RIGHT + 1,
            META_REMOVE = META_NOP + 1,
            META_INCLUDE = META_REMOVE + 1,
            META_EXIT = META_INCLUDE + 1,
            META_IF = META_EXIT + 1,
            META_ENDIF = META_IF + 1,
            META_PRAGMA = META_ENDIF + 1,

            META_ARGS = META_PRAGMA + 1,
            META_VERSION = META_ARGS + 1,
            META_REVISION = META_VERSION + 1,
            META_TYPEOF = META_REVISION + 1,
            META_VALUEOF = META_TYPEOF + 1,
            META_SOURCE = META_VALUEOF + 1,
            META_BASE = META_SOURCE + 1,
            META_LINE = META_BASE + 1,
            META_OUT = META_LINE + 1,
            META_ERR = META_OUT + 1,
            META_WARN = META_ERR + 1,
            META_PRINT = META_WARN + 1;

    // base token code for reserved words
    public int RES_BASE_ID = SIZE_BASE + META_BASE_ID;

    // reserved words
    public int RES_THIS = RES_BASE_ID,
            RES_SUPER = RES_THIS + 1,
            RES_PUBLIC = RES_SUPER + 1,
            RES_FINAL = RES_PUBLIC + 1,
            RES_SYNCHRONIZED = RES_FINAL + 1,
            RES_VAR = RES_SYNCHRONIZED + 1,
            RES_LET = RES_VAR + 1,
            RES_NEW = RES_LET + 1,
            RES_OP = RES_NEW + 1,
            RES_LAMBDA = RES_OP + 1,
            RES_CLASS = RES_LAMBDA + 1,
            RES_RETURN = RES_CLASS + 1,
            RES_BREAK = RES_RETURN + 1,
            RES_CONTINUE = RES_BREAK + 1,
            RES_THROW = RES_CONTINUE + 1,
            RES_IMPORT = RES_THROW + 1,
            RES_IF = RES_IMPORT + 1,
            RES_ELSE = RES_IF + 1,
            RES_WHILE = RES_ELSE + 1,
            RES_DO = RES_WHILE + 1,
            RES_FOR = RES_DO + 1,
            RES_SWITCH = RES_FOR + 1,
            RES_CASE = RES_SWITCH + 1,
            RES_DEFAULT = RES_CASE + 1,
            RES_TRY = RES_DEFAULT + 1,
            RES_CATCH = RES_TRY + 1,
            RES_FINALLY = RES_CATCH + 1;

    public int PCT_BASE_ID = SIZE_BASE + RES_BASE_ID;

    public int PCT_IDX_SEPARATOR = 0,
            PCT_IDX_DELIMITER = 1,
            PCT_IDX_LPAREN = 2,
            PCT_IDX_RPAREN = 3,
            PCT_IDX_BLOCKSTART = 4,
            PCT_IDX_BLOCKEND = 5,
            PCT_IDX_IDXSTART = 6,
            PCT_IDX_IDXEND = 7,
            PCT_IDX_MAPSTART = 8,
            PCT_IDX_MAPEND = 9,
            PCT_IDX_ARRAYSTART = 10,
            PCT_IDX_ARRAYEND = 11,
            PCT_IDX_LISTSTART = 12,
            PCT_IDX_LISTEND = 13,
            PCT_IDX_CASELABEL = 14;

    public int PCT_SEPARATOR = PCT_BASE_ID + PCT_IDX_SEPARATOR,
            PCT_DELIMITER = PCT_BASE_ID + PCT_IDX_DELIMITER,
            PCT_LPAREN = PCT_BASE_ID + PCT_IDX_LPAREN,
            PCT_RPAREN = PCT_BASE_ID + PCT_IDX_RPAREN,
            PCT_BLOCKSTART = PCT_BASE_ID + PCT_IDX_BLOCKSTART,
            PCT_BLOCKEND = PCT_BASE_ID + PCT_IDX_BLOCKEND,
            PCT_IDXSTART = PCT_BASE_ID + PCT_IDX_IDXSTART,
            PCT_IDXEND = PCT_BASE_ID + PCT_IDX_IDXEND,
            PCT_MAPSTART = PCT_BASE_ID + PCT_IDX_MAPSTART,
            PCT_MAPEND = PCT_BASE_ID + PCT_IDX_MAPEND,
            PCT_ARRAYSTART = PCT_BASE_ID + PCT_IDX_ARRAYSTART,
            PCT_ARRAYEND = PCT_BASE_ID + PCT_IDX_ARRAYEND,
            PCT_LISTSTART = PCT_BASE_ID + PCT_IDX_LISTSTART,
            PCT_LISTEND = PCT_BASE_ID + PCT_IDX_LISTEND,
            PCT_CASELABEL = PCT_BASE_ID + PCT_IDX_CASELABEL;

    // operator code bits: L(31) R(30) AR(29-28) PRIO(27-24) ID(23-0)
    public int OP_LEFTASSOC = 0x40000000,
            OP_RIGHTASSOC = 0x20000000,
            OP_NONASSOC = 0x00000000,
            OP_ASSOC_MASK = 0x60000000,

            OP_ARITY_MASK = 0x18000000,
            OP_SHIFT_ARITY = 27,
            OP_UNARY = 1 << OP_SHIFT_ARITY,
            OP_BINARY = 2 << OP_SHIFT_ARITY,
            OP_TERNARY = 3 << OP_SHIFT_ARITY,

            OP_PRIO_MASK = 0x07800000,
            OP_SHIFT_PRIO = 23,
            OP_PRIO_0 = 0 << OP_SHIFT_PRIO,
            OP_PRIO_1 = 1 << OP_SHIFT_PRIO,
            OP_PRIO_2 = 2 << OP_SHIFT_PRIO,
            OP_PRIO_3 = 3 << OP_SHIFT_PRIO,
            OP_PRIO_4 = 4 << OP_SHIFT_PRIO,
            OP_PRIO_5 = 5 << OP_SHIFT_PRIO,
            OP_PRIO_6 = 6 << OP_SHIFT_PRIO,
            OP_PRIO_7 = 7 << OP_SHIFT_PRIO,
            OP_PRIO_8 = 8 << OP_SHIFT_PRIO,
            OP_PRIO_9 = 9 << OP_SHIFT_PRIO,
            OP_PRIO_10 = 10 << OP_SHIFT_PRIO,
            OP_PRIO_11 = 11 << OP_SHIFT_PRIO,
            OP_PRIO_12 = 12 << OP_SHIFT_PRIO,
            OP_PRIO_13 = 13 << OP_SHIFT_PRIO,
            OP_PRIO_14 = 14 << OP_SHIFT_PRIO,
            OP_PRIO_15 = 15 << OP_SHIFT_PRIO,
            OP_PRIO_POSTFIX = OP_PRIO_15,
            OP_PRIO_PREFIX = OP_PRIO_14,
            OP_PRIO_COND_OR = OP_PRIO_4,
            OP_PRIO_TERNARY = OP_PRIO_3,
            OP_PRIO_ASSIGN = OP_PRIO_2,

            OP_ID_MASK = 0x007fffff,

            // define built-in operator IDs. Note that OP_INC and OP_DEC use
            // priority 14
            // to qualify as prefix operators - a special check is needed to
            // allow these
            // as postfix operators (prio 15).
            OP_DOT = OP_LEFTASSOC | OP_BINARY | OP_PRIO_15 | 1,
            OP_FUNC = OP_LEFTASSOC | OP_BINARY | OP_PRIO_15 | 2,

            OP_NEG = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 1,
            OP_NOT = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 2,
            OP_LNOT = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 3,
            OP_ABS = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 4,
            OP_INC = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 5,
            OP_DEC = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 6,
            OP_HEX = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 7,
            OP_SOURCE = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 8,
            OP_ID = OP_RIGHTASSOC | OP_UNARY | OP_PRIO_14 | 9,

            OP_POW = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_13 | 1,

            OP_MUL = OP_LEFTASSOC | OP_BINARY | OP_PRIO_12 | 1,
            OP_DIV = OP_LEFTASSOC | OP_BINARY | OP_PRIO_12 | 2,
            OP_MOD = OP_LEFTASSOC | OP_BINARY | OP_PRIO_12 | 3,

            OP_ADD = OP_LEFTASSOC | OP_BINARY | OP_PRIO_11 | 1,
            OP_SUB = OP_LEFTASSOC | OP_BINARY | OP_PRIO_11 | 2,
            OP_ISECT = OP_LEFTASSOC | OP_BINARY | OP_PRIO_11 | 3,

            OP_SHL = OP_LEFTASSOC | OP_BINARY | OP_PRIO_10 | 1,
            OP_SHR = OP_LEFTASSOC | OP_BINARY | OP_PRIO_10 | 2,
            OP_SHRA = OP_LEFTASSOC | OP_BINARY | OP_PRIO_10 | 3,

            OP_LT = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 1,
            OP_LE = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 2,
            OP_GE = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 3,
            OP_GT = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 4,
            OP_MIN = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 5,
            OP_MAX = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 6,
            OP_ELEM = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 7,
            OP_SUBSET = OP_LEFTASSOC | OP_BINARY | OP_PRIO_9 | 8,

            OP_EQ = OP_LEFTASSOC | OP_BINARY | OP_PRIO_8 | 1,
            OP_NE = OP_LEFTASSOC | OP_BINARY | OP_PRIO_8 | 2,
            OP_IEQ = OP_LEFTASSOC | OP_BINARY | OP_PRIO_8 | 3,
            OP_INE = OP_LEFTASSOC | OP_BINARY | OP_PRIO_8 | 4,
            OP_IOF = OP_LEFTASSOC | OP_BINARY | OP_PRIO_8 | 5,

            OP_CONS = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_7 | 1,
            OP_COMPOSE = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_7 | 2,

            OP_AND = OP_LEFTASSOC | OP_BINARY | OP_PRIO_6 | 1,
            OP_OR = OP_LEFTASSOC | OP_BINARY | OP_PRIO_6 | 2,
            OP_XOR = OP_LEFTASSOC | OP_BINARY | OP_PRIO_6 | 3,

            OP_COND_AND = OP_LEFTASSOC | OP_BINARY | OP_PRIO_5 | 1,

            OP_COND_OR = OP_LEFTASSOC | OP_BINARY | OP_PRIO_4 | 2,

            OP_IFTHEN = OP_RIGHTASSOC | OP_TERNARY | OP_PRIO_3 | 1,
            OP_ELSE = OP_RIGHTASSOC | OP_TERNARY | OP_PRIO_3 | 2,
            OP_SEQ = OP_NONASSOC | OP_TERNARY | OP_PRIO_3 | 3,
            OP_COUNT_SEQ = OP_NONASSOC | OP_TERNARY | OP_PRIO_3 | 4,

            OP_ASSIGN = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 1,
            OP_ASSGN_XCHG = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 2,
            OP_ASSGN_MUL = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 3,
            OP_ASSGN_DIV = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 4,
            OP_ASSGN_MOD = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 5,
            OP_ASSGN_ADD = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 6,
            OP_ASSGN_SUB = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 7,
            OP_ASSGN_SHL = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 8,
            OP_ASSGN_SHR = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 9,
            OP_ASSGN_AND = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 10,
            OP_ASSGN_OR = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 11,
            OP_ASSGN_XOR = OP_RIGHTASSOC | OP_BINARY | OP_PRIO_2 | 12,

            OP_BEFORE = OP_LEFTASSOC | OP_BINARY | OP_PRIO_1 | 1,
            OP_THEN = OP_LEFTASSOC | OP_BINARY | OP_PRIO_1 | 2;

    public int[] OP_CODES = {
            OP_DOT, OP_FUNC,
            OP_NEG, OP_NOT, OP_LNOT, OP_ABS, OP_INC, OP_DEC, OP_HEX, OP_SOURCE,
            OP_ID,
            OP_POW,
            OP_MUL, OP_DIV, OP_MOD,
            OP_ADD, OP_SUB, OP_ISECT,
            OP_SHL, OP_SHR, OP_SHRA,
            OP_LT, OP_LE, OP_GE, OP_GT, OP_MIN, OP_MAX, OP_ELEM, OP_SUBSET,
            OP_EQ, OP_NE, OP_IEQ, OP_INE, OP_IOF,
            OP_CONS, OP_COMPOSE,
            OP_AND, OP_OR, OP_XOR,
            OP_COND_AND,
            OP_COND_OR,
            OP_IFTHEN, OP_ELSE, OP_SEQ, OP_COUNT_SEQ,
            OP_ASSIGN, OP_ASSGN_XCHG, OP_ASSGN_MUL, OP_ASSGN_DIV,
            OP_ASSGN_MOD, OP_ASSGN_ADD, OP_ASSGN_SUB, OP_ASSGN_SHL,
            OP_ASSGN_SHR,
            OP_ASSGN_AND, OP_ASSGN_OR, OP_ASSGN_XOR,
            OP_BEFORE, OP_THEN
    };

    public int[] OP_COMPOUND_OPS = {
            OP_ID, OP_BEFORE, OP_MUL, OP_DIV, OP_MOD,
            OP_ADD, OP_SUB, OP_SHL, OP_SHR,
            OP_AND, OP_OR, OP_XOR
    };

    public int OP_IDX_DOT = 0,
            OP_IDX_FUNC = OP_IDX_DOT + 1,

            OP_IDX_NEG = OP_IDX_FUNC + 1,
            OP_IDX_NOT = OP_IDX_NEG + 1,
            OP_IDX_LNOT = OP_IDX_NOT + 1,
            OP_IDX_ABS = OP_IDX_LNOT + 1,
            OP_IDX_INC = OP_IDX_ABS + 1,
            OP_IDX_DEC = OP_IDX_INC + 1,
            OP_IDX_HEX = OP_IDX_DEC + 1,
            OP_IDX_SOURCE = OP_IDX_HEX + 1,
            OP_IDX_ID = OP_IDX_SOURCE + 1,

            OP_IDX_POW = OP_IDX_ID + 1,

            OP_IDX_MUL = OP_IDX_POW + 1,
            OP_IDX_DIV = OP_IDX_MUL + 1,
            OP_IDX_MOD = OP_IDX_DIV + 1,

            OP_IDX_ADD = OP_IDX_MOD + 1,
            OP_IDX_SUB = OP_IDX_ADD + 1,
            OP_IDX_ISECT = OP_IDX_SUB + 1,

            OP_IDX_SHL = OP_IDX_ISECT + 1,
            OP_IDX_SHR = OP_IDX_SHL + 1,
            OP_IDX_SHRA = OP_IDX_SHR + 1,

            OP_IDX_LT = OP_IDX_SHRA + 1,
            OP_IDX_LE = OP_IDX_LT + 1,
            OP_IDX_GE = OP_IDX_LE + 1,
            OP_IDX_GT = OP_IDX_GE + 1,
            OP_IDX_MIN = OP_IDX_GT + 1,
            OP_IDX_MAX = OP_IDX_MIN + 1,
            OP_IDX_ELEM = OP_IDX_MAX + 1,
            OP_IDX_SUBSET = OP_IDX_ELEM + 1,

            OP_IDX_EQ = OP_IDX_SUBSET + 1,
            OP_IDX_NE = OP_IDX_EQ + 1,
            OP_IDX_IEQ = OP_IDX_NE + 1,
            OP_IDX_INE = OP_IDX_IEQ + 1,
            OP_IDX_IOF = OP_IDX_INE + 1,

            OP_IDX_CONS = OP_IDX_IOF + 1,
            OP_IDX_COMPOSE = OP_IDX_CONS + 1,

            OP_IDX_AND = OP_IDX_COMPOSE + 1,
            OP_IDX_OR = OP_IDX_AND + 1,
            OP_IDX_XOR = OP_IDX_OR + 1,

            OP_IDX_COND_AND = OP_IDX_XOR + 1,

            OP_IDX_COND_OR = OP_IDX_COND_AND + 1,

            OP_IDX_IFTHEN = OP_IDX_COND_OR + 1, OP_IDX_ELSE = OP_IDX_IFTHEN + 1,
            OP_IDX_SEQ = OP_IDX_ELSE + 1,
            OP_IDX_COUNT_SEQ = OP_IDX_SEQ + 1,

            OP_IDX_ASSIGN = OP_IDX_COUNT_SEQ + 1,
            OP_IDX_ASSGN_XCHG = OP_IDX_ASSIGN + 1,
            OP_IDX_ASSGN_MUL = OP_IDX_ASSGN_XCHG + 1,
            OP_IDX_ASSGN_DIV = OP_IDX_ASSGN_MUL + 1,
            OP_IDX_ASSGN_MOD = OP_IDX_ASSGN_DIV + 1,
            OP_IDX_ASSGN_ADD = OP_IDX_ASSGN_MOD + 1,
            OP_IDX_ASSGN_SUB = OP_IDX_ASSGN_ADD + 1,
            OP_IDX_ASSGN_SHL = OP_IDX_ASSGN_SUB + 1,
            OP_IDX_ASSGN_SHR = OP_IDX_ASSGN_SHL + 1,
            OP_IDX_ASSGN_AND = OP_IDX_ASSGN_SHR + 1,
            OP_IDX_ASSGN_OR = OP_IDX_ASSGN_AND + 1,
            OP_IDX_ASSGN_XOR = OP_IDX_ASSGN_OR + 1,

            OP_IDX_BEFORE = OP_IDX_ASSGN_XOR + 1,
            OP_IDX_THEN = OP_IDX_BEFORE + 1,

            OP_COUNT = OP_IDX_THEN + 1;

    public int TYP_IDX_OBJECT = 0,
            TYP_IDX_VOID = 2;

}
