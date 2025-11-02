# AI at the Speed of Silicon
Repository created for Javafest demo
SIMD & Vector API demos, JMH benchmarks, and JVM behavior for JavaFest.

## Prerequisites
- JDK 25 (set JAVA_HOME)
  - Download Openjdk/Oracle JDK 25, eg: https://www.oracle.com/in/java/technologies/downloads/
- Maven 3.8+
- Any IDE

## How to run
We are using incubator modules here(Vector), so we need to add --add-modules jdk.incubator.vector as VM params for running the java code.
- java --add-modules jdk.incubator.vector src/main/java/com/javafest/aiatspeed/PerformanceDemos.java

