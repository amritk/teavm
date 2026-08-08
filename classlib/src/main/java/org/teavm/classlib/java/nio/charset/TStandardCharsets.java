/*
 *  Copyright 2018 Alexey Andreev.
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
package org.teavm.classlib.java.nio.charset;

import org.teavm.classlib.java.nio.charset.impl.TAsciiCharset;
import org.teavm.classlib.java.nio.charset.impl.TIso8859Charset;
import org.teavm.classlib.java.nio.charset.impl.TSingleByteCharset;
import org.teavm.classlib.java.nio.charset.impl.TUTF16Charset;
import org.teavm.classlib.java.nio.charset.impl.TUTF32Charset;
import org.teavm.classlib.java.nio.charset.impl.TUTF8Charset;

public final class TStandardCharsets {
    private TStandardCharsets() {
    }

    public static final TCharset UTF_8 = TUTF8Charset.INSTANCE;
    public static final TCharset US_ASCII = new TAsciiCharset();
    public static final TCharset ISO_8859_1 = new TIso8859Charset();
    public static final TCharset UTF_16 = new TUTF16Charset("UTF-16", true, false);
    public static final TCharset UTF_16BE = new TUTF16Charset("UTF-16BE", false, false);
    public static final TCharset UTF_16LE = new TUTF16Charset("UTF-16LE", false, true);

    // Not in java.nio.charset.StandardCharsets, but Charset.forName has to
    // answer for them: every JDK ships them, and charset-detection code asks for
    // them by name and expects a Charset rather than an exception.
    public static final TCharset UTF_32BE = new TUTF32Charset("UTF-32BE", false);
    public static final TCharset UTF_32LE = new TUTF32Charset("UTF-32LE", true);
    public static final TCharset WINDOWS_1251 = new TSingleByteCharset("windows-1251",
            "\u0402\u0403\u201A\u0453\u201E\u2026\u2020\u2021"
                    + "\u20AC\u2030\u0409\u2039\u040A\u040C\u040B\u040F"
                    + "\u0452\u2018\u2019\u201C\u201D\u2022\u2013\u2014"
                    + "\uFFFD\u2122\u0459\u203A\u045A\u045C\u045B\u045F"
                    + "\u00A0\u040E\u045E\u0408\u00A4\u0490\u00A6\u00A7"
                    + "\u0401\u00A9\u0404\u00AB\u00AC\u00AD\u00AE\u0407"
                    + "\u00B0\u00B1\u0406\u0456\u0491\u00B5\u00B6\u00B7"
                    + "\u0451\u2116\u0454\u00BB\u0458\u0405\u0455\u0457"
                    + "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417"
                    + "\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F"
                    + "\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427"
                    + "\u0428\u0429\u042A\u042B\u042C\u042D\u042E\u042F"
                    + "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437"
                    + "\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F"
                    + "\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447"
                    + "\u0448\u0449\u044A\u044B\u044C\u044D\u044E\u044F");
}
