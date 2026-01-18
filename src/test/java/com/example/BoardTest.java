package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe klasy Board.
 */
class BoardTest {

    @Test
    void testBoardInitializationIsEmpty() {
        Board board = new Board(5);

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                assertEquals(Stone.EMPTY, board.get(x, y));
            }
        }
    }

    @Test
    void testInBoundsValidCoordinates() {
        Board board = new Board(3);

        assertTrue(board.inBounds(0, 0));
        assertTrue(board.inBounds(2, 2));
    }

    @Test
    void testInBoundsInvalidCoordinates() {
        Board board = new Board(3);

        assertFalse(board.inBounds(-1, 0));
        assertFalse(board.inBounds(0, -1));
        assertFalse(board.inBounds(3, 0));
        assertFalse(board.inBounds(0, 3));
    }

    @Test
    void testSetAndGetStone() {
        Board board = new Board(3);

        board.set(1, 1, Stone.BLACK);
        assertEquals(Stone.BLACK, board.get(1, 1));

        board.set(1, 1, Stone.WHITE);
        assertEquals(Stone.WHITE, board.get(1, 1));
    }

    @Test
    void testGetSize() {
        Board board = new Board(9);
        assertEquals(9, board.getSize());
    }

    @Test
    void testBoardCopyCreatesIndependentBoard() {
        Board board = new Board(3);
        board.set(0, 0, Stone.BLACK);

        Board copy = board.copy();

        assertEquals(board, copy);

        copy.set(0, 0, Stone.WHITE);
        assertNotEquals(board, copy);
    }

    @Test
    void testBoardEqualitySameBoards() {
        Board b1 = new Board(3);
        Board b2 = new Board(3);

        b1.set(1, 1, Stone.BLACK);
        b2.set(1, 1, Stone.BLACK);

        assertEquals(b1, b2);
    }

    @Test
    void testBoardEqualityDifferentBoards() {
        Board b1 = new Board(3);
        Board b2 = new Board(3);

        b1.set(1, 1, Stone.BLACK);
        b2.set(1, 1, Stone.WHITE);

        assertNotEquals(b1, b2);
    }

    @Test
    void testToStringEmptyBoard() {
        Board board = new Board(2);

        String expected =
                ". . \n" +
                        ". . \n";

        assertEquals(expected, board.toString());
    }

    @Test
    void testToStringWithStones() {
        Board board = new Board(2);
        board.set(0, 0, Stone.BLACK);
        board.set(1, 1, Stone.WHITE);

        String expected =
                "B . \n" +
                        ". W \n";

        assertEquals(expected, board.toString());
    }
}