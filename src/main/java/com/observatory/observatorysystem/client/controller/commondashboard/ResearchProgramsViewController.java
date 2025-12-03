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

public class ResearchProgramsViewController {

    @FXML
    private TableView<ResearchProgram> programsTable;

    @FXML
    private TableColumn<ResearchProgram, Long> idColumn;

    @FXML
    private TableColumn<ResearchProgram, String> nameColumn;

    @FXML
    private TableColumn<ResearchProgram, String> descriptionColumn;

    @FXML
    private TableColumn<ResearchProgram, String> startDateColumn;

    @FXML
    private TableColumn<ResearchProgram, String> endDateColumn;

    @FXML
    private TableColumn<ResearchProgram, String> statusColumn;

    @FXML
    private TableColumn<ResearchProgram, String> budgetColumn;

    @FXML
    private TableColumn<ResearchProgram, String> leadScientistColumn;

    @FXML
    private Label titleLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<ResearchProgram> programsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        leadScientistColumn.setCellValueFactory(new PropertyValueFactory<>("leadScientistName"));

        programsTable.setItems(programsData);

        // Устанавливаем заголовок в зависимости от роли
        if (SessionContext.isAdmin()) {
            titleLabel.setText("Просмотр научных программ - Администратор");
        } else {
            titleLabel.setText("Научные программы - Ученый");
        }

        loadPrograms();
    }

    private void loadPrograms() {
        try {
            String response = apiService.get("/programs");
            System.out.println("Ответ от /programs: " + response);

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                        "Не удалось загрузить данные о программах.");
                programsData.clear();
                return;
            }

            programsData.clear();

            List<Map<String, Object>> programsList = objectMapper.readValue(
                    response,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> programMap : programsList) {
                addProgramToTable(programMap);
            }

            System.out.println("Загружено программ: " + programsData.size());

        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка доступа",
                    "Доступ запрещен. Убедитесь, что вы авторизованы в системе.");
            programsData.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить программы: " + e.getMessage());
        }
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
                // Убираем время если есть
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

            // Бюджет
            String budget = "0.00";
            if (programMap.get("budget") != null) {
                try {
                    budget = String.format("%.2f", Double.parseDouble(programMap.get("budget").toString()));
                } catch (NumberFormatException e) {
                    budget = programMap.get("budget").toString();
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

            System.out.println("Добавлена программа: " + name + ", руководитель: " + leadScientistName);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге программы: " + programMap);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadPrograms();
    }

    @FXML
    private void handleClose() {
        programsTable.getScene().getWindow().hide();
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

        // Геттеры
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