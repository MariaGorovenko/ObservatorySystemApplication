package com.observatory.observatorysystem.client.controller;

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
            loginStage.setScene(new Scene(root, 600, 400));
            loginStage.setTitle("Вход в систему");
            loginStage.setResizable(false);
            loginStage.show();

            // Закрываем текущее окно авторизации
            if (currentStage != null) {
                currentStage.close();
            }
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
            registerStage.setScene(new Scene(root, 600, 500));
            registerStage.setTitle("Регистрация");
            registerStage.setResizable(false);
            registerStage.show();

            // Закрываем текущее окно авторизации
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}