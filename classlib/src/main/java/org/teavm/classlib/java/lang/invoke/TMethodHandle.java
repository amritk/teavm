/*
 *  Copyright 2017 Alexey Andreev.
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
package org.teavm.classlib.java.lang.invoke;

import org.teavm.classlib.java.lang.TClass;

public abstract class TMethodHandle {
    // MethodHandle.invoke and invokeExact are polymorphic-signature methods: the
    // caller writes whatever descriptor it likes and the JVM resolves it at run
    // time. An ahead-of-time compiler has to see a matching declaration, so the
    // descriptors this program emits are spelled out. That is whack-a-mole, and
    // the real fix is for the compiler to treat these two names specially.
    public Object invoke() {
        throw unsupported();
    }

    public void invoke(Object arg) {
        throw unsupported();
    }

    public void invoke(java.util.ResourceBundle arg) {
        throw unsupported();
    }

    public void invokeExact(Object[] args) {
        throw unsupported();
    }

    public Object invoke(Object... args) {
        throw unsupported();
    }

    public Object invokeWithArguments(Object... args) {
        throw unsupported();
    }

    public Object invokeWithArguments(java.util.List<?> args) {
        throw unsupported();
    }

    public TMethodHandle bindTo(Object receiver) {
        throw unsupported();
    }

    public TMethodHandle asType(TMethodType newType) {
        throw unsupported();
    }

    public TMethodHandle asSpreader(TClass<?> arrayType, int arrayLength) {
        throw unsupported();
    }

    public TMethodType type() {
        throw unsupported();
    }

    private static RuntimeException unsupported() {
        return new UnsupportedOperationException("MethodHandles cannot be resolved ahead of time");
    }
}
