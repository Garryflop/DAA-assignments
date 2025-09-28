package org.AITU.utils;

import java.util.Arrays;
import java.util.Random;
import org.AITU.metrics.MetricsCollector;

/**
 * Utility functions for array manipulation and common operations
 */
public class ArrayUtils {

    private static final Random random = new Random();

    /**
     * Swaps two elements in an array
     */
    public static void swap(int[] arr, int i, int j) {
        if (i != j) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * Swaps with metrics tracking
     */
    public static void swap(int[] arr, int i, int j, MetricsCollector metrics) {
        if (i != j) {
            metrics.incrementSwaps();
            swap(arr, i, j);
        }
    }

    /**
     * Shuffles an array using Fisher-Yates algorithm
     */
    public static void shuffle(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            swap(arr, i, j);
        }
    }

    /**
     * Partitions array around pivot at index pivotIndex
     * Returns final position of pivot
     */
    public static int partition(int[] arr, int low, int high, int pivotIndex, MetricsCollector metrics) {
        int pivotValue = arr[pivotIndex];

        // Move pivot to end
        swap(arr, pivotIndex, high, metrics);

        int storeIndex = low;
        for (int i = low; i < high; i++) {
            metrics.incrementComparisons();
            if (arr[i] < pivotValue) {
                swap(arr, i, storeIndex, metrics);
                storeIndex++;
            }
        }

        // Move pivot to its final position
        swap(arr, storeIndex, high, metrics);
        return storeIndex;
    }

    /**
     * Three-way partition for handling duplicates
     * Returns [lt, gt] where arr[low..lt-1] < pivot, arr[lt..gt] == pivot, arr[gt+1..high] > pivot
     */
    public static int[] partition3Way(int[] arr, int low, int high, MetricsCollector metrics) {
        if (high <= low) return new int[]{low, high};

        int pivot = arr[low];
        int i = low;
        int lt = low;
        int gt = high;

        while (i <= gt) {
            metrics.incrementComparisons();
            if (arr[i] < pivot) {
                swap(arr, i++, lt++, metrics);
            } else if (arr[i] > pivot) {
                swap(arr, i, gt--, metrics);
            } else {
                i++;
            }
        }

        return new int[]{lt, gt};
    }

    /**
     * Insertion sort for small arrays (used in cutoff optimization)
     */
    public static void insertionSort(int[] arr, int low, int high, MetricsCollector metrics) {
        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j = i - 1;

            while (j >= low && arr[j] > key) {
                metrics.incrementComparisons();
                arr[j + 1] = arr[j];
                j--;
            }
            if (j >= low) metrics.incrementComparisons(); // For the final comparison

            arr[j + 1] = key;
        }
    }

    /**
     * Generates test arrays
     */
    public static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(size * 10);
        }
        return arr;
    }

    public static int[] generateSortedArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = i;
        }
        return arr;
    }

    public static int[] generateReverseSortedArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = size - i;
        }
        return arr;
    }

    public static int[] generateArrayWithDuplicates(int size) {
        int[] arr = new int[size];
        int uniqueValues = Math.max(1, size / 10); // 10% unique values
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(uniqueValues);
        }
        return arr;
    }

    /**
     * Validates if array is sorted
     */
    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a copy of array
     */
    public static int[] copyArray(int[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }
}