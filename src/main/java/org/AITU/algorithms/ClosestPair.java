package org.AITU.algorithms;

import org.AITU.metrics.MetricsCollector;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Closest Pair of Points in 2D using Divide and Conquer
 *
 * Time Complexity: O(n log n)
 * Space Complexity: O(n) for auxiliary arrays
 * Recurrence: T(n) = 2T(n/2) + O(n)
 *
 * The algorithm:
 * 1. Sort points by x-coordinate
 * 2. Recursively find closest pairs in left and right halves
 * 3. Check strip region for potentially closer pairs
 * 4. Use y-sorted order for efficient strip scanning (7-point theorem)
 */
public class ClosestPair {

    // Small n cutoff for brute force
    private static final int BRUTE_FORCE_CUTOFF = 3;
    private final MetricsCollector metrics;

    public static class Point {
        public final double x;
        public final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double distanceTo(Point other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", x, y);
        }
    }

    public static class PointPair {
        public final Point p1;
        public final Point p2;
        public final double distance;

        public PointPair(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
            this.distance = p1.distanceTo(p2);
        }

        @Override
        public String toString() {
            return String.format("Points: %s - %s, Distance: %.4f", p1, p2, distance);
        }
    }

    public ClosestPair() {
        this.metrics = new MetricsCollector("ClosestPair");
    }

    /**
     * Public interface to find closest pair of points
     */
    public PointPair findClosestPair(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("Need at least 2 points");
        }

        metrics.reset();
        metrics.setInputSize(points.length);
        metrics.startTimer();

        // Create sorted arrays by x and y coordinates
        Point[] pointsByX = Arrays.copyOf(points, points.length);
        Point[] pointsByY = Arrays.copyOf(points, points.length);
        metrics.incrementAllocations(2 * points.length);

        Arrays.sort(pointsByX, Comparator.comparingDouble(p -> p.x));
        Arrays.sort(pointsByY, Comparator.comparingDouble(p -> p.y));

        PointPair result = closestPairRecursive(pointsByX, pointsByY);

        metrics.stopTimer();
        return result;
    }

    /**
     * Recursive divide-and-conquer implementation
     */
    private PointPair closestPairRecursive(Point[] px, Point[] py) {
        MetricsCollector.enterRecursion();

        try {
            int n = px.length;

            // Base case: use brute force for small arrays
            if (n <= BRUTE_FORCE_CUTOFF) {
                return bruteForce(px);
            }

            // Divide: split into left and right halves
            int mid = n / 2;
            Point midPoint = px[mid];

            // Create y-sorted arrays for left and right halves
            Point[] pyl = new Point[mid];
            Point[] pyr = new Point[n - mid];
            int li = 0, ri = 0;

            for (Point p : py) {
                metrics.incrementComparisons();
                if (p.x <= midPoint.x && li < mid) {
                    pyl[li++] = p;
                } else {
                    pyr[ri++] = p;
                }
            }

            // Handle edge case where all points with midPoint.x go to left
            while (li < mid) {
                pyl[li++] = pyr[--ri];
            }

            metrics.incrementAllocations(n); // for pyl and pyr arrays

            // Create x-sorted arrays for recursion
            Point[] pxl = Arrays.copyOfRange(px, 0, mid);
            Point[] pxr = Arrays.copyOfRange(px, mid, n);
            metrics.incrementAllocations(n);

            // Conquer: recursively find closest pairs in each half
            PointPair closestLeft = closestPairRecursive(pxl, pyl);
            PointPair closestRight = closestPairRecursive(pxr, pyr);

            // Find minimum of the two
            PointPair closestSoFar = (closestLeft.distance < closestRight.distance) ?
                    closestLeft : closestRight;
            metrics.incrementComparisons();

            // Combine: check strip for potentially closer pairs
            return checkStrip(py, midPoint.x, closestSoFar);

        } finally {
            MetricsCollector.exitRecursion();
        }
    }

    /**
     * Check strip region for closer pairs
     * Uses the fact that we only need to check at most 7 points ahead
     */
    private PointPair checkStrip(Point[] py, double midX, PointPair closestSoFar) {
        double delta = closestSoFar.distance;

        // Build strip array with points within delta of dividing line
        Point[] strip = new Point[py.length];
        int stripSize = 0;

        for (Point p : py) {
            metrics.incrementComparisons();
            if (Math.abs(p.x - midX) < delta) {
                strip[stripSize++] = p;
            }
        }

        // Check each point with next 7 points in y-sorted order
        PointPair closest = closestSoFar;

        for (int i = 0; i < stripSize; i++) {
            // By geometry, only need to check next 7 points
            for (int j = i + 1; j < stripSize && j < i + 8; j++) {
                metrics.incrementComparisons();

                // Early termination if y-distance exceeds delta
                if (strip[j].y - strip[i].y >= delta) {
                    break;
                }

                double dist = strip[i].distanceTo(strip[j]);
                metrics.incrementComparisons();

                if (dist < closest.distance) {
                    closest = new PointPair(strip[i], strip[j]);
                    delta = dist; // Update delta for further pruning
                }
            }
        }

        return closest;
    }

    /**
     * Brute force O(n²) algorithm for small inputs
     */
    private PointPair bruteForce(Point[] points) {
        int n = points.length;
        if (n < 2) return null;

        PointPair closest = new PointPair(points[0], points[1]);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                metrics.incrementComparisons();
                double dist = points[i].distanceTo(points[j]);

                if (dist < closest.distance) {
                    closest = new PointPair(points[i], points[j]);
                }
            }
        }

        return closest;
    }

    /**
     * Brute force O(n²) implementation for validation
     */
    public PointPair findClosestPairBruteForce(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("Need at least 2 points");
        }

        metrics.reset();
        metrics.setInputSize(points.length);
        metrics.startTimer();

        PointPair result = bruteForce(points);

        metrics.stopTimer();
        return result;
    }

    /**
     * Generate random points for testing
     */
    public static Point[] generateRandomPoints(int n, double maxCoord) {
        Random random = new Random();
        Point[] points = new Point[n];

        for (int i = 0; i < n; i++) {
            points[i] = new Point(
                    random.nextDouble() * maxCoord,
                    random.nextDouble() * maxCoord
            );
        }

        return points;
    }

    /**
     * Generate points in a grid pattern
     */
    public static Point[] generateGridPoints(int gridSize) {
        Point[] points = new Point[gridSize * gridSize];
        int index = 0;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                points[index++] = new Point(i, j);
            }
        }

        return points;
    }

    public MetricsCollector getMetrics() {
        return metrics;
    }
}