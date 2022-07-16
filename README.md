[![Coverage Status](https://coveralls.io/repos/github/chiquitinxx/functional/badge.svg?branch=main)](https://coveralls.io/github/chiquitinxx/functional?branch=main)

# functional

Library with classes to improve your functional style programming experience. It offers the following classes:

Pair<L,R> -> a class containing a pair of values

Result<T> -> A mix between "Optional" and "Either".

LazyResult<T> -> Lazy and asynchronous version of Result.

Matcher<I, O> -> Little pattern matcher evaluator for values.

## Dependencies

Java 8, no more dependencies.

## Run pitest coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release
