package com.example;

import java.net.*;

/**
 * Zarządza jedną sesją gry Go pomiędzy dwoma graczami.
 * Odpowiada za stan gry, tury, punktację i komunikację z klientami.
 */
public class GameSession {

    /** Aktualna plansza gry. */
    private final Board board = new Board(19);

    /** Silnik reguł gry. */
    private final RulesEngine rules = new RulesEngine();

    /** Klient grający czarnymi kamieniami. */
    private Player black;

    /** Klient grający białymi kamieniami. */
    private Player white;

    /** Kamień gracza, którego jest aktualnie tura. */
    private Stone currentTurn = Stone.BLACK;

    /** Poprzedni stan planszy (do reguły Ko). */
    private Board previousBoard = null;

    /** Liczba kolejnych pasów. */
    private int consecutivePasses = 0;

    /** Informacja, czy gra została zakończona. */
    private boolean gameOver = false;

    /** Informacja, czy trwa faza punktacji. */
    private boolean scoringPhase = false;

    /** Liczba jeńców czarnego gracza. */
    private int blackPrisoners = 0;

    /** Liczba jeńców białego gracza. */
    private int whitePrisoners = 0;

    /** Czy czarny zakończył usuwanie kamieni. */
    private boolean blackDone = false;

    /** Czy biały zakończył usuwanie kamieni. */
    private boolean whiteDone = false;

    /**
     * Tworzy nową sesję gry dla dwóch graczy.
     *
     * @param p1 Gniazdo gracza czarnego.
     * @param p2 Gniazdo gracza białego.
     * @throws Exception w przypadku błędu połączenia.
     */
    public GameSession(Socket p1, Socket p2) throws Exception {
        black = new ClientHandler(p1, Stone.BLACK, this);
        white = new ClientHandler(p2, Stone.WHITE, this);
    }

    /** Konstruktor dla Gry: Człowiek vs BOT */
    public GameSession(Socket p1) throws Exception {
        this.black = new ClientHandler(p1, Stone.BLACK, this);
        this.white = new Bot(Stone.WHITE, this);
    }

    /** Uruchamia grę i rozpoczyna wątki klientów. */
    public void start() {
        black.start();
        white.start();
        broadcast("Game started. BLACK begins.", true);
    }

    /**
     * Obsługuje ruch przesłany przez gracza.
     *
     * @param move   Wykonany ruch.
     * @param sender Gracz wykonujący ruch.
     */
    public synchronized void handleMove(Move move, Player sender) {
        if (gameOver) return;

        if (scoringPhase) {
            handleScoringMove(move, sender);
            return;
        }

        if (move.resign) {
            endGameByResignation(sender);
            return;
        }

        if (sender.getStone() != currentTurn) {
            sender.sendState(new GameState(board.toString(), "Not your turn", false));
            return;
        }


        if (move.pass) {
            consecutivePasses++;
            if (consecutivePasses >= 2) {
                startScoringPhase();
                return;
            }
            switchTurn(sender, "You passed", "Opponent passed. Your turn.");
            return;
        }

        consecutivePasses = 0;

        boolean ok = rules.applyMove(board, move, currentTurn, this);

        if (!ok) {
            sender.sendState(new GameState(board.toString(), "Invalid move", true));
            return;
        }

        switchTurn(sender, "Move accepted", "Your turn");
    }

    /** Rozpoczyna fazę punktacji. */
    private void startScoringPhase() {
        scoringPhase = true;
        blackDone = false;
        whiteDone = false;

        String msg = "SCORING PHASE. Click DEAD stones to remove them.\nPress DONE when finished.";
        black.sendState(new GameState(board.toString(), msg, true));
        white.sendState(new GameState(board.toString(), msg, true));
    }

