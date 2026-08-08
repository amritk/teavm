/*
 *  Copyright 2026 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio.charset.impl;

import org.teavm.classlib.java.nio.charset.TCharset;
import org.teavm.classlib.java.nio.charset.TCharsetDecoder;
import org.teavm.classlib.java.nio.charset.TCharsetEncoder;

/**
 * A charset where bytes 0x00-0x7F are ASCII and 0x80-0xFF come from a table.
 * ISO-8859-1 is the case where the table is the identity, and has its own
 * implementation that needs no table at all.
 */
public class TSingleByteCharset extends TCharset {
    private final char[] byteToChar;

    public TSingleByteCharset(String canonicalName, String highHalf) {
        super(canonicalName, new String[0]);
        if (highHalf.length() != 128) {
            throw new IllegalArgumentException("expected 128 entries for bytes 0x80-0xFF");
        }
        byteToChar = new char[256];
        for (int i = 0; i < 128; ++i) {
            byteToChar[i] = (char) i;
            byteToChar[i + 128] = highHalf.charAt(i);
        }
    }

    char[] byteToChar() {
        return byteToChar;
    }

    @Override
    public boolean contains(TCharset cs) {
        return cs == this;
    }

    @Override
    public TCharsetDecoder newDecoder() {
        return new TSingleByteDecoder(this, byteToChar);
    }

    @Override
    public TCharsetEncoder newEncoder() {
        return new TSingleByteEncoder(this, byteToChar);
    }
}
