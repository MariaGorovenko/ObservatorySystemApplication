package com.observatory.observatorysystem.client;

import javafx.stage.Stage;

public class StageManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // Опционально: метод для показа auth-main
    public static void showAuthMain() {
        if (primaryStage != null) {
            primaryStage.show();
        }
    }

    public static void hideAuthMain() {
        if (primaryStage != null) {
            primaryStage.hide(); // или close(), если хотите полностью закрыть
        }
    }
}