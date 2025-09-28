package org.AITU.algorithms;

import org.AITU.metrics.MetricsCollector;
import org.AITU.utils.ArrayUtils;

/**
 * MergeSort implementation with optimizations:
 * - Reusable buffer to reduce allocations
 * - Small-n cutoff to insertion sort
 * - Metrics tracking
 *
 * Time Complexity: O(n log n) - Master Theorem Case 2
 * Space Complexity: O(n) for auxiliary array
 * Recurrence: T(n) = 2T(n/2) + O(n)
 */
public class MergeSort {

    private static final int INSERTION_SORT_CUTOFF = 10;
    private int[] aux; // Reusable auxiliary array
    private final MetricsCollector metrics;

    public MergeSort() {
        this.metrics = new MetricsCollector("MergeSort");
    }

    /**
     * Public interface for sorting
     */
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        // Allocate auxiliary array once
        aux = new int[arr.length];
        metrics.incrementAllocations(arr.length);

        mergeSortHelper(arr, 0, arr.length - 1);

        metrics.stopTimer();
        aux = null; // Allow garbage collection
    }

    /**
     * Recursive helper with cutoff optimization
     */
    private void mergeSortHelper(int[] arr, int low, int high) {
        MetricsCollector.enterRecursion();

        try {
            // Base case: use insertion sort for small arrays
            if (high - low < INSERTION_SORT_CUTOFF) {
                ArrayUtils.insertionSort(arr, low, high, metrics);
                return;
            }

            // Divide
            int mid = low + (high - low) / 2;

            // Conquer
            mergeSortHelper(arr, low, mid);
            mergeSortHelper(arr, mid + 1, high);

            // Combine
            merge(arr, low, mid, high);

        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Merges two sorted subarrays arr[low..mid] and arr[mid+1..high]
     */
    private void merge(int[] arr, int low, int mid, int high) {
        // Copy to auxiliary array
        for (int k = low; k <= high; k++) {
            aux[k] = arr[k];
        }

        int i = low;       // Index for left subarray
        int j = mid + 1;   // Index for right subarray
        int k = low;       // Index for merged array

        // Merge back to original array
        while (i <= mid && j <= high) {
            metrics.incrementComparisons();
            if (aux[i] <= aux[j]) {
                arr[k++] = aux[i++];
            } else {
                arr[k++] = aux[j++];
            }
        }

        // Copy remaining elements from left subarray (if any)
        while (i <= mid) {
            arr[k++] = aux[i++];
        }

        // Copy remaining elements from right subarray (if any)
        while (j <= high) {
            arr[k++] = aux[j++];
        }
    }

    /**
     * Alternative implementation without reusable buffer for comparison
     */
    public void sortWithoutBufferReuse(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        metrics.reset();
        metrics.setInputSize(arr.length);
        metrics.startTimer();

        mergeSortNoReuse(arr, 0, arr.length - 1);

        metrics.stopTimer();
    }

    private void mergeSortNoReuse(int[] arr, int low, int high) {
        MetricsCollector.enterRecursion();

        try {
            if (low < high) {
                int mid = low + (high - low) / 2;

                mergeSortNoReuse(arr, low, mid);
                mergeSortNoReuse(arr, mid + 1, high);

                mergeWithNewBuffer(arr, low, mid, high);
            }
        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    private void mergeWithNewBuffer(int[] arr, int low, int mid, int high) {
        int n1 = mid - low + 1;
        int n2 = high - mid;

        // Create temporary arrays (new allocation each time)
        int[] left = new int[n1];
        int[] right = new int[n2];
        metrics.incrementAllocations(n1 + n2);

        // Copy data
        for (int i = 0; i < n1; i++) {
            left[i] = arr[low + i];
        }
        for (int j = 0; j < n2; j++) {
            right[j] = arr[mid + 1 + j];
        }

        // Merge
        int i = 0, j = 0, k = low;

        while (i < n1 && j < n2) {
            metrics.incrementComparisons();
            if (left[i] <= right[j]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }

        while (i < n1) {
            arr[k++] = left[i++];
        }

        while (j < n2) {
            arr[k++] = right[j++];
        }
    }

    public MetricsCollector getMetrics() {
        return metrics;
    }
}