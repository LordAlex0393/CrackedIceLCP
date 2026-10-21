package org.lordalex.clackedicelcp;

public enum GameMode {
    DEFAULT,  // Сюжет: переходы, ломается стекло
    SINGLE,   // Цикл: прошел уровень - телепорт на его же начало
    TRAINING  // Тренировка: все 3 уровня, телепорт в самый первый
}