package com.observatory.observatorysystem.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Добавьте поле для хранения сессии
    private static String jsessionId = null;

    // Обновите метод login для сохранения сессии
    public Map<String, Object> login(String username, String password) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Сохраняем JSESSIONID из cookies
        response.headers().firstValue("Set-Cookie").ifPresent(cookie -> {
            if (cookie.contains("JSESSIONID")) {
                jsessionId = cookie.split(";")[0];
                System.out.println("=== SAVED SESSION ===");
                System.out.println("JSESSIONID: " + jsessionId);
            }
        });

        return objectMapper.readValue(response.body(), Map.class);
    }

    // Обновите метод get для передачи сессии
    public String get(String endpoint) throws Exception {
        System.out.println("GET запрос: " + BASE_URL + endpoint);
        System.out.println("Текущая сессия: " + jsessionId);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        // Добавляем cookie сессии если есть
        if (jsessionId != null) {
            requestBuilder.header("Cookie", jsessionId);
            System.out.println("Using session: " + jsessionId);
        }

        HttpRequest request = requestBuilder.GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== DEBUG RESPONSE ===");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("Response Headers: " + response.headers());
        System.out.println("Cookies: " + response.headers().allValues("Set-Cookie"));
        System.out.println("======================");

        return response.body();
    }

    // Обновите метод put для передачи сессии
    public String put(String endpoint, String jsonBody) throws Exception {
        System.out.println("PUT запрос: " + BASE_URL + endpoint);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        // Добавляем cookie сессии если есть
        if (jsessionId != null) {
            requestBuilder.header("Cookie", jsessionId);
            System.out.println("Using session: " + jsessionId);
        }

        HttpRequest request = requestBuilder
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== DEBUG PUT RESPONSE ===");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("======================");

        return response.body();
    }

    // Обновите метод post для передачи сессии
    public String post(String endpoint, String jsonBody) throws Exception {
        System.out.println("POST запрос: " + BASE_URL + endpoint);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        // Добавляем cookie сессии если есть
        if (jsessionId != null) {
            requestBuilder.header("Cookie", jsessionId);
            System.out.println("Using session: " + jsessionId);
        }

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== DEBUG POST RESPONSE ===");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("======================");

        return response.body();
    }

    // DELETE запрос
    public String delete(String endpoint) throws Exception {
        System.out.println("DELETE запрос: " + BASE_URL + endpoint);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        // Добавляем cookie сессии если есть
        if (jsessionId != null) {
            requestBuilder.header("Cookie", jsessionId);
            System.out.println("Using session: " + jsessionId);
        }

        HttpRequest request = requestBuilder.DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== DEBUG DELETE RESPONSE ===");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("======================");

        // Проверяем статус код
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new RuntimeException("Доступ запрещен: недостаточно прав для удаления");
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException("Ошибка сервера: " + response.statusCode());
        }

        return response.body();
    }

    // Метод для проверки сессии
    public String checkSession() throws Exception {
        return get("/auth/check-session");
    }

    // Метод для очистки сессии
    public void logout() {
        jsessionId = null;
        System.out.println("Session cleared");
    }
}