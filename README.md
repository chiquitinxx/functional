[![Coverage Status](https://coveralls.io/repos/github/chiquitinxx/functional/badge.svg?branch=main)](https://coveralls.io/github/chiquitinxx/functional?branch=main)

# functional

Library with classes to improve your functional style programming experience. It offers the following classes:

Pair<L,R> -> a class containing a pair of values.

Trio<F,S,T> -> a class containing a trio of values.

Result<T, F extends Failure> -> Interface mix between "Optional" and "Either".

DirectResult<T, F> -> Implementation of Result.

AsyncResult<T, F> -> Asynchronous version of Result.

LazyResult<T, F> -> Lazy version of Result.

Matcher<I, O> -> Little pattern matcher evaluator for values.

## Dependencies

Java 8, no more dependencies.

## Run pitest mutation coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release
