package com.example;

import java.io.Serializable;

public class Move implements Serializable {
    public final int x;
    public final int y;
    public final boolean pass;
    public final boolean resign;

    // zwykły ruch
    public Move(int x, int y) {
        this.x = x;
        this.y = y;
        this.pass = false;
        this.resign = false;
    }

    // PASS
    public static Move pass() {
        return new Move(-1, -1, true, false);
    }

    // RESIGN
    public static Move resign() {
        return new Move(-1, -1, false, true);
    }

    private Move(int x, int y, boolean pass, boolean resign) {
        this.x = x;
        this.y = y;
        this.pass = pass;
        this.resign = resign;
    }
}