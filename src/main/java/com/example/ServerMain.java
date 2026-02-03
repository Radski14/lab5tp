package com.example;

import java.net.*;
import java.util.Scanner;

/**
 * Główna klasa serwera gry Go.
 * Odpowiada za uruchomienie gniazda serwerowego ({@link ServerSocket}),
 * oczekiwanie na połączenia od dwóch klientów i zainicjowanie sesji gry.
 */
public class ServerMain {

    /** Domyślny numer portu, na którym serwer nasłuchuje połączeń. */
    private static final int PORT = 12345;
    /**
     * Punkt wejścia aplikacji serwerowej.
     * Metoda wykonuje następujące kroki:
     * Otwiera gniazdo serwerowe na porcie 12345.
     * Wstrzymuje wykonanie (blokuje wątek) do momentu połączenia się pierwszego gracza.
     * Ponownie blokuje wątek do momentu połączenia się drugiego gracza.
     * Tworzy obiekt {@link GameSession}, który przejmuje dalszą obsługę logiki gry.
     *
     * @param args Argumenty linii komend (nieużywane).
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started, waiting for players on port " + PORT + "...");

            Socket p1 = serverSocket.accept();
            System.out.println("Player 1 connected from: " + p1.getInetAddress());
            Scanner scanner = new Scanner(System.in);
            System.out.println("wybierz opcje:");
            System.out.println("1. bot");
            String input = scanner.nextLine();
            if (input.equals("1")) {
                GameSession session = new GameSession(p1); // Konstruktor z botem
                session.start();
            } else {
                System.out.println("Waiting for Player 2...");
                Socket p2 = serverSocket.accept();
                GameSession session = new GameSession(p1, p2);
                session.start();
            }

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}