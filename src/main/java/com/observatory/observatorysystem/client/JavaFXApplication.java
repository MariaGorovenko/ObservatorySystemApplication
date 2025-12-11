package com.observatory.observatorysystem.client;

import com.observatory.observatorysystem.client.controller.AuthMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class JavaFXApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        StageManager.setPrimaryStage(primaryStage);  // ← Сохраняем

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth-main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Загрузка CSS (добавьте в начало пути / если нужно)
        try {
            URL cssUrl = getClass().getResource("/static/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS успешно загружен");
            } else {
                System.err.println("CSS файл не найден!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AuthMainController controller = loader.getController();
        controller.setStage(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Система астрономической обсерватории");
        primaryStage.setWidth(900);
        primaryStage.setHeight(800);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}