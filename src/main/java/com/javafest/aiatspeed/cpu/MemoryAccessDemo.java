/**
 * @author dmorye
 **/
package com.javafest.aiatspeed.cpu;
import java.util.*;

public class MemoryAccessDemo {
    public static void main(String[] args) {
        final int SIZE = 50_000_000; // 50 million elements
        int[] array = new int[SIZE];

        // Initialize the array
        for (int i = 0; i < SIZE; i++) {
            array[i] = i+1;
        }

        // Warm-up phase (to stabilize JIT and cache)
        warmup(array);

        // --- Sequential access ---
        long startSeq = System.nanoTime();
        long sumSeq = 0;
        for (int i = 0; i < SIZE; i++) {
            sumSeq += array[i];
        }
        long endSeq = System.nanoTime();

        // --- Random access ---
        int[] randomIndices = generateRandomIndices(SIZE);
        long startRand = System.nanoTime();
        long sumRand = 0;
        for (int i = 0; i < SIZE; i++) {
            sumRand += array[randomIndices[i]];
        }
        long endRand = System.nanoTime();

        // --- Results ---
        System.out.printf("Sequential access time: %.2f ms%n", (endSeq - startSeq) / 1_000_000.0);
        System.out.printf("Random access time: %.2f ms%n", (endRand - startRand) / 1_000_000.0);
        System.out.printf("Sum check: %d %d%n", sumSeq, sumRand);
    }

    // Generates a random permutation of indices [0, SIZE)
    private static int[] generateRandomIndices(int size) {
        int[] indices = new int[size];
        for (int i = 0; i < size; i++) indices[i] = i;
        Random rand = new Random(42); // fixed seed for repeatability
        for (int i = size - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }
        return indices;
    }

    // Simple warm-up loop to trigger JIT and fill caches
    private static void warmup(int[] arr) {
        long temp = 0;
        for (int i = 0; i < arr.length; i++) {
            temp += arr[i];
        }
    }
}

