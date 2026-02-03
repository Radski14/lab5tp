package com.example;

public interface Player {
    /** Wysyła stan gry do gracza (przez sieć lub do bota) */
    void sendState(GameState state);

    /** Zwraca kolor gracza */
    Stone getStone();

    /** Rozpoczyna działanie gracza */
    void start();
}