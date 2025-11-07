package com.javafest.aiatspeed;

import java.util.Random;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class PerformanceDemos {

    private static final int SIZE = 10_000_000;
    private static final Random RAND = new Random(42);

    void main() {
        run();
    }

    public static void run() {
        System.out.println("=== Java Performance Demos ===");
        System.out.println();

//        cacheDemo();
        System.out.println();

//        branchPredictionDemo();
        System.out.println();

        vectorDemo();
        System.out.println();

//        jitDemo();
        System.out.println();

//        gcDemo();
    }

    //  CPU Cache Demo
    static void cacheDemo() {
        System.out.println(" CPU Cache Demo");
        int N = 2048;
        int[][] matrix = new int[N][N];

        long start = System.nanoTime();
        long sum = 0;
        // Row-major (sequential)
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                sum += matrix[i][j];
        long rowTime = System.nanoTime() - start;

        start = System.nanoTime();
        sum = 0;
        // Column-major (cache unfriendly)
        for (int j = 0; j < N; j++)
            for (int i = 0; i < N; i++)
                sum += matrix[i][j];
        long colTime = System.nanoTime() - start;

        double diff = (colTime - rowTime) * 100.0 / rowTime;
        System.out.printf("Row-major access: %.2f ms%n", rowTime / 1_000_000.0);
        System.out.printf("Column-major access: %.2f ms%n", colTime / 1_000_000.0);
        System.out.printf("Column-major was %.1f%% slower%n", diff);
    }

    //  Branch Prediction Demo
    static void branchPredictionDemo() {
        System.out.println(" Branch Prediction Demo");

        int[] data = new int[SIZE];
        for (int i = 0; i < SIZE; i++)
            data[i] = RAND.nextInt();

        // Predictable branches
        long sum = 0;
        long start = System.nanoTime();
        for (int x : data) {
            if (x >= 0) sum += x;
            else sum -= x;
        }
        long rowTime = System.nanoTime() - start;

        // Unpredictable branches
        sum = 0;
        Random random = new Random(99);
        start = System.nanoTime();
        for (int x : data) {
            if (random.nextBoolean()) sum += x;
            else sum -= x;
        }
        long colTime = System.nanoTime() - start;

        double ratio = (double) colTime / rowTime;
        System.out.printf("Predictable branches: %.2f ms%n", rowTime / 1_000_000.0);
        System.out.printf("Unpredictable branches: %.2f ms%n", colTime / 1_000_000.0);
        System.out.printf("Unpredictable branches are %.1f%% slower (%.2fx)%n",
                (ratio - 1) * 100, ratio);

        if (sum == 42) System.out.println("Magic!"); // avoid JIT elimination
    }

    //  SIMD / Vector API Demo
    static void vectorDemo() {
        System.out.println(" SIMD / Vector API Demo");
        int size = 50_000_000;
        float[] a = new float[size];
        float[] b = new float[size];
        float[] c = new float[size];
        for (int i = 0; i < size; i++) {
            a[i] = RAND.nextFloat();
            b[i] = RAND.nextFloat();
        }

        // Warm-up JIT
        for (int i = 0; i < 2; i++) normalAdd(a, b, c);
        for (int i = 0; i < 2; i++) vectorAdd(a, b, c);

        long start = System.nanoTime();
        normalAdd(a, b, c);
        long mid = System.nanoTime();
        vectorAdd(a, b, c);
        long end = System.nanoTime();

        double normalMs = (mid - start) / 1_000_000.0;
        double vectorMs = (end - mid) / 1_000_000.0;
        double ratio = normalMs / vectorMs;

        System.out.printf("Normal loop: %.2f ms%n", normalMs);
        System.out.printf("Vector API loop: %.2f ms%n", vectorMs);
        System.out.printf("⚡ Vector API was %.2fx faster%n", ratio);
    }

    static void normalAdd(float[] a, float[] b, float[] c) {
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] * b[i] + 1.5f;
        }
    }

    static void vectorAdd(float[] a, float[] b, float[] c) {
        VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        int i = 0;
        for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            FloatVector vc = va.mul(vb).add(1.5f);
            vc.intoArray(c, i);
        }
        for (; i < a.length; i++) {
            c[i] = a[i] * b[i] + 1.5f;
        }
    }

    //  JIT Optimization Demo
    static void jitDemo() {
        System.out.println(" JIT Optimization Demo");
        long start, end;

        // Before JIT warm-up
        start = System.nanoTime();
        double slow = slowFunction();
        end = System.nanoTime();
        double firstRun = (end - start) / 1_000_000.0;

        // After repeated execution (JIT kicks in)
        for (int i = 0; i < 100_000; i++) slowFunction();
        start = System.nanoTime();
        double fast = slowFunction();
        end = System.nanoTime();
        double secondRun = (end - start) / 1_000_000.0;

        System.out.printf("First run: %.3f ms%n", firstRun);
        System.out.printf("After JIT warm-up: %.3f ms%n", secondRun);
        System.out.printf("JIT made it %.1fx faster%n", firstRun / secondRun);
    }

    static double slowFunction() {
        double sum = 0;
        for (int i = 0; i < 10_000; i++) {
            sum += Math.sqrt(i) * Math.sin(i);
        }
        return sum;
    }

    //  GC Demo
    static void gcDemo() {
        System.out.println("Garbage Collector Demo");
        long start = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            int[] temp = new int[1_000_000]; // allocate large arrays
            temp[0] = i;
        }

        long end = System.nanoTime();
        double duration = (end - start) / 1_000_000.0;
        System.out.printf("Mass allocation loop: %.2f ms%n", duration);
        System.out.println(" You may notice minor pauses = GC in action!");
    }
}
