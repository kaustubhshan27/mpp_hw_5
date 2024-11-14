package edu.vt.ece.hw5;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import edu.vt.ece.hw5.sets.CoarseSet;
import edu.vt.ece.hw5.sets.FineSet;
import edu.vt.ece.hw5.sets.LazySet;
import edu.vt.ece.hw5.sets.LockFreeSet;
import edu.vt.ece.hw5.sets.OptimisticSet;
import edu.vt.ece.hw5.sets.Set;

public class Benchmark {
    private static final int UPPER_BOUND = 100;
    private static final int ITERATIONS = 10000;
    private static final int BYTE_PADDING = 64;      // To avoid false sharing

    private static Set<Integer> mySet;
    private static boolean[] containsResults;

    private static float CONTAINS_LIMIT; // Percentage of contains operations
    private static float ADD_LIMIT;      // Percentage of add operations
    private static float REMOVE_LIMIT;   // Percentage of remove operations

    public static void main(String[] args) throws Throwable {
        // Check for correct number of arguments
        if (args.length < 3) {
            System.err.println("Usage: java Benchmark <SetType> <ThreadCount> <ContainsPercentage>");
            System.exit(1);
        }

        // Initialize the set type and thread count from command line arguments
        mySet = getSet(args[0]); // SetType
        int threadCount = Integer.parseInt(args[1]); // ThreadCount

        // Parse the contains percentage from the command line
        CONTAINS_LIMIT = Float.parseFloat(args[2]) / 100.0f; // Convert percentage to a decimal
        if (CONTAINS_LIMIT < 0 || CONTAINS_LIMIT > 1) {
            System.err.println("Contains percentage must be between 0 and 100.");
            System.exit(1);
        }

        // Calculate ADD_LIMIT and REMOVE_LIMIT dynamically
        ADD_LIMIT = (1 - CONTAINS_LIMIT) / 2;
        REMOVE_LIMIT = ADD_LIMIT; // Equal share of remaining percentage

        // Initialize array for storing contains operation results
        containsResults = new boolean[threadCount * BYTE_PADDING];

        // Create tasks for each thread
        List<Callable<Long>> calls = getCallables(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long totalExecutionTime = 0;
        try {
            // Invoke all tasks and collect total execution time
            for (Future<Long> future : executor.invokeAll(calls)) {
                try {
                    totalExecutionTime += future.get();
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            }
        } finally {
            executor.shutdown();
        }

        // Calculate throughput
        int totalOperations = threadCount * ITERATIONS;
        double totalExecutionTimeSeconds = totalExecutionTime / 1_000_000_000.0; // Convert ns to seconds
        double throughput = totalOperations / totalExecutionTimeSeconds;

        // Output results of the contains operations and the total execution time
        // System.out.println(Arrays.toString(containsResults));
        System.out.println("Total Execution Time (ns): " + totalExecutionTime);
        System.out.println("Total Operations: " + totalOperations);
        System.out.println("Throughput (operations per second): " + throughput);
    }

    private static Set<Integer> getSet(String setType) {
        switch (SetType.valueOf(setType)) {
            case CoarseSet:
                return new CoarseSet<>();
            case FineSet:
                return new FineSet<>();
            case LazySet:
                return new LazySet<>();
            case LockFreeSet:
                return new LockFreeSet<>();
            case OptimisticSet:
                return new OptimisticSet<>();
            default:
                throw new IllegalArgumentException("Invalid set type");
        }
    }

    private static List<Callable<Long>> getCallables(int threadCount) {
        List<Callable<Long>> calls = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            calls.add(() -> doStuff(index, threadCount));
        }

        return calls;
    }

    private static long doStuff(int index, int threadCount) {
        Random rand = ThreadLocalRandom.current();
        long startTime = System.nanoTime();

        // Determine operation type for this thread based on the calculated limits
        float threadRatio = (float) index / threadCount;

        Operation operation;
        if (threadRatio < ADD_LIMIT) {
            operation = Operation.ADD;
        } else if (threadRatio < (ADD_LIMIT + REMOVE_LIMIT)) {
            operation = Operation.REMOVE;
        } else {
            operation = Operation.CONTAINS;
        }

        for (int i = 0; i < ITERATIONS; i++) {
            int num = rand.nextInt(UPPER_BOUND);

            switch (operation) {
                case ADD:
                    mySet.add(num);
                    break;
                case REMOVE:
                    mySet.remove(num);
                    break;
                case CONTAINS:
                    containsResults[index * BYTE_PADDING] = mySet.contains(num);
                    break;
            }
        }

        long endTime = System.nanoTime();
        return endTime - startTime; // Return execution time for this thread
    }

    private enum Operation {
        ADD,
        REMOVE,
        CONTAINS
    }
}
