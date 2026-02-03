package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reprezentuje gracza komputerowego (Bota) działającego po stronie serwera.
 *
 * Bot implementuje interfejs {@link Player} i podejmuje decyzje na podstawie
 * analizy stanu planszy. Strategia bota opiera się na priorytetyzacji ruchów:
 *
 * Atak (Capture): Jeśli ruch zbija kamienie przeciwnika, jest wybierany w pierwszej kolejności.
 * Presja (Pressure): Jeśli bicie nie jest możliwe, bot stara się otaczać przeciwnika (stawiać kamienie obok niego).
 * Losowy legalny (Random Legal): W przeciwnym razie wybiera losowy dozwolony ruch.
 * Pass: Jeśli brak legalnych ruchów, bot pasuje.
 *
 */
public class Bot implements Player {

    private final Stone stone;
    private final GameSession session;
    // Silnik zasad używany do symulacji legalności ruchów
    private final RulesEngine rules = new RulesEngine();
    private final Random random = new Random();

    /**
     * Tworzy nową instancję bota.
     *
     * @param stone   Kolor kamieni bota (BLACK lub WHITE).
     * @param session Sesja gry, do której bot należy (umożliwia wysyłanie ruchów).
     */
    public Bot(Stone stone, GameSession session) {
        this.stone = stone;
        this.session = session;
    }

    @Override
    public Stone getStone() {
        return stone;
    }

    @Override
    public void start() {
        System.out.println("Bot " + stone + " ready.");
    }

    /**
     * Odbiera aktualny stan gry od serwera.
     * Jeśli jest tura bota, uruchamia proces myślowy w osobnym wątku,
     * aby nie blokować głównego wątku serwera.
     *
     * @param state Obiekt zawierający reprezentację planszy i komunikaty gry.
     */
    @Override
    public void sendState(GameState state) {
        // Reaguj tylko, jeśli to nasza tura i gra się nie skończyła
        if (state.yourTurn && !state.message.contains("GAME OVER")) {
            new Thread(() -> thinkAndMove(state.board)).start();
        }
    }

    /**
     * Główna logika decyzyjna bota.
     * Analizuje planszę, symuluje wszystkie możliwe ruchy i wybiera najlepszy.
     *
     * @param boardStr Reprezentacja planszy w postaci ciągu znaków.
     */
    private void thinkAndMove(String boardStr) {
        sleep(); // Symulacja czasu "namysłu" (dla lepszego UX)

        Board board = parseBoard(boardStr);
        int size = board.getSize();

        // Listy przechowujące potencjalne ruchy w zależności od ich jakości
        List<Move> capture = new ArrayList<>();  // Ruchy zbijające
        List<Move> pressure = new ArrayList<>(); // Ruchy otaczające
        List<Move> legal = new ArrayList<>();    // Wszystkie inne legalne ruchy

        // Iteracja przez każde pole planszy
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                // Pomiń zajęte pola
                if (board.get(x, y) != Stone.EMPTY) continue;

                Move move = new Move(x, y, false, false, false);

                // Tworzymy kopię planszy i sesji do bezpiecznej symulacji
                // (RulesEngine modyfikuje przekazaną planszę)
                Board simulation = board.copy();
                GameSession simSession = session.copyForSimulation();

                // Sprawdź czy ruch jest legalny (wg zasad Ko, samobójstwa itp.)
                if (!rules.applyMove(simulation, move, stone, simSession))
                    continue;

                // Ruch jest legalny - dodajemy do bazy
                legal.add(move);

                // Klasyfikacja taktyczna ruchu
                if (isCapture(board, simulation)) {
                    capture.add(move); // Priorytet 1: Zbijanie
                } else if (isAdjacentToOpponent(board, x, y)) {
                    pressure.add(move); // Priorytet 2: Otaczanie
                }
            }
        }

        // Wybór ostatecznego ruchu na podstawie priorytetów
        Move chosen = chooseMove(capture, pressure, legal);

        // Wykonanie ruchu w prawdziwej sesji gry
        session.handleMove(chosen, this);
    }

    /**
     * Wybiera ruch z dostępnych list zgodnie z hierarchią ważności.
     */
    private Move chooseMove(List<Move> capture, List<Move> pressure, List<Move> legal) {
        if (!capture.isEmpty()) return randomFrom(capture);
        if (!pressure.isEmpty()) return randomFrom(pressure);
        if (!legal.isEmpty()) return randomFrom(legal);

        // Brak legalnych ruchów -> PASS
        return new Move(-1, -1, true, false, false);
    }

    /**
     * Sprawdza, czy wykonanie ruchu spowodowało zbicie kamieni przeciwnika.
     * Porównuje planszę przed i po symulacji ruchu.
     *
     * @param before Plansza przed ruchem.
     * @param after  Plansza po symulacji ruchu.
     * @return true, jeśli liczba kamieni na planszy zmalała (ktoś został zbity).
     */
    private boolean isCapture(Board before, Board after) {
        int removed = 0;
        for (int x = 0; x < before.getSize(); x++)
            for (int y = 0; y < before.getSize(); y++)
                // Jeśli wcześniej był kamień, a teraz jest pusto -> został zbity
                if (before.get(x, y) != Stone.EMPTY && after.get(x, y) == Stone.EMPTY)
                    removed++;
        return removed > 0;
    }

    /**
     * Sprawdza, czy dane pole sąsiaduje bezpośrednio z kamieniem przeciwnika.
     * Służy do strategii "pressure" (wywierania presji/otaczania).
     */
    private boolean isAdjacentToOpponent(Board board, int x, int y) {
        for (int[] n : neighbors(x, y)) {
            if (board.inBounds(n[0], n[1])
                    && board.get(n[0], n[1]) == stone.opposite()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zwraca współrzędne 4 sąsiadów (góra, dół, lewo, prawo).
     */
    private List<int[]> neighbors(int x, int y) {
        return List.of(
                new int[]{x + 1, y},
                new int[]{x - 1, y},
                new int[]{x, y + 1},
                new int[]{x, y - 1}
        );
    }

    private Move randomFrom(List<Move> moves) {
        return moves.get(random.nextInt(moves.size()));
    }

    private void sleep() {
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    /**
     * Konwertuje tekstową reprezentację planszy (z GameState) na obiekt Board.
     */
    private Board parseBoard(String boardStr) {
        String[] rows = boardStr.split("\n");
        Board b = new Board(rows.length);

        for (int y = 0; y < rows.length; y++) {
            String[] cells = rows[y].split(" ");
            for (int x = 0; x < cells.length; x++) {
                if (cells[x].equals("B")) b.set(x, y, Stone.BLACK);
                else if (cells[x].equals("W")) b.set(x, y, Stone.WHITE);
            }
        }
        return b;
    }
}