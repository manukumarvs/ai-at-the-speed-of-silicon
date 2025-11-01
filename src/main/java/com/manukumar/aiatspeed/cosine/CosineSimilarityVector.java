package com.manukumar.aiatspeed.cosine;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class CosineSimilarityVector {
    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public static float cosine(float[] a, float[] b) {
        int i = 0;
        FloatVector vdot = FloatVector.zero(SPECIES);
        FloatVector va2 = FloatVector.zero(SPECIES);
        FloatVector vb2 = FloatVector.zero(SPECIES);
        int upper = SPECIES.loopBound(a.length);
        for (; i < upper; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            vdot = vdot.add(va.mul(vb));
            va2 = va2.add(va.mul(va));
            vb2 = vb2.add(vb.mul(vb));
        }
        float[] tmp = new float[SPECIES.length()];
        float dot = 0f, na = 0f, nb = 0f;
        vdot.intoArray(tmp, 0); for (float v : tmp) dot += v;
        va2.intoArray(tmp, 0); for (float v : tmp) na += v;
        vb2.intoArray(tmp, 0); for (float v : tmp) nb += v;
        for (; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / ((float)(Math.sqrt(na) * Math.sqrt(nb)));
    }

    public static void main(String[] args) {
        int n = 10_000_000;
        float[] a = new float[n];
        float[] b = new float[n];
        java.util.Random r = new java.util.Random(123);
        for (int i = 0; i < n; i++) { a[i] = r.nextFloat(); b[i] = r.nextFloat(); }

        // warmup
        float res = cosine(a,b);

        long t0 = System.nanoTime();
        res = cosine(a,b);
        long ms = (System.nanoTime() - t0)/1_000_000;
        System.out.printf("Vector cosine: %d ms (result %.6f)%n", ms, res);
    }
}
