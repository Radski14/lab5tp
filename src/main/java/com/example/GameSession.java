package com.example;

import java.net.*;

public class GameSession {
    private final Board board = new Board(19);
    private final RulesEngine rules = new RulesEngine();
    private ClientHandler black;
    private ClientHandler white;
    private Stone currentTurn = Stone.BLACK;
    private Board previousBoard = null;
    private int blackPrisoners = 0;
    private int whitePrisoners = 0;

    private int consecutivePasses = 0;
    private boolean gameOver = false;

    public boolean isGameOver() {
        return gameOver;
    }

    public void endGame() {
        gameOver = true;
    }


    public void addPrisoner(Stone capturer) {
        if (capturer == Stone.BLACK) blackPrisoners++;
        else whitePrisoners++;
    }

    public GameSession(Socket p1, Socket p2) throws Exception {
        black = new ClientHandler(p1, Stone.BLACK, this);
        white = new ClientHandler(p2, Stone.WHITE, this);
    }




    //    zmiana 1
    public void start() {
        black.start();
        white.start();

        black.sendState(new GameState(
                board.toString(),
                "You are BLACK. Game started. BLACK begins.",
                true
        ));

        white.sendState(new GameState(
                board.toString(),
                "You are WHITE. Game started. BLACK begins.",
                false
        ));
    }

    public synchronized void handleMove(Move move, ClientHandler sender) {
        // ===== RESIGN =====
        if (move.resign) {
            gameOver = true;

            ClientHandler winner = sender.getStone() == Stone.BLACK ? white : black;

            sender.sendState(new GameState(
                    board.toString(),
                    "You resigned. You lose.",
                    false
            ));

            winner.sendState(new GameState(
                    board.toString(),
                    "Opponent resigned. You win.",
                    false
            ));
            return;
        }



        if (gameOver) {
            sender.sendState(new GameState(
                    board.toString(),
                    "Game already ended",
                    false
            ));
            return;
        }

        // sprawdzenie tury
        if (sender.getStone() != currentTurn) {
            sender.sendState(new GameState(
                    board.toString(),
                    "Not your turn",
                    false
            ));
            return;
        }

        // ===== PASS =====
        if (move.pass) {
            consecutivePasses++;

            if (consecutivePasses >= 2) {
                gameOver = true;
                finishGame();
                return;
            }

            currentTurn = currentTurn.opposite();

            sender.sendState(new GameState(
                    board.toString(),
                    "You passed",
                    false
            ));

            ClientHandler other = sender.getStone() == Stone.BLACK ? white : black;
            other.sendState(new GameState(
                    board.toString(),
                    "Opponent passed. Your turn.",
                    true
            ));
            return;
        }

        // reset passów przy normalnym ruchu
        consecutivePasses = 0;

        // normalny ruch
        boolean ok = rules.applyMove(board, move, currentTurn, this);
        if (!ok) {
            sender.sendState(new GameState(
                    board.toString(),
                    "Invalid move",
                    true
            ));
            return;
        }

        currentTurn = currentTurn.opposite();

        sender.sendState(new GameState(
                board.toString(),
                "Move accepted",
                false
        ));

        ClientHandler other = sender.getStone() == Stone.BLACK ? white : black;
        other.sendState(new GameState(
                board.toString(),
                "Your turn",
                true
        ));
    }

    private void finishGame() {
        ScoringEngine engine = new ScoringEngine();
        ScoringResult result = engine.score(
                board,
                blackPrisoners,
                whitePrisoners
        );

        String msg =
                "Game over\n" +
                        "BLACK: " + result.blackScore + "\n" +
                        "WHITE: " + result.whiteScore + "\n" +
                        (result.blackScore > result.whiteScore ? "BLACK wins" : "WHITE wins");

        black.sendState(new GameState(board.toString(), msg, false));
        white.sendState(new GameState(board.toString(), msg, false));
    }

    public Board getPreviousBoard() {
        return previousBoard;
    }

    public void setPreviousBoard(Board b) {
        previousBoard = b;
    }

}