package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.SessionContext;
import com.observatory.observatorysystem.client.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ScientistDashboardController {

    @FXML
    private Label welcomeLabel;

    public void setUserInfo() {
        welcomeLabel.setText("Добро пожаловать, " + SessionContext.getCurrentFullName() + "! (Ученый)");
    }

    @FXML
    private void handleLogout() {
        SessionContext.logout();

        try {
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.close();  // Закрываем дашборд

            StageManager.showAuthMain();  // Показываем обратно исходное окно авторизации

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось вернуться на экран входа");
        }
    }

    @FXML
    private void handleViewTelescopes() {
        try {
            System.out.println("Opening telescopes view as scientist...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientistview/telescopes-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("Доступные телескопы - Ученый");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть список телескопов: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewPrograms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientistview/research-programs-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Научные программы - Ученый");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть список программ");
        }
    }

    @FXML
    private void handleViewMyObservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientistview/my-observations.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1200, 600));
            stage.setTitle("Ваши заявки");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть список Ваших заявок");
        }
    }

    @FXML
    private void handleCreateObservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientistview/create-observation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Создание новой заявки");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть форму для создания новой заявки");
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