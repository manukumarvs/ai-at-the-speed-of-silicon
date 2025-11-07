package com.javafest.aiatspeed.vector.thread;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.stream.IntStream;

/**
 * Demonstrates:
 *  1. Scalar computation (single-thread, no SIMD)
 *  2. Vectorized computation (single-thread, SIMD)
 *  3. Vectorized + Multithreaded computation (multi-core + SIMD)
 *
 * Fix: Corrected chunk alignment and loop bounds.
 */
public class BadVectorAndThreadDemo {

    private static final int SIZE = 100_000_000; // 100 million
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public static void main(String[] args) {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] result = new float[SIZE];

        for (int i = 0; i < SIZE; i++) {
            a[i] = i * 0.001f;
            b[i] = (SIZE - i) * 0.001f;
        }

        System.out.println("Running with " + Runtime.getRuntime().availableProcessors() + " cores...\n");

        benchmark("Scalar (Single Core, No SIMD)", () -> addScalar(a, b, result));
        benchmark("Vectorized (Single Core, SIMD)", () -> addVector(a, b, result));
        benchmark("Vectorized + Multithreaded (SIMD + Threads)", () -> addVectorParallel(a, b, result));
    }

    /** Scalar addition (no SIMD) */
    private static void addScalar(float[] a, float[] b, float[] result) {
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
    }

    /** Vectorized addition (SIMD on single core) */
    private static void addVector(float[] a, float[] b, float[] result) {
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);

        for (; i < upperBound; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            va.add(vb).intoArray(result, i);
        }

        // Remaining elements
        for (; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
    }

    /** Vectorized + parallelized (SIMD + multi-core) */
    private static void addVectorParallel(float[] a, float[] b, float[] result) {
        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = (int) Math.ceil((double) a.length / cores);

        IntStream.range(0, cores).parallel().forEach(core -> {
            int start = core * chunkSize;
            int end = Math.min(a.length, start + chunkSize);

            int i = start;
            int upperBound = SPECIES.loopBound(end); // loopBound ensures safe vector boundary

            for (; i < upperBound; i += SPECIES.length()) {
                FloatVector va = FloatVector.fromArray(SPECIES, a, i);
                FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
                va.add(vb).intoArray(result, i);
            }

            // Handle tail elements for this chunk
            for (; i < end; i++) {
                result[i] = a[i] + b[i];
            }
        });
    }

    /** Benchmarking helper */
    private static void benchmark(String label, Runnable task) {
        System.gc();
        long start = System.nanoTime();
        task.run();
        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;
        System.out.printf("%-45s : %8.2f ms%n", label, ms);
    }
}
