package org.AITU;

import org.AITU.algorithms.*;
import org.AITU.metrics.MetricsCollector;
import org.AITU.utils.ArrayUtils;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        String algorithm = args[0];
        int size = Integer.parseInt(args[1]);
        String outputFile = args.length > 2 ? args[2] : "metrics.csv";

        runBenchmarks(algorithm, size, outputFile);
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp target/classes org.AITU.Main <algorithm> <maxSize> [outputFile]");
        System.out.println("Algorithms: mergesort, quicksort, select, closest, all");
        System.out.println("Example: java -cp target/classes org.AITU.Main mergesort 100000 results.csv");
    }

    private static void runBenchmarks(String algorithm, int maxSize, String outputFile) {
        MetricsCollector.clearAllMetrics();
        System.out.println("=== Running Benchmarks ===");
        System.out.println("Algorithm: " + algorithm);
        System.out.println("Max Size: " + maxSize);
        System.out.println("Output File: " + outputFile);
        System.out.println();

        // Run for different input sizes
        for (int n = 10; n <= maxSize; n *= 2) {
            System.out.printf("Testing n=%d:%n", n);

            // Run 3 trials for each size to get average
            for (int trial = 0; trial < 3; trial++) {
                switch (algorithm.toLowerCase()) {
                    case "mergesort":
                        runMergeSort(n);
                        break;
                    case "quicksort":
                        runQuickSort(n);
                        break;
                    case "select":
                        runSelect(n);
                        break;
                    case "closest":
                        if (n <= 10000) { // Limit for closest pair
                            runClosestPair(n);
                        }
                        break;
                    case "all":
                        runMergeSort(n);
                        runQuickSort(n);
                        runSelect(n);
                        if (n <= 10000) {
                            runClosestPair(n);
                        }
                        break;
                    default:
                        System.err.println("Unknown algorithm: " + algorithm);
                        return;
                }
            }
            System.out.println();
        }

        MetricsCollector.exportToCSV(outputFile);
        System.out.println("Results exported to " + outputFile);
    }

    private static void runMergeSort(int n) {
        int[] arr = ArrayUtils.generateRandomArray(n);
        MergeSort mergeSort = new MergeSort();

        MetricsCollector.resetDepth();
        mergeSort.sort(arr);

        // Verify correctness
        if (!ArrayUtils.isSorted(arr)) {
            System.err.println("  MergeSort failed to sort correctly!");
        }

        MetricsCollector metrics = mergeSort.getMetrics();
        int maxDepth = MetricsCollector.getMaxDepth();

        System.out.printf("  MergeSort: time=%.3fms, comparisons=%d, swaps=%d, depth=%d%n",
                metrics.getElapsedTimeMillis(),
                metrics.getComparisons(),
                metrics.getSwaps(),
                maxDepth);

        metrics.recordRun();
    }

    private static void runQuickSort(int n) {
        int[] arr = ArrayUtils.generateRandomArray(n);
        QuickSort quickSort = new QuickSort();

        MetricsCollector.resetDepth();
        quickSort.sort(arr);

        // Verify correctness
        if (!ArrayUtils.isSorted(arr)) {
            System.err.println("  QuickSort failed to sort correctly!");
        }

        MetricsCollector metrics = quickSort.getMetrics();
        int maxDepth = MetricsCollector.getMaxDepth();

        // Verify depth is bounded
        int expectedMaxDepth = 2 * (int) (Math.log(n) / Math.log(2)) + 10;
        if (maxDepth > expectedMaxDepth) {
            System.err.printf("  Warning: QuickSort depth %d exceeds expected %d%n",
                    maxDepth, expectedMaxDepth);
        }

        System.out.printf("  QuickSort: time=%.3fms, comparisons=%d, swaps=%d, depth=%d%n",
                metrics.getElapsedTimeMillis(),
                metrics.getComparisons(),
                metrics.getSwaps(),
                maxDepth);

        metrics.recordRun();
    }

    private static void runSelect(int n) {
        int[] arr = ArrayUtils.generateRandomArray(n);
        DeterministicSelect select = new DeterministicSelect();
        int k = n / 2; // Find median

        MetricsCollector.resetDepth();
        int result = select.select(arr, k);

        // Verify correctness
        int[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);
        if (result != sorted[k]) {
            System.err.printf("  Select failed: expected %d, got %d%n", sorted[k], result);
        }

        MetricsCollector metrics = select.getMetrics();
        int maxDepth = MetricsCollector.getMaxDepth();

        System.out.printf("  Select (k=%d, value=%d): time=%.3fms, comparisons=%d, depth=%d%n",
                k, result,
                metrics.getElapsedTimeMillis(),
                metrics.getComparisons(),
                maxDepth);

        metrics.recordRun();
    }

    private static void runClosestPair(int n) {
        ClosestPair.Point[] points = ClosestPair.generateRandomPoints(n, 1000.0);
        ClosestPair closestPair = new ClosestPair();

        MetricsCollector.resetDepth();
        ClosestPair.PointPair result = closestPair.findClosestPair(points);

        // Verify correctness for small inputs
        if (n <= 100) {
            ClosestPair.PointPair bruteForce = closestPair.findClosestPairBruteForce(points);
            double diff = Math.abs(result.distance - bruteForce.distance);
            if (diff > 0.0001) {
                System.err.printf("  ClosestPair mismatch: DC=%.4f, BF=%.4f%n",
                        result.distance, bruteForce.distance);
            }
        }

        MetricsCollector metrics = closestPair.getMetrics();
        int maxDepth = MetricsCollector.getMaxDepth();

        System.out.printf("  ClosestPair (dist=%.4f): time=%.3fms, comparisons=%d, depth=%d%n",
                result.distance,
                metrics.getElapsedTimeMillis(),
                metrics.getComparisons(),
                maxDepth);

        metrics.recordRun();
    }

    // Additional testing methods
    public static void runCorrectnessTests() {
        System.out.println("=== Running Correctness Tests ===\n");

        // Test MergeSort
        System.out.println("Testing MergeSort:");
        testMergeSort();

        // Test QuickSort
        System.out.println("\nTesting QuickSort:");
        testQuickSort();

        // Test Select
        System.out.println("\nTesting DeterministicSelect:");
        testSelect();

        // Test Closest Pair
        System.out.println("\nTesting ClosestPair:");
        testClosestPair();

        System.out.println("\n=== All Tests Completed ===");
    }

    private static void testMergeSort() {
        MergeSort mergeSort = new MergeSort();
        int passed = 0, total = 0;

        // Test different array types
        int[][] testCases = {
                ArrayUtils.generateRandomArray(100),
                ArrayUtils.generateSortedArray(100),
                ArrayUtils.generateReverseSortedArray(100),
                ArrayUtils.generateArrayWithDuplicates(100),
                new int[]{},  // Empty
                new int[]{42},  // Single element
                new int[]{3, 1, 4, 1, 5, 9, 2, 6}  // Small array
        };

        for (int[] arr : testCases) {
            int[] copy = Arrays.copyOf(arr, arr.length);
            mergeSort.sort(copy);
            if (ArrayUtils.isSorted(copy)) {
                passed++;
            }
            total++;
        }

        System.out.printf("  Passed %d/%d tests%n", passed, total);
    }

    private static void testQuickSort() {
        QuickSort quickSort = new QuickSort();
        int passed = 0, total = 0;

        // Test standard QuickSort
        int[] test1 = ArrayUtils.generateRandomArray(1000);
        quickSort.sort(test1);
        if (ArrayUtils.isSorted(test1)) passed++;
        total++;

        // Test 3-way QuickSort with duplicates
        int[] test2 = ArrayUtils.generateArrayWithDuplicates(1000);
        quickSort.sort3Way(test2);
        if (ArrayUtils.isSorted(test2)) passed++;
        total++;

        // Test median-of-three on sorted array
        int[] test3 = ArrayUtils.generateSortedArray(1000);
        quickSort.sortMedianOfThree(test3);
        if (ArrayUtils.isSorted(test3)) passed++;
        total++;

        // Test recursion depth
        MetricsCollector.resetDepth();
        int[] test4 = ArrayUtils.generateRandomArray(1024);
        quickSort.sort(test4);
        int depth = MetricsCollector.getMaxDepth();
        int expectedMax = 2 * (int) (Math.log(1024) / Math.log(2)) + 10;
        if (depth <= expectedMax) passed++;
        total++;

        System.out.printf("  Passed %d/%d tests (max depth: %d, expected: <=%d)%n",
                passed, total, depth, expectedMax);
    }

    private static void testSelect() {
        DeterministicSelect select = new DeterministicSelect();
        int passed = 0;
        int total = 100;

        // Run 100 random trials
        for (int i = 0; i < total; i++) {
            int size = 10 + (int) (Math.random() * 990);
            int[] arr = ArrayUtils.generateRandomArray(size);
            int k = (int) (Math.random() * size);

            int result = select.select(arr, k);

            // Verify against sorting
            Arrays.sort(arr);
            if (result == arr[k]) {
                passed++;
            }
        }

        System.out.printf("  Passed %d/%d random trials%n", passed, total);
    }

    private static void testClosestPair() {
        ClosestPair closestPair = new ClosestPair();
        int passed = 0, total = 0;

        // Test against brute force for small inputs
        for (int n : new int[]{10, 20, 50, 100, 200}) {
            ClosestPair.Point[] points = ClosestPair.generateRandomPoints(n, 100.0);

            ClosestPair.PointPair dcResult = closestPair.findClosestPair(points);
            ClosestPair.PointPair bfResult = closestPair.findClosestPairBruteForce(points);

            double diff = Math.abs(dcResult.distance - bfResult.distance);
            if (diff < 0.0001) {
                passed++;
            } else {
                System.err.printf("  Failed for n=%d: DC=%.4f, BF=%.4f%n",
                        n, dcResult.distance, bfResult.distance);
            }
            total++;
        }

        System.out.printf("  Passed %d/%d validation tests against O(n²)%n", passed, total);
    }

    // Demo method for visual demonstration
    public static void runDemo() {
        System.out.println("=== Algorithm Demonstration ===\n");

        int[] demo = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Original array: " + Arrays.toString(demo));

        // MergeSort demo
        int[] arr1 = Arrays.copyOf(demo, demo.length);
        new MergeSort().sort(arr1);
        System.out.println("After MergeSort: " + Arrays.toString(arr1));

        // QuickSort demo
        int[] arr2 = Arrays.copyOf(demo, demo.length);
        new QuickSort().sort(arr2);
        System.out.println("After QuickSort: " + Arrays.toString(arr2));

        // Select demo
        int median = new DeterministicSelect().select(Arrays.copyOf(demo, demo.length), 3);
        System.out.println("Median (3rd element): " + median);

        // Closest pair demo
        System.out.println("\nClosest Pair Demo:");
        ClosestPair.Point[] points = {
                new ClosestPair.Point(2, 3),
                new ClosestPair.Point(12, 30),
                new ClosestPair.Point(40, 50),
                new ClosestPair.Point(5, 1),
                new ClosestPair.Point(12, 10),
                new ClosestPair.Point(3, 4)
        };

        for (ClosestPair.Point p : points) {
            System.out.println("  " + p);
        }

        ClosestPair.PointPair closest = new ClosestPair().findClosestPair(points);
        System.out.println("Closest pair: " + closest);
    }
}