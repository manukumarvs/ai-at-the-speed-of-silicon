package com.javafest.aiatspeed.vector;

import com.javafest.aiatspeed.util.Timer;

import module jdk.incubator.vector;

import java.util.Random;
/**
 * Demonstrates the usage of Java's Vector API for computing the dot product of two large float arrays.
 * This class compares the performance of a scalar implementation versus a vectorized implementation.
 */
public class DotProductVectorDemo {

    /**
     * The size of the float arrays used for the dot product computation.
     */
    private static final int SIZE = 40_000_000;

    /**
     * The preferred vector species for FloatVector operations.
     */
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

   void main() {
       run();
   }
    /**
     * Runs the dot product demonstration, comparing scalar and vectorized implementations.
     */
    public static void run() {
        System.out.println(" Dot-product Demo (scalar vs Vector API)");
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        Random r = new Random(123);
        for (int i = 0; i < SIZE; i++) {
            a[i] = r.nextFloat();
            b[i] = r.nextFloat();
        }

        // Warm-up
        scalarDot(a,b);
        vectorDot(a,b);

        Timer t = new Timer();
        float s1 = scalarDot(a,b);
        System.out.printf("Scalar dot: %d ms (result %.3f)%n", t.elapsedMillis(), s1);

        t.reset();
        float s2 = vectorDot(a,b);
        System.out.printf("Vector dot: %d ms (result %.3f)%n", t.elapsedMillis(), s2);
    }

    /**
     * Computes the dot product of two float arrays using a scalar implementation.
     *
     * @param a the first float array
     * @param b the second float array
     * @return the dot product of the two arrays
     */
    static float scalarDot(float[] a, float[] b) {
        float sum = 0f;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

    /**
     * Computes the dot product of two float arrays using a vectorized implementation.
     *
     * @param a the first float array
     * @param b the second float array
     * @return the dot product of the two arrays
     */
    static float vectorDot(float[] a, float[] b) {
        int i = 0;
        FloatVector acc = FloatVector.zero(SPECIES);
        int upper = SPECIES.loopBound(a.length);
        for (; i < upper; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            acc = acc.add(va.mul(vb));
        }
        float sum = 0f;
        float[] tmp = new float[SPECIES.length()];
        acc.intoArray(tmp, 0);
        for (float v : tmp) sum += v;
        for (; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }
}
