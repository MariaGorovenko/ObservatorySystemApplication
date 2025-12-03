package com.observatory.observatorysystem.client.controller.scientistdashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.observatory.observatorysystem.client.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MyObservationsController {

    @FXML private TableView<Observation> observationsTable;
    @FXML private TableColumn<Observation, Long> idColumn;
    @FXML private TableColumn<Observation, String> objectNameColumn;
    @FXML private TableColumn<Observation, String> programColumn;
    @FXML private TableColumn<Observation, String> telescopeColumn;
    @FXML private TableColumn<Observation, String> requestedStartColumn;
    @FXML private TableColumn<Observation, String> requestedEndColumn;
    @FXML private TableColumn<Observation, String> priorityColumn;
    @FXML private TableColumn<Observation, String> statusColumn;
    @FXML private TableColumn<Observation, Void> actionsColumn;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label totalCountLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<Observation> observationsData = FXCollections.observableArrayList();
    private ObservableList<String> statusFilters = FXCollections.observableArrayList(
            "Все", "Ожидает", "Одобрено", "Запланировано", "Завершено", "Отменено"
    );

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterControls();
        loadMyObservations();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        objectNameColumn.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        telescopeColumn.setCellValueFactory(new PropertyValueFactory<>("telescopeName"));
        requestedStartColumn.setCellValueFactory(new PropertyValueFactory<>("requestedStart"));
        requestedEndColumn.setCellValueFactory(new PropertyValueFactory<>("requestedEnd"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priorityText"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));

        // Колонка действий
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Просмотр");
            private final Button cancelBtn = new Button("Отменить");
            private final HBox pane = new HBox(viewBtn, cancelBtn);

            {
                pane.setSpacing(5);
                viewBtn.setStyle("-fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
                cancelBtn.setStyle("-fx-font-size: 10; -fx-background-color: #FF9800; -fx-text-fill: white;");

                viewBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    viewObservationDetails(obs);
                });

                cancelBtn.setOnAction(event -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    cancelObservation(obs);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Observation obs = getTableView().getItems().get(getIndex());
                    // Показываем кнопку отмены только для ожидающих заявок
                    cancelBtn.setVisible("PENDING".equals(obs.getStatus()) || "APPROVED".equals(obs.getStatus()));
                    setGraphic(pane);
                }
            }
        });

        observationsTable.setItems(observationsData);
    }

    private void setupFilterControls() {
        statusFilterCombo.setItems(statusFilters);
        statusFilterCombo.setValue("Все");
        statusFilterCombo.setOnAction(event -> filterByStatus());
    }

    private void loadMyObservations() {
        try {
            Long userId = SessionContext.getCurrentUserId();

            if (userId == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось определить ID пользователя");
                return;
            }

            String response = apiService.get("/observations/user/" + userId);

            if (response == null || response.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить ваши заявки.");
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
            filterByStatus(); // Применяем текущий фильтр

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить заявки: " + e.getMessage());
        }
    }

    private void filterByStatus() {
        String selectedStatus = statusFilterCombo.getValue();
        if ("Все".equals(selectedStatus)) {
            observationsTable.setItems(observationsData);
        } else {
            ObservableList<Observation> filtered = FXCollections.observableArrayList();
            String statusToFilter = "";

            switch (selectedStatus) {
                case "Ожидает": statusToFilter = "PENDING"; break;
                case "Одобрено": statusToFilter = "APPROVED"; break;
                case "Запланировано": statusToFilter = "SCHEDULED"; break;
                case "Завершено": statusToFilter = "COMPLETED"; break;
                case "Отменено": statusToFilter = "CANCELLED"; break;
            }

            for (Observation obs : observationsData) {
                if (obs.getStatus().equals(statusToFilter)) {
                    filtered.add(obs);
                }
            }
            observationsTable.setItems(filtered);
        }
    }

    private void viewObservationDetails(Observation observation) {
        try {
            String response = apiService.get("/observations/" + observation.getId());

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> details = objectMapper.readValue(response, Map.class);

                StringBuilder detailsText = new StringBuilder();
                detailsText.append("Детали заявки #").append(observation.getId()).append("\n\n");
                detailsText.append("Объект: ").append(details.get("objectName")).append("\n");
                detailsText.append("Программа: ").append(((Map)details.get("program")).get("name")).append("\n");
                detailsText.append("Телескоп: ").append(((Map)details.get("telescope")).get("name")).append("\n");
                detailsText.append("Координаты: ").append(details.get("coordinates")).append("\n");
                detailsText.append("Спектральный диапазон: ").append(details.get("spectralRange")).append("\n");
                detailsText.append("Запрошено: ").append(details.get("requestedStart")).append(" - ")
                        .append(details.get("requestedEnd")).append("\n");
                detailsText.append("Приоритет: ").append(details.get("priority")).append("\n");
                detailsText.append("Статус: ").append(details.get("status")).append("\n");
                detailsText.append("Описание: ").append(details.get("resultDescription")).append("\n");

                TextArea textArea = new TextArea(detailsText.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);

                ScrollPane scrollPane = new ScrollPane(textArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(400);

                Stage detailsStage = new Stage();
                detailsStage.setTitle("Детали заявки #" + observation.getId());
                detailsStage.setScene(new Scene(scrollPane, 500, 450));
                detailsStage.show();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить детали заявки: " + e.getMessage());
        }
    }

    private void cancelObservation(Observation observation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение отмены");
        confirmation.setHeaderText("Отмена заявки");
        confirmation.setContentText("Вы уверены, что хотите отменить заявку на объект '" +
                observation.getObjectName() + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String updateJson = "{\"status\":\"CANCELLED\"}";
                String response = apiService.put("/observations/" + observation.getId(), updateJson);

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Заявка отменена!");
                loadMyObservations();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось отменить заявку: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadMyObservations();
    }

    @FXML
    private void handleClose() {
        observationsTable.getScene().getWindow().hide();
    }

    private void addObservationToTable(Map<String, Object> obsMap) {
        try {
            Long id = obsMap.get("id") != null ? Long.valueOf(obsMap.get("id").toString()) : 0L;
            String objectName = obsMap.get("objectName") != null ? (String) obsMap.get("objectName") : "N/A";
            String programName = obsMap.get("programName") != null ? (String) obsMap.get("programName") : "N/A";
            String telescopeName = obsMap.get("telescopeName") != null ? (String) obsMap.get("telescopeName") : "N/A";

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
                    requestedStart, requestedEnd, priority, priorityText, status, statusText);
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
        private final String requestedStart;
        private final String requestedEnd;
        private final Integer priority;
        private final String priorityText;
        private final String status;
        private final String statusText;

        public Observation(Long id, String objectName, String programName, String telescopeName,
                           String requestedStart, String requestedEnd,
                           Integer priority, String priorityText, String status, String statusText) {
            this.id = id;
            this.objectName = objectName;
            this.programName = programName;
            this.telescopeName = telescopeName;
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
        public String getRequestedStart() { return requestedStart; }
        public String getRequestedEnd() { return requestedEnd; }
        public Integer getPriority() { return priority; }
        public String getPriorityText() { return priorityText; }
        public String getStatus() { return status; }
        public String getStatusText() { return statusText; }
    }
}