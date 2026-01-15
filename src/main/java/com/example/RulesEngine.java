package com.example;

import java.util.*;

public class RulesEngine {

    public boolean applyMove(Board board, Move move, Stone stone, GameSession session) {
        int x = move.x;
        int y = move.y;

        if (!board.inBounds(x, y)) return false;
        if (board.get(x, y) != Stone.EMPTY) return false;

        // zapamiętaj planszę PRZED ruchem
        Board beforeMove = board.copy();

        board.set(x, y, stone);

        int captured = 0;

        // bicie łańcuchów przeciwnika
        for (int[] n : neighbors(x, y)) {
            int nx = n[0], ny = n[1];
            if (!board.inBounds(nx, ny)) continue;

            if (board.get(nx, ny) == stone.opposite()) {
                Set<Point> chain = collectChain(board, nx, ny);
                if (!hasLiberty(board, chain)) {
                    removeChain(board, chain);
                    captured += chain.size();
                }
            }
        }

        // samobójstwo (jeśli nic nie zbito)
        Set<Point> myChain = collectChain(board, x, y);
        if (!hasLiberty(board, myChain) && captured == 0) {
            restoreBoard(board, beforeMove);
            return false;
        }

        // ===== REGUŁA KO =====
        Board prev = session.getPreviousBoard();
        if (prev != null && board.equals(prev)) {
            restoreBoard(board, beforeMove);
            return false;
        }

        // zapamiętaj pozycję sprzed ruchu (do KO w następnym ruchu)
        session.setPreviousBoard(beforeMove);

        // dodaj jeńców
        for (int i = 0; i < captured; i++) {
            session.addPrisoner(stone);
        }

        return true;
    }


    /* ===================== LOGIKA ===================== */

    private Set<Point> collectChain(Board board, int x, int y) {
        Stone color = board.get(x, y);
        Set<Point> chain = new HashSet<>();
        Queue<Point> q = new LinkedList<>();

        Point start = new Point(x, y);
        chain.add(start);
        q.add(start);

        while (!q.isEmpty()) {
            Point p = q.poll();
            for (int[] n : neighbors(p.x, p.y)) {
                int nx = n[0], ny = n[1];
                Point np = new Point(nx, ny);

                if (board.inBounds(nx, ny)
                        && board.get(nx, ny) == color
                        && !chain.contains(np)) {
                    chain.add(np);
                    q.add(np);
                }
            }
        }
        return chain;
    }

    private boolean hasLiberty(Board board, Set<Point> chain) {
        for (Point p : chain) {
            for (int[] n : neighbors(p.x, p.y)) {
                int nx = n[0], ny = n[1];
                if (board.inBounds(nx, ny)
                        && board.get(nx, ny) == Stone.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeChain(Board board, Set<Point> chain) {
        for (Point p : chain) {
            board.set(p.x, p.y, Stone.EMPTY);
        }
    }

    private List<int[]> neighbors(int x, int y) {
        List<int[]> list = new ArrayList<>();
        list.add(new int[]{x + 1, y});
        list.add(new int[]{x - 1, y});
        list.add(new int[]{x, y + 1});
        list.add(new int[]{x, y - 1});
        return list;
    }

    private void restoreBoard(Board board, Board snapshot) {
        for (int x = 0; x < board.getSize(); x++)
            for (int y = 0; y < board.getSize(); y++)
                board.set(x, y, snapshot.get(x, y));
    }


    /* ===================== POINT ===================== */

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        @Override public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}