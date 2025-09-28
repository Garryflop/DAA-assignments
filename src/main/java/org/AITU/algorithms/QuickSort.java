package org.AITU.algorithms;

import org.AITU.metrics.MetricsCollector;
import org.AITU.utils.ArrayUtils;
import java.util.Random;

/**
 * QuickSort implementation with optimizations:
 * - Randomized pivot selection
 * - Tail recursion optimization (recurse on smaller partition, iterate on larger)
 * - Small-n cutoff to insertion sort
 * - Three-way partitioning for duplicates
 *
 * Time Complexity: O(n log n) average, O(n²) worst case
 * Space Complexity: O(log n) stack space due to tail recursion optimization
 * Recurrence: T(n) = T(k) + T(n-k-1) + O(n), where k is partition size
 */
public class QuickSort {

    private static final int INSERTION_SORT_CUTOFF = 10;
    private final Random random = new Random();
    private final MetricsCollector metrics;

    public QuickSort() {
        this.metrics = new MetricsCollector("QuickSort");
    }

    /**
     * Public interface for sorting
     */
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        // Shuffle array to ensure randomized performance
        ArrayUtils.shuffle(arr);

        quickSortOptimized(arr, 0, arr.length - 1);

        metrics.stopTimer();
    }

    /**
     * QuickSort with tail recursion optimization
     * Always recurses on smaller partition, iterates on larger
     */
    private void quickSortOptimized(int[] arr, int low, int high) {
        while (low < high) {
            MetricsCollector.enterRecursion();

            // Use insertion sort for small subarrays
            if (high - low < INSERTION_SORT_CUTOFF) {
                ArrayUtils.insertionSort(arr, low, high, metrics);
                MetricsCollector.exitRecursion();
                return;
            }

            // Choose random pivot
            int pivotIndex = low + random.nextInt(high - low + 1);
            int partitionIndex = ArrayUtils.partition(arr, low, high, pivotIndex, metrics);

            // Recurse on smaller partition, iterate on larger (tail recursion optimization)
            int leftSize = partitionIndex - low;
            int rightSize = high - partitionIndex;

            if (leftSize < rightSize) {
                // Recurse on smaller left partition
                quickSortOptimized(arr, low, partitionIndex - 1);
                // Iterate on larger right partition
                low = partitionIndex + 1;
            } else {
                // Recurse on smaller right partition
                quickSortOptimized(arr, partitionIndex + 1, high);
                // Iterate on larger left partition
                high = partitionIndex - 1;
            }

            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Three-way QuickSort for handling duplicates efficiently
     * Partitions into: < pivot, == pivot, > pivot
     */
    public void sort3Way(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        quickSort3Way(arr, 0, arr.length - 1);

        metrics.stopTimer();
    }

    private void quickSort3Way(int[] arr, int low, int high) {
        if (low >= high) return;

        MetricsCollector.enterRecursion();

        try {
            // Use insertion sort for small subarrays
            if (high - low < INSERTION_SORT_CUTOFF) {
                ArrayUtils.insertionSort(arr, low, high, metrics);
                return;
            }

            // Three-way partition
            int[] partition = ArrayUtils.partition3Way(arr, low, high, metrics);
            int lt = partition[0];
            int gt = partition[1];

            // Recurse on partitions that don't contain the pivot
            quickSort3Way(arr, low, lt - 1);
            quickSort3Way(arr, gt + 1, high);

        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Standard QuickSort without optimizations (for comparison)
     */
    public void sortBasic(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        quickSortBasic(arr, 0, arr.length - 1);

        metrics.stopTimer();
    }

    private void quickSortBasic(int[] arr, int low, int high) {
        if (low < high) {
            MetricsCollector.enterRecursion();

            try {
                int pivotIndex = low + random.nextInt(high - low + 1);
                int partitionIndex = ArrayUtils.partition(arr, low, high, pivotIndex, metrics);

                quickSortBasic(arr, low, partitionIndex - 1);
                quickSortBasic(arr, partitionIndex + 1, high);

            } finally {
                MetricsCollector.exitRecursion();
            }
        }
    }

    /**
     * Median-of-three pivot selection for better performance on sorted data
     */
    private int medianOfThree(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;

        metrics.incrementComparisons();
        if (arr[low] > arr[mid]) {
            ArrayUtils.swap(arr, low, mid, metrics);
        }

        metrics.incrementComparisons();
        if (arr[low] > arr[high]) {
            ArrayUtils.swap(arr, low, high, metrics);
        }

        metrics.incrementComparisons();
        if (arr[mid] > arr[high]) {
            ArrayUtils.swap(arr, mid, high, metrics);
        }

        return mid;
    }

    /**
     * QuickSort with median-of-three pivot selection
     */
    public void sortMedianOfThree(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        quickSortMedianOfThree(arr, 0, arr.length - 1);

        metrics.stopTimer();
    }

    private void quickSortMedianOfThree(int[] arr, int low, int high) {
        while (low < high) {
            MetricsCollector.enterRecursion();

            if (high - low < INSERTION_SORT_CUTOFF) {
                ArrayUtils.insertionSort(arr, low, high, metrics);
                MetricsCollector.exitRecursion();
                return;
            }

            // Use median-of-three for pivot selection
            int pivotIndex = medianOfThree(arr, low, high);
            int partitionIndex = ArrayUtils.partition(arr, low, high, pivotIndex, metrics);

            int leftSize = partitionIndex - low;
            int rightSize = high - partitionIndex;

            if (leftSize < rightSize) {
                quickSortMedianOfThree(arr, low, partitionIndex - 1);
                low = partitionIndex + 1;
            } else {
                quickSortMedianOfThree(arr, partitionIndex + 1, high);
                high = partitionIndex - 1;
            }

            MetricsCollector.exitRecursion();
        }
    }

    public MetricsCollector getMetrics() {
        return metrics;
    }
}