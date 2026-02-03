package com.example;

import java.io.*;
import java.net.Socket;

/**
 * Obsługuje połączenie sieciowe z pojedynczym klientem (graczem) w osobnym wątku.
 * Klasa odpowiada za odbieranie ruchów od klienta oraz przesyłanie mu aktualnego stanu gry.
 */
public class ClientHandler extends Thread implements Player {
    /** Strumień wejściowy do odbierania obiektów od klienta. */
    private final ObjectInputStream in;
    /** Strumień wyjściowy do wysyłania obiektów do klienta. */
    private final ObjectOutputStream out;
    /** Kolor kamienia przypisany do tego klienta. */
    private final Stone stone;
    /** Sesja gry, do której przypisany jest ten kontroler. */
    private final GameSession session;

    /**
     * Tworzy nowy obiekt obsługi klienta i inicjalizuje strumienie obiektowe.
     *
     * @param socket Otwarte gniazdo (socket) połączenia z klientem.
     * @param stone Kolor kamienia ({@link Stone}), którym gra ten klient.
     * @param session Referencja do aktywnej sesji gry ({@link GameSession}).
     * @throws IOException Jeśli wystąpi błąd podczas tworzenia strumieni wejścia/wyjścia.
     */
    public ClientHandler(Socket socket, Stone stone, GameSession session) throws IOException {
        this.stone = stone;
        this.session = session;

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Zwraca kolor kamienia przypisany do tego gracza.
     *
     * @return Obiekt {@link Stone} reprezentujący kolor gracza.
     */
    public Stone getStone() {
        return stone;
    }

    /**
     * Wysyła aktualny stan gry do klienta w sposób asynchroniczny względem odbioru danych.
     *
     * @param state Obiekt {@link GameState} zawierający dane o aktualnej sytuacji na planszy.
     */
    public void sendState(GameState state) {
        try {
            out.writeObject(state);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Client disconnected: " + stone);
        }
    }

    /**
     * Główna pętla wątku obsługująca komunikację przychodzącą.
     * Metoda w pętli oczekuje na obiekty typu {@link Move} przesyłane przez klienta.
     * Po odebraniu ruchu, przekazuje go do sesji gry w celu przetworzenia.
     * Pętla kończy się w momencie rozłączenia klienta lub wystąpienia błędu komunikacji.
     */
    @Override
    public void run() {
        try {
            while (true) {
                Move move = (Move) in.readObject();
                session.handleMove(move, this);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + stone);
        }
    }
}