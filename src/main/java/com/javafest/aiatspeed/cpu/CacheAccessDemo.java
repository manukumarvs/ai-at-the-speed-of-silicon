/**
 * @author dmorye
 **/
package com.javafest.aiatspeed.cpu;
import java.util.Random;

public class CacheAccessDemo {

    private static final int SIZE = 20_000;      // number of rows/columns
    private static final int[][] matrix = new int[SIZE][SIZE];

    public static void main(String[] args) {
        printSystemInfo();

        // Fill matrix with random values
        System.out.println("Initialising...");
        Random random = new Random(42);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                matrix[i][j] = random.nextInt(100);
            }
        }

        System.out.println("Matrix initialized. Starting access tests...");

        // Warm-up
        rowMajorAccess();
        columnMajorAccess();

        // Measure row-major access
        long start1 = System.nanoTime();
        long sum1 = rowMajorAccess();
        long end1 = System.nanoTime();

        // Measure column-major access
        long start2 = System.nanoTime();
        long sum2 = columnMajorAccess();
        long end2 = System.nanoTime();

        double rowMajorTime = (end1 - start1) / 1_000_000.0;
        double columnMajorTime = (end2 - start2) / 1_000_000.0;

        System.out.println("-----------------------------------");
        System.out.printf("Row-major sum: %d, time: %.2f ms%n", sum1, rowMajorTime);
        System.out.printf("Column-major sum: %d, time: %.2f ms%n", sum2, columnMajorTime);
        System.out.println("-----------------------------------");
        double speedup = columnMajorTime / rowMajorTime;
        System.out.printf("Row-major access is %.2f times faster than column-major access%n", speedup);
    }

    // Access the matrix row by row
    private static long rowMajorAccess() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sum += matrix[i][j];
            }
        }
        return sum;
    }

    // Access the matrix column by column
    private static long columnMajorAccess() {
        long sum = 0;
        for (int j = 0; j < SIZE; j++) {
            for (int i = 0; i < SIZE; i++) {
                sum += matrix[i][j];
            }
        }
        return sum;
    }

    private static void printSystemInfo() {
        System.out.println("🧾 System and JVM Info");
        System.out.println("=======================");
        System.out.println("CPU cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("JVM version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.printf("Heap: %.2f MB free / %.2f MB total%n",
                Runtime.getRuntime().freeMemory() / 1e6,
                Runtime.getRuntime().totalMemory() / 1e6);
    }
}

