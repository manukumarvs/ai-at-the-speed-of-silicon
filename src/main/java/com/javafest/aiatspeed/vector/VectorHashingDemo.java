package com.javafest.aiatspeed.vector;

import module jdk.incubator.vector;
import module java.base;

/**
 * VectorHashingDemo
 * Simulates a simple "embedding hashing" or fingerprinting by computing many dot-products
 * in a vectorized way and demonstrates speed vs scalar approach.
 */
public class VectorHashingDemo {

    static final int VECTOR_SIZE = 512;
    static final int NUM_INPUTS = 200_000; // number of embeddings to process
    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    /**
     * The main entry point for the VectorHashingDemo application. This method
     * demonstrates the performance difference between scalar and vectorized
     * processing of dot products. It generates random input data, performs
     * a warm-up run, and then measures the execution time of both scalar and
     * vectorized processing. The results are printed to the console, including
     * the execution time and speedup achieved by vectorization.
     *
     * @param args command-line arguments (not used)
     */

    public static void main(String[] args) {
        System.out.println("VectorHashingDemo starting...");
        float[][] inputs = new float[NUM_INPUTS][VECTOR_SIZE];
        float[] weights = new float[VECTOR_SIZE];
        Random rand = new Random(1234);
        for (int i = 0; i < NUM_INPUTS; i++)
            for (int j = 0; j < VECTOR_SIZE; j++)
                inputs[i][j] = rand.nextFloat();
        for (int j = 0; j < VECTOR_SIZE; j++) weights[j] = rand.nextFloat();

        // Warm-up
        float s1 = scalarProcess(inputs, weights);
        float s2 = vectorProcess(inputs, weights);

        long t0 = System.nanoTime();
        float r1 = scalarProcess(inputs, weights);
        long scalarMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        float r2 = vectorProcess(inputs, weights);
        long vectorMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.printf("Scalar total: %d ms, result sample %.6f%n", scalarMs,
                r1);
        System.out.printf("Vector total: %d ms, result sample %.6f%n", vectorMs,
                r2);
        System.out.printf("Speedup: %.2fx%n",
                (double) scalarMs / Math.max(1, vectorMs));
    }

    /**
     * Computes the sum of dot products between the input vectors and a given weight vector.
     * <p>
     * This method iterates over each input vector, computes the dot product with the weight vector,
     * and accumulates the results.
     *
     * @param inputs  a 2D array of input vectors, where each row represents a vector
     * @param weights a 1D array representing the weight vector
     * @return the sum of dot products between the input vectors and the weight vector
     */
    static float scalarProcess(float[][] inputs, float[] weights) {
        float sum = 0f;
        for (int i = 0; i < inputs.length; i++) {
            float dot = 0f;
            for (int j = 0; j < weights.length; j++)
                dot += inputs[i][j] * weights[j];
            sum += dot; // accumulate to prevent elimination
        }
        return sum;
    }

    /**
     * Computes the sum of dot products between the input vectors and a given weight vector
     * using vectorized operations.
     * <p>
     * This method utilizes the Vector API to perform dot product calculations in a
     * vectorized manner, which can lead to significant performance improvements over
     * scalar processing for large datasets.
     *
     * @param inputs  a 2D array of input vectors, where each row represents a vector
     * @param weights a 1D array representing the weight vector
     * @return the sum of dot products between the input vectors and the weight vector
     */
    static float vectorProcess(float[][] inputs, float[] weights) {
        float total = 0f;
        int len = weights.length;
        int upper = SPECIES.loopBound(len);
        for (int i = 0; i < inputs.length; i++) {
            FloatVector acc = FloatVector.zero(SPECIES);
            int j = 0;
            for (; j < upper; j += SPECIES.length()) {
                FloatVector vIn = FloatVector.fromArray(SPECIES, inputs[i], j);
                FloatVector vW = FloatVector.fromArray(SPECIES, weights, j);
                acc = acc.add(vIn.mul(vW));
            }
            float dot = 0f;
            float[] tmp = new float[SPECIES.length()];
            acc.intoArray(tmp, 0);
            for (float v : tmp) dot += v;
            for (; j < len; j++) dot += inputs[i][j] * weights[j];
            total += dot;
        }
        return total;
    }
}
