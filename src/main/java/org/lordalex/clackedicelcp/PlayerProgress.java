package org.lordalex.clackedicelcp;

import java.util.HashMap;
import java.util.Map;

public class PlayerProgress {
    // индекс следующего ожидаемого уровня (0 = level1, 1 = level2, ...)
    private int nextIndex = 0;
    // времена за каждый пройденный уровень по порядку
    private final Map<String, Long> times = new HashMap<>();

    public int getNextIndex() { return nextIndex; }
    public void setNextIndex(int i) { this.nextIndex = i; }

    public void addTime(String mazeId, long millis) {
        times.put(mazeId, millis);
    }

    public long getTime(String mazeId) {
        Long t = times.get(mazeId);
        return t == null ? 0L : t;
    }

    public long getTotalTime() {
        long sum = 0;
        for (Long t : times.values()) sum += t;
        return sum;
    }

    public void reset() {
        nextIndex = 0;
        times.clear();
    }
}