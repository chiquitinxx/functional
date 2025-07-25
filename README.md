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

// Getting the value
String resultString = finalResult.getOrThrow(); // "Final value: 200"
Integer errorCase = failure.orElse(f -> -1);    // -1
```

### `Fun<Input, Output>` and `Fun2<Input1, Input2, Output>`

*   **Fun**: A functional interface representing a function that takes one argument and returns a result.
*   **Fun2**: A functional interface similar to `Fun`, but representing a function that takes two arguments and returns a result.

**Usage:**

```java
// Fun example: squaring a number
Fun<Integer, Integer> square = i -> i * i;
Result<Integer> result = square.apply(2);
System.out.println(result.getOrThrow()); // 4

// Fun2 example: adding two numbers
Fun2<Integer, Integer, Integer> add = (a, b) -> a + b;
Result<Integer> sumResult = add.apply(3, 5);
System.out.println(sumResult.getOrThrow()); // 8

// Fun example: composition
Fun<Integer, Integer> twoTimes = Fun.from(a -> 2 * a);
Fun<Integer, Integer> threeTimes = Fun.from(a -> 3 * a);
Fun<Integer, Integer> sixTimes = Fun.compose(twoTimes, threeTimes);
System.out.println(sixTimes.apply(5).getOrThrow()); //30

// Fun2 example: curry
Fun2<Integer, Integer, Integer> sum = Fun2.from(Integer::sum);
Fun<Integer, Integer> plus10 = sum.curry(10);
System.out.println(plus10.apply(2).getOrThrow()); //12
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
    Agent.update(counter, value -> value + 1);
}

Integer finalCount = Agent.get(counter); // 1000
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
```

## Dependencies

Java 8, no more dependencies.

## Run pitest mutation coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

https://central.sonatype.org/publish/publish-maven/

To deploy snapshot: mvn clean deploy -P release
