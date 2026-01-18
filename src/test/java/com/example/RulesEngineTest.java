package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe silnika reguł gry Go.
 */
class RulesEngineTest {

    private RulesEngine rules;
    private Board board;
    private GameSession session;

    @BeforeEach
    void setUp() {
        rules = new RulesEngine();
        board = new Board(9);

        session = mock(GameSession.class);
        when(session.getPreviousBoard()).thenReturn(null);
        doNothing().when(session).addPrisoner(any());
        doNothing().when(session).setPreviousBoard(any());
    }

    @Test
    void testValidMove() {
        Move move = new Move(4, 4, false, false, false);

        boolean result = rules.applyMove(board, move, Stone.BLACK, session);

        assertTrue(result);
        assertEquals(Stone.BLACK, board.get(4, 4));
    }

    @Test
    void testMoveOutOfBounds() {
        Move move = new Move(-1, 0, false, false, false);

        boolean result = rules.applyMove(board, move, Stone.BLACK, session);

        assertFalse(result);
    }

    @Test
    void testMoveOnOccupiedField() {
        board.set(2, 2, Stone.BLACK);
        Move move = new Move(2, 2, false, false, false);

        boolean result = rules.applyMove(board, move, Stone.WHITE, session);

        assertFalse(result);
    }

    @Test
    void testCaptureStone() {

        board.set(1, 0, Stone.BLACK);
        board.set(0, 1, Stone.BLACK);
        board.set(1, 2, Stone.BLACK);
        board.set(1, 1, Stone.WHITE);

        Move move = new Move(2, 1, false, false, false);
        boolean result = rules.applyMove(board, move, Stone.BLACK, session);

        assertTrue(result);
        assertEquals(Stone.EMPTY, board.get(1, 1));
        assertEquals(Stone.BLACK, board.get(2, 1));
    }

    @Test
    void testSuicideMove() {

        board.set(1, 0, Stone.WHITE);
        board.set(0, 1, Stone.WHITE);
        board.set(2, 1, Stone.WHITE);
        board.set(1, 2, Stone.WHITE);

        Move move = new Move(1, 1, false, false, false);
        boolean result = rules.applyMove(board, move, Stone.BLACK, session);

        assertFalse(result);
        assertEquals(Stone.EMPTY, board.get(1, 1));
    }
}