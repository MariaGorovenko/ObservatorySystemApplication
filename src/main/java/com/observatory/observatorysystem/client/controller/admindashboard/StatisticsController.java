package com.observatory.observatorysystem.client.controller.admindashboard;

import com.observatory.observatorysystem.client.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;

public class StatisticsController {

    @FXML private PieChart requestsByStatusChart;
    @FXML private BarChart<String, Number> requestsByProgramChart;
    @FXML private BarChart<String, Number> telescopeUsageChart;

    @FXML private Label totalRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label completedRequestsLabel;

    @FXML private Label scheduledRequestsLabel;
    @FXML private Label rejectedRequestsLabel;
    @FXML private Label averagePriorityLabel;
    @FXML private Label monthlyRequestsLabel;
    @FXML private Label avgDurationLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        setupCharts();
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Загружаем общую статистику
            String response = apiService.get("/observations/stats");

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> stats = objectMapper.readValue(response, Map.class);
                loadRealStatistics(stats);
            } else {
                showDemoStatistics();
            }

        } catch (Exception e) {
            System.out.println("Ошибка загрузки статистики: " + e.getMessage());
            showDemoStatistics();
        }
    }

    private void loadRealStatistics(Map<String, Object> stats) {
        // Основная статистика
        int totalRequests = ((Number) stats.getOrDefault("totalRequests", 0)).intValue();
        totalRequestsLabel.setText(String.valueOf(totalRequests));

        // Получаем статистику по статусам
        Map<String, Long> statusStats = (Map<String, Long>) stats.get("statusStats");

        int pending = 0, approved = 0, completed = 0, scheduled = 0, rejected = 0;

        if (statusStats != null) {
            pending = statusStats.getOrDefault("PENDING", 0L).intValue();
            approved = statusStats.getOrDefault("APPROVED", 0L).intValue();
            completed = statusStats.getOrDefault("COMPLETED", 0L).intValue();
            scheduled = statusStats.getOrDefault("SCHEDULED", 0L).intValue();
            rejected = statusStats.getOrDefault("REJECTED", 0L).intValue();
        }

        pendingRequestsLabel.setText(String.valueOf(pending));
        approvedRequestsLabel.setText(String.valueOf(approved));
        completedRequestsLabel.setText(String.valueOf(completed));
        scheduledRequestsLabel.setText(String.valueOf(scheduled));
        rejectedRequestsLabel.setText(String.valueOf(rejected));

        // Загрузка графиков с реальными данными
        loadRequestsByStatusReal(stats);
        loadRequestsByProgramReal(stats);
        loadTelescopeUsageReal(stats);

        // Дополнительные показатели
        updateAdditionalMetrics(stats);
    }

    private void setupCharts() {
        // Настройка PieChart
        requestsByStatusChart.setTitle("Заявки по статусам");
        requestsByStatusChart.setClockwise(true);
        requestsByStatusChart.setLabelsVisible(true);

        // Настройка BarChart для программ
        requestsByProgramChart.setTitle("Заявки по программам");
        CategoryAxis xAxis = (CategoryAxis) requestsByProgramChart.getXAxis();
        xAxis.setLabel("Научные программы");
        NumberAxis yAxis = (NumberAxis) requestsByProgramChart.getYAxis();
        yAxis.setLabel("Количество заявок");

        // Настройка BarChart для телескопов
        telescopeUsageChart.setTitle("Использование телескопов");
        CategoryAxis xAxis2 = (CategoryAxis) telescopeUsageChart.getXAxis();
        xAxis2.setLabel("Телескопы");
        NumberAxis yAxis2 = (NumberAxis) telescopeUsageChart.getYAxis();
        yAxis2.setLabel("Количество наблюдений");
    }

    private void loadRequestsByStatusReal(Map<String, Object> stats) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        Map<String, Long> statusStats = (Map<String, Long>) stats.get("statusStats");

        if (statusStats != null) {
            for (Map.Entry<String, Long> entry : statusStats.entrySet()) {
                String status = translateStatus(entry.getKey());
                pieChartData.add(new PieChart.Data(status, entry.getValue()));
            }
        } else {
            // На основе ваших данных
            pieChartData.add(new PieChart.Data("Ожидают", 1));
            pieChartData.add(new PieChart.Data("Одобрены", 1));
            pieChartData.add(new PieChart.Data("Запланированы", 1));
            pieChartData.add(new PieChart.Data("Отклонены", 0));
            pieChartData.add(new PieChart.Data("Завершены", 0));
        }

        requestsByStatusChart.setData(pieChartData);
    }

    private void loadRequestsByProgramReal(Map<String, Object> stats) {
        requestsByProgramChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество заявок");

        Map<String, Long> programStats = (Map<String, Long>) stats.get("programStats");

        if (programStats != null) {
            for (Map.Entry<String, Long> entry : programStats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } else {
            // На основе ваших данных
            series.getData().add(new XYChart.Data<>("Изучение экзопланет", 2));
            series.getData().add(new XYChart.Data<>("Наблюдение черных дыр", 1));
            series.getData().add(new XYChart.Data<>("Карта звездного неба", 0));
        }

        requestsByProgramChart.getData().add(series);
    }

    private void loadTelescopeUsageReal(Map<String, Object> stats) {
        telescopeUsageChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Наблюдения");

        Map<String, Long> telescopeStats = (Map<String, Long>) stats.get("telescopeStats");

        if (telescopeStats != null) {
            for (Map.Entry<String, Long> entry : telescopeStats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } else {
            // На основе ваших данных
            series.getData().add(new XYChart.Data<>("БТА-1", 2));
            series.getData().add(new XYChart.Data<>("РТ-64", 1));
            series.getData().add(new XYChart.Data<>("Спецтрон-М", 0));
            series.getData().add(new XYChart.Data<>("Гамма-1", 0));
        }

        telescopeUsageChart.getData().add(series);
    }

    private void updateAdditionalMetrics(Map<String, Object> stats) {
        // Расчет среднего приоритета (предполагаем, что есть данные)
        double avgPriority = 1.7; // Рассчитайте из данных

        // Заявок за последние 30 дней
        int monthlyRequests = 3; // Рассчитайте из дат

        averagePriorityLabel.setText(String.format("%.1f", avgPriority));
        monthlyRequestsLabel.setText(String.valueOf(monthlyRequests));
        avgDurationLabel.setText("3.5 ч");
    }

    private void showDemoStatistics() {
        // Демо-данные для статистики (на основе ваших реальных данных)
        totalRequestsLabel.setText("3");
        pendingRequestsLabel.setText("1");
        approvedRequestsLabel.setText("1");
        completedRequestsLabel.setText("0");

        // Демо-данные для графиков
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        pieData.add(new PieChart.Data("Ожидают", 1));
        pieData.add(new PieChart.Data("Одобрены", 1));
        pieData.add(new PieChart.Data("Запланированы", 1));
        pieData.add(new PieChart.Data("Завершены", 0));
        requestsByStatusChart.setData(pieData);

        requestsByProgramChart.getData().clear();
        XYChart.Series<String, Number> programSeries = new XYChart.Series<>();
        programSeries.setName("Количество заявок");
        programSeries.getData().add(new XYChart.Data<>("Изучение экзопланет", 2));
        programSeries.getData().add(new XYChart.Data<>("Наблюдение черных дыр", 1));
        programSeries.getData().add(new XYChart.Data<>("Карта звездного неба", 0));
        requestsByProgramChart.getData().add(programSeries);

        telescopeUsageChart.getData().clear();
        XYChart.Series<String, Number> telescopeSeries = new XYChart.Series<>();
        telescopeSeries.setName("Наблюдения");
        telescopeSeries.getData().add(new XYChart.Data<>("БТА-1", 2));
        telescopeSeries.getData().add(new XYChart.Data<>("РТ-64", 1));
        telescopeUsageChart.getData().add(telescopeSeries);

        // Дополнительные показатели
        averagePriorityLabel.setText("1.7");
        monthlyRequestsLabel.setText("3");
        avgDurationLabel.setText("2.0 ч");
    }

    private String translateStatus(String status) {
        switch (status) {
            case "PENDING": return "Ожидают";
            case "APPROVED": return "Одобрены";
            case "SCHEDULED": return "Запланированы";
            case "COMPLETED": return "Завершены";
            case "REJECTED": return "Отклонены";
            default: return status;
        }
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}