package test.yila.functional;

import dev.yila.functional.Mutation;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutationTest {

    @Test
    public void createMutation() {
        Mutation<Integer> mutation = Mutation.create(7);
        assertEquals(7, mutation.get());
    }

    @RepeatedTest(1000)
    public void mutateMultipleTimesThreadSafe() {
        Mutation<Integer> mutation = Mutation.create(5);
        List<Integer> numbers = Arrays.asList(3, 4, 5, 6, 7, 8);
        int allMultiplied = mutation.get() * numbers.stream().reduce(1, (acc, number) -> acc * number);
        numbers.parallelStream()
                .forEach(number -> mutation.mutate(oldValue -> oldValue * number));
        assertEquals(allMultiplied, mutation.get());
    }
}
