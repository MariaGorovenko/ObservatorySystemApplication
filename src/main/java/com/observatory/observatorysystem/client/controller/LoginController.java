package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.StageManager;
import com.observatory.observatorysystem.client.service.ApiService;
import com.observatory.observatorysystem.client.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            StageManager.showAuthMain();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось вернуться на главный экран");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните все поля");
            return;
        }

        try {
            // 1. Логин запрос
            String loginJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String loginResponse = apiService.post("/auth/login", loginJson);

            if (loginResponse == null || loginResponse.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверное имя пользователя или пароль");
                return;
            }

            // 2. Парсим ответ от сервера
            Map<String, Object> loginData = objectMapper.readValue(loginResponse, Map.class);

            // 3. Проверяем успешность логина
            boolean success = loginData.containsKey("success") &&
                    (Boolean.TRUE.equals(loginData.get("success")) ||
                            "true".equals(loginData.get("success").toString()));

            if (!success) {
                String message = loginData.containsKey("message") ?
                        (String) loginData.get("message") : "Неверное имя пользователя или пароль";
                showAlert(Alert.AlertType.ERROR, "Ошибка", message);
                return;
            }

            // 4. Получаем данные ИЗ ОТВЕТА ЛОГИНА (не делаем лишних запросов!)
            String role = loginData.get("role") != null ? (String) loginData.get("role") : "SCIENTIST";
            String fullName = loginData.get("fullName") != null ? (String) loginData.get("fullName") : "";

            // 5. Получаем userId ИЗ ОТВЕТА ЛОГИНА
            Long userId = null;
            if (loginData.containsKey("userId")) {
                Object userIdObj = loginData.get("userId");
                if (userIdObj != null) {
                    userId = Long.valueOf(userIdObj.toString());
                }
            }

            // 6. Если userId нет в ответе, только тогда получаем его через API
            if (userId == null) {
                userId = getUserIdByUsername(username);
            }

            if (userId == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить ID пользователя");
                return;
            }

            // 7. Сохраняем в SessionContext
            SessionContext.login(username, role, fullName, userId);

            System.out.println("Логин успешен: " + username + ", ID: " + userId + ", роль: " + role);

            Stage currentLoginStage = (Stage) usernameField.getScene().getWindow();
            currentLoginStage.close();

            StageManager.hideAuthMain();

            // 8. Переходим на соответствующую панель
            navigateToDashboard(role);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка подключения: " + e.getMessage());
        }
    }

    // Упрощенный метод получения ID (используется только если нет в ответе логина)
    private Long getUserIdByUsername(String username) {
        try {
            String response = apiService.get("/users"); // БЕЗ /api!
            if (response != null && !response.trim().isEmpty()) {
                List<Map<String, Object>> users = objectMapper.readValue(
                        response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );

                for (Map<String, Object> user : users) {
                    String userUsername = user.get("username") != null ? (String) user.get("username") : "";
                    if (userUsername.equals(username)) {
                        return user.get("id") != null ? Long.valueOf(user.get("id").toString()) : null;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении ID пользователя: " + e.getMessage());
        }
        return null;
    }

    private void navigateToDashboard(String role) {
        try {

            if ("ADMIN".equals(role)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin-dashboard.fxml"));
                Parent root = loader.load();

                AdminDashboardController controller = loader.getController();
                controller.setUserInfo();

                Stage adminStage = new Stage();
                adminStage.setScene(new Scene(root, 800, 600));
                adminStage.setTitle("Панель администратора");
                adminStage.show();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientist-dashboard.fxml"));
                Parent root = loader.load();

                ScientistDashboardController controller = loader.getController();
                controller.setUserInfo();

                Stage scientistStage = new Stage();
                scientistStage.setScene(new Scene(root, 800, 600));
                scientistStage.setTitle("Панель ученого");
                scientistStage.show();
            }

            StageManager.hideAuthMain();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить панель управления");
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