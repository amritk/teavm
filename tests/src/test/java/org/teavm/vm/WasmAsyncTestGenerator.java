/*
 *  Copyright 2025 Alexey Andreev.
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
package org.teavm.vm;

import java.util.function.Consumer;
import org.teavm.backend.wasm.intrinsics.WasmGCBodyIntrinsic;
import org.teavm.backend.wasm.intrinsics.WasmGCCodeGenContext;
import org.teavm.backend.wasm.model.WasmFunction;
import org.teavm.backend.wasm.model.WasmLocal;
import org.teavm.backend.wasm.model.WasmType;
import org.teavm.backend.wasm.model.instruction.WasmInstructionBuilder;
import org.teavm.backend.wasm.model.instruction.WasmIntBinaryOperation;
import org.teavm.backend.wasm.model.instruction.WasmIntType;
import org.teavm.model.MethodReference;

public class WasmAsyncTestGenerator implements WasmGCBodyIntrinsic {
    private WasmGCCodeGenContext context;

    public WasmAsyncTestGenerator(WasmGCCodeGenContext context) {
        this.context = context;
    }

    @Override
    public void apply(MethodReference method, WasmFunction function) {
        var param = new WasmLocal(WasmType.INT32, "n");
        var generator = new Generator(context, param);
        switch (method.getName()) {
            case "generatedMethod":
                function.add(param);
                generator.generate(function.getBody().builder());
                break;
            case "loopAfterBranch":
                function.add(param);
                generator.generateLoopAfterBranch(function.getBody().builder());
                break;
            case "loopAfterThrow":
                function.add(param);
                generator.generateLoopAfterThrow(function.getBody().builder());
                break;
            default:
                break;
        }
    }

    private static class Generator {
        final WasmGCCodeGenContext context;
        final WasmLocal param;

        Generator(WasmGCCodeGenContext context, WasmLocal param) {
            this.context = context;
            this.param = param;
        }

        void generate(WasmInstructionBuilder builder) {
            builder.i32Const(1);
            block(builder, b1 -> {
                b1.i32Const(10);
                block(b1, b2 -> {
                    b2.i32Const(100);
                    block(b2, b3 -> {
                        b3
                                .i32Const(1000)
                                .i32Const(1)
                                .getLocal(param)
                                .intBinary(WasmIntType.INT32, WasmIntBinaryOperation.EQ)
                                .branch(b1)
                                .drop()
                                .i32Const(2000)
                                .i32Const(2)
                                .getLocal(param)
                                .intBinary(WasmIntType.INT32, WasmIntBinaryOperation.EQ)
                                .branch(b2)
                                .drop()
                                .i32Const(3000)
                                .i32Const(3)
                                .getLocal(param)
                                .intBinary(WasmIntType.INT32, WasmIntBinaryOperation.EQ)
                                .branch(b3)
                                .drop()
                                .i32Const(4000);
                    });
                    b2.call(sumFn(), true);
                });
                b1.call(sumFn(), true);
            });
            builder.intBinary(WasmIntType.INT32, WasmIntBinaryOperation.ADD);
        }

        /*
         * A suspending loop that follows a suspending block whose body ends with a branch. The
         * branch empties the type stack, and neither it nor the untyped loop records the depth of
         * the stack they leave behind, so the coroutine transformation used to see the depth
         * recorded by the i32.const two instructions earlier and index the (by then empty) stack
         * snapshot with it.
         */
        void generateLoopAfterBranch(WasmInstructionBuilder builder) {
            var outer = builder.block(WasmType.INT32);
            escapeIfNonZero(outer);
            var branching = outer.block();
            branching
                    .i32Const(1)
                    .i32Const(2)
                    .call(sumFn(), true)
                    .i32Const(7)
                    .breakTo(outer.list);
            suspendingLoop(outer);
            outer.i32Const(0);
        }

        /*
         * Same shape, with the type stack emptied by throw instead of by a branch. This is what
         * Kotlin coroutine state machines produce in practice.
         */
        void generateLoopAfterThrow(WasmInstructionBuilder builder) {
            var outer = builder.block(WasmType.INT32);
            escapeIfNonZero(outer);
            var throwing = outer.block();
            throwing
                    .i32Const(1)
                    .i32Const(2)
                    .call(sumFn(), true)
                    .drop()
                    .call(newExceptionFn(), false)
                    .throw_(context.exceptionTag());
            suspendingLoop(outer);
            outer.i32Const(0);
        }

        private void escapeIfNonZero(WasmInstructionBuilder outer) {
            outer.getLocal(param);
            var conditional = outer.conditional();
            conditional.getThenBlock().builder()
                    .i32Const(100)
                    .breakTo(outer.list);
        }

        private void suspendingLoop(WasmInstructionBuilder builder) {
            builder.loop()
                    .i32Const(1)
                    .i32Const(2)
                    .call(sumFn(), true)
                    .drop();
        }

        private void block(WasmInstructionBuilder builder, Consumer<WasmInstructionBuilder> body) {
            var block = builder.block(WasmType.INT32);
            body.accept(block);
        }

        private WasmFunction sumFn() {
            return context.functions().forStaticMethod(new MethodReference(WasmAsyncTest.class, "sum", int.class,
                    int.class, int.class));
        }

        private WasmFunction newExceptionFn() {
            return context.functions().forStaticMethod(new MethodReference(WasmAsyncTest.class, "newException",
                    RuntimeException.class));
        }
    }
}
