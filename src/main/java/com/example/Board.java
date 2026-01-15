package com.example;

public class Board {
    private final Stone[][] grid;
    private final int size;

    public Board(int size) {
        this.size = size;
        grid = new Stone[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = Stone.EMPTY;
    }


    //czy współrzędne są w planszy
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    public Stone get(int x, int y) {
        return grid[x][y];
    }

    public void set(int x, int y, Stone s) {
        grid[x][y] = s;
    }

    //zwróć graficzną reprezentacje planszy
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                sb.append(grid[x][y] == Stone.BLACK ? 'B' :
                        grid[x][y] == Stone.WHITE ? 'W' : '.');
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public int getSize() {
        return size;
    }


    public Board copy() {
        Board b = new Board(size);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                b.grid[x][y] = this.grid[x][y];
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board)) return false;
        Board other = (Board) o;
        if (this.size != other.size) return false;

        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                if (this.grid[x][y] != other.grid[x][y])
                    return false;

        return true;
    }

}