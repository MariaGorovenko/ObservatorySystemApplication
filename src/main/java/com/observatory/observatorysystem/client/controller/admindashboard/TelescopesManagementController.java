package com.observatory.observatorysystem.client.controller.admindashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TelescopesManagementController {

    @FXML private TableView<Telescope> telescopesTable;
    @FXML private TableColumn<Telescope, Long> idColumn;
    @FXML private TableColumn<Telescope, String> nameColumn;
    @FXML private TableColumn<Telescope, String> typeColumn;
    @FXML private TableColumn<Telescope, String> locationColumn;
    @FXML private TableColumn<Telescope, Boolean> operationalColumn;
    @FXML private TableColumn<Telescope, Void> actionsColumn;

    // Форма добавления
    @FXML private TextField newNameField;
    @FXML private TextField newTypeField;
    @FXML private TextField newLocationField;
    @FXML private CheckBox newOperationalCheckBox;
    @FXML private Button addButton;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<Telescope> telescopesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTelescopes();
    }

    private void setupTableColumns() {
        // Основные колонки
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        operationalColumn.setCellValueFactory(new PropertyValueFactory<>("operational"));

        // Колонка действий
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button("Сменить статус");
            private final Button deleteBtn = new Button("Удалить");
            private final HBox pane = new HBox(toggleBtn, deleteBtn);

            {
                pane.setSpacing(5);
                toggleBtn.setStyle("-fx-font-size: 11;");
                deleteBtn.setStyle("-fx-font-size: 11; -fx-background-color: #ff4444; -fx-text-fill: white;");

                toggleBtn.setOnAction(event -> {
                    Telescope telescope = getTableView().getItems().get(getIndex());
                    toggleTelescopeStatus(telescope);
                });

                deleteBtn.setOnAction(event -> {
                    Telescope telescope = getTableView().getItems().get(getIndex());
                    deleteTelescope(telescope);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        telescopesTable.setItems(telescopesData);
    }

    private void loadTelescopes() {
        try {
            String response = apiService.get("/telescopes");

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа", "Не удалось загрузить телескопы.");
                return;
            }

            telescopesData.clear();
            List<Map<String, Object>> telescopesList = objectMapper.readValue(
                    response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> telescopeMap : telescopesList) {
                addTelescopeToTable(telescopeMap);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить телескопы: " + e.getMessage());
        }
    }

    // ДОБАВЛЕНИЕ нового телескопа
    @FXML
    private void handleAddTelescope() {
        String name = newNameField.getText();
        String type = newTypeField.getText();
        String location = newLocationField.getText();
        Boolean isOperational = newOperationalCheckBox.isSelected();

        if (name.isEmpty() || type.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните название и тип телескопа");
            return;
        }

        try {
            String telescopeJson = String.format(
                    "{\"name\":\"%s\",\"type\":\"%s\",\"location\":\"%s\",\"isOperational\":%s}",
                    name, type, location, isOperational
            );

            String response = apiService.post("/telescopes", telescopeJson);

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

                if (responseMap.containsKey("id")) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Телескоп добавлен!");
                    clearAddForm();
                    loadTelescopes(); // Обновляем таблицу
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить телескоп: " + e.getMessage());
        }
    }

    // СМЕНА СТАТУСА телескопа
    private void toggleTelescopeStatus(Telescope telescope) {
        try {
            Boolean newStatus = !telescope.getOperational();

            String telescopeJson = String.format(
                    "{\"name\":\"%s\",\"type\":\"%s\",\"location\":\"%s\",\"isOperational\":%s}",
                    telescope.getName(), telescope.getType(), telescope.getLocation(), newStatus
            );

            String response = apiService.put("/telescopes/" + telescope.getId(), telescopeJson);

            if (response != null && !response.trim().isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Статус телескопа '" + telescope.getName() + "' изменен на: " +
                                (newStatus ? "рабочий" : "нерабочий"));
                loadTelescopes();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить статус: " + e.getMessage());
        }
    }

    // УДАЛЕНИЕ телескопа
    private void deleteTelescope(Telescope telescope) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удаление телескопа");
        confirmation.setContentText("Вы уверены, что хотите удалить телескоп '" + telescope.getName() + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String response = apiService.delete("/telescopes/" + telescope.getId());

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Телескоп '" + telescope.getName() + "' удален!");
                loadTelescopes(); // Обновляем таблицу

            } catch (RuntimeException e) {
                // Обработка ошибок доступа
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить телескоп: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadTelescopes();
    }

    @FXML
    private void handleClose() {
        telescopesTable.getScene().getWindow().hide();
    }

    private void clearAddForm() {
        newNameField.clear();
        newTypeField.clear();
        newLocationField.clear();
        newOperationalCheckBox.setSelected(true);
    }

    private void addTelescopeToTable(Map<String, Object> telescopeMap) {
        try {
            Long id = telescopeMap.get("id") != null ? Long.valueOf(telescopeMap.get("id").toString()) : 0L;
            String name = telescopeMap.get("name") != null ? (String) telescopeMap.get("name") : "N/A";
            String type = telescopeMap.get("type") != null ? (String) telescopeMap.get("type") : "N/A";
            String location = telescopeMap.get("location") != null ? (String) telescopeMap.get("location") : "Не указано";

            Boolean isOperational = true;
            if (telescopeMap.get("isOperational") != null) {
                if (telescopeMap.get("isOperational") instanceof Boolean) {
                    isOperational = (Boolean) telescopeMap.get("isOperational");
                } else {
                    isOperational = Boolean.parseBoolean(telescopeMap.get("isOperational").toString());
                }
            }

            telescopesData.add(new Telescope(id, name, type, location, isOperational));

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге телескопа: " + telescopeMap);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Класс Telescope остается таким же
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

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getLocation() { return location; }
        public Boolean getOperational() { return operational; }
    }
}
