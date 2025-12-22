package com.observatory.observatorysystem.client.controller.admindashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

import java.util.Map;

public class StatisticsController {

    // Метки из FXML (все 5 колонок + дополнительные)
//    @FXML private Label totalRequestsLabel;
//    @FXML private Label pendingRequestsLabel;
//    @FXML private Label approvedRequestsLabel;
//    @FXML private Label scheduledRequestsLabel;
//    @FXML private Label completedRequestsLabel;

    // Графики
    @FXML private PieChart requestsByStatusChart;
    @FXML private BarChart<String, Number> requestsByProgramChart;
    @FXML private BarChart<String, Number> telescopeUsageChart;

    private final ApiService apiService = new ApiService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadStatistics(); // Загрузка при открытии окна
    }

    private void loadStatistics() {
        try {
            String json = apiService.getStatistics(); // Метод, который ты добавил
            Map<String, Object> data = mapper.readValue(json, new TypeReference<>() {});

            // Заполняем все метки безопасно
//            totalRequestsLabel.setText(String.valueOf(data.getOrDefault("totalRequests", 0)));
//            pendingRequestsLabel.setText(String.valueOf(data.getOrDefault("pendingRequests", 0)));
//            approvedRequestsLabel.setText(String.valueOf(data.getOrDefault("approvedRequests", 0)));
//            scheduledRequestsLabel.setText(String.valueOf(data.getOrDefault("scheduledRequests", 0)));
//            completedRequestsLabel.setText(String.valueOf(data.getOrDefault("completedRequests", 0)));

            // Круговая диаграмма по статусам
            Map<String, Integer> statusStats = (Map<String, Integer>) data.getOrDefault("statusStats", Map.of());
            requestsByStatusChart.getData().clear();
            statusStats.forEach((status, count) -> {
                PieChart.Data pieData = new PieChart.Data(translateStatus(status), count);
                requestsByStatusChart.getData().add(pieData);
            });

            // Столбчатые диаграммы
            loadBarChart(requestsByProgramChart, (Map<String, Integer>) data.getOrDefault("programStats", Map.of()), "Программы");
            loadBarChart(telescopeUsageChart, (Map<String, Integer>) data.getOrDefault("telescopeStats", Map.of()), "Телескопы");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить статистику: " + e.getMessage());
            e.printStackTrace(); // Для отладки
        }
    }

    private void loadBarChart(BarChart<String, Number> chart, Map<String, Integer> stats, String title) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);
        stats.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().clear();
        chart.getData().add(series);
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Ожидают";
            case "APPROVED" -> "Одобрены";
            case "SCHEDULED" -> "Запланированы";
            case "COMPLETED" -> "Завершены";
            case "REJECTED", "CANCELLED" -> "Отменены";
            default -> status;
        };
    }

    @FXML
    private void handleUpdateCharts() {
        loadStatistics();
        showAlert(Alert.AlertType.INFORMATION, "Обновлено", "Статистика успешно обновлена");
    }

    @FXML
    private void handleClose() {
        requestsByStatusChart.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}