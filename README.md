# functional

Library with classes to improve your functional style programming experience. It offers the following classes:

Pair<L,R> -> a class containing a pair of values

Result<T> -> A mix between "Optional" and "Either".

Mutation<T> -> To wrap a class, so all the operations in the class are thread safe.

LazyResult<T> -> Lazy and asynchronous version of Result.

Matcher<I, O> -> Little pattern matcher evaluator for values.

## Dependencies

Java 8, no more dependencies.

## Run pitest coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release