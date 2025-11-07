package com.javafest.aiatspeed.vector.thread;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.util.stream.IntStream;

public class GoodVectorAndThreadDemo {

    private static final int SIZE = 100_000_000; // 100M elements
    private static final VectorSpecies<Float> SPECIES =
            FloatVector.SPECIES_PREFERRED;
//    private static final VectorSpecies<Float> SPECIES =
//            FloatVector.SPECIES_128;
    private static final int ITERATIONS = 5; // warm-up + measurement rounds

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] result1 = new float[SIZE];
        float[] result2 = new float[SIZE];
        float[] result3 = new float[SIZE];

        for (int i = 0; i < SIZE; i++) {
            a[i] = i * 0.001f;
            b[i] = (SIZE - i) * 0.002f;
        }

        System.out.println(
                "Running with " + Runtime.getRuntime().availableProcessors() +
                        " cores...");
        System.out.println(
                "Using " + SPECIES.length() + "-lane SIMD vectors\n");

        runBenchmarks("Scalar (Single Core, No SIMD)", () -> addScalar(a, b,
                result1));
        System.out.println("Scalar result = " + result1[320000]);
        System.out.println("Scalar result = " + result1[680000]);
        runBenchmarks("Vectorized (Single Core, SIMD)", () -> addVector(a, b,
                result2));
        System.out.println("Vectorized result = " + result2[320000]);
        System.out.println("Vectorized result = " + result2[680000]);
        runBenchmarks("Vectorized + Multithreaded (SIMD + Threads)",
                () -> addVectorParallel(a, b, result3));
        System.out.println("Parallel Vectorized result = " + result3[320000]);
        System.out.println("Parallel Vectorized result = " + result3[680000]);
    }

    private static void runBenchmarks(String label, Runnable task) {
        // Warm-up
        for (int i = 0; i < ITERATIONS; i++) task.run();

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) task.run();
        long end = System.nanoTime();

        double ms = (end - start) / (1_000_000.0 * ITERATIONS);
        System.out.printf("%-45s : %8.2f ms%n", label, ms);
    }

    /**
     * Scalar baseline
     */
    private static void addScalar(float[] a, float[] b, float[] result) {
        for (int i = 0; i < a.length; i++) {
            result[i] = (float) Math.sqrt(Math.sin(a[i] * b[i]));
        }
    }

    /**
     * SIMD vectorization on single core
     */
    private static void addVector(float[] a, float[] b, float[] result) {
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);

        for (; i < upperBound; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);

            // heavier operation
            FloatVector res =
                    va.mul(vb).lanewise(VectorOperators.SIN)
                            .lanewise(VectorOperators.SQRT);
            res.intoArray(result, i);
        }

        for (; i < a.length; i++) {
            result[i] = (float) Math.sqrt(Math.sin(a[i] * b[i]));
        }
    }

    /**
     * SIMD + multithreading
     */
    private static void addVectorParallel(float[] a, float[] b,
                                          float[] result) {
        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = (int) Math.ceil((double) a.length / cores);

        IntStream.range(0, cores).parallel().forEach(core -> {
            int start = core * chunkSize;
            int end = Math.min(a.length, start + chunkSize);

            int i = start;
            int upperBound = SPECIES.loopBound(end);

            for (; i < upperBound; i += SPECIES.length()) {
                FloatVector va = FloatVector.fromArray(SPECIES, a, i);
                FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
                FloatVector res =
                        va.mul(vb).lanewise(VectorOperators.SIN)
                                .lanewise(VectorOperators.SQRT);
                res.intoArray(result, i);
            }

            for (; i < end; i++) {
                result[i] = (float) Math.sqrt(Math.sin(a[i] * b[i]));
            }
        });
    }
}
