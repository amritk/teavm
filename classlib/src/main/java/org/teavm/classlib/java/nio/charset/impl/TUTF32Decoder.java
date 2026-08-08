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

public class TUTF32Decoder extends TBufferedDecoder {
    private final boolean littleEndian;

    public TUTF32Decoder(TCharset cs, boolean littleEndian) {
        // One code point per four bytes, and a supplementary one takes two
        // chars, so the worst case is one char per two bytes.
        super(cs, 0.5f, 0.5f);
        this.littleEndian = littleEndian;
    }

    @Override
    protected TCoderResult arrayDecode(byte[] inArray, int inPos, int inSize, char[] outArray, int outPos, int outSize,
            Controller controller) {
        TCoderResult result = null;
        while (inPos < inSize && outPos < outSize) {
            if (inPos + 4 > inSize) {
                if (!controller.hasMoreInput(4)) {
                    result = TCoderResult.UNDERFLOW;
                }
                break;
            }
            int b1 = inArray[inPos] & 0xFF;
            int b2 = inArray[inPos + 1] & 0xFF;
            int b3 = inArray[inPos + 2] & 0xFF;
            int b4 = inArray[inPos + 3] & 0xFF;
            int codePoint = littleEndian
                    ? b1 | (b2 << 8) | (b3 << 16) | (b4 << 24)
                    : b4 | (b3 << 8) | (b2 << 16) | (b1 << 24);
            if (codePoint < 0 || codePoint > Character.MAX_CODE_POINT
                    || (codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE)) {
                result = TCoderResult.malformedForLength(4);
                break;
            }
            if (Character.isSupplementaryCodePoint(codePoint)) {
                if (outPos + 2 > outSize) {
                    if (!controller.hasMoreOutput(2)) {
                        result = TCoderResult.OVERFLOW;
                    }
                    break;
                }
                outArray[outPos++] = Character.highSurrogate(codePoint);
                outArray[outPos++] = Character.lowSurrogate(codePoint);
            } else {
                outArray[outPos++] = (char) codePoint;
            }
            inPos += 4;
        }

        controller.setInPosition(inPos);
        controller.setOutPosition(outPos);
        return result;
    }
}
