package org.lordalex.clackedicelcp.Platforms;

public interface Platform {
    void start();
    void stop();
    void reset(); // Возвращает платформу в изначальное положение (pos1)
}