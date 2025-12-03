package com.observatory.observatorysystem.client.controller.scientistdashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.observatory.observatorysystem.client.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class CreateObservationController {

    @FXML private ComboBox<String> programCombo;
    @FXML private ComboBox<String> telescopeCombo;
    @FXML private TextField objectNameField;
    @FXML private TextField coordinatesField;
    @FXML private TextField spectralRangeField;
    @FXML private DatePicker startDatePicker;
    @FXML private Spinner<Integer> startHourSpinner;
    @FXML private Spinner<Integer> startMinuteSpinner;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> endHourSpinner;
    @FXML private Spinner<Integer> endMinuteSpinner;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea descriptionArea;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObservableList<String> programsList = FXCollections.observableArrayList();
    private ObservableList<String> telescopesList = FXCollections.observableArrayList();
    private Map<Long, String> programIdMap = new java.util.HashMap<>();
    private Map<Long, String> telescopeIdMap = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        setupSpinners();
        setupPriorityCombo();
        loadPrograms();
        loadTelescopes();
    }

    private void setupSpinners() {
        // Спиннеры для часов
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
        startHourSpinner.setValueFactory(hourFactory);
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));

        // Спиннеры для минут
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        startMinuteSpinner.setValueFactory(minuteFactory);
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Устанавливаем текущую дату по умолчанию
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupPriorityCombo() {
        priorityCombo.getItems().addAll("Высокий (1)", "Средний (2)", "Низкий (3)");
        priorityCombo.setValue("Средний (2)");
    }

    private void loadPrograms() {
        try {
            String response = apiService.get("/programs");
            if (response != null && !response.trim().isEmpty()) {
                List<Map<String, Object>> programs = objectMapper.readValue(
                        response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );

                programsList.clear();
                programIdMap.clear();
                for (Map<String, Object> program : programs) {
                    Long id = program.get("id") != null ? Long.valueOf(program.get("id").toString()) : 0L;
                    String name = program.get("name") != null ? (String) program.get("name") : "N/A";
                    String status = program.get("status") != null ? (String) program.get("status") : "";

                    // Показываем только активные программы
                    if ("ACTIVE".equals(status) || "PLANNED".equals(status)) {
                        String displayName = name + " (" + status + ")";
                        programsList.add(displayName);
                        programIdMap.put(id, displayName);
                    }
                }
                programCombo.setItems(programsList);
                if (!programsList.isEmpty()) {
                    programCombo.setValue(programsList.get(0));
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить программы: " + e.getMessage());
        }
    }

    private void loadTelescopes() {
        try {
            String response = apiService.get("/telescopes");
            if (response != null && !response.trim().isEmpty()) {
                List<Map<String, Object>> telescopes = objectMapper.readValue(
                        response, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );

                telescopesList.clear();
                telescopeIdMap.clear();
                for (Map<String, Object> telescope : telescopes) {
                    Long id = telescope.get("id") != null ? Long.valueOf(telescope.get("id").toString()) : 0L;
                    String name = telescope.get("name") != null ? (String) telescope.get("name") : "N/A";
                    Boolean isOperational = telescope.get("isOperational") != null ?
                            Boolean.valueOf(telescope.get("isOperational").toString()) : true;

                    // Показываем только рабочие телескопы
                    if (isOperational) {
                        String type = telescope.get("type") != null ? (String) telescope.get("type") : "";
                        String displayName = name + " (" + type + ")";
                        telescopesList.add(displayName);
                        telescopeIdMap.put(id, displayName);
                    }
                }
                telescopeCombo.setItems(telescopesList);
                if (!telescopesList.isEmpty()) {
                    telescopeCombo.setValue(telescopesList.get(0));
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить телескопы: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        // Валидация (остается как было)
        if (programCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Выберите научную программу");
            return;
        }
        if (telescopeCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Выберите телескоп");
            return;
        }
        if (objectNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите название объекта наблюдения");
            return;
        }
        if (startDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Выберите дату начала");
            return;
        }
        if (endDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Выберите дату окончания");
            return;
        }

        try {
            // Находим ID выбранной программы
            Long programId = findKeyByValue(programIdMap, programCombo.getValue());
            // Находим ID выбранного телескопа
            Long telescopeId = findKeyByValue(telescopeIdMap, telescopeCombo.getValue());

            // Получаем ID текущего пользователя
            Long userId = SessionContext.getCurrentUserId();

            if (userId == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось определить ID пользователя");
                return;
            }

            // Собираем дату и время
            LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue())
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue())
            );

            // Проверяем, что окончание позже начала
            if (endDateTime.isBefore(startDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Дата окончания должна быть позже даты начала");
                return;
            }

            // Извлекаем приоритет
            String priorityText = priorityCombo.getValue();
            int priority = 2; // по умолчанию средний
            if (priorityText.contains("1")) priority = 1;
            else if (priorityText.contains("3")) priority = 3;

            // Создаем JSON
            String observationJson = String.format(
                    "{\"programId\":%d,\"telescopeId\":%d,\"userId\":%d," +
                            "\"objectName\":\"%s\",\"coordinates\":\"%s\",\"spectralRange\":\"%s\"," +
                            "\"requestedStart\":\"%s\",\"requestedEnd\":\"%s\",\"priority\":%d," +
                            "\"resultDescription\":\"%s\"}",
                    programId,
                    telescopeId,
                    userId,  // Используем полученный ID
                    objectNameField.getText().trim(),
                    coordinatesField.getText().trim(),
                    spectralRangeField.getText().trim(),
                    startDateTime.toString(),
                    endDateTime.toString(),
                    priority,
                    descriptionArea.getText().trim()
            );

            System.out.println("Отправляем заявку: " + observationJson);

            String response = apiService.post("/observations", observationJson);

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Заявка успешно подана!\nНомер заявки: " + responseMap.get("id"));
                clearForm();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось подать заявку: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        objectNameField.getScene().getWindow().hide();
    }

    private void clearForm() {
        objectNameField.clear();
        coordinatesField.clear();
        spectralRangeField.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startHourSpinner.getValueFactory().setValue(12);
        startMinuteSpinner.getValueFactory().setValue(0);
        endHourSpinner.getValueFactory().setValue(12);
        endMinuteSpinner.getValueFactory().setValue(0);
        descriptionArea.clear();
    }

    private Long findKeyByValue(Map<Long, String> map, String value) {
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
