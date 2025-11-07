package com.javafest.aiatspeed.benchmark;

public class CosineSimilarityScalar {
    public static float cosine(float[] a, float[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double)a[i] * b[i];
            na += (double)a[i] * a[i];
            nb += (double)b[i] * b[i];
        }
        return (float)(dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }

    public static void main(String[] args) {
        int n = 10_000_000_00;
        float[] a = new float[n];
        float[] b = new float[n];
        java.util.Random r = new java.util.Random(123);
        for (int i = 0; i < n; i++) { a[i] = r.nextFloat(); b[i] = r.nextFloat(); }

        // warmup
        float res;
//                = cosine(a,b);

        long t0 = System.nanoTime();
        res = cosine(a,b);
        long ms = (System.nanoTime() - t0)/1_000_000;
        System.out.printf("Scalar cosine: %d ms (result %.6f)%n", ms, res);
    }
}
