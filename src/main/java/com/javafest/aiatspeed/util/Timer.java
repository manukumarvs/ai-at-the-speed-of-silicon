package com.javafest.aiatspeed.util;

public class Timer {
    private long t;
    public Timer() { t = System.nanoTime(); }
    public long elapsedMillis() { return (System.nanoTime() - t) / 1_000_000; }
    public void reset() { t = System.nanoTime(); }
}

