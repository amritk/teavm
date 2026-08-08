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

public class TMethodHandles {
    /**
     * Enough shape for code that acquires a Lookup and hands it around. Actually
     * resolving a handle is what a closed world cannot do, so every lookup
     * throws rather than returning something that would fail later, somewhere
     * less obvious.
     */
    public static Lookup lookup() {
        return new Lookup();
    }

    public static Lookup publicLookup() {
        return new Lookup();
    }

    public static final class Lookup {
        public TMethodHandle findVirtual(Class<?> refc, String name, TMethodType type) {
            throw unsupported();
        }

        public TMethodHandle findStatic(Class<?> refc, String name, TMethodType type) {
            throw unsupported();
        }

        public TMethodHandle findSpecial(Class<?> refc, String name, TMethodType type, Class<?> specialCaller) {
            throw unsupported();
        }

        public TMethodHandle findConstructor(Class<?> refc, TMethodType type) {
            throw unsupported();
        }

        public TMethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
            throw unsupported();
        }

        public TMethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
            throw unsupported();
        }

        public TMethodHandle unreflectGetter(java.lang.reflect.Field field) {
            throw unsupported();
        }

        public TMethodHandle unreflectSetter(java.lang.reflect.Field field) {
            throw unsupported();
        }

        public TMethodHandle unreflect(java.lang.reflect.Method method) {
            throw unsupported();
        }

        public Class<?> lookupClass() {
            throw unsupported();
        }

        private static RuntimeException unsupported() {
            return new UnsupportedOperationException("MethodHandles cannot be resolved ahead of time");
        }
    }
}
