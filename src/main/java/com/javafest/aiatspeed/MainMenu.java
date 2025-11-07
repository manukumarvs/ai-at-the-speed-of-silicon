package com.javafest.aiatspeed;

import com.javafest.aiatspeed.cpu.BranchPredictionDemo;
import com.javafest.aiatspeed.cpu.CacheAccessDemo;
import com.javafest.aiatspeed.cpu.MemoryAccessDemo;
import com.javafest.aiatspeed.vector.DotProductVectorDemo;
import com.javafest.aiatspeed.cosine.CosineSimilarityComparison;
import com.javafest.aiatspeed.vector.VectorPerformanceDemo;
import com.javafest.aiatspeed.vector.thread.GoodVectorAndThreadDemo;

public class MainMenu {
    void main() {
        IO.println("""
                === AI at the Speed of Silicon — Demos ===
                 1) Run BranchPredictionDemo
                 2) Run CacheAccessDemo
                 3) Run MemoryAccessDemo
                 4) Dot-product (scalar vs vector)
                 5) Vector Performance (scalar vs vector)
                 6) Cosine Similarity Comparison Demo (scalar vs vector)
                 7) Vector And Thread Demo
                 8) Exit
                """);
        while (true) {
            String line = IO.readln("Choose demo [1-8]: ");
            if (line == null) return;
            switch (line.trim()) {
                case "1" -> BranchPredictionDemo.main(null);
                case "2" -> CacheAccessDemo.main(null);
                case "3" -> MemoryAccessDemo.main(null);
                case "4" -> DotProductVectorDemo.run();
                case "5" -> VectorPerformanceDemo.run();
                case "6" -> CosineSimilarityComparison.run();
                case "7" -> GoodVectorAndThreadDemo.run();
                case "8" -> {
                    System.out.println("Bye");
                    return;
                }
                default -> System.out.println("Invalid");
            }
        }
    }
}

