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

public class TSingleByteEncoder extends TBufferedEncoder {
    private final char[] byteToChar;

    public TSingleByteEncoder(TCharset cs, char[] byteToChar) {
        super(cs, 1, 1);
        this.byteToChar = byteToChar;
    }

    private int encodeChar(char c) {
        if (c < 0x80) {
            return c;
        }
        for (int i = 128; i < 256; ++i) {
            if (byteToChar[i] == c) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected TCoderResult arrayEncode(char[] inArray, int inPos, int inSize, byte[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            char c = inArray[inPos];
            if (Character.isHighSurrogate(c)) {
                if (inPos + 1 == inSize) {
                    if (!controller.hasMoreInput(2)) {
                        result = TCoderResult.UNDERFLOW;
                    }
                    break;
                }
                result = Character.isLowSurrogate(inArray[inPos + 1])
                        ? TCoderResult.unmappableForLength(2)
                        : TCoderResult.malformedForLength(1);
                break;
            } else if (Character.isLowSurrogate(c)) {
                result = TCoderResult.malformedForLength(1);
                break;
            }
            int b = encodeChar(c);
            if (b < 0) {
                result = TCoderResult.unmappableForLength(1);
                break;
            }
            inPos++;
            outArray[outPos++] = (byte) b;
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }
}
