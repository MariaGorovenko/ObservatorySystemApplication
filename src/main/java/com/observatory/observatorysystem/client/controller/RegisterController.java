package com.observatory.observatorysystem.client.controller;

import com.observatory.observatorysystem.client.SessionContext;
import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Map;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Button registerButton;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth-main.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setScene(new Scene(root, 500, 400));
            mainStage.setTitle("Система астрономической обсерватории");
            mainStage.setResizable(false);
            mainStage.show();

            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось вернуться на главный экран");
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        // Валидация
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните обязательные поля (логин, пароль, ФИО)");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароли не совпадают");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароль должен содержать минимум 6 символов");
            return;
        }

        try {
            // Создаем JSON для регистрации
            String userJson = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"SCIENTIST\",\"fullName\":\"%s\",\"email\":\"%s\"}",
                    username, password, fullName, email.isEmpty() ? "" : email
            );

            System.out.println("Отправляем запрос на регистрацию: " + userJson);

            // Используем ApiService для регистрации
            String response = apiService.post("/auth/register", userJson);

            System.out.println("Ответ от сервера при регистрации: " + response);

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Нет ответа от сервера");
                return;
            }

            // Парсим JSON ответ от регистрации
            Map<String, Object> registerResponse = objectMapper.readValue(response, Map.class);

            // Проверяем успешность регистрации
            boolean success = registerResponse.containsKey("success") &&
                    (Boolean.TRUE.equals(registerResponse.get("success")) ||
                            "true".equals(registerResponse.get("success").toString()));

            if (!success) {
                String message = registerResponse.containsKey("message") ?
                        (String) registerResponse.get("message") : "Неизвестная ошибка регистрации";
                showAlert(Alert.AlertType.ERROR, "Ошибка регистрации", message);
                return;
            }

            // УСПЕШНАЯ РЕГИСТРАЦИЯ!

            // 1. Получаем userId ИЗ ОТВЕТА РЕГИСТРАЦИИ (не делаем запрос /users!)
            Long userId = null;
            if (registerResponse.containsKey("userId")) {
                Object userIdObj = registerResponse.get("userId");
                if (userIdObj != null) {
                    userId = Long.valueOf(userIdObj.toString());
                    System.out.println("Получен userId из ответа регистрации: " + userId);
                }
            }

            // 2. Если userId нет в ответе, пробуем авторизоваться и получить его из ответа логина
            if (userId == null) {
                System.out.println("userId нет в ответе регистрации, пробуем авторизоваться...");
                String loginJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                String loginResponse = apiService.post("/auth/login", loginJson);

                if (loginResponse != null && !loginResponse.trim().isEmpty()) {
                    Map<String, Object> loginData = objectMapper.readValue(loginResponse, Map.class);
                    if (loginData.containsKey("userId")) {
                        Object userIdObj = loginData.get("userId");
                        if (userIdObj != null) {
                            userId = Long.valueOf(userIdObj.toString());
                            System.out.println("Получен userId из ответа логина: " + userId);
                        }
                    }
                }
            }

            if (userId == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Регистрация прошла успешно, но не удалось получить ID пользователя.\n" +
                                "Пожалуйста, войдите в систему вручную.");
                handleLogin(); // Переходим на форму входа
                return;
            }

            // 3. Сохраняем в SessionContext
            SessionContext.login(username, "SCIENTIST", fullName, userId);

            System.out.println("Регистрация и вход успешны: " + username + ", ID: " + userId);

            // 4. Показываем успешное сообщение и переходим на дашборд
            showAlert(Alert.AlertType.INFORMATION, "Успех",
                    "Регистрация и вход прошли успешно!\n" +
                            "Добро пожаловать, " + fullName + "!");

            // 5. Переходим на панель ученого
            openScientistDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private void openScientistDashboard() {
        try {
            Stage currentStage = (Stage) registerButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/scientist-dashboard.fxml"));
            Parent root = loader.load();

            ScientistDashboardController controller = loader.getController();
            controller.setUserInfo();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root, 800, 600));
            newStage.setTitle("Система обсерватории - Ученый");
            newStage.show();

            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть панель управления");
        }
    }

    @FXML
    private void handleLogin() {
        try {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root, 500, 450));
            loginStage.setTitle("Вход в систему");
            loginStage.setResizable(false);
            loginStage.show();

            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
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