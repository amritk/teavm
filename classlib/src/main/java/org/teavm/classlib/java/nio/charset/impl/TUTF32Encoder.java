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

public class TUTF32Encoder extends TBufferedEncoder {
    private final boolean littleEndian;

    public TUTF32Encoder(TCharset cs, boolean littleEndian) {
        super(cs, 4, 4);
        this.littleEndian = littleEndian;
    }

    @Override
    protected TCoderResult arrayEncode(char[] inArray, int inPos, int inSize, byte[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            if (outPos + 4 > outSize) {
                if (!controller.hasMoreOutput(4)) {
                    result = TCoderResult.OVERFLOW;
                }
                break;
            }
            char c = inArray[inPos];
            int codePoint;
            int consumed;
            if (Character.isHighSurrogate(c)) {
                if (inPos + 1 == inSize) {
                    if (!controller.hasMoreInput(2)) {
                        result = TCoderResult.UNDERFLOW;
                    }
                    break;
                }
                char next = inArray[inPos + 1];
                if (!Character.isLowSurrogate(next)) {
                    result = TCoderResult.malformedForLength(1);
                    break;
                }
                codePoint = Character.toCodePoint(c, next);
                consumed = 2;
            } else if (Character.isLowSurrogate(c)) {
                result = TCoderResult.malformedForLength(1);
                break;
            } else {
                codePoint = c;
                consumed = 1;
            }
            if (littleEndian) {
                outArray[outPos++] = (byte) codePoint;
                outArray[outPos++] = (byte) (codePoint >> 8);
                outArray[outPos++] = (byte) (codePoint >> 16);
                outArray[outPos++] = (byte) (codePoint >> 24);
            } else {
                outArray[outPos++] = (byte) (codePoint >> 24);
                outArray[outPos++] = (byte) (codePoint >> 16);
                outArray[outPos++] = (byte) (codePoint >> 8);
                outArray[outPos++] = (byte) codePoint;
            }
            inPos += consumed;
        }
        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }
}
