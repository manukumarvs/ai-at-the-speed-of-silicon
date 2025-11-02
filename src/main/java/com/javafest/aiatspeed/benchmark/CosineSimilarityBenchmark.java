package com.javafest.aiatspeed.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CosineSimilarityBenchmark {
    @Param({"10000"})
    public int size;
    private float[] a,b;
    @Setup(Level.Trial)
    public void setup() {
        a = new float[size]; b = new float[size];
        java.util.Random r = new java.util.Random(123);
        for (int i = 0; i < size; i++) { a[i] = r.nextFloat(); b[i] = r.nextFloat(); }
    }
    @Benchmark public float scalar() { return CosineSimilarityScalar.cosine(a,b); }
    @Benchmark public float vector() { return CosineSimilarityVector.cosine(a,b); }
}
