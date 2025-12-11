package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthMainController {

    private Stage currentStage;

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root, 900, 800));
            loginStage.setTitle("Вход в систему");
            loginStage.setResizable(true);
            loginStage.show();

            StageManager.hideAuthMain();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setScene(new Scene(root, 900, 800));
            registerStage.setTitle("Регистрация");
            registerStage.setResizable(true);
            registerStage.show();

            StageManager.hideAuthMain();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}