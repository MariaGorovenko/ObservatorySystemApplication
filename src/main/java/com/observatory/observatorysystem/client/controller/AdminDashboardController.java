package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.SessionContext;
import com.observatory.observatorysystem.client.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
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
            currentStage.close();  // Закрываем дашборд

            StageManager.showAuthMain();  // Показываем обратно исходное окно авторизации

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось вернуться на экран входа");
        }
    }

    @FXML
    private void handleUsersManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/users-management.fxml"));
            Parent root = loader.load();

            Stage usersStage = new Stage();
            usersStage.setScene(new Scene(root, 900, 700));
            usersStage.setTitle("Управление пользователями");
            usersStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление пользователями");
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
    private void handleManagePrograms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/programs-management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Управление научными программами");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление программами");
        }
    }

    @FXML
    private void handleManageAllObservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/all-observations-management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1300, 800));
            stage.setTitle("Управление заявками");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть управление заявками");
        }
    }

    @FXML
    private void handleViewStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminview/statistics.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1100, 800));
            stage.setTitle("Просмотр статистики по заявкам");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть просмотр программ");
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