package com.example;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class VBoxWithStatus extends VBox {

    public VBoxWithStatus(javafx.scene.Node board, Label status) {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        getChildren().addAll(board, status);
    }
}

