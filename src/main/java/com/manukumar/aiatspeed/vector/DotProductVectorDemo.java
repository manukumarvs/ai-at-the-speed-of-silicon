package com.manukumar.aiatspeed.vector;

import com.manukumar.aiatspeed.util.Timer;
//import jdk.incubator.vector.FloatVector;
//import jdk.incubator.vector.VectorSpecies;
import module jdk.incubator.vector;

import java.util.Random;

public class DotProductVectorDemo {
    private static final int SIZE = 40_000_000;
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public static void run() {
        System.out.println(" Dot-product Demo (scalar vs Vector API)");
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        Random r = new Random(123);
        for (int i = 0; i < SIZE; i++) { a[i] = r.nextFloat(); b[i] = r.nextFloat(); }

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

    static float scalarDot(float[] a, float[] b) {
        float sum = 0f;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

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

