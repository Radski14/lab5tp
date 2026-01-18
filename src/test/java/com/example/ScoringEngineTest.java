package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe silnika punktacji gry Go.
 */
class ScoringEngineTest {

    private ScoringEngine engine;
    private Board board;

    @BeforeEach
    void setUp() {
        engine = new ScoringEngine();
        board = new Board(5);
    }

    @Test
    void testEmptyBoardScore() {
        ScoringResult result = engine.score(board, 0, 0, 6.5f);

        assertEquals(0, result.blackScore);
        assertEquals(6.5f, result.whiteScore);
    }

    @Test
    void testSimpleTerritory() {
        board.set(0, 0, Stone.BLACK);
        board.set(0, 1, Stone.BLACK);
        board.set(1, 0, Stone.BLACK);

        ScoringResult result = engine.score(board, 0, 0, 0);

        assertTrue(result.blackScore > 0);
        assertEquals(0, result.whiteScore);
    }
}