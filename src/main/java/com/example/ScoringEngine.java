package com.example;

import java.util.*;

public class ScoringEngine {

    public ScoringResult score(Board board,
                               int blackPrisoners,
                               int whitePrisoners) {

        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        int blackTerritory = 0;
        int whiteTerritory = 0;

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {

                if (board.get(x, y) == Stone.EMPTY && !visited[x][y]) {
                    Territory t = floodTerritory(board, x, y, visited);

                    if (t.owner == Stone.BLACK)
                        blackTerritory += t.size;
                    else if (t.owner == Stone.WHITE)
                        whiteTerritory += t.size;
                }
            }
        }

        int blackScore = blackTerritory + blackPrisoners;
        int whiteScore = whiteTerritory + whitePrisoners;

        return new ScoringResult(blackScore, whiteScore);
    }

    private Territory floodTerritory(Board board,
                                     int sx, int sy,
                                     boolean[][] visited) {

        Set<Point> points = new HashSet<>();
        Set<Stone> borderingColors = new HashSet<>();

        Queue<Point> q = new LinkedList<>();
        q.add(new Point(sx, sy));
        visited[sx][sy] = true;

        while (!q.isEmpty()) {
            Point p = q.poll();
            points.add(p);

            for (int[] n : neighbors(p.x, p.y)) {
                int nx = n[0], ny = n[1];
                if (!board.inBounds(nx, ny)) continue;

                Stone s = board.get(nx, ny);
                if (s == Stone.EMPTY && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    q.add(new Point(nx, ny));
                } else if (s != Stone.EMPTY) {
                    borderingColors.add(s);
                }
            }
        }

        // SEKI lub neutral
        if (borderingColors.size() != 1)
            return new Territory(Stone.EMPTY, 0);

        Stone owner = borderingColors.iterator().next();

        // sprawdź czy kamienie graniczne są żywe
        if (touchesNeutral(board, points, owner))
            return new Territory(Stone.EMPTY, 0);

        return new Territory(owner, points.size());
    }


    private boolean touchesNeutral(Board board,
                                   Set<Point> territory,
                                   Stone owner) {

        for (Point p : territory) {
            for (int[] n : neighbors(p.x, p.y)) {
                int nx = n[0], ny = n[1];
                if (!board.inBounds(nx, ny)) continue;

                if (board.get(nx, ny) == owner.opposite()) {
                    return true; // seki
                }
            }
        }
        return false;
    }

    private static class Territory {
        Stone owner;
        int size;

        Territory(Stone owner, int size) {
            this.owner = owner;
            this.size = size;
        }
    }

    private List<int[]> neighbors(int x, int y) {
        List<int[]> list = new ArrayList<>();
        list.add(new int[]{x + 1, y});
        list.add(new int[]{x - 1, y});
        list.add(new int[]{x, y + 1});
        list.add(new int[]{x, y - 1});
        return  list;
    }
}