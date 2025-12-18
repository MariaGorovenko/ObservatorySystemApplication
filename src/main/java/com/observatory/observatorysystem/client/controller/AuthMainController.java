package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
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
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось перейти в форму аутентификации");
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
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось перейти в форму регистрации");
        }
    }

    @FXML
    private void handleAboutAuthor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/about-author.fxml"));  // укажи правильный путь
            Parent root = loader.load();

            Stage aboutStage = new Stage();
            aboutStage.setScene(new Scene(root, 600, 500));
            aboutStage.setTitle("Об авторе");
            aboutStage.setResizable(false);
            aboutStage.initModality(Modality.APPLICATION_MODAL);  // модальное окно
            aboutStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно 'Об авторе'");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}