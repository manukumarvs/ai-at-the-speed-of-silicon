package com.javafest.aiatspeed.cosine;

import java.util.*;
import java.util.stream.IntStream;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class MovieRecommendationCosineDemo {

    // Toggle this flag based on whether you want to use SIMD vectors
    private static final boolean USE_VECTOR = true; // set to true for Vector
    // API version (JDK 25+)
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_256;

    // Movie class
    static class Movie {
        String title;
        float[] embedding;

        Movie(String title, float[] embedding) {
            this.title = title;
            this.embedding = embedding;
        }
    }

    public static void main(String[] args) {
        // Simulated 128D embeddings for movies
        Movie[] movies = new Movie[]{
                new Movie("Interstellar", generateEmbedding(128, 0.9f)),
                new Movie("The Martian", generateEmbedding(128, 0.88f)),
                new Movie("Star Wars", generateEmbedding(128, 0.80f)),
                new Movie("Fast & Furious", generateEmbedding(128, 0.1f)),
                new Movie("Gravity", generateEmbedding(128, 0.85f))
        };

        // Simulate a user query vector (space exploration)
        float[] queryEmbedding = generateEmbedding(128, 0.87f);

        System.out.println("=== Movie Recommendation Using Cosine Similarity ===");
        System.out.println("Use Vector API? " + USE_VECTOR);
        System.out.println();

        // Compute similarity scores
        List<Result> results = new ArrayList<>();
        for (Movie movie : movies) {
            double similarity = USE_VECTOR
                    ? cosineSimilarityVector(queryEmbedding, movie.embedding)
                    : cosineSimilarityScalar(queryEmbedding, movie.embedding);
            results.add(new Result(movie.title, similarity));
        }

        // Sort by similarity descending
        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        // Print results
        System.out.println("Query: Recommend a mind-bending science fiction movie about space exploration");
        System.out.println("------------------------------------------------------------");
        results.forEach(r ->
                System.out.printf("Movie: %-20s  | Similarity Score: %.6f%n", r.title, r.similarity)
        );

        System.out.println("\nTop Recommendation: " + results.get(0).title);
    }

    // Simulated embedding generator
    private static float[] generateEmbedding(int dimension, float baseValue) {
        Random random = new Random();
        float[] embedding = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            embedding[i] = baseValue + (random.nextFloat() - 0.5f) * 0.1f; // slight noise
        }
        return embedding;
    }

    // Scalar cosine similarity
    private static double cosineSimilarityScalar(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Vector API cosine similarity
    private static double cosineSimilarityVector(float[] a, float[] b) {
        FloatVector dotV, normAV, normBV;
        float dot = 0, normA = 0, normB = 0;

        int i = 0;
        for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);

            dotV = va.mul(vb);
            normAV = va.mul(va);
            normBV = vb.mul(vb);

            dot += dotV.reduceLanes(VectorOperators.ADD);
            normA += normAV.reduceLanes(VectorOperators.ADD);
            normB += normBV.reduceLanes(VectorOperators.ADD);
        }

        // Handle tail elements
        for (; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Result class
    static class Result {
        String title;
        double similarity;

        Result(String title, double similarity) {
            this.title = title;
            this.similarity = similarity;
        }
    }
}
