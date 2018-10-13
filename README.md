# The Hojo language and toolkit.

This repository contains the reference implementation of the Hojo language
interpreter, including the general toolkit used for its implementation.

## About the Hojo language

The Hojo language is basically a subset of the Java programming language
version 1.2, extended with a long list of features, including:

  * Functions as first-level objects, Lambdas and closures (inspired by SML)
  * Collections and Maps as first-level objects (inspired by Python)
  * Standard library of generic functions (inspired by SML)
  * Varargs, default arguments, extended for loops
    (similar to Java's own but much later extensions)
  * Preprocessor with macro and include capability (inspired by C)
  * Lazy typing and type inference
  * Extended type system, with advanced type conversions and including
    transparent bignum arithmetic
  * Extra operators (including JavaScript's `===`)
  * List slices (inspired by Python)
  * OS shell-command interface (inspired by Bash)
  * Library of high-level file-IO functions (inspired by GNU/Linux)
  * Library of standard functions with Swing UIs for user interaction

The main idea of [attempting to] fix the shortcomings of a language by
extending it with even more features, is actually the primary way in which
Hojo was inspired from C++.
(Analogously to C++, the end result was a linear improvement in programmer
productivity achieved at the cost of an exponential increase in complexity;
in Hojo's case the complexity is mostly hidden inside the compiler/library
implementation, though at the cost of maintainability and performance
issues there).

The reference implementation includes a shell, both command-line and
graphical.

The language reference document is part of the source
(`src/org/xodonex/hojo/resource/LangGuide.htm`);
it is also available as online help within the graphical shell.


### About the name
The name Hojo (pronounced like "hot-joe") is originally an acronym for
"Higher-Order Java Objects". Addition of higher-order functions to a
Java-like language was a major design driver for Hojo, hence this acronym.


### More language metadata

  * _Paradigm_: Object-oriented, imperative, functional, procedural, reflective
  * _Designed by_: Henrik Lauritzen
  * _Developer_: Henrik Lauritzen
  * _First appeared_: 1998, 20 years ago
  * _Stable release_: 1.3.0 / 13 October 2018
  * _Preview release_: N/A
  * _Typing discipline_: dynamic or static, strong
  * _License_: AGPLv3
  * _Filename extensions_: `.hjo`, `.hojo`
  * _Website_: (the upstream of this file)
  * _Dialects_: None
  * _Influenced by_: Java, Python, Standard ML, C, JavaScript, Bash, C++
  * _Influenced_: N/A
  * _Most similar to_: BeanShell, Jython, Groovy, JavaScript


## About the Hojo toolkit
The Hojo reference implementation is based on numerous utilities which
are quite general, i.e., have no ties to the Hojo use-case).
These utilities include data structures, thread-handling utilities,
a logging API and a large number of Swing-GUI components and helpers.

In principle the toolkit could be separately maintained - and it's
always been hosted in a separate package namespace
(`util` for the toolkit, `hojo` for the language reference implementation
and its related tools).
For historic reasons - and for convenience (so long as the number of
users is limited) the toolkit and language reference are currently
maintained in the same repository (i.e., this one).


## About the 20th anniversary edition
The Hojo language began its existence in 1998, and was mature in
late 2000 (only minor code-maintenance and no language changes since then)


In celebration of the 20th birthday of the codebase, this current
repository has been created. The contents are an overhauled
code (code-style consistency, typo fixes etc),
with a new version number (1.3.0) - and a new free-software license.

Apart from the (superficial) overhaul of the code, it remains essentially
the original version, which was written in 2001 or earlier, i.e.
during the reign of Java 1.2.
The code was mostly written in WordPad (sic), hence the dire need for
a tool-assisted overhaul (thanks Eclipse!).

The 20th anniversary addition has been overhauled and tested with
Java 8, which actually works without incident - not bad for a 20
year old codebase (it's almost "Write once, run anywhere"!).
Congratulations to Java and the JVM for providing, for the first time
in software history, this kind of stability and platform independence!

In summary: The 20th anniversary edition is intentionally a faithful
representation of the latest-but-ancient code, not least for historical
reasons.
Any real code-changes (such as fixes to the regressions seen with Java >= 9)
are left as an exercise for the hypothetical future.


## Limitations, known issues etc.

Regarding the language, the following built-in design limitations apply:

  1. One of the earliest design choices (indeed, it was the original reason
     for the design of the language) that interpretation should be possible
     with limited look-ahead. This was in the days when CPUs were
     significantly faster than the telephone lines used for networking,
     and hence limited-lookahead-with-immediate-execution was the killer
     feature.

     Though the language itself does not preclude a radically new
     implementation, e.g. with bytecode compilation, this certainly is
     not a transparent addition to the current implementation.

  2. The type system is based on instances of `java.lang.Class`,
     with extra work/work-arounds for the types that cannot be represented
     (such as functions etc.).
     There is very little chance that generics could be added to the language
     without a (second) complexity explosion in its type system, and
     for little gain, since most of the time the type checking is necessarily
     performed runtime by the reflection APIs.

Regarding the codebase (implementation) itself, the following limitations
apply:

  1. The language reference document (which was hand-written in WordPad)
     has not been completely updated with some language changes, and
     needs an update.
  2. Java Language/API features beyond 1.4 are not used.
  3. Javadoc coverage is incomplete (but all formatting errors have been fixed
     as part of the tool-assisted overhaul).
  4. No formal test suite exists (Hojo is an example of _design-driven_
     development). But the language/interpreter itself is a test tool -
     Hojo code can exercise the underlying Java code, and regression-test
     itself. More automated testing would be possible, of course.
