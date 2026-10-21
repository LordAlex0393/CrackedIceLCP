package org.lordalex.clackedicelcp;

import java.util.HashMap;
import java.util.Map;

public class PlayerProgress {

    private int nextIndex = 0;

    // чистое время — только удачные попытки
    private final Map<String, Long> times = new HashMap<>();

    // реальное время — от первого блока level1 до последнего блока текущего уровня
    private final Map<String, Long> realTimes = new HashMap<>();

    // момент старта цепочки (нажатие первого блока level1)
    private Long chainStart = null;

    // момент старта попытки на конкретный уровень
    private final Map<String, Long> attemptStart = new HashMap<>();

    public int getNextIndex() { return nextIndex; }
    public void setNextIndex(int i) { this.nextIndex = i; }

    // ===================== ЦЕПОЧКА =====================

    public void startChainIfNeeded(long now) {
        if (chainStart == null) chainStart = now;
    }

    public boolean isChainStarted() { return chainStart != null; }
    public long getChainStart() { return chainStart == null ? 0 : chainStart; }

    // ===================== ПОПЫТКА =====================

    public void startAttempt(String mazeId, long now) {
        attemptStart.put(mazeId, now);
    }

    public long getAttemptStart(String mazeId) {
        Long t = attemptStart.get(mazeId);
        return t == null ? 0 : t;
    }

    public boolean hasAttempt(String mazeId) {
        return attemptStart.containsKey(mazeId);
    }

    // ===================== ЗАПИСЬ =====================

    public void addTime(String mazeId, long millis) {
        times.put(mazeId, millis);
    }

    public void addRealTime(String mazeId, long millis) {
        realTimes.put(mazeId, millis);
    }

    public long getTime(String mazeId) {
        Long t = times.get(mazeId);
        return t == null ? 0L : t;
    }

    public long getRealTime(String mazeId) {
        Long t = realTimes.get(mazeId);
        return t == null ? 0L : t;
    }

    public long getTotalTime() {
        long sum = 0;
        for (Long t : times.values()) sum += t;
        return sum;
    }

    public long getTotalRealTime() {
        long sum = 0;
        for (Long t : realTimes.values()) sum += t;
        return sum;
    }

    // ===================== СБРОС =====================

    public void reset() {
        nextIndex = 0;
        times.clear();
        realTimes.clear();
        attemptStart.clear();
        chainStart = null;
    }

    /**
     * Сброс только попытки (фейл), статистика удачных уровней сохраняется.
     */
    public void resetAttemptOnly(String mazeId) {
        attemptStart.remove(mazeId);
    }
}