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

#### Polymorphic Execution (Unique Feature)

Unlike most libraries that force you to choose a different API for sync and async code (e.g., `Optional` vs `CompletableFuture`), this library provides a **unified abstraction**. You can write your business logic against the `Result` interface, and it remains agnostic to *how* or *when* the computation occurs:

1.  **`DirectResult<T>`**: Synchronous, immediate execution.
2.  **`AsyncResult<T>`**: Asynchronous execution (non-blocking).
3.  **`LazyResult<T>`**: Deferred execution (computed only when accessed).

This allows you to swap execution models (e.g., moving a heavy calculation from sync to async) without changing the functional pipelines (`map`, `flatMap`) of your service.

#### Synchronous Usage

```java
Result<Integer> success = DirectResult.ok(100);
Result<Integer> failure = DirectResult.failure(new DescriptionFailure("Something went wrong"));

Result<String> finalResult = success
    .map(value -> value * 2) // Returns Result.ok(200)
    .flatMap(value -> DirectResult.ok("Final value: " + value));

String resultString = finalResult.getOrThrow(); // "Final value: 200"
Integer errorCase = failure.orElse(f -> -1);    // -1
```

#### Asynchronous Usage with `AsyncResult`

When performing asynchronous operations, you are responsible for providing and managing an `ExecutorService`. This gives you full control over your application's concurrency model.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

try {
    Supplier<Integer> longRunningTask = () -> {
        // ... some computation ...
        return 200;
    };
    Result<Integer> asyncResult = AsyncResult.create(executor, longRunningTask);

    Result<String> finalAsyncResult = asyncResult
        .map(value -> "The value is: " + value);
    System.out.println(finalAsyncResult.getOrThrow()); // "The value is: 200"

    Result<String> parallelResult = Result.inParallel(
        executor,
        (s1, s2) -> DirectResult.ok(s1 + " " + s2), // Join function
        () -> "Hello",                             // First task
        () -> "World"                              // Second task
    );

    System.out.println(parallelResult.getOrThrow()); // "Hello World"

} finally {
    executor.shutdown();
}
```

#### Lazy Usage with `LazyResult`

`LazyResult` defers the execution of the supplier and all chained functions until the value is explicitly requested (e.g., via `getOrThrow()`). It also memoizes the result once computed.

```java
AtomicInteger counter = new AtomicInteger(0);

Result<Integer> lazy = LazyResult.create(() -> {
    counter.incrementAndGet();
    return 10;
});

Result<Integer> mapped = lazy.map(v -> v * 2);
System.out.println(counter.get()); // 0
        
System.out.println(mapped.getOrThrow()); // 20
System.out.println(counter.get());       // 1

// The result is memoized; subsequent calls won't re-run the supplier
System.out.println(lazy.getOrThrow());   // 10
System.out.println(counter.get());       // 1
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

### `Validation`

A utility class for validating values against one or multiple criteria. It returns a `Result` that either contains the valid value or a `Failure` indicating which validation rule failed.

**Usage:**

```java
Failure tooShort = new DescriptionFailure("Must be at least 5 characters");
Failure noNumber = new DescriptionFailure("Must contain a number");

// Validate a value against multiple rules using Pairs
Result<String> result = Validation.validate("pass1",
    Pair.of(tooShort, s -> s.length() >= 5),
    Pair.of(noNumber, s -> s.matches(".*\\d.*"))
);

// Single validation
Result<Integer> ageResult = Validation.validate(25, 
    new DescriptionFailure("Must be an adult"), 
    age -> age >= 18
);
```

### `Agent<T>`

A thread-safe container that implements the Actor model. It holds a value and ensures that all modifications to that value are executed sequentially and safely in a concurrent environment using a non-blocking queue.

**Usage:**

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
Agent<Integer> counter = Agent.create(executor, 0);

for (int i = 0; i < 1000; i++) {
    //Update functions are executed async in order
    Agent.update(counter, value -> value + 1);
}

Result<Integer> result = Agent.get(counter);
System.out.println(result.getOrThrow()); // 1000

// Set a maximum size of the allowed functions in the queue 
Agent<Integer> boundedCounter = Agent.create(executor, 0, 100);
```

## Dependencies

Java 8, no more dependencies.

## Run pitest mutation coverage

mvn clean verify org.pitest:pitest-maven:mutationCoverage

## Release information

Check deployments here: https://central.sonatype.com/publishing/deployments

To create deploy to be release: mvn clean deploy -Prelease -Pgpg

This is the setup needed in .m2/settings.xml
```
<settings>
  <servers>
    <server>
	<id>central</id>
	<username><-- USERNAME --></username>
	<password><-- PASSWORD --></password>
    </server>
  </servers>

  <profiles>
        <profile>
            <id>gpg</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.passphrase><-- PASSPHRASE --></gpg.passphrase>
            </properties>
        </profile>
  </profiles>
</settings>
```
