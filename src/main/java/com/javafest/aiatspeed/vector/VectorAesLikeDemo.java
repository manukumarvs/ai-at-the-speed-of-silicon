package com.javafest.aiatspeed.vector;

import com.javafest.aiatspeed.util.Timer;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;

import java.util.Random;

    /**
     * Runs the AES-like Vector Block Demo. This method demonstrates the usage of
     * Java's Vector API to perform a simple block transformation. It measures the
     * time taken to perform the transformation on a large block of data.
     *
     * The demo performs the following steps:
     * 1. Initializes input and output byte arrays of size {@value #BLOCKS} * 16.
     * 2. Fills the input array with random data.
     * 3. Performs a warmup run of the transformation to ensure the JIT compiler
     *    optimizes the code.
     * 4. Measures the time taken to perform the transformation and prints the result.
     *
     * Note that this is an educational demo and not intended for production use.
     */

public class VectorAesLikeDemo {
    private static final int BLOCKS = 2_000_000;
    private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_128;

    void main() {
        run();
    }
    public static void run() {
        System.out.println(" AES-like Vector Block Demo (educational)");
        byte[] in = new byte[BLOCKS * 16];
        byte[] out = new byte[in.length];
        new Random(42).nextBytes(in);

        // Warmup
        transformBlocks(in, out);
        transformBlocks(in, out);

        Timer t = new Timer();
        transformBlocks(in, out);
        System.out.printf("Vector block transform: %d ms%n", t.elapsedMillis());
    }

    static void transformBlocks(byte[] in, byte[] out) {
        int i = 0;
        int len = in.length;
        int step = SPECIES.length();
        for (; i + step <= len; i += step) {
            var va = ByteVector.fromArray(SPECIES, in, i);
            var v1 = va.lanewise(VectorOperators.XOR, (byte)0x5A);
            var v2 = v1.lanewise(VectorOperators.AND, (byte)0x0F);
            var v3 = v2.lanewise(VectorOperators.ADD, (byte)1);
            v3.intoArray(out, i);
        }
        for (; i < len; i++) out[i] = (byte)(in[i] ^ 0x5A);
    }
}
