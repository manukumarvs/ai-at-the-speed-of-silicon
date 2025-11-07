# AI at the Speed of Silicon
Repository contains SIMD & Vector API demos, JMH benchmarks, and JVM behavior demos for JavaFest.

## Prerequisites
- JDK 25 (set JAVA_HOME)
  - Download Openjdk/Oracle JDK 25, eg: https://www.oracle.com/in/java/technologies/downloads/
  - export JAVA_HOME=your_java_downloaded_location
  - export PATH=$JAVA_HOME/bin:$PATH
- Maven 3.8+
- Any IDE

## How to run
We are using incubator modules here(Vector), so we need to add --add-modules jdk.incubator.vector as VM params for running the java code.
### If you are running from a terminal
      javac --add-modules jdk.incubator.vector src/main/java/com/javafest/aiatspeed/vector/DotProductVectorDemo.java
      java --add-modules jdk.incubator.vector src/main/java/com/javafest/aiatspeed/vector/DotProductVectorDemo
### If you are running using IntelliJ/Eclipse
      Use latest IntelliJ/Eclipse as JDK 25 might not be recognized in older versions
      We recommend IntelliJ IDEA 2025.2.* versions
      For Eclipse:
            Version: 2025-09 (4.37.0) or above
            Go to Eclipse Marketplace: You can install the "Java 25 Support for Eclipse 2025-09 (4.37)" feature directly from the marketplace within the IDE by going to Help > Eclipse     Marketplace... and searching for it.
      Add --add-modules jdk.incubator.vector as VM params
      Run your java code
### If you are running using Maven
      mvn clean package
      java --add-modules jdk.incubator.vector -jar target/ai-at-the-speed-of-silicon-1.0-SNAPSHOT.jar

## Try with different flags
Try with some of these flags to see the difference.
- -XX:-UseSuperWord
- -XX:+UseStringDeduplication -XX:+PrintStringDeduplicationStatistics
- -XX:+UnlockDiagnosticVMOptions -XX:+PrintIntrinsics
- -XX:+PrintCompilation




