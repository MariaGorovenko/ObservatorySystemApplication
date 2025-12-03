package com.observatory.observatorysystem.client.controller.admindashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.Map;

public class UsersManagementController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<User> usersData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.setItems(usersData);

        loadUsers();
    }

    private void loadUsers() {
        try {
            String response = apiService.get("/users");
            System.out.println("Ответ от /users: " + response);

            // Проверка на пустой ответ (403 ошибка)
            if (response == null || response.trim().isEmpty()) {
                System.out.println("Пустой ответ - вероятно 403 Forbidden (нет прав доступа)");
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                        "У вас недостаточно прав для просмотра пользователей. Требуется роль ADMIN.");
                usersData.clear();
                return;
            }

            usersData.clear();

            // Используем TypeReference для правильного парсинга List
            List<Map<String, Object>> usersList = objectMapper.readValue(
                    response,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> userMap : usersList) {
                addUserToTable(userMap);
            }

            System.out.println("Загружено пользователей: " + usersData.size());

        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            // Специальная обработка ошибки парсинга пустого ответа
            System.out.println("Ошибка парсинга - пустой ответ от сервера (403 Forbidden)");
            showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                    "Доступ запрещен. Убедитесь, что вы вошли как администратор.");
            usersData.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить пользователей: " + e.getMessage());
        }
    }

    // Метод для добавления пользователя в таблицу
    private void addUserToTable(Map<String, Object> userMap) {
        try {
            // Безопасное извлечение данных
            Long id = userMap.get("id") != null ? Long.valueOf(userMap.get("id").toString()) : 0L;
            String username = userMap.get("username") != null ? (String) userMap.get("username") : "N/A";
            String fullName = userMap.get("fullName") != null ? (String) userMap.get("fullName") : "N/A";
            String email = userMap.get("email") != null ? (String) userMap.get("email") : "";
            String role = userMap.get("role") != null ? (String) userMap.get("role") : "SCIENTIST";

            User user = new User(id, username, fullName, email, role);
            usersData.add(user);
            System.out.println("Добавлен пользователь: " + user.getUsername());
        } catch (Exception e) {
            System.out.println("Ошибка при парсинге пользователя: " + userMap);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            changeUserRole(selectedUser);
        } else {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите пользователя для редактирования");
        }
    }

    @FXML
    private void handleChangeRole() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            changeUserRole(selectedUser);
        } else {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите пользователя для смены роли");
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void changeUserRole(User user) {
        try {
            String newRole = user.getRole().equals("ADMIN") ? "SCIENTIST" : "ADMIN";

            String userJson = String.format(
                    "{\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}",
                    user.getUsername(), user.getFullName(), user.getEmail(), newRole
            );

            String response = apiService.put("/users/" + user.getId(), userJson);

            // Проверка на пустой ответ
            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                        "Недостаточно прав для изменения роли пользователя.");
                return;
            }

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            if (responseMap.containsKey("id")) {
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Роль пользователя " + user.getUsername() + " изменена на: " + newRole);
                loadUsers(); // Обновляем таблицу
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить роль пользователя");
            }
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            // Обработка 403 при изменении роли
            showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                    "Доступ запрещен. Недостаточно прав для изменения ролей.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить роль: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Вспомогательный класс для таблицы
    public static class User {
        private final Long id;
        private final String username;
        private final String fullName;
        private final String email;
        private final String role;

        public User(Long id, String username, String fullName, String email, String role) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }

        // Геттеры
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}