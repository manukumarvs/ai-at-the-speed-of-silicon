package com.javafest.aiatspeed.cosine;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * CosineSimilarityDemo
 *
 * Presentation/demo code: semantic search example using 128-D embeddings.
 * - Scalar cosine similarity
 * - Vector API cosine similarity (SPECIES_256)
 *
 * Run with:
 *   --enable-preview --add-modules jdk.incubator.vector
 *
 * Notes:
 * - Embeddings are synthetic but clustered so similar sentences score highly.
 * - This is educational code (not production embedding pipeline).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CosineSimilarityDemo {
//    @Param({"10000"})
//    public int size;
    private static final int DIM = 128;
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_256;

    void main() {
        run();
    }

    public static void run() {
        List<String> sentences = List.of(
                "How to optimize Java performance using Vector API",
                "Spring Boot REST services tutorial",
                "Hardware acceleration for AI inference in Java",
                "Quantum-resistant cryptography in Java 25",
                "Best practices for garbage collection tuning"
        );

        // Query we want to match
        String queryText = "How can Java speed up AI workloads using hardware features?";

        // Build clustered embeddings for sentences and query
        Map<String, float[]> embeddings = generateClusteredEmbeddings(sentences);
        float[] queryEmbedding = generateClusteredQueryEmbedding(queryText);

        // Warm-up (JIT + possible intrinsic setup)
        for (int i = 0; i < 3; i++) {
            computeAllScalar(embeddings, queryEmbedding);
            computeAllVector(embeddings, queryEmbedding);
        }

        // Measure a few runs and keep the best (simple approach)
        int runs = 5;
        long bestScalar = Long.MAX_VALUE;
        long bestVector = Long.MAX_VALUE;
        float[] scalarResults = null;
        float[] vectorResults = null;

        for (int r = 0; r < runs; r++) {
            long t0 = System.nanoTime();
            float[] s = computeAllScalar(embeddings, queryEmbedding);
            long t1 = System.nanoTime();
            bestScalar = Math.min(bestScalar, t1 - t0);
            scalarResults = s;

            t0 = System.nanoTime();
            float[] v = computeAllVector(embeddings, queryEmbedding);
            t1 = System.nanoTime();
            bestVector = Math.min(bestVector, t1 - t0);
            vectorResults = v;
        }

        // Print results
        System.out.println("\n=== Query ===");
        System.out.println(queryText);
        System.out.println();

        System.out.printf("Scalar best-run time: %d ms%n", bestScalar / 1_000_000);
        printRanking(sentences, scalarResults);

        System.out.println();

        System.out.printf("Vector best-run time: %d ms%n", bestVector / 1_000_000);
        printRanking(sentences, vectorResults);

        System.out.println();
        System.out.printf("Simple speedup (scalar / vector): %.2fx%n",
                (double) Math.max(1, bestScalar) / Math.max(1, bestVector));

        // Verification that scores match closely
        System.out.println();
        System.out.println("Per-sentence abs(score_scalar - score_vector):");
        int i = 0;
        for (String s : sentences) {
            double d = Math.abs(scalarResults[i] - vectorResults[i]);
            System.out.printf("%d. %s -> diff=%.8f%n", i + 1, s, d);
            i++;
        }
    }

    // --------- helpers ---------

    private static float[] computeAllScalar(Map<String, float[]> map, float[] q) {
        float[] out = new float[map.size()];
        int idx = 0;
        for (float[] v : map.values()) {
            out[idx++] = cosineScalar(v, q);
        }
        return out;
    }

    private static float[] computeAllVector(Map<String, float[]> map, float[] q) {
        float[] out = new float[map.size()];
        int idx = 0;
        for (float[] v : map.values()) {
            out[idx++] = cosineVector(v, q);
        }
        return out;
    }

    // Standard scalar cosine similarity (double accumulators for stability)
    public static float cosineScalar(float[] a, float[] b) {
        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            double av = a[i];
            double bv = b[i];
            dot += av * bv;
            na += av * av;
            nb += bv * bv;
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0.0 ? 0f : (float) (dot / denom);
    }

    // Vectorized cosine similarity using FloatVector SPECIES_256
    public static float cosineVector(float[] a, float[] b) {
        int len = a.length;
        int i = 0;
        FloatVector vdot = FloatVector.zero(SPECIES);
        FloatVector va2 = FloatVector.zero(SPECIES);
        FloatVector vb2 = FloatVector.zero(SPECIES);

        int upper = SPECIES.loopBound(len);
        for (; i < upper; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            vdot = vdot.add(va.mul(vb));
            va2 = va2.add(va.mul(va));
            vb2 = vb2.add(vb.mul(vb));
        }

        float dot = vdot.reduceLanes(VectorOperators.ADD);
        float na = va2.reduceLanes(VectorOperators.ADD);
        float nb = vb2.reduceLanes(VectorOperators.ADD);

        // tail
        for (; i < len; i++) {
            float av = a[i];
            float bv = b[i];
            dot += av * bv;
            na += av * av;
            nb += bv * bv;
        }

        double denom = Math.sqrt((double) na) * Math.sqrt((double) nb);
        return denom == 0.0 ? 0f : (float) (dot / denom);
    }

    private static void printRanking(List<String> sentences, float[] scores) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) idx.add(i);
        idx.sort((x, y) -> Float.compare(scores[y], scores[x])); // descending
        for (int rank = 0; rank < idx.size(); rank++) {
            int i = idx.get(rank);
            System.out.printf("%2d. (score=%.6f) %s%n", rank + 1, scores[i], sentences.get(i));
        }
    }

    // ----------------- Embedding generation (clustered) -----------------

    // Create a query embedding clustered towards 'performance/AI' center
    private static float[] generateClusteredQueryEmbedding(String queryText) {
        Random rnd = new Random(queryText.hashCode());
        float[] center = new float[DIM];
        for (int i = 0; i < DIM; i++) {
            center[i] = (float) (0.6 + 0.35 * Math.cos(i * 0.11));
        }
        float[] emb = new float[DIM];
        for (int i = 0; i < DIM; i++) {
            emb[i] = center[i] + (float) (rnd.nextGaussian() * 0.02);
        }
        normalizeInPlace(emb);
        return emb;
    }

    // Create clustered embeddings for sentences: choose a center by keywords
    private static Map<String, float[]> generateClusteredEmbeddings(List<String> sentences) {
        Map<String, float[]> map = new LinkedHashMap<>();
        Random rnd = new Random(42);

        // define a handful of centers (performance, ai/hw, ops, crypto, general)
        float[] perf = new float[DIM];
        float[] ai = new float[DIM];
        float[] ops = new float[DIM];
        float[] crypto = new float[DIM];
        float[] general = new float[DIM];

        for (int i = 0; i < DIM; i++) {
            perf[i] = (float) (0.6 + 0.3 * Math.cos(i * 0.09));
            ai[i] = (float) (0.35 + 0.5 * Math.sin(i * 0.07));
            ops[i] = (float) (0.25 + 0.45 * Math.cos(i * 0.05));
            crypto[i] = (float) (0.2 + 0.45 * Math.sin(i * 0.13));
            general[i] = (float) (0.4 + 0.35 * Math.cos(i * 0.03));
        }

        List<float[]> centers = List.of(perf, ai, ops, crypto, general);

        for (String s : sentences) {
            int cid = chooseCenter(s.toLowerCase());
            float[] c = centers.get(cid);
            float[] emb = new float[DIM];
            for (int i = 0; i < DIM; i++) {
                emb[i] = c[i] + (float) (rnd.nextGaussian() * 0.03);
            }
            normalizeInPlace(emb);
            map.put(s, emb);
        }
        return map;
    }

    // naive keyword-based center chooser to keep semantics consistent
    private static int chooseCenter(String text) {
        if (text.contains("performance") || text.contains("optimiz") || text.contains("speed")) return 0; // perf
        if (text.contains("hardware") || text.contains("ai") || text.contains("vector")) return 1; // ai/hw
        if (text.contains("spring") || text.contains("rest") || text.contains("micro")) return 2; // ops
        if (text.contains("quantum") || text.contains("crypt")) return 3; // crypto
        return 4; // general
    }

    private static void normalizeInPlace(float[] v) {
        double s = 0.0;
        for (float x : v) s += (double) x * x;
        double n = Math.sqrt(s);
        if (n == 0.0) return;
        for (int i = 0; i < v.length; i++) v[i] = (float) (v[i] / n);
    }
}
