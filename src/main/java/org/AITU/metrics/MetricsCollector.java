package org.AITU.metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Metrics collection system for algorithm performance analysis
 */
public class MetricsCollector {

    // Thread-local storage for recursion depth tracking
    private static ThreadLocal<Integer> currentDepth = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<Integer> threadMaxDepth = ThreadLocal.withInitial(() -> 0);

    // Counters
    private long comparisons;
    private long swaps;
    private long allocations;
    private long startTime;
    private long endTime;
    private int inputSize;
    private String algorithmName;

    // recorded per-run values (instance)
    private int recordedMaxDepth;

    // Storage for multiple runs
    private static List<MetricsCollector> allMetrics = new ArrayList<>();

    public MetricsCollector(String algorithmName) {
        this.algorithmName = algorithmName;
        reset();
    }

    public void reset() {
        comparisons = 0;
        swaps = 0;
        allocations = 0;
        startTime = 0;
        endTime = 0;
        inputSize = 0;
        recordedMaxDepth = 0;
        // don't automatically reset thread depth here unless you intend to
        // resetDepth(); // optional: can be called externally before a run
    }

    // Depth tracking methods
    public static void enterRecursion() {
        int depth = currentDepth.get() + 1;
        currentDepth.set(depth);
        if (depth > threadMaxDepth.get()) {
            threadMaxDepth.set(depth);
        }
    }

    public static void exitRecursion() {
        currentDepth.set(currentDepth.get() - 1);
    }

    public static int getMaxDepth() {
        return threadMaxDepth.get();
    }

    public static void resetDepth() {
        currentDepth.set(0);
        threadMaxDepth.set(0);
    }

    // Counter methods
    public void incrementComparisons() {
        comparisons++;
    }

    public void incrementComparisons(long count) {
        comparisons += count;
    }

    public void incrementSwaps() {
        swaps++;
    }

    public void incrementAllocations() {
        allocations++;
    }

    public void incrementAllocations(long count) {
        allocations += count;
    }

    // Timer methods
    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        endTime = System.nanoTime();
    }

    public long getElapsedTimeNanos() {
        return endTime - startTime;
    }

    public double getElapsedTimeMillis() {
        return (endTime - startTime) / 1_000_000.0;
    }

    // Set and get
    public void setInputSize(int size) {
        this.inputSize = size;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSwaps() {
        return swaps;
    }

    public long getAllocations() {
        return allocations;
    }

    public int getInputSize() {
        return inputSize;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    // Recording and reporting
    public void recordRun() {
        allMetrics.add(this.copy());
    }

    private MetricsCollector copy() {
        MetricsCollector copy = new MetricsCollector(this.algorithmName);
        copy.comparisons = this.comparisons;
        copy.swaps = this.swaps;
        copy.allocations = this.allocations;
        copy.startTime = this.startTime;
        copy.endTime = this.endTime;
        copy.inputSize = this.inputSize;
        // save the thread-local max depth into the instance copy
        copy.recordedMaxDepth = MetricsCollector.getMaxDepth();
        return copy;
    }

    // CSV export
    public static void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Заголовки
            writer.println("Algorithm,InputSize,TimeMillis,Comparisons,Swaps,Allocations,MaxDepth");

            for (MetricsCollector metrics : allMetrics) {
                // Используем Locale.US чтобы десятичный разделитель был точкой (.)
                writer.printf(Locale.US, "%s,%d,%.3f,%d,%d,%d,%d%n",
                        metrics.algorithmName,
                        metrics.inputSize,
                        metrics.getElapsedTimeMillis(),
                        metrics.comparisons,
                        metrics.swaps,
                        metrics.allocations,
                        metrics.recordedMaxDepth
                );
            }

            System.out.println("Metrics exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public static void clearAllMetrics() {
        allMetrics.clear();
    }

    @Override
    public String toString() {
        return String.format(
                "%s: n=%d, time=%.3fms, comparisons=%d, swaps=%d, allocations=%d, maxDepth=%d",
                algorithmName, inputSize, getElapsedTimeMillis(),
                comparisons, swaps, allocations, recordedMaxDepth
        );
    }
}
