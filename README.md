# AI at the Speed of Silicon
Repository created for Javafest demo
SIMD & Vector API demos, JMH benchmarks, and JVM behavior for JavaFest.

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
      java --add-modules jdk.incubator.vector src/main/java/com/javafest/aiatspeed/MainMenu.java
### If you are running using IntelliJ
      Add --add-modules jdk.incubator.vector as VM params
      Run your java code
### If you are running using Maven
      mvn clean package
      java --add-modules jdk.incubator.vector -jar target/ai-at-the-speed-of-silicon-1.0-SNAPSHOT.jar



