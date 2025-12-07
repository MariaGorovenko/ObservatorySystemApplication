package com.observatory.observatorysystem.client.controller.admindashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.*;

public class AllObservationsController {

    @FXML private TableView<Observation> observationsTable;
    @FXML private TableColumn<Observation, Long> idColumn;
    @FXML private TableColumn<Observation, String> objectNameColumn;
    @FXML private TableColumn<Observation, String> programColumn;
    @FXML private TableColumn<Observation, String> telescopeColumn;
    @FXML private TableColumn<Observation, String> userColumn;
    @FXML private TableColumn<Observation, String> requestedStartColumn;
    @FXML private TableColumn<Observation, String> requestedEndColumn;
    @FXML private TableColumn<Observation, Integer> priorityColumn;
    @FXML private TableColumn<Observation, String> statusColumn;
    @FXML private TableColumn<Observation, Void> actionsColumn;

    @FXML private ComboBox<String> filterTelescopeCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ComboBox<Integer> filterPriorityCombo;
    @FXML private DatePicker filterStartDate;
    @FXML private DatePicker filterEndDate;
    @FXML private Label totalCountLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<Observation> observationsData = FXCollections.observableArrayList();
    private ObservableList<String> telescopeList = FXCollections.observableArrayList();
    private ObservableList<String> statusList = FXCollections.observableArrayList("Все", "PENDING", "APPROVED", "SCHEDULED", "COMPLETED", "CANCELLED");
    private ObservableList<Integer> priorityList = FXCollections.observableArrayList(1, 2, 3);

    // Храним ID телескопов для фильтрации
    private Map<String, Long> telescopeIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterControls();
        loadTelescopesForFilter();
        loadObservations();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        objectNameColumn.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        telescopeColumn.setCellValueFactory(new PropertyValueFactory<>("telescopeName"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        requestedStartColumn.setCellValueFactory(new PropertyValueFactory<>("requestedStart"));
        requestedEndColumn.setCellValueFactory(new PropertyValueFactory<>("requestedEnd"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Колонка действий
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Одобрить");
            private final Button completeBtn = new Button("Завершить");
            private final Button cancelBtn = new Button("Отменить");
            private final Button deleteBtn = new Button("Удалить");
            private final HBox pane = new HBox(approveBtn, completeBtn, cancelBtn, deleteBtn);

            {
                pane.setSpacing(5);
                approveBtn.setStyle("-fx-font-size: 10; -fx-background-color: #4CAF50; -fx-text-fill: white;");
                completeBtn.setStyle("-fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
                cancelBtn.setStyle("-fx-font-size: 10; -fx-background-color: #FF9800; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-background-color: #F44336; -fx-text-fill: white;");

                approveBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    changeStatus(obs, "APPROVED");
                });

                completeBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    changeStatus(obs, "COMPLETED");
                });

                cancelBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    changeStatus(obs, "CANCELLED");
                });

                deleteBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    deleteObservation(obs);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Observation obs = getTableView().getItems().get(getIndex());
                    // Показываем только нужные кнопки в зависимости от статуса
                    approveBtn.setVisible(obs.getStatus().equals("PENDING"));
                    completeBtn.setVisible(obs.getStatus().equals("APPROVED") || obs.getStatus().equals("SCHEDULED"));
                    cancelBtn.setVisible(!obs.getStatus().equals("CANCELLED") && !obs.getStatus().equals("COMPLETED"));
                    deleteBtn.setVisible(true);
                    setGraphic(pane);
                }
            }
        });

        observationsTable.setItems(observationsData);
    }

    private void setupFilterControls() {
        filterStatusCombo.setItems(statusList);
        filterStatusCombo.setValue("Все");

        filterPriorityCombo.getItems().addAll(null, 1, 2, 3);
        filterPriorityCombo.setValue(null);
    }

    private void loadTelescopesForFilter() {
        try {
            String response = apiService.get("/telescopes");
            if (response != null && !response.trim().isEmpty()) {
                List<Map<String, Object>> telescopes = objectMapper.readValue(
                        response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );

                telescopeList.clear();
                telescopeIdMap.clear();
                telescopeList.add("Все");

                for (Map<String, Object> telescope : telescopes) {
                    String name = telescope.get("name") != null ? (String) telescope.get("name") : "N/A";
                    Long id = telescope.get("id") != null ? Long.valueOf(telescope.get("id").toString()) : 0L;
                    telescopeList.add(name);
                    telescopeIdMap.put(name, id);
                }
                filterTelescopeCombo.setItems(telescopeList);
                filterTelescopeCombo.setValue("Все");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке телескопов: " + e.getMessage());
        }
    }

    private void loadObservations() {
        try {
            // Собираем параметры фильтрации
            Map<String, String> params = new HashMap<>();

            // Телескоп
            String selectedTelescope = filterTelescopeCombo.getValue();
            if (selectedTelescope != null && !selectedTelescope.equals("Все") && telescopeIdMap.containsKey(selectedTelescope)) {
                Long telescopeId = telescopeIdMap.get(selectedTelescope);
                params.put("telescopeId", telescopeId.toString());
            }

            // Статус
            String selectedStatus = filterStatusCombo.getValue();
            if (selectedStatus != null && !selectedStatus.equals("Все")) {
                params.put("status", selectedStatus);
            }

            // Приоритет
            Integer selectedPriority = filterPriorityCombo.getValue();
            if (selectedPriority != null) {
                params.put("priority", selectedPriority.toString());
            }

            // Даты
            LocalDate startDate = filterStartDate.getValue();
            if (startDate != null) {
                params.put("startDate", startDate.toString());
            }

            LocalDate endDate = filterEndDate.getValue();
            if (endDate != null) {
                params.put("endDate", endDate.toString());
            }

            // Формируем URL с параметрами
            String endpoint = "/observations";
            if (!params.isEmpty()) {
                endpoint = "/observations/filter";
                StringBuilder queryParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (queryParams.length() > 0) {
                        queryParams.append("&");
                    }
                    queryParams.append(entry.getKey()).append("=").append(entry.getValue());
                }
                endpoint += "?" + queryParams.toString();
            }

            System.out.println("Запрос к API: " + endpoint);

            String response = apiService.get(endpoint);

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить заявки.");
                return;
            }

            observationsData.clear();
            List<Map<String, Object>> observationsList = objectMapper.readValue(
                    response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> obsMap : observationsList) {
                addObservationToTable(obsMap);
            }

            totalCountLabel.setText(String.valueOf(observationsData.size()));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить заявки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        loadObservations();
    }

    @FXML
    private void handleResetFilter() {
        filterTelescopeCombo.setValue("Все");
        filterStatusCombo.setValue("Все");
        filterPriorityCombo.setValue(null);
        filterStartDate.setValue(null);
        filterEndDate.setValue(null);
        loadObservations();
    }

    @FXML
    private void handleRefresh() {
        loadObservations();
        loadTelescopesForFilter();
    }

    @FXML
    private void handleClose() {
        observationsTable.getScene().getWindow().hide();
    }

    private void changeStatus(Observation observation, String newStatus) {
        try {
            String updateJson = String.format(
                    "{\"status\":\"%s\"}",
                    newStatus
            );

            String response = apiService.put("/observations/" + observation.getId(), updateJson);

            if (response != null && !response.trim().isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Статус заявки изменен на: " + newStatus);
                loadObservations();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить статус: " + e.getMessage());
        }
    }

    private void deleteObservation(Observation observation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удаление заявки");
        confirmation.setContentText("Вы уверены, что хотите удалить заявку на объект '" +
                observation.getObjectName() + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String response = apiService.delete("/observations/" + observation.getId());

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Заявка удалена!");
                loadObservations();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить заявку: " + e.getMessage());
            }
        }
    }

    private void addObservationToTable(Map<String, Object> obsMap) {
        try {
            Long id = obsMap.get("id") != null ? Long.valueOf(obsMap.get("id").toString()) : 0L;
            String objectName = obsMap.get("objectName") != null ? (String) obsMap.get("objectName") : "N/A";
            String programName = obsMap.get("programName") != null ? (String) obsMap.get("programName") : "N/A";
            String telescopeName = obsMap.get("telescopeName") != null ? (String) obsMap.get("telescopeName") : "N/A";
            String userName = obsMap.get("userName") != null ? (String) obsMap.get("userName") : "N/A";

            // Обработка дат
            String requestedStart = "Не указано";
            if (obsMap.get("requestedStart") != null) {
                requestedStart = obsMap.get("requestedStart").toString();
                if (requestedStart.contains("T")) {
                    requestedStart = requestedStart.replace("T", " ");
                }
            }

            String requestedEnd = "Не указано";
            if (obsMap.get("requestedEnd") != null) {
                requestedEnd = obsMap.get("requestedEnd").toString();
                if (requestedEnd.contains("T")) {
                    requestedEnd = requestedEnd.replace("T", " ");
                }
            }

            Integer priority = obsMap.get("priority") != null ? Integer.valueOf(obsMap.get("priority").toString()) : 3;
            String status = obsMap.get("status") != null ? (String) obsMap.get("status") : "PENDING";

            String priorityText = "";
            switch (priority) {
                case 1: priorityText = "Высокий"; break;
                case 2: priorityText = "Средний"; break;
                case 3: priorityText = "Низкий"; break;
            }

            String statusText = "";
            switch (status) {
                case "PENDING": statusText = "Ожидает"; break;
                case "APPROVED": statusText = "Одобрено"; break;
                case "SCHEDULED": statusText = "Запланировано"; break;
                case "COMPLETED": statusText = "Завершено"; break;
                case "CANCELLED": statusText = "Отменено"; break;
            }

            Observation observation = new Observation(id, objectName, programName, telescopeName,
                    userName, requestedStart, requestedEnd, priority, priorityText, status, statusText);
            observationsData.add(observation);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге заявки: " + obsMap);
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
    public static class Observation {
        private final Long id;
        private final String objectName;
        private final String programName;
        private final String telescopeName;
        private final String userName;
        private final String requestedStart;
        private final String requestedEnd;
        private final Integer priority;
        private final String priorityText;
        private final String status;
        private final String statusText;

        public Observation(Long id, String objectName, String programName, String telescopeName,
                           String userName, String requestedStart, String requestedEnd,
                           Integer priority, String priorityText, String status, String statusText) {
            this.id = id;
            this.objectName = objectName;
            this.programName = programName;
            this.telescopeName = telescopeName;
            this.userName = userName;
            this.requestedStart = requestedStart;
            this.requestedEnd = requestedEnd;
            this.priority = priority;
            this.priorityText = priorityText;
            this.status = status;
            this.statusText = statusText;
        }

        public Long getId() { return id; }
        public String getObjectName() { return objectName; }
        public String getProgramName() { return programName; }
        public String getTelescopeName() { return telescopeName; }
        public String getUserName() { return userName; }
        public String getRequestedStart() { return requestedStart; }
        public String getRequestedEnd() { return requestedEnd; }
        public Integer getPriority() { return priority; }
        public String getPriorityText() { return priorityText; }
        public String getStatus() { return status; }
        public String getStatusText() { return statusText; }
    }
}