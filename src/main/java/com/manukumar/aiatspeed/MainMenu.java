package com.manukumar.aiatspeed;

import com.manukumar.aiatspeed.vector.DotProductVectorDemo;
import com.manukumar.aiatspeed.cosine.CosineSimilarityComparison;
import com.manukumar.aiatspeed.vector.VectorPerformanceDemo;
import com.manukumar.aiatspeed.cosine.CosineSimilarityDemo;
import com.manukumar.aiatspeed.jitgc.JitWarmupDemo;
import com.manukumar.aiatspeed.jitgc.GcPauseDemo;

public class MainMenu {
    void main() {
        IO.println("""
        === AI at the Speed of Silicon — Demos ===
         1) Run All Performance Demos
         2) Dot-product (scalar vs vector)
         3) Vector Performance (scalar vs vector)
         4) Cosine Similarity Comparison Demo (scalar vs vector)
         5) CosineSimilarity Demo
         6) Exit
        """);
        while (true) {
            String line = IO.readln("Choose demo [1-6]: ");
            if (line == null) return;
            switch (line.trim()) {
                case "1" -> PerformanceDemos.run();
                case "2" -> DotProductVectorDemo.run();
                case "3" -> VectorPerformanceDemo.run();
                case "4" -> CosineSimilarityComparison.run();
                case "5" -> CosineSimilarityDemo.run();
                case "6" -> {
                    System.out.println("Bye");
                    return;
                }
                default -> System.out.println("Invalid");
            }
        }
    }
}

