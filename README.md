[![Coverage Status](https://coveralls.io/repos/github/chiquitinxx/functional/badge.svg?branch=main)](https://coveralls.io/github/chiquitinxx/functional?branch=main)

# functional

A Java 8 library designed to enhance your functional programming experience by providing a collection of robust and thread-safe classes for handling common functional patterns.

## Core Classes

### `Pair<L, R>` and `Trio<F, S, T>`

Simple, immutable generic containers for holding two (`Pair`) or three (`Trio`) values of any type.

**Usage:**

```java
// Create a Pair
Pair<String, Integer> pair = Pair.of("Age", 30);
String key = pair.getLeft();   // "Age"
Integer value = pair.getRight(); // 30

// Create a Trio
Trio<String, String, Integer> person = Trio.of("John", "Doe", 30);
String firstName = person.getFirst();  // "John"
String lastName = person.getSecond(); // "Doe"
Integer age = person.getThird();   // 30
```

### `Result<T>`

An interface that represents the result of an operation, which can be either a success (holding a value of type `T`) or a failure (holding a `Failure` object). It combines the safety of `Optional` with the error-handling capabilities of `Either`.

It has three implementations:

1.  **`DirectResult<T>`**: A synchronous, immediate result.
2.  **`AsyncResult<T>`**: An asynchronous result, backed by `CompletableFuture`.
3.  **`LazyResult<T>`**: A lazy-evaluated result that only computes its value when it's needed.

**Usage:**

```java
// Creating a success result
Result<Integer> success = DirectResult.ok(100);

// Creating a failure result
Result<Integer> failure = DirectResult.failure(new DescriptionFailure("Something went wrong"));

// Chaining operations with map and flatMap
Result<String> finalResult = success
    .map(value -> value * 2) // Returns Result.ok(200)
    .flatMap(value -> DirectResult.ok("Final value: " + value));

// Handling outcomes
finalResult.onSuccess(System.out::println); // Prints "Final value: 200"
finalResult.onFailure(fail -> System.err.println(fail.toOptionalString()));

// Getting the value
String resultString = finalResult.getOrThrow(); // "Final value: 200"
Integer errorCase = failure.orElse(f -> -1);    // -1
```

### `Matcher<I, O>`

A simple yet powerful pattern matcher that evaluates an input value against a series of conditions (`when`) and returns an output value.

**Usage:**

```java
Matcher<Integer, String> numberMatcher = Matcher
    .when(i -> i == 1, i -> "One")
    .when(i -> i == 2, i -> "Two")
    .when(i -> i > 2, i -> "More than two");

Optional<String> result1 = numberMatcher.eval(1); // Optional.of("One")
Optional<String> result2 = numberMatcher.eval(5); // Optional.of("More than two")
Optional<String> result3 = numberMatcher.eval(0); // Optional.empty()
```

### `Agent<T>`

A thread-safe container that holds a value and ensures that all modifications to that value are executed sequentially and safely in a concurrent environment.

**Usage:**

```java
// Create an agent with an initial value
Agent<Integer> counter = Agent.create(0);

// Send functions to update the value asynchronously and thread-safely
// For example, in a multi-threaded environment:
for (int i = 0; i < 1000; i++) {
    counter.send(value -> value + 1);
}

// To get the value, you must send a function that returns it
Integer finalCount = counter.get(value -> value).getOrThrow(); // 1000
```

### `ImmutableList<T>`

A thread-safe, immutable list that is designed to be easily and efficiently divided into a `head` (the first element) and a `tail` (the rest of the list).

**Usage:**

```java
ImmutableList<Integer> list = ImmutableList.create(1, 2, 3, 4);

// Get the head
Integer head = list.head(); // 1

// Get the tail
Result<ImmutableList<Integer>> tailResult = list.tail();

// The tail is a Result because the list could be empty
tailResult.onSuccess(tail -> {
    System.out.println(tail.head()); // 2
    System.out.println(tail.toString()); // "[2, 3, 4]"
});

// An empty tail returns a failure
ImmutableList.create(1).tail().onFailure(f -> {
    System.out.println("No tail exists!");
});
```

## Dependencies

Java 8, no more dependencies.

## Run pitest mutation coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release
