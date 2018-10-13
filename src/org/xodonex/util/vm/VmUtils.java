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
package org.xodonex.util.vm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Utilities related to the JVM bytecode.
 */
public class VmUtils {

    // class access modifiers
    public final static int ACC_INVALID = -1, // invalid
            ACC_PUBLIC = 0x0001, // Declared public; may be accessed from
                                 // outside its package.
            ACC_FINAL = 0x0010, // Declared final; no subclasses allowed.
            ACC_SUPER = 0x0020, // Treat superclass methods specially when
                                // invoked by the invokespecial instruction.
            ACC_INTERFACE = 0x0200, // Is an interface, not a class.
            ACC_ABSTRACT = 0x0400; // Declared abstract; may not be
                                   // instantiated.

    // constant pool definitions
    public final static int CONSTANT_Class = 7,
            CONSTANT_Fieldref = 9,
            CONSTANT_Methodref = 10,
            CONSTANT_InterfaceMethodref = 11,
            CONSTANT_String = 8,
            CONSTANT_Integer = 3,
            CONSTANT_Float = 4,
            CONSTANT_Long = 5,
            CONSTANT_Double = 6,
            CONSTANT_NameAndType = 12,
            CONSTANT_Utf8 = 1;

    private VmUtils() {
    }

    /**
     * Extract the class access modifiers from the class file referenced by the
     * input stream, and close the input stream. Return ACC_INVALID if an error
     * occurs.
     *
     * @param input
     *            the input stream containing .class data
     * @return the contained class' acccess modifiers, or ACC_INVALID on error.
     */
    public static int getAccessModifiers(DataInputStream input) {
        try {
            try {
                if (input.readInt() != 0xcafebabe) {
                    // invalid .class magic number
                    return ACC_INVALID;
                }

                // skip the major and minor version
                input.skipBytes(4);

                // skip the constant pool
                int constCount = input.readUnsignedShort();
                for (int i = 1; i < constCount; i++) {
                    switch (input.readByte()) {
                    case CONSTANT_Class:
                    case CONSTANT_String:
                        input.skipBytes(2);
                        break;
                    case CONSTANT_Fieldref:
                    case CONSTANT_Methodref:
                    case CONSTANT_InterfaceMethodref:
                    case CONSTANT_Integer:
                    case CONSTANT_Float:
                    case CONSTANT_NameAndType:
                        input.skipBytes(4);
                        break;
                    case CONSTANT_Long:
                    case CONSTANT_Double:
                        input.skipBytes(8);
                        i++; // uses two entries
                        break;
                    case CONSTANT_Utf8:
                        input.skipBytes(input.readShort());
                        break;
                    default:
                        return ACC_INVALID;
                    }
                }

                // return the access field
                return input.readShort();
            }
            finally {
                input.close();
            }
        }
        catch (IOException e) {
            return ACC_INVALID;
        }
    }

}
