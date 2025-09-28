package org.AITU.algorithms;

import org.AITU.metrics.MetricsCollector;
import org.AITU.utils.ArrayUtils;
import java.util.Arrays;

/**
 * Deterministic Select algorithm using Median of Medians
 * Finds the k-th smallest element in O(n) worst-case time
 *
 * Time Complexity: O(n) worst case
 * Space Complexity: O(log n) for recursion stack
 * Recurrence: T(n) ≤ T(n/5) + T(7n/10) + O(n)
 *
 * The algorithm guarantees that the pivot splits the array
 * such that at least 30% of elements are smaller and 30% are larger
 */
public class DeterministicSelect {

    private static final int GROUP_SIZE = 5;
    private final MetricsCollector metrics;

    public DeterministicSelect() {
        this.metrics = new MetricsCollector("DeterministicSelect");
    }

    /**
     * Public interface to find k-th smallest element (0-indexed)
     */
    public int select(int[] arr, int k) {
        if (arr == null || arr.length == 0 || k < 0 || k >= arr.length) {
            throw new IllegalArgumentException("Invalid input");
        }

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        // Make a copy to avoid modifying original array
        int[] workArray = Arrays.copyOf(arr, arr.length);
        metrics.incrementAllocations(arr.length);

        int result = deterministicSelect(workArray, 0, arr.length - 1, k);

        metrics.stopTimer();
        return result;
    }

    /**
     * Main recursive select function
     */
    private int deterministicSelect(int[] arr, int low, int high, int k) {
        MetricsCollector.enterRecursion();

        try {
            // Base case: single element
            if (low == high) {
                return arr[low];
            }

            // Find median of medians as pivot
            int pivotIndex = medianOfMedians(arr, low, high);

            // Partition around pivot
            int partitionIndex = ArrayUtils.partition(arr, low, high, pivotIndex, metrics);

            // Calculate position of partition in the array
            int position = partitionIndex - low;

            // Recurse on appropriate partition
            if (k == position) {
                return arr[partitionIndex];
            } else if (k < position) {
                // Recurse on left partition (smaller elements)
                return deterministicSelect(arr, low, partitionIndex - 1, k);
            } else {
                // Recurse on right partition (larger elements)
                return deterministicSelect(arr, partitionIndex + 1, high, k - position - 1);
            }

        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Finds the median of medians
     * Groups array into groups of 5, finds median of each group,
     * then recursively finds median of those medians
     */
    private int medianOfMedians(int[] arr, int low, int high) {
        int n = high - low + 1;

        // Base case: use simple median for small arrays
        if (n <= GROUP_SIZE) {
            return findMedianIndex(arr, low, high);
        }

        // Number of complete groups of 5
        int numGroups = (n + GROUP_SIZE - 1) / GROUP_SIZE;
        int[] medians = new int[numGroups];
        metrics.incrementAllocations(numGroups);

        // Find median of each group
        for (int i = 0; i < numGroups; i++) {
            int groupStart = low + i * GROUP_SIZE;
            int groupEnd = Math.min(groupStart + GROUP_SIZE - 1, high);

            // Sort the group and find its median
            int medianIndex = findMedianIndex(arr, groupStart, groupEnd);
            medians[i] = arr[medianIndex];

            // Move median to front section of array for easy access
            ArrayUtils.swap(arr, low + i, medianIndex, metrics);
        }

        // Recursively find median of medians
        // The medians are now in arr[low..low+numGroups-1]
        if (numGroups == 1) {
            return low;
        } else {
            // Find median of the medians array
            int medianOfMediansValue = deterministicSelect(
                    medians, 0, numGroups - 1, numGroups / 2
            );

            // Find index of this value in original array
            for (int i = low; i < low + numGroups; i++) {
                if (arr[i] == medianOfMediansValue) {
                    return i;
                }
            }

            return low; // Fallback (shouldn't reach here)
        }
    }

    /**
     * Finds median index of small array using insertion sort
     */
    private int findMedianIndex(int[] arr, int low, int high) {
        // Create index array to track original positions
        int n = high - low + 1;
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) {
            indices[i] = low + i;
        }

        // Sort indices by array values using insertion sort
        for (int i = 1; i < n; i++) {
            int j = i;
            while (j > 0) {
                metrics.incrementComparisons();
                if (arr[indices[j]] < arr[indices[j - 1]]) {
                    // Swap indices
                    Integer temp = indices[j];
                    indices[j] = indices[j - 1];
                    indices[j - 1] = temp;
                    j--;
                } else {
                    break;
                }
            }
        }

        // Return median index
        return indices[n / 2];
    }

    /**
     * Alternative implementation: Randomized QuickSelect for comparison
     */
    public int quickSelect(int[] arr, int k) {
        if (arr == null || arr.length == 0 || k < 0 || k >= arr.length) {
            throw new IllegalArgumentException("Invalid input");
        }

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        int[] workArray = Arrays.copyOf(arr, arr.length);
        metrics.incrementAllocations(arr.length);
        ArrayUtils.shuffle(workArray);

        int result = randomizedSelect(workArray, 0, arr.length - 1, k);

        metrics.stopTimer();
        return result;
    }

    private int randomizedSelect(int[] arr, int low, int high, int k) {
        MetricsCollector.enterRecursion();

        try {
            if (low == high) {
                return arr[low];
            }

            // Random pivot (already shuffled)
            int pivotIndex = low;
            int partitionIndex = ArrayUtils.partition(arr, low, high, pivotIndex, metrics);

            int position = partitionIndex - low;

            if (k == position) {
                return arr[partitionIndex];
            } else if (k < position) {
                return randomizedSelect(arr, low, partitionIndex - 1, k);
            } else {
                return randomizedSelect(arr, partitionIndex + 1, high, k - position - 1);
            }

        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Helper method to find median value
     */
    public int findMedian(int[] arr) {
        return select(arr, arr.length / 2);
    }

    public MetricsCollector getMetrics() {
        return metrics;
    }
}