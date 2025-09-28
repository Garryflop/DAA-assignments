package org.AITU.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.AITU.algorithms.*;
import org.AITU.utils.ArrayUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks for algorithm performance comparison
 * Focus on Select vs Sort comparison as specified
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class AlgorithmBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    @Param({"0", "25", "50", "75", "99"})
    private int percentile; // Which percentile to select

    private int[] randomData;
    private int[] sortedData;
    private int[] reverseSortedData;
    private int[] duplicateData;
    private int k; // k-th element to select

    private MergeSort mergeSort;
    private QuickSort quickSort;
    private DeterministicSelect deterministicSelect;

    @Setup(Level.Trial)
    public void setupTrial() {
        mergeSort = new MergeSort();
        quickSort = new QuickSort();
        deterministicSelect = new DeterministicSelect();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        // Generate fresh data for each invocation
        randomData = ArrayUtils.generateRandomArray(size);
        sortedData = ArrayUtils.generateSortedArray(size);
        reverseSortedData = ArrayUtils.generateReverseSortedArray(size);
        duplicateData = ArrayUtils.generateArrayWithDuplicates(size);

        // Calculate k based on percentile
        k = (size * percentile) / 100;
        if (k >= size) k = size - 1;
    }

    // ============== SELECT BENCHMARKS ==============

    @Benchmark
    public int benchmarkDeterministicSelect() {
        return deterministicSelect.select(randomData.clone(), k);
    }

    @Benchmark
    public int benchmarkQuickSelect() {
        return deterministicSelect.quickSelect(randomData.clone(), k);
    }

    @Benchmark
    public int benchmarkSortAndSelect() {
        int[] copy = randomData.clone();
        Arrays.sort(copy);
        return copy[k];
    }

    // ============== SORTING BENCHMARKS ==============

    @Benchmark
    public void benchmarkMergeSortRandom() {
        mergeSort.sort(randomData.clone());
    }

    @Benchmark
    public void benchmarkQuickSortRandom() {
        quickSort.sort(randomData.clone());
    }

    @Benchmark
    public void benchmarkQuickSort3WayDuplicates() {
        quickSort.sort3Way(duplicateData.clone());
    }

    @Benchmark
    public void benchmarkArraysSort() {
        Arrays.sort(randomData.clone());
    }

    // ============== WORST CASE BENCHMARKS ==============

    @Benchmark
    public void benchmarkMergeSortSorted() {
        mergeSort.sort(sortedData.clone());
    }

    @Benchmark
    public void benchmarkQuickSortSorted() {
        quickSort.sort(sortedData.clone());
    }

    @Benchmark
    public void benchmarkQuickSortMedianOfThreeSorted() {
        quickSort.sortMedianOfThree(sortedData.clone());
    }

    // ============== CLOSEST PAIR BENCHMARKS ==============

    @State(Scope.Benchmark)
    public static class ClosestPairState {
        @Param({"100", "500", "1000", "2000"})
        public int pointCount;

        public ClosestPair.Point[] points;
        public ClosestPair closestPair;

        @Setup(Level.Trial)
        public void setup() {
            closestPair = new ClosestPair();
        }

        @Setup(Level.Invocation)
        public void setupPoints() {
            points = ClosestPair.generateRandomPoints(pointCount, 1000);
        }
    }

    @Benchmark
    public ClosestPair.PointPair benchmarkClosestPairDivideConquer(ClosestPairState state) {
        return state.closestPair.findClosestPair(state.points);
    }

    @Benchmark
    public ClosestPair.PointPair benchmarkClosestPairBruteForce(ClosestPairState state) {
        if (state.pointCount > 2000) {
            // Skip brute force for large inputs
            return state.closestPair.findClosestPair(state.points);
        }
        return state.closestPair.findClosestPairBruteForce(state.points);
    }

    // ============== MAIN METHOD ==============

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AlgorithmBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    // ============== COMPARISON BENCHMARKS ==============

    /**
     * Specific benchmark comparing deterministic select vs sorting
     * for finding the median
     */
    @State(Scope.Benchmark)
    public static class MedianComparisonState {
        @Param({"1000", "10000", "100000"})
        public int size;

        public int[] data;
        public DeterministicSelect selector;

        @Setup(Level.Trial)
        public void setupTrial() {
            selector = new DeterministicSelect();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            data = ArrayUtils.generateRandomArray(size);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public int findMedianViaSelect(MedianComparisonState state) {
        return state.selector.findMedian(state.data.clone());
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public int findMedianViaSort(MedianComparisonState state) {
        int[] copy = state.data.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }
}