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

public class ProgramsManagementController {

    @FXML private TableView<ResearchProgram> programsTable;
    @FXML private TableColumn<ResearchProgram, Long> idColumn;
    @FXML private TableColumn<ResearchProgram, String> nameColumn;
    @FXML private TableColumn<ResearchProgram, String> descriptionColumn;
    @FXML private TableColumn<ResearchProgram, String> startDateColumn;
    @FXML private TableColumn<ResearchProgram, String> endDateColumn;
    @FXML private TableColumn<ResearchProgram, String> statusColumn;
    @FXML private TableColumn<ResearchProgram, String> budgetColumn;
    @FXML private TableColumn<ResearchProgram, String> leadScientistColumn;
    @FXML private TableColumn<ResearchProgram, Void> actionsColumn;

    // Форма добавления
    @FXML private TextField newNameField;
    @FXML private TextArea newDescriptionField;
    @FXML private DatePicker newStartDatePicker;
    @FXML private DatePicker newEndDatePicker;
    @FXML private TextField newBudgetField;
    @FXML private ComboBox<String> newStatusComboBox;
    @FXML private ComboBox<String> newLeadScientistComboBox;
    @FXML private Button addButton;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<ResearchProgram> programsData = FXCollections.observableArrayList();
    private ObservableList<String> scientistsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupForm();
        loadPrograms();
        loadScientists();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        leadScientistColumn.setCellValueFactory(new PropertyValueFactory<>("leadScientistName"));

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
                    ResearchProgram program = getTableView().getItems().get(getIndex());
                    toggleProgramStatus(program);
                });

                deleteBtn.setOnAction(event -> {
                    ResearchProgram program = getTableView().getItems().get(getIndex());
                    deleteProgram(program);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        programsTable.setItems(programsData);
    }

    private void setupForm() {
        // Заполняем комбобокс статусов
        newStatusComboBox.getItems().addAll("PLANNED", "ACTIVE", "COMPLETED", "CANCELLED");
        newStatusComboBox.setValue("PLANNED");
    }

    private void loadScientists() {
        try {
            String response = apiService.get("/users");
            if (response != null && !response.trim().isEmpty()) {
                List<Map<String, Object>> usersList = objectMapper.readValue(
                        response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );

                scientistsList.clear();
                for (Map<String, Object> userMap : usersList) {
                    String role = userMap.get("role") != null ? (String) userMap.get("role") : "";
                    if ("SCIENTIST".equals(role)) {
                        String fullName = userMap.get("fullName") != null ? (String) userMap.get("fullName") : "N/A";
                        Long id = userMap.get("id") != null ? Long.valueOf(userMap.get("id").toString()) : 0L;
                        scientistsList.add(id + ":" + fullName);
                    }
                }
                newLeadScientistComboBox.setItems(scientistsList);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке ученых: " + e.getMessage());
        }
    }

    private void loadPrograms() {
        try {
            String response = apiService.get("/programs");

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа", "Не удалось загрузить программы.");
                return;
            }

            programsData.clear();
            List<Map<String, Object>> programsList = objectMapper.readValue(
                    response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> programMap : programsList) {
                addProgramToTable(programMap);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить программы: " + e.getMessage());
        }
    }

    // ДОБАВЛЕНИЕ новой программы
    @FXML
    private void handleAddProgram() {
        String name = newNameField.getText();
        String description = newDescriptionField.getText();
        String startDate = newStartDatePicker.getValue() != null ? newStartDatePicker.getValue().toString() : null;
        String endDate = newEndDatePicker.getValue() != null ? newEndDatePicker.getValue().toString() : null;
        String status = newStatusComboBox.getValue();
        String budget = newBudgetField.getText();
        String selectedScientist = newLeadScientistComboBox.getValue();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните название программы");
            return;
        }

        try {
            // Парсим выбранного ученого
            Long leadScientistId = null;
            if (selectedScientist != null && !selectedScientist.isEmpty()) {
                String[] parts = selectedScientist.split(":");
                if (parts.length > 0) {
                    leadScientistId = Long.valueOf(parts[0]);
                }
            }

            // Создаем JSON согласно структуре ProgramRequest
            String programJson;
            if (leadScientistId != null) {
                programJson = String.format(
                        "{\"name\":\"%s\",\"description\":\"%s\",\"startDate\":%s,\"endDate\":%s,\"status\":\"%s\",\"budget\":%s,\"leadScientistId\":%d}",
                        name,
                        description != null ? description : "",
                        startDate != null ? "\"" + startDate + "\"" : "null",
                        endDate != null ? "\"" + endDate + "\"" : "null",
                        status != null ? status : "PLANNED",
                        budget.isEmpty() ? "0" : budget,
                        leadScientistId
                );
            } else {
                programJson = String.format(
                        "{\"name\":\"%s\",\"description\":\"%s\",\"startDate\":%s,\"endDate\":%s,\"status\":\"%s\",\"budget\":%s}",
                        name,
                        description != null ? description : "",
                        startDate != null ? "\"" + startDate + "\"" : "null",
                        endDate != null ? "\"" + endDate + "\"" : "null",
                        status != null ? status : "PLANNED",
                        budget.isEmpty() ? "0" : budget
                );
            }

            System.out.println("Отправляем JSON: " + programJson);

            String response = apiService.post("/programs", programJson);

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

                if (responseMap.containsKey("id")) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Программа добавлена!");
                    clearAddForm();
                    loadPrograms();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить программу: " + e.getMessage());
        }
    }

    // СМЕНА СТАТУСА программы
    private void toggleProgramStatus(ResearchProgram program) {
        try {
            // Простая логика смены статуса
            String newStatus = "ACTIVE";
            if ("ACTIVE".equals(program.getStatus())) {
                newStatus = "COMPLETED";
            } else if ("COMPLETED".equals(program.getStatus())) {
                newStatus = "PLANNED";
            }

            // ФИКС: Используем точку в числах и правильный формат бюджета
            String budgetValue = program.getBudget().replace(",", "."); // Заменяем запятую на точку

            // Для смены статуса не меняем leadScientistId
            String programJson = String.format(
                    "{\"name\":\"%s\",\"description\":\"%s\",\"startDate\":%s,\"endDate\":%s,\"status\":\"%s\",\"budget\":%s}",
                    program.getName(),
                    program.getDescription() != null ? program.getDescription() : "",
                    program.getStartDate() != null && !program.getStartDate().equals("Не указана") ? "\"" + program.getStartDate() + "\"" : "null",
                    program.getEndDate() != null && !program.getEndDate().equals("Не указана") ? "\"" + program.getEndDate() + "\"" : "null",
                    newStatus,
                    budgetValue // Используем исправленное значение
            );

            System.out.println("Исправленный JSON для обновления статуса: " + programJson);

            String response = apiService.put("/programs/" + program.getId(), programJson);

            if (response != null && !response.trim().isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Статус программы '" + program.getName() + "' изменен на: " + newStatus);
                loadPrograms();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить статус: " + e.getMessage());
        }
    }

    // УДАЛЕНИЕ программы
    private void deleteProgram(ResearchProgram program) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удаление программы");
        confirmation.setContentText("Вы уверены, что хотите удалить программу '" + program.getName() + "'?\n\n" +
                "Внимание: все связанные заявки на наблюдения также будут удалены.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String response = apiService.delete("/programs/" + program.getId());

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Программа и связанные заявки удалены!");
                loadPrograms();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить программу: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadPrograms();
        loadScientists();
    }

    @FXML
    private void handleClose() {
        programsTable.getScene().getWindow().hide();
    }

    private void clearAddForm() {
        newNameField.clear();
        newDescriptionField.clear();
        newStartDatePicker.setValue(null);
        newEndDatePicker.setValue(null);
        newBudgetField.clear();
        newStatusComboBox.setValue("PLANNED");
        newLeadScientistComboBox.setValue(null);
    }

    private void addProgramToTable(Map<String, Object> programMap) {
        try {
            Long id = programMap.get("id") != null ? Long.valueOf(programMap.get("id").toString()) : 0L;
            String name = programMap.get("name") != null ? (String) programMap.get("name") : "N/A";
            String description = programMap.get("description") != null ? (String) programMap.get("description") : "";

            // Обработка дат
            String startDate = "Не указана";
            if (programMap.get("startDate") != null) {
                startDate = programMap.get("startDate").toString();
                if (startDate.contains("T")) {
                    startDate = startDate.split("T")[0];
                }
            }

            String endDate = "Не указана";
            if (programMap.get("endDate") != null) {
                endDate = programMap.get("endDate").toString();
                if (endDate.contains("T")) {
                    endDate = endDate.split("T")[0];
                }
            }

            String status = programMap.get("status") != null ? (String) programMap.get("status") : "PLANNED";

            // Бюджет - ФИКС: используем точку как разделитель
            String budget = "0.00";
            if (programMap.get("budget") != null) {
                try {
                    double budgetValue = Double.parseDouble(programMap.get("budget").toString());
                    budget = String.format("%.2f", budgetValue).replace(",", ".");
                } catch (NumberFormatException e) {
                    budget = programMap.get("budget").toString().replace(",", ".");
                }
            }

            // Получаем информацию о руководителе
            String leadScientistName = "Не назначен";
            if (programMap.get("leadScientist") != null) {
                Map<String, Object> scientistMap = (Map<String, Object>) programMap.get("leadScientist");
                if (scientistMap.get("fullName") != null) {
                    leadScientistName = (String) scientistMap.get("fullName");
                } else if (scientistMap.get("username") != null) {
                    leadScientistName = (String) scientistMap.get("username");
                }
            }

            ResearchProgram program = new ResearchProgram(id, name, description, startDate,
                    endDate, status, budget, leadScientistName);
            programsData.add(program);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге программы: " + programMap);
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

    // Вспомогательный класс для таблицы
    public static class ResearchProgram {
        private final Long id;
        private final String name;
        private final String description;
        private final String startDate;
        private final String endDate;
        private final String status;
        private final String budget;
        private final String leadScientistName;

        public ResearchProgram(Long id, String name, String description, String startDate,
                               String endDate, String status, String budget, String leadScientistName) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.budget = budget;
            this.leadScientistName = leadScientistName;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getStatus() { return status; }
        public String getBudget() { return budget; }
        public String getLeadScientistName() { return leadScientistName; }
    }
}