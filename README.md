[![Coverage Status](https://coveralls.io/repos/github/chiquitinxx/functional/badge.svg?branch=main)](https://coveralls.io/github/chiquitinxx/functional?branch=main)

# functional

Library with classes to improve your functional style programming experience. It offers the following classes:

Pair<L,R> -> a class containing a pair of values.

Trio<F,S,T> -> a class containing a trio of values.

Result<T> -> Interface mix between "Optional" and "Either".

DirectResult<T> -> Synchronous implementation of Result.

AsyncResult<T> -> Asynchronous version of Result.

LazyResult<T> -> Lazy version of Result.

Matcher<I, O> -> Little pattern matcher evaluator for values.

Agent<T> -> store value, and modifications are thread safe.

ImmutableList<T> -> immutable list to divide with head and tail.

## Dependencies

Java 8, no more dependencies.

## Run pitest mutation coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release
