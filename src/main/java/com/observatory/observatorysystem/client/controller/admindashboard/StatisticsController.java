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

import java.util.List;
import java.util.Map;

public class StatisticsController {

    @FXML private PieChart requestsByStatusChart;
    @FXML private BarChart<String, Number> requestsByProgramChart;
    @FXML private BarChart<String, Number> telescopeUsageChart;

    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Button updateChartsButton;
    @FXML private Button closeButton;

    @FXML private Label totalRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label completedRequestsLabel;

    private ApiService apiService = new ApiService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadStatistics();
        setupCharts();
    }

    private void loadStatistics() {
        try {
            // Загружаем общую статистику
            String response = apiService.get("/observations/statistics");

            if (response != null && !response.trim().isEmpty()) {
                Map<String, Object> stats = objectMapper.readValue(response, Map.class);

                totalRequestsLabel.setText(stats.getOrDefault("totalRequests", "0").toString());
                pendingRequestsLabel.setText(stats.getOrDefault("pendingRequests", "0").toString());
                approvedRequestsLabel.setText(stats.getOrDefault("approvedRequests", "0").toString());
                completedRequestsLabel.setText(stats.getOrDefault("completedRequests", "0").toString());

                // Загружаем данные для графиков
                loadRequestsByStatus(stats);
                loadRequestsByProgram();
                loadTelescopeUsage();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить статистику: " + e.getMessage());
        }
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

    private void loadRequestsByStatus(Map<String, Object> stats) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Примерные данные - в реальности получаем с сервера
        pieChartData.add(new PieChart.Data("PENDING", 15));
        pieChartData.add(new PieChart.Data("APPROVED", 25));
        pieChartData.add(new PieChart.Data("REJECTED", 5));
        pieChartData.add(new PieChart.Data("COMPLETED", 35));

        requestsByStatusChart.setData(pieChartData);
    }

    private void loadRequestsByProgram() {
        requestsByProgramChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество заявок");

        // Примерные данные
        series.getData().add(new XYChart.Data<>("Экзопланеты", 20));
        series.getData().add(new XYChart.Data<>("Черные дыры", 15));
        series.getData().add(new XYChart.Data<>("Темная материя", 25));
        series.getData().add(new XYChart.Data<>("Картографирование", 10));

        requestsByProgramChart.getData().add(series);
    }

    private void loadTelescopeUsage() {
        telescopeUsageChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Наблюдения");

        // Примерные данные
        series.getData().add(new XYChart.Data<>("РТ-1", 45));
        series.getData().add(new XYChart.Data<>("РТ-64", 30));
        series.getData().add(new XYChart.Data<>("Спектр-Р", 25));
        series.getData().add(new XYChart.Data<>("Гамма-1", 10));

        telescopeUsageChart.getData().add(series);
    }

    @FXML
    private void handleUpdateCharts() {
        showAlert(Alert.AlertType.INFORMATION, "Информация", "Фильтрация статистики по датам будет реализована позже");
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