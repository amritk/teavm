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
import org.teavm.classlib.java.nio.charset.TCoderResult;

public class TSingleByteDecoder extends TBufferedDecoder {
    private final char[] byteToChar;

    public TSingleByteDecoder(TCharset cs, char[] byteToChar) {
        super(cs, 1, 1);
        this.byteToChar = byteToChar;
    }

    @Override
    protected TCoderResult arrayDecode(byte[] inArray, int inPos, int inSize, char[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            char c = byteToChar[inArray[inPos] & 0xFF];
            // A hole in the table is a byte the encoding does not define. It is
            // malformed input rather than U+FFFD, which is a character a
            // conforming stream is allowed to contain.
            if (c == 0xFFFD) {
                result = TCoderResult.malformedForLength(1);
                break;
            }
            inPos++;
            outArray[outPos++] = c;
        }

        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);

        return result;
    }
}
