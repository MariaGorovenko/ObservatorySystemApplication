package com.observatory.observatorysystem.client;

import com.observatory.observatorysystem.client.controller.AuthMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth-main.fxml"));
        Parent root = loader.load();

        AuthMainController controller = loader.getController();
        controller.setStage(primaryStage); // Передаем главный stage

        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setTitle("Система астрономической обсерватории");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}