package com.javafest.aiatspeed.vector;

import java.util.Random;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class VectorPerformanceDemo {

    private static final int SIZE = 50_000_000;
    private static final float[] a = new float[SIZE];
    private static final float[] b = new float[SIZE];
    private static final float[] c = new float[SIZE];

    static {
        Random r = new Random(42);
        for (int i = 0; i < SIZE; i++) {
            a[i] = r.nextFloat();
            b[i] = r.nextFloat();
        }
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        // Warm-up runs to trigger JIT compilation
        for (int i = 0; i < 3; i++) {
            normalAdd();
            vectorAdd();
        }

        // Measure scalar version
        long start = System.nanoTime();
        normalAdd();
        long mid = System.nanoTime();
        vectorAdd();
        long end = System.nanoTime();

        System.out.printf("Normal loop: %.2f ms%n", (mid - start) / 1_000_000.0);
        System.out.printf("Vector API loop: %.2f ms%n", (end - mid) / 1_000_000.0);
    }


    static void normalAdd() {
        for (int i = 0; i < SIZE; i++) {
            // Heavier math to make compute cost visible
            float x = a[i];
            float y = b[i];
            c[i] = (float)Math.sqrt(Math.sqrt(x) * Math.sqrt(y) + (x * y));
        }
    }

    static void vectorAdd() {
        VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        int i = 0;
        for (; i < SPECIES.loopBound(SIZE); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);

            // Heavier math, all in SIMD
            FloatVector vc = (va.mul(vb).add(va.sqrt().mul(vb.sqrt()))).sqrt();
            vc.intoArray(c, i);
        }
        for (; i < SIZE; i++) {
            float x = a[i];
            float y = b[i];
            c[i] = (float)(x * y + Math.sqrt(x) * Math.sqrt(y));
        }
    }
}
