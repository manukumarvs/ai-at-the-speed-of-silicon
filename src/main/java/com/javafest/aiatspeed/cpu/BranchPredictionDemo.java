/**
 * @author dmorye
 **/
package com.javafest.aiatspeed.cpu;
import java.util.Random;

public class BranchPredictionDemo {

    private static final int SIZE = 500_000_000; //5 billion
    
    private static final int[] data = new int[SIZE];
    private static final boolean[] randomFlags = new boolean[SIZE];

    public static void main(String[] args) {
        Random rand = new Random(42); // fixed seed for repeatability

        // Fill arrays
        for (int i = 0; i < SIZE; i++) {
            data[i] = i+1;
            randomFlags[i] = rand.nextBoolean();
        }

        // Warm up
        predictable();
        unpredictable();

        // Measure predictable branch
        long start1 = System.nanoTime();
        long sum1 = predictable();
        long end1 = System.nanoTime();

        // Measure unpredictable branch
        long start2 = System.nanoTime();
        long sum2 = unpredictable();
        long end2 = System.nanoTime();

        System.out.printf("Predictable Branch Time: %.2f ms (sum=%d)%n",
                (end1 - start1) / 1_000_000.0, sum1);
        System.out.printf("Unpredictable Branch Time: %.2f ms (sum=%d)%n",
                (end2 - start2) / 1_000_000.0, sum2);
    }

    private static long predictable() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            if (i % 2 == 0) {
                sum += data[i];
            }
        }
        return sum;
    }

    private static long unpredictable() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            if (randomFlags[i]) {
                sum += data[i];
            }
        }
        return sum;
    }
}

