# Plan: getting the fork upstream

Goal: send the fork's changes upstream in a form the maintainer will merge, and move
everything else out of the fork. In the end `amritk/teavm` becomes a plain mirror, or
goes away.

## 1. What the fork actually carries

`git log upstream/master..origin/master` lists 50 commits, but 36 of them are upstream
commits with rewritten hashes. The net tree diff (`git diff upstream/master origin/master`)
covers 88 files, +3905/−38, and comes from **14 fork-authored commits**:

| Commit | Area | Status |
|---|---|---|
| f2ced35 | wasm gc: stale stack depth in the coroutine transformation | **Merged upstream** as #1224, in a reworked form. The fork still carries the parts the maintainer rejected (javadoc + `else depthBeforeLastInstructionOut = 0`). |
| 33b5376 | wasm gc: `local.tee` type | **Upstream** as 277c6e0 (the #1225 fix, with tests in `WasmAsyncTest`). The fork still has its own `WasmTeeType*` test classes. |
| fdd8538 | wasm gc: flattened conditional type | **Upstream** as 9642523 + fd78e03 (the #1226 fix, with tests in `AsyncTest`). The fork still has its own `WasmFlattenedConditional*` test classes and comments. |
| ab52ce5 | wasm gc: reset coroutine state in `finally` | Rejected in the #1224 review. Only reachable after an internal compiler error. |
| 950ec1a | wasm gc: `staticInitialValue` null type | The commit itself says "defensive rather than a reproducible failure". |
| a726013 | optimizer: `ClassInitElimination` counts instance calls as class init | **Real silent miscompile.** No test yet. |
| bb4261f | reflection: proxy of an interface that has a constant | **Real bug**, with a one-line reproducer. |
| 79282ce | classlib: `Collectors.joining`, `\R`, `\p{IsAlphabetic}`, `java.home`/`user.dir` | Four unrelated changes in one commit. The first three are real bugs. |
| bf3166f | classlib: `String.lines`, `StringBuilder.repeat`, `File.toPath`, `Spliterators.*`, `IOError`, `System.exit`, `URLClassLoader`, `Normalizer`, `BreakIterator` | Mix of real JDK API gaps and stubs that only throw. |
| 1b2ea90 | classlib: UTF-32BE/LE, windows-1251, `Charset.forName` key bug | Real feature + a latent bug, ~400 lines. |
| 7ae061d | classlib: `os.version`, `os.arch` | `os.arch = "wasm32"` is wrong on the JS and C backends. |
| 6907e14 | classlib: 47 files of single-threaded stand-ins (executors, locks, futures, management, beans, awt, `MethodHandle`) | The commit itself marks it "Fork-only". |
| f6502f8 | classlib: `Thread.yield`/`sleep`, `Object.wait` made non-async | The commit says it is wrong for TeaVM in general. Breaks green threads. |
| e335458 | checkstyle fixes for the above | Goes away with the above. |

I checked current upstream master, and these bugs are still there: the `joining`
empty-first-element bug (`TCollectors` line 82), the proxy `<clinit>` bug
(`ProxyDependencySupport` line 143), and the `forName` key mismatch (`TCharset` line 149).

## 2. What the maintainer objected to

From the review threads on #1224, #1225, #1226 (and #1245, someone else's duplicate of #1225):

1. **Wrong root cause in the description.** #1225: "This PR does just the opposite
   that it states … you LLM did not determine the root issue properly". The code was
   right, but the description was wrong, and that cost credibility.
2. **Essay comments in code.** #1226: "Please, avoid such comments. I believe, they
   quickly become outdated." Every fork commit has paragraph-long comments.
3. **New test classes instead of reusing existing ones.** #1225, #1226, #1245: "It
   makes totally no sense to introduce a new test class, while an existing one could be
   reused." Async/Wasm regressions go in `WasmAsyncTest` / `AsyncTest`, and classlib
   tests go in the existing `*Test` for that class.
4. **Defensive extras.** #1224: "This is excessive. The only case when we get here is
   that we already had a broken module."
5. **Structure he would have written differently.** #1224: "it could have wrapped it in
   the existing method"; generators added without renaming the original one.
6. **The bottom line:** "Please, review the code after agent."

The maintainer uses Claude himself (upstream has `AGENTS.md` and commit 6d9f160, "Allow
Claude to run any gradle command"). He doesn't object to AI being involved. He objects
to unreviewed output: diffs that are bigger than the fix, and descriptions nobody
checked. Also, 277c6e0 and 9642523 landed even though #1225 and #1226 were closed.
The fixes were good; how they were packaged was not.

## 3. Rules for every upstream PR

- **One fix per PR, a handful of lines where possible.** Keep only one PR open at a
  time until two or three have merged cleanly. Then go up to two or three in parallel.
- **Write and check the description yourself.** Cover what fails, a minimal Java
  reproducer, why it fails (the real root cause, checked against the code), and what
  the fix changes. Keep it to about 5–10 lines and leave out the IntelliJ/ktfmt backstory
  beyond one sentence.
- **Commit style matches upstream:** `area: short lowercase summary` (`classlib:`,
  `wasm gc:`, `js:`, `c:`), with no body or a two-line one. No essays.
- **No explanatory comments** unless the code is truly non-obvious, and then one line.
- **Tests go in existing test classes.** Before adding a test, `grep` for the class
  that already covers that area. Never add a new test class or generator if an
  existing one fits.
- **No defensive code** for states that can't happen in a valid module.
- **Check upstream first.** `git fetch upstream && git log upstream/master -- <file>`
  and search open PRs/issues before starting. The `local.tee` fix was duplicated by
  someone else within two weeks.
- **Run it locally on every backend before opening the PR:**
  `./gradlew :tests:test --tests <Test> -Pteavm.tests.js=true -Pteavm.tests.wasm-gc=true -Pteavm.tests.c=true -Pteavm.tests.optimized=true`
  plus `./gradlew checkstyleMain`.
- **Read the final diff line by line before pushing**, the way you would review someone
  else's PR. If you can't explain a line, delete it.
- **Respond to review in person.** Don't paste agent output into the thread.
  Force-push the fix into the same single commit.

## 4. What happens to each change

### A. Drop from the fork now, because upstream already has it

- f2ced35, 33b5376, fdd8538: delete the fork's leftover hunks in `WasmTypeInference`
  and `CoroutineTransformation`, the `WasmTeeType*` / `WasmFlattenedConditional*`
  test classes and their `WasmTestPlugin` registration.
- ab52ce5 (`finally` cleanup): drop. It was already rejected, and only matters after a
  compiler crash.
- 950ec1a (`staticInitialValue`): drop unless a failing program turns up. If one does,
  it becomes a normal bug-fix PR with a test.

### B. Send as small PRs, in this order

The easiest wins go first, to rebuild trust. Each item is a separate PR.

1. **`classlib: fix Collectors.joining dropping empty first element`**. A few lines
   (track "first" explicitly, as `StringJoiner` does) plus one assertion in
   `CollectorsTest`. Best opening PR: obviously correct and trivial to review.
2. **`reflection: don't generate proxy workers for static interface methods`**. The
   3-line `STATIC` skip in `ProxyDependencySupport`, plus a test with
   `interface Foo { int X = 1; void bar(); }` in the existing proxy test class. Leave out
   the `getDeclaredMethod` message changes (separate PR if wanted).
3. **`classlib: support \R in regex`**, a test in `PatternTest`.
4. **`classlib: support \p{IsAlphabetic}`**, a test in `PatternTest`. Mention the
   `\P{...}` negation bug as a separate **issue**, not in this PR.
5. **`classlib: add String.lines()`**, **`StringBuilder.repeat(int, int)`**,
   **`File.toPath()`**, **`Spliterators.iterator` / `AbstractSpliterator`**,
   **`IOError`**. One PR each, every one an ordinary JDK API gap with a JDK-conformant
   test. Re-check each against the JDK spec. For example, `lines()` builds an
   `ArrayList` eagerly. That's fine, but be ready to make it lazy if asked.
6. **`classlib: fix Charset.forName lookup key`**, then **`classlib: add UTF-32BE and
   UTF-32LE`**. Before sending windows-1251, ask in an issue whether he wants
   single-byte code pages in classlib (code size).
7. **Missing members on existing classes** from 6907e14 (`Character`, `Class`,
   `Locale.forLanguageTag`, `ResourceBundle`, `ConcurrentHashMap`,
   `FileSystemProvider`, `Thread.setContextClassLoader`), one small PR per class.
   `Thread.State`/`getState()` must report the real state of TeaVM's green threads,
   not a constant `RUNNABLE`.

### C. Open an issue first, PR only if he agrees

8. **`ClassInitElimination` treats instance calls as initializing the class**
   (a726013). This is the most valuable fix in the fork, a silent miscompile, but it
   has no test. Steps:
   - Re-verify it on current upstream. Upstream changed `ClassInitializerAnalysis` in
     6f3bd4b (Oct 7), which changes which initializers count as dynamic.
   - Build a reproducer test: a class whose `<clinit>` the analysis treats as dynamic,
     an instance call on it, then a static-field read in the same method after
     inlining. Put it in an existing optimizer/class-init test class.
   - Open an issue with the reproducer and the one-line fix (`invoke.getInstance() == null`,
     the same condition `ClassInitInsertion` uses). He may prefer to fix it himself,
     which is fine.
9. **One reachable `@Async` method makes a third of the program a coroutine, and the
   module traps at instantiation** (the analysis in f6502f8). This is a real upstream
   bug report: `WasmGCJSRuntime.stringToJs` gets coroutine-transformed, the module
   initializer calls it, and the coroutine prologue dereferences `Fiber.current()`
   before any fiber exists. File an issue with a minimal `@JSExport` module that
   reaches `Thread.yield()`, and ask which direction he wants: narrower
   `AsyncMethodFinder` edges, initializers that tolerate no fiber, or a "no threads"
   compiler option. **Don't** send the stand-in that removes `@Async` from
   `yield`/`sleep`/`wait`. It breaks threading for everyone.
10. **`MethodHandle.invoke` / `invokeExact` signature-polymorphic methods.** These
    need compiler support. Open an issue describing the problem, not the per-descriptor
    stubs.
11. **`java.home`, `user.dir`, `os.version`, `os.arch` defaults and `System.exit`.**
    Ask what values he wants per backend (the fork's `os.arch = "wasm32"` is wrong on JS
    and C) and whether `exit` should throw.
12. **`java.util.concurrent` executors / futures / locks / `CompletableFuture`.**
    Upstream has green threads, and its `TArrayBlockingQueue` really blocks via
    `@Async`. Any upstream version has to do the same. The fork's versions throw on
    `take()`, so they don't qualify. Ask which classes he would take, then implement
    them properly one at a time. This is real work, not a port.

### D. Move out of the fork into your own project

Upstream has a supported extension point for this: a `@Autoregistered`
`SimpleSubstitutionPolicy` on the compile classpath, loaded via `ServiceLoader`
(see `tools/perf/.../JmhSubstitutionPolicy.java` and
`classlib/.../ClasslibSubstitutionPolicy.java`). A small jar in the formatter project,
say `teavm-shims`, can contain:

```java
@Autoregistered
public class ShimSubstitutionPolicy extends SimpleSubstitutionPolicy {
    @Override
    public void contribute(SubstitutionSink sink) {
        sink.selectClasses(inPackage("java", true))
                .packagePrefix("com.example.teavmshims.")
                .simpleNamePrefix("T");
    }
}
```

Any `java.*` class **not** present in upstream classlib then resolves to the shim's
`T`-class. That covers the declaration-only / throwing stubs that upstream won't take:
`java.lang.management.*`, `java.beans.PropertyChangeSupport`,
`java.awt.EventQueue`, `URLClassLoader`, `Normalizer`, `BreakIterator`, and the
single-threaded executors and locks until real ones land upstream (step 12).

Caveat: when several rules match, the first rule whose target class exists wins, in
`ServiceLoader` order. So this is reliable only for classes upstream *doesn't* have. Don't
use it to replace `TThread`, `TObject` or other existing classes. Prototype it with one
class (e.g. `ManagementFactory`) before moving the rest.

## 5. Order of work

| Phase | Work | Fork state after |
|---|---|---|
| 0 | Rebuild the fork as `upstream/master` + the remaining fork commits, with group A dropped and commits split per section 4. Verify the formatter project still builds against it. | ~10 small commits |
| 1 | PRs B1 → B2, one at a time. File issues C8 and C9 in parallel; issues are cheap and he can think about them while PRs are reviewed. | shrinking |
| 2 | B3–B7 as earlier ones merge; C8 PR if he agrees. | shrinking |
| 3 | Extract group D into the shims jar; follow up on C9–C12 by whatever he decides. | only the `yield`/`sleep`/`wait` patch (3 files) until C9 is resolved |
| 4 | C9 resolved upstream; the formatter project depends on a released TeaVM + shims jar. | **fork retired** |

Until phase 4, keep the remaining fork-only behavior as a small patch set on top of an
upstream tag, not as a long-lived divergent `master`.
