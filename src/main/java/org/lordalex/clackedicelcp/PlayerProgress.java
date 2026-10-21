package org.lordalex.clackedicelcp;

import java.util.HashMap;
import java.util.Map;

public class PlayerProgress {

    private int nextIndex = 0;

    // чистое время удачной попытки на уровень
    private final Map<String, Long> times = new HashMap<>();

    // общее время, потраченное на уровень (с фейлами)
    private final Map<String, Long> levelTotalTimes = new HashMap<>();

    // момент старта цепочки (нажатие первого блока level1)
    private Long chainStart = null;

    // момент первого шага на текущий уровень
    private final Map<String, Long> attemptStart = new HashMap<>();

    public int getNextIndex() { return nextIndex; }
    public void setNextIndex(int i) { this.nextIndex = i; }

    // ===================== ЦЕПОЧКА =====================

    public void startChainIfNeeded(long now) {
        if (chainStart == null) chainStart = now;
    }

    public long getChainStart() { return chainStart == null ? 0 : chainStart; }

    // ===================== ПОПЫТКА =====================

    public void startAttemptIfNeeded(String mazeId, long now) {
        if (!attemptStart.containsKey(mazeId)) {
            attemptStart.put(mazeId, now);
        }
    }

    public long getAttemptStart(String mazeId) {
        Long t = attemptStart.get(mazeId);
        return t == null ? 0 : t;
    }

    // ===================== ЗАПИСЬ =====================

    public void addTime(String mazeId, long millis) {
        times.put(mazeId, millis);
    }

    public void addLevelTotalTime(String mazeId, long millis) {
        levelTotalTimes.put(mazeId, millis);
    }

    public long getTime(String mazeId) {
        Long t = times.get(mazeId);
        return t == null ? 0L : t;
    }

    public long getLevelTotalTime(String mazeId) {
        Long t = levelTotalTimes.get(mazeId);
        return t == null ? 0L : t;
    }

    public long getTotalTime() {
        long sum = 0;
        for (Long t : times.values()) sum += t;
        return sum;
    }

    // ===================== СБРОС =====================

    public void reset() {
        nextIndex = 0;
        times.clear();
        levelTotalTimes.clear();
        attemptStart.clear();
        chainStart = null;
    }
}