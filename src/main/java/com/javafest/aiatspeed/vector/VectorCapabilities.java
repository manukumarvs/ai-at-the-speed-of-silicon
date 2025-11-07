package com.javafest.aiatspeed.vector;

import jdk.incubator.vector.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VectorCapabilities {

public static void main(String[] args) {
    run(args);
}

    private static void run(String[] args) {
        boolean quickBench = false;
    if(args != null && args.length > 0) {
            quickBench =
                   args.length > 0 && "--quick-bench".equals(args[0]);
       }

        System.out.println("Java Vector API capabilities (Preferred species and lane counts)");
        System.out.println("JDK: " + System.getProperty("java.version")
        + " | OS/Arch: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch"));
        System.out.println();

        report("byte", ByteVector.SPECIES_PREFERRED, 8);
        report("short", ShortVector.SPECIES_PREFERRED, 16);
        report("int", IntVector.SPECIES_PREFERRED, 32);
        report("long", LongVector.SPECIES_PREFERRED, 64);
        report("float", FloatVector.SPECIES_PREFERRED, 32);
        report("double",DoubleVector.SPECIES_PREFERRED, 64);

        if (quickBench) {
        System.out.println("\nQuick bench (rough, not JMH): elementwise y = a * s + b (float)");
        quickBenchSaxpyFloat(50_000_000); // adjust size for your machine
        }

        System.out.println("\nNotes:");
        System.out.println("- Theoretical max speedup for a tight, compute-bound loop ~ laneCount (e.g., 8 lanes => up to ~8x).");
        System.out.println("- Real speedup is usually less (memory bandwidth, cache, tail handling, branches). Measure with JMH for accuracy.");
    }

    private static void report(String typeName, VectorSpecies<?> species, int elemBits) {
int lanes = species.length();
int vbits = species.vectorBitSize();
String shape = species.vectorShape().toString();

System.out.printf("\nType=%-6s Preferred: shape=%-5s vectorBits=%-4d " +
                "lanes=%-3d elemBits=%-2d%n",
typeName, shape, vbits, lanes, elemBits);

// Theoretical upper bound for compute-bound kernels
double theoretical = Math.max(1, lanes);
System.out.printf(" Theoretical max speedup vs scalar (compute-bound): ~%.1fx%n", theoretical);

// Show standard species lane counts for context
if (typeName.equals("float")) {
showStandard("S_64 ", FloatVector.SPECIES_64);
showStandard("S_128", FloatVector.SPECIES_128);
showStandard("S_256", FloatVector.SPECIES_256);
showStandard("S_512", FloatVector.SPECIES_512);
} else if (typeName.equals("double")) {
showStandard("S_64 ", DoubleVector.SPECIES_64);
showStandard("S_128", DoubleVector.SPECIES_128);
showStandard("S_256", DoubleVector.SPECIES_256);
showStandard("S_512", DoubleVector.SPECIES_512);
} else if (typeName.equals("int")) {
showStandard("S_64 ", IntVector.SPECIES_64);
showStandard("S_128", IntVector.SPECIES_128);
showStandard("S_256", IntVector.SPECIES_256);
showStandard("S_512", IntVector.SPECIES_512);
} else if (typeName.equals("long")) {
showStandard("S_64 ", LongVector.SPECIES_64);
showStandard("S_128", LongVector.SPECIES_128);
showStandard("S_256", LongVector.SPECIES_256);
showStandard("S_512", LongVector.SPECIES_512);
} else if (typeName.equals("short")) {
showStandard("S_64 ", ShortVector.SPECIES_64);
showStandard("S_128", ShortVector.SPECIES_128);
showStandard("S_256", ShortVector.SPECIES_256);
showStandard("S_512", ShortVector.SPECIES_512);
} else if (typeName.equals("byte")) {
showStandard("S_64 ", ByteVector.SPECIES_64);
showStandard("S_128", ByteVector.SPECIES_128);
showStandard("S_256", ByteVector.SPECIES_256);
showStandard("S_512", ByteVector.SPECIES_512);
}
}

private static void showStandard(String label, VectorSpecies<?> s) {
System.out.printf(" %-5s lanes=%-3d vectorBits=%-4d%n", label, s.length(), s.vectorBitSize());
}

// Quick, rough microbench to estimate actual speedup on your CPU
private static void quickBenchSaxpyFloat(int n) {
float[] a = new float[n], b = new float[n], y = new float[n];
Random rnd = new Random(1);
for (int i = 0; i < n; i++) {
a[i] = rnd.nextFloat();
b[i] = rnd.nextFloat();
}
float s = 1.2345f;

// Warmup
saxpyScalar(a, b, y, s);
saxpyVector(a, b, y, s);

long t1 = timeMs(() -> saxpyScalar(a, b, y, s));
long t2 = timeMs(() -> saxpyVector(a, b, y, s));

double speedup = (double) t1 / Math.max(1, t2);
System.out.printf(" Scalar: %d ms, Vector: %d ms, speedup ~= %.2fx%n", t1, t2, speedup);
System.out.println(" Warning: This is a rough estimate. Use JMH for accurate benchmarking.");
}

private static void saxpyScalar(float[] a, float[] b, float[] y, float s) {
for (int i = 0; i < a.length; i++) {
y[i] = a[i] * s + b[i];
}
}

private static void saxpyVector(float[] a, float[] b, float[] y, float s) {
final VectorSpecies<Float> SPEC = FloatVector.SPECIES_PREFERRED;
int i = 0, upper = SPEC.loopBound(a.length);
FloatVector vs = FloatVector.broadcast(SPEC, s);
for (; i < upper; i += SPEC.length()) {
FloatVector va = FloatVector.fromArray(SPEC, a, i);
FloatVector vb = FloatVector.fromArray(SPEC, b, i);
va.fma(vs, vb).intoArray(y, i);
}
for (; i < a.length; i++) {
y[i] = a[i] * s + b[i];
}
}

private static long timeMs(Runnable r) {
long t0 = System.nanoTime();
r.run();
return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
}
}