    /**
     * Obsługuje akcje graczy w fazie punktacji.
     *
     * @param move   Ruch punktacyjny.
     * @param sender Gracz wykonujący akcję.
     */
    private void handleScoringMove(Move move, Player sender) {

        if (move.doneScoring) {
            if (sender.getStone() == Stone.BLACK) blackDone = true;
            else whiteDone = true;

            sender.sendState(new GameState(board.toString(), "Waiting for opponent...", false));

            if (blackDone && whiteDone) {
                finishGameAndScore();
            }
            return;
        }

        if (board.inBounds(move.x, move.y)) {
            Stone target = board.get(move.x, move.y);

            if (target != Stone.EMPTY) {
                board.set(move.x, move.y, Stone.EMPTY);

                if (target == Stone.BLACK) whitePrisoners++;
                else blackPrisoners++;

                blackDone = false;
                whiteDone = false;

                String msg = "Stone removed. Keep marking or press DONE.";
                black.sendState(new GameState(board.toString(), msg, true));
                white.sendState(new GameState(board.toString(), msg, true));
            }
        }
    }

    /** Kończy grę i oblicza wynik. */
    private void finishGameAndScore() {
        gameOver = true;
        ScoringEngine engine = new ScoringEngine();
        ScoringResult result = engine.score(board, blackPrisoners, whitePrisoners, 6.5f);

        String msg = String.format(
                "GAME OVER\nBLACK: %.1f | WHITE: %.1f\n%s wins!",
                result.blackScore,
                result.whiteScore,
                result.blackScore > result.whiteScore ? "BLACK" : "WHITE"
        );

        black.sendState(new GameState(board.toString(), msg, false));
        white.sendState(new GameState(board.toString(), msg, false));
    }

    /**
     * Zmienia turę gracza.
     *
     * @param currentSender Gracz wykonujący ruch.
     * @param msgSelf       Komunikat dla niego.
     * @param msgOther      Komunikat dla przeciwnika.
     */
    private void switchTurn(Player currentSender, String msgSelf, String msgOther) {
        currentTurn = currentTurn.opposite();
        Player other = (currentSender.getStone() == Stone.BLACK) ? white : black;

        currentSender.sendState(new GameState(board.toString(), msgSelf, false));
        other.sendState(new GameState(board.toString(), msgOther, true));
    }

    /**
     * Kończy grę przez poddanie się gracza.
     *
     * @param loser Gracz, który się poddał.
     */
    private void endGameByResignation(Player loser) {
        gameOver = true;
        Player winner = (loser.getStone() == Stone.BLACK) ? white : black;
        loser.sendState(new GameState(board.toString(), "You resigned. You lose.", false));
        winner.sendState(new GameState(board.toString(), "Opponent resigned. You win.", false));
    }

    /**
     * Wysyła ten sam stan gry do obu graczy.
     *
     * @param msg       Treść komunikatu.
     * @param blackTurn Czy czarny ma turę.
     */
    private void broadcast(String msg, boolean blackTurn) {
        black.sendState(new GameState(board.toString(), msg, blackTurn));
        white.sendState(new GameState(board.toString(), msg, !blackTurn));
    }

    /**
     * Dodaje jeńca do odpowiedniego gracza.
     *
     * @param capturer Gracz, który zdobył kamień.
     */
    public void addPrisoner(Stone capturer) {
        if (capturer == Stone.BLACK) blackPrisoners++;
        else whitePrisoners++;
    }

    /** Zwraca poprzedni stan planszy. */
    public Board getPreviousBoard() {
        return previousBoard;
    }

    /** Ustawia poprzedni stan planszy. */
    public void setPreviousBoard(Board b) {
        previousBoard = b;
    }

    /**
     * Tworzy lekką kopię sesji wyłącznie do symulacji ruchów (BOT).
     * Nie zawiera graczy ani komunikacji.
     */
    public GameSession copyForSimulation() {
        GameSession sim = new GameSession();

        sim.previousBoard =
                this.previousBoard == null ? null : this.previousBoard.copy();

        return sim;
    }

    private GameSession() {}
}