package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    public void setUserInfo() {
        welcomeLabel.setText("Добро пожаловать, " + SessionContext.getCurrentFullName() + "! (Администратор)");
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
    private void handleUsersManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/users-management.fxml"));
            Parent root = loader.load();

            Stage usersStage = new Stage();
            usersStage.setScene(new Scene(root, 700, 500));
            usersStage.setTitle("Управление пользователями");
            usersStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление пользователями");
        }
    }

    @FXML
    private void handleViewTelescopes() {
        try {
            System.out.println("Opening telescopes view...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/commonview/telescopes-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Просмотр телескопов - Администратор");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть просмотр телескопов: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageTelescopes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/telescopes-management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("Управление телескопами");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление телескопами");
        }
    }

    @FXML
    private void handleViewPrograms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/commonview/research-programs-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Просмотр научных программ - Администратор");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть просмотр программ");
        }
    }

    @FXML
    private void handleManagePrograms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/programs-management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1100, 800));
            stage.setTitle("Управление научными программами");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление программами");
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