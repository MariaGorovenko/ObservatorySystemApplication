package com.observatory.observatorysystem.client.controller.commondashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.observatory.observatorysystem.client.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;

public class TelescopesViewController {

    @FXML
    private TableView<Telescope> telescopesTable;

    @FXML
    private TableColumn<Telescope, Long> idColumn;

    @FXML
    private TableColumn<Telescope, String> nameColumn;

    @FXML
    private TableColumn<Telescope, String> typeColumn;

    @FXML
    private TableColumn<Telescope, String> locationColumn;

    @FXML
    private TableColumn<Telescope, Boolean> operationalColumn;

    @FXML
    private Label titleLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<Telescope> telescopesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        operationalColumn.setCellValueFactory(new PropertyValueFactory<>("operational"));

        telescopesTable.setItems(telescopesData);

        // Устанавливаем заголовок в зависимости от роли
        if (SessionContext.isAdmin()) {
            titleLabel.setText("Просмотр телескопов - Администратор");
        } else {
            titleLabel.setText("Доступные телескопы - Ученый");
        }

        loadTelescopes();
    }

    private void loadTelescopes() {
        try {
            String response = apiService.get("/telescopes");
            System.out.println("Ответ от /telescopes: " + response);

            // Проверка на пустой ответ (403 ошибка)
            if (response == null || response.trim().isEmpty()) {
                System.out.println("Пустой ответ - вероятно 403 Forbidden");
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                        "Не удалось загрузить данные о телескопах.");
                telescopesData.clear();
                return;
            }

            telescopesData.clear();

            // Используем TypeReference для правильного парсинга List
            List<Map<String, Object>> telescopesList = objectMapper.readValue(
                    response,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> telescopeMap : telescopesList) {
                addTelescopeToTable(telescopeMap);
            }

            System.out.println("Загружено телескопов: " + telescopesData.size());

        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            // Обработка ошибки парсинга пустого ответа
            System.out.println("Ошибка парсинга - пустой ответ от сервера (403 Forbidden)");
            showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                    "Доступ запрещен. Убедитесь, что вы авторизованы в системе.");
            telescopesData.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить телескопы: " + e.getMessage());
        }
    }

    // Метод для добавления телескопа в таблицу
    private void addTelescopeToTable(Map<String, Object> telescopeMap) {
        try {
            // Безопасное извлечение данных
            Long id = telescopeMap.get("id") != null ? Long.valueOf(telescopeMap.get("id").toString()) : 0L;
            String name = telescopeMap.get("name") != null ? (String) telescopeMap.get("name") : "N/A";
            String type = telescopeMap.get("type") != null ? (String) telescopeMap.get("type") : "N/A";
            String location = telescopeMap.get("location") != null ? (String) telescopeMap.get("location") : "Не указано";

            // Обработка boolean поля
            Boolean isOperational = true; // значение по умолчанию
            if (telescopeMap.get("isOperational") != null) {
                if (telescopeMap.get("isOperational") instanceof Boolean) {
                    isOperational = (Boolean) telescopeMap.get("isOperational");
                } else {
                    isOperational = Boolean.parseBoolean(telescopeMap.get("isOperational").toString());
                }
            }

            Telescope telescope = new Telescope(id, name, type, location, isOperational);
            telescopesData.add(telescope);
            System.out.println("Добавлен телескоп: " + telescope.getName());

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге телескопа: " + telescopeMap);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadTelescopes();
    }

    @FXML
    private void handleClose() {
        // Закрываем окно
        telescopesTable.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Вспомогательный класс для таблицы
    public static class Telescope {
        private final Long id;
        private final String name;
        private final String type;
        private final String location;
        private final Boolean operational;

        public Telescope(Long id, String name, String type, String location, Boolean operational) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.location = location;
            this.operational = operational;
        }

        // Геттеры
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getLocation() { return location; }
        public Boolean getOperational() { return operational; }
    }
}