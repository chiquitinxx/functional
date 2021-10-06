# functional

Library with classes to improve your functional style programming experience.

Pair<L,R> -> a class containing a pair of values

Result<T> -> contains the result or the failures. A mix between "Optional" and "Either".

## Dependencies

Java 17, no more dependencies.

## Run pitest coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release