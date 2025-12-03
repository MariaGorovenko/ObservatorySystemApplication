package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.SessionContext;
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth-main.fxml"));
            Parent root = loader.load();

            Stage authStage = new Stage();
            authStage.setScene(new Scene(root, 500, 400));
            authStage.setTitle("Система астрономической обсерватории");
            authStage.setResizable(false);
            authStage.show();

            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewTelescopes() {
        try {
            System.out.println("Opening telescopes view as scientist...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/commonview/telescopes-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/commonview/research-programs-view.fxml"));
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}