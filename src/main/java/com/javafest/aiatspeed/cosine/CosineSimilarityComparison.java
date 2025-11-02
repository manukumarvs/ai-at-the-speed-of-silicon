package com.javafest.aiatspeed.cosine;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.util.Random;

public class CosineSimilarityComparison {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    void main() {
        run();
    }

    public static void run() {
        int size = 1_000_000; // Large size to demonstrate performance difference

        float[] a = generateRandomArray(size);
        float[] b = generateRandomArray(size);

        // Warm-up to trigger JIT optimization
        for (int i = 0; i < 10; i++) {
            cosineSimilarityScalar(a, b);
            cosineSimilarityVector(a, b);
        }

        long startTime = System.nanoTime();
        float scalarResult = cosineSimilarityScalar(a, b);
        long scalarTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        float vectorResult = cosineSimilarityVector(a, b);
        long vectorTime = System.nanoTime() - startTime;

        System.out.println("===== Cosine Similarity Performance Comparison =====");
        System.out.println("Scalar Result: " + scalarResult + " | Time (ns): " + scalarTime);
        System.out.println("Vector Result: " + vectorResult + " | Time (ns): " + vectorTime);

        double speedup = (double) scalarTime / vectorTime;
        System.out.printf("⚡ SIMD Speedup: %.2fx faster than scalar%n", speedup);
    }

    // Scalar implementation
    public static float cosineSimilarityScalar(float[] a, float[] b) {
        float dot = 0f;
        float magA = 0f;
        float magB = 0f;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }

        return (float) (dot / (Math.sqrt(magA) * Math.sqrt(magB)));
    }
//  return (float) (dot / (Math.sqrt(magA*magB)));
    // SIMD Vector API implementation
    public static float cosineSimilarityVector(float[] a, float[] b) {
        FloatVector dotVec = FloatVector.zero(SPECIES);
        FloatVector magAVec = FloatVector.zero(SPECIES);
        FloatVector magBVec = FloatVector.zero(SPECIES);

        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var aVec = FloatVector.fromArray(SPECIES, a, i);
            var bVec = FloatVector.fromArray(SPECIES, b, i);

            dotVec = dotVec.add(aVec.mul(bVec));
            magAVec = magAVec.add(aVec.mul(aVec));
            magBVec = magBVec.add(bVec.mul(bVec));
        }

        float dot = dotVec.reduceLanes(VectorOperators.ADD);
        float magA = magAVec.reduceLanes(VectorOperators.ADD);
        float magB = magBVec.reduceLanes(VectorOperators.ADD);

        // Scalar remainder
        for (; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }

        return (float) (dot / (Math.sqrt(magA) * Math.sqrt(magB)));
    }

    private static float[] generateRandomArray(int size) {
        Random random = new Random();
        float[] array = new float[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextFloat();
        }
        return array;
    }
}
