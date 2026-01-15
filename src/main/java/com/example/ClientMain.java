package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMain extends Application {

    private static final int SIZE = 19;
    private static final double CELL = 32;
    private static final double MARGIN = 30;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private boolean yourTurn = false;
    private boolean gameOver = false;

    private Pane stoneLayer = new Pane();
    private Label status = new Label("Connecting...");

    private Button passBtn = new Button("PASS");
    private Button resignBtn = new Button("RESIGN");

    @Override
    public void start(Stage stage) throws Exception {

        Socket socket = new Socket("localhost", 12345);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        double sizePx = MARGIN * 2 + CELL * (SIZE - 1);

        Canvas boardCanvas = new Canvas(sizePx, sizePx);
        drawBoard(boardCanvas.getGraphicsContext2D());

        stoneLayer.setPrefSize(sizePx, sizePx);
        stoneLayer.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));

        StackPane board = new StackPane(boardCanvas, stoneLayer);

        passBtn.setOnAction(e -> sendMove(Move.pass()));
        resignBtn.setOnAction(e -> sendMove(Move.resign()));

        HBox controls = new HBox(10, passBtn, resignBtn);
        controls.setAlignment(javafx.geometry.Pos.CENTER);

        VBox root = new VBox(10, board, controls, status);
        root.setAlignment(javafx.geometry.Pos.CENTER);

        stage.setScene(new Scene(root));
        stage.setHeight(850);
        stage.setTitle("GO");
        stage.show();

        startReceiver();
    }

    /* ===================== BOARD DRAWING ===================== */

    private void drawBoard(GraphicsContext g) {
        g.setFill(Color.web("#DEB887"));
        g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());

        g.setStroke(Color.BLACK);

        for (int i = 0; i < SIZE; i++) {
            double p = MARGIN + i * CELL;
            g.strokeLine(MARGIN, p, MARGIN + CELL * (SIZE - 1), p);
            g.strokeLine(p, MARGIN, p, MARGIN + CELL * (SIZE - 1));
        }

    }

    /* ===================== INPUT ===================== */

    private void handleClick(double mx, double my) {
        if (!yourTurn || gameOver) return;

        int x = (int) Math.round((mx - MARGIN) / CELL);
        int y = (int) Math.round((my - MARGIN) / CELL);

        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return;

        sendMove(new Move(x, y));
    }

    /* ===================== NETWORK ===================== */

    private void sendMove(Move m) {
        try {
            out.writeObject(m);
            out.flush();
        } catch (Exception e) {
            status.setText("Connection error");
        }
    }

    private void startReceiver() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    GameState s = (GameState) in.readObject();
                    Platform.runLater(() -> updateUI(s));
                }
            } catch (Exception e) {
                Platform.runLater(() -> status.setText("Disconnected"));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /* ===================== UPDATE ===================== */

    private void updateUI(GameState state) {
        yourTurn = state.yourTurn;
        status.setText(state.message);

        if (state.message.contains("Game over")
                || state.message.contains("win")
                || state.message.contains("lose")
                || state.message.contains("resigned")) {
            gameOver = true;
        }

        passBtn.setDisable(!yourTurn || gameOver);
        resignBtn.setDisable(gameOver);

        redrawStones(state.board);
    }

    private void redrawStones(String board) {
        stoneLayer.getChildren().clear();
        String[] rows = board.split("\n");

        for (int y = 0; y < SIZE; y++) {
            String[] cells = rows[y].trim().split(" ");
            for (int x = 0; x < SIZE; x++) {
                if (cells[x].equals("B"))
                    stoneLayer.getChildren().add(stone(x, y, Color.BLACK));
                else if (cells[x].equals("W"))
                    stoneLayer.getChildren().add(stone(x, y, Color.WHITE));
            }
        }
    }

    private Circle stone(int x, int y, Color c) {
        Circle s = new Circle(
                MARGIN + x * CELL,
                MARGIN + y * CELL,
                CELL * 0.45
        );
        s.setFill(c);
        s.setStroke(Color.BLACK);
        return s;
    }

    public static void main(String[] args) {
        launch();
    }
}
