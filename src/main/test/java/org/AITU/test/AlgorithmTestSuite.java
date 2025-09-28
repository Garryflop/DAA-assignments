package org.AITU.test;

import org.AITU.algorithms.*;
import org.AITU.metrics.MetricsCollector;
import org.AITU.utils.ArrayUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Random;

public class AlgorithmTestSuite {

    private static final int[] TEST_SIZES = {0, 1, 10, 100, 1000, 10000};
    private static final int RANDOM_TEST_ITERATIONS = 100;

    @Nested
    @DisplayName("MergeSort Tests")
    class MergeSortTests {
        private MergeSort mergeSort;

        @BeforeEach
        void setUp() {
            mergeSort = new MergeSort();
        }

        @Test
        @DisplayName("Correctly sorts random arrays")
        void testRandomArrays() {
            for (int size : TEST_SIZES) {
                if (size == 0) continue;
                int[] arr = ArrayUtils.generateRandomArray(size);
                mergeSort.sort(arr);
                assertTrue(ArrayUtils.isSorted(arr),
                        "Array of size " + size + " should be sorted");
            }
        }

        @Test
        @DisplayName("Handles edge cases")
        void testEdgeCases() {
            // Empty array
            int[] empty = {};
            mergeSort.sort(empty);
            assertEquals(0, empty.length);

            // Single element
            int[] single = {42};
            mergeSort.sort(single);
            assertEquals(42, single[0]);

            // Already sorted
            int[] sorted = {1, 2, 3, 4, 5};
            mergeSort.sort(sorted);
            assertTrue(ArrayUtils.isSorted(sorted));

            // Reverse sorted
            int[] reverse = {5, 4, 3, 2, 1};
            mergeSort.sort(reverse);
            assertTrue(ArrayUtils.isSorted(reverse));

            // All duplicates
            int[] duplicates = {5, 5, 5, 5, 5};
            mergeSort.sort(duplicates);
            assertTrue(ArrayUtils.isSorted(duplicates));
        }

        @Test
        @DisplayName("Recursion depth is O(log n)")
        void testRecursionDepth() {
            for (int n = 16; n <= 1024; n *= 2) {
                int[] arr = ArrayUtils.generateRandomArray(n);
                MetricsCollector.resetDepth();
                mergeSort.sort(arr);
                int maxDepth = MetricsCollector.getMaxDepth();
                int expectedMaxDepth = (int)(Math.log(n) / Math.log(2)) + 5; // +5 for safety margin

                assertTrue(maxDepth <= expectedMaxDepth,
                        String.format("For n=%d, depth=%d should be <= %d",
                                n, maxDepth, expectedMaxDepth));
            }
        }
    }

    @Nested
    @DisplayName("QuickSort Tests")
    class QuickSortTests {
        private QuickSort quickSort;

        @BeforeEach
        void setUp() {
            quickSort = new QuickSort();
        }

        @Test
        @DisplayName("Correctly sorts random arrays")
        void testRandomArrays() {
            for (int size : TEST_SIZES) {
                if (size == 0) continue;
                int[] arr = ArrayUtils.generateRandomArray(size);
                quickSort.sort(arr);
                assertTrue(ArrayUtils.isSorted(arr),
                        "Array of size " + size + " should be sorted");
            }
        }

        @Test
        @DisplayName("Handles arrays with many duplicates")
        void testDuplicates() {
            int[] arr = ArrayUtils.generateArrayWithDuplicates(1000);
            quickSort.sort3Way(arr);
            assertTrue(ArrayUtils.isSorted(arr));
        }

        @Test
        @DisplayName("Recursion depth is bounded by O(log n) with tail optimization")
        void testBoundedRecursionDepth() {
            // Test that recursion depth is approximately 2*log2(n) or less
            for (int n = 16; n <= 1024; n *= 2) {
                int maxObservedDepth = 0;

                // Run multiple times due to randomization
                for (int trial = 0; trial < 10; trial++) {
                    int[] arr = ArrayUtils.generateRandomArray(n);
                    MetricsCollector.resetDepth();
                    quickSort.sort(arr);
                    int depth = MetricsCollector.getMaxDepth();
                    maxObservedDepth = Math.max(maxObservedDepth, depth);
                }

                int expectedMaxDepth = 2 * (int)(Math.log(n) / Math.log(2)) + 10;
                assertTrue(maxObservedDepth <= expectedMaxDepth,
                        String.format("For n=%d, max depth=%d should be <= %d",
                                n, maxObservedDepth, expectedMaxDepth));
            }
        }

        @Test
        @DisplayName("Median-of-three handles sorted arrays efficiently")
        void testMedianOfThree() {
            int[] sorted = ArrayUtils.generateSortedArray(1000);
            quickSort.sortMedianOfThree(sorted);
            assertTrue(ArrayUtils.isSorted(sorted));

            int[] reverse = ArrayUtils.generateReverseSortedArray(1000);
            quickSort.sortMedianOfThree(reverse);
            assertTrue(ArrayUtils.isSorted(reverse));
        }
    }

    @Nested
    @DisplayName("Deterministic Select Tests")
    class SelectTests {
        private DeterministicSelect select;
        private Random random = new Random(42);

        @BeforeEach
        void setUp() {
            select = new DeterministicSelect();
        }

        @Test
        @DisplayName("Finds correct k-th smallest element")
        void testCorrectSelection() {
            for (int trial = 0; trial < RANDOM_TEST_ITERATIONS; trial++) {
                int n = random.nextInt(100) + 1;
                int[] arr = ArrayUtils.generateRandomArray(n);
                int k = random.nextInt(n);

                // Find k-th smallest using our algorithm
                int result = select.select(arr, k);

                // Verify by sorting
                int[] sorted = Arrays.copyOf(arr, arr.length);
                Arrays.sort(sorted);

                assertEquals(sorted[k], result,
                        String.format("Trial %d: k=%d, n=%d", trial, k, n));
            }
        }

        @Test
        @DisplayName("Finds median correctly")
        void testMedian() {
            for (int size : new int[]{5, 10, 15, 20, 25, 100}) {
                int[] arr = ArrayUtils.generateRandomArray(size);
                int median = select.findMedian(arr);

                int[] sorted = Arrays.copyOf(arr, arr.length);
                Arrays.sort(sorted);

                assertEquals(sorted[size/2], median);
            }
        }

        @Test
        @DisplayName("Handles edge cases")
        void testEdgeCases() {
            // Single element
            int[] single = {42};
            assertEquals(42, select.select(single, 0));

            // Find minimum
            int[] arr = {3, 1, 4, 1, 5, 9, 2, 6};
            assertEquals(1, select.select(arr, 0));

            // Find maximum
            assertEquals(9, select.select(arr, arr.length - 1));
        }

        @Test
        @DisplayName("QuickSelect comparison")
        void testQuickSelectComparison() {
            for (int trial = 0; trial < 50; trial++) {
                int n = random.nextInt(100) + 1;
                int[] arr = ArrayUtils.generateRandomArray(n);
                int k = random.nextInt(n);

                int deterministicResult = select.select(arr, k);
                int quickSelectResult = select.quickSelect(arr, k);

                assertEquals(deterministicResult, quickSelectResult);
            }
        }
    }

    @Nested
    @DisplayName("Closest Pair Tests")
    class ClosestPairTests {
        private ClosestPair closestPair;

        @BeforeEach
        void setUp() {
            closestPair = new ClosestPair();
        }

        @Test
        @DisplayName("Finds correct closest pair for small inputs")
        void testSmallInputs() {
            // Simple test case
            ClosestPair.Point[] points = {
                    new ClosestPair.Point(0, 0),
                    new ClosestPair.Point(1, 1),
                    new ClosestPair.Point(2, 2),
                    new ClosestPair.Point(0.5, 0.5)
            };

            ClosestPair.PointPair result = closestPair.findClosestPair(points);
            double expectedDistance = Math.sqrt(0.5);

            assertEquals(expectedDistance, result.distance, 0.0001);
        }

        @Test
        @DisplayName("Validates against brute force on small n")
        void testValidationAgainstBruteForce() {
            for (int n = 10; n <= 100; n += 10) {
                ClosestPair.Point[] points = ClosestPair.generateRandomPoints(n, 100);

                ClosestPair.PointPair dcResult = closestPair.findClosestPair(points);
                ClosestPair.PointPair bfResult = closestPair.findClosestPairBruteForce(points);

                assertEquals(bfResult.distance, dcResult.distance, 0.0001,
                        "Divide-and-conquer should match brute force for n=" + n);
            }
        }

        @Test
        @DisplayName("Handles edge cases")
        void testEdgeCases() {
            // Two points
            ClosestPair.Point[] twoPoints = {
                    new ClosestPair.Point(0, 0),
                    new ClosestPair.Point(3, 4)
            };
            ClosestPair.PointPair result = closestPair.findClosestPair(twoPoints);
            assertEquals(5.0, result.distance, 0.0001);

            // Collinear points
            ClosestPair.Point[] collinear = {
                    new ClosestPair.Point(0, 0),
                    new ClosestPair.Point(1, 0),
                    new ClosestPair.Point(2, 0),
                    new ClosestPair.Point(3, 0)
            };
            result = closestPair.findClosestPair(collinear);
            assertEquals(1.0, result.distance, 0.0001);

            // Grid points
            ClosestPair.Point[] grid = ClosestPair.generateGridPoints(5);
            result = closestPair.findClosestPair(grid);
            assertEquals(1.0, result.distance, 0.0001);
        }

        @Test
        @DisplayName("Recursion depth is O(log n)")
        void testRecursionDepth() {
            for (int n = 16; n <= 256; n *= 2) {
                ClosestPair.Point[] points = ClosestPair.generateRandomPoints(n, 1000);
                MetricsCollector.resetDepth();
                closestPair.findClosestPair(points);
                int maxDepth = MetricsCollector.getMaxDepth();
                int expectedMaxDepth = (int)(Math.log(n) / Math.log(2)) + 5;

                assertTrue(maxDepth <= expectedMaxDepth,
                        String.format("For n=%d, depth=%d should be <= %d",
                                n, maxDepth, expectedMaxDepth));
            }
        }
    }
}