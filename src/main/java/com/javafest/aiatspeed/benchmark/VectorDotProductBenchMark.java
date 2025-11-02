package com.javafest.aiatspeed.benchmark;

import org.openjdk.jmh.annotations.*;
import module jdk.incubator.vector;

import java.util.concurrent.TimeUnit;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class VectorDotProductBenchMark {

    @Param({"10000"})
    public int size;

    private float[] a, b;
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    @Setup(Level.Trial)
    public void setup() {
        a = new float[size];
        b = new float[size];
        Random r = new Random(123);
        for (int i = 0; i < size; i++) { a[i] = r.nextFloat(); b[i] = r.nextFloat(); }
    }

    @Benchmark
    public float scalarDot() {
        float sum = 0f;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

    @Benchmark
    public float vectorDot() {
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
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

}

