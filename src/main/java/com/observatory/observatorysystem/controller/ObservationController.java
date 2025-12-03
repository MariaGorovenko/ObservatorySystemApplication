package com.observatory.observatorysystem.controller;

import com.observatory.observatorysystem.entity.ObservationRequest;
import com.observatory.observatorysystem.entity.ResearchProgram;
import com.observatory.observatorysystem.entity.Telescope;
import com.observatory.observatorysystem.entity.User;
import com.observatory.observatorysystem.repository.ObservationRequestRepository;
import com.observatory.observatorysystem.repository.ResearchProgramRepository;
import com.observatory.observatorysystem.repository.TelescopeRepository;
import com.observatory.observatorysystem.repository.UserRepository;
import com.observatory.observatorysystem.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/observations")
public class ObservationController {

    @Autowired
    private ObservationRequestRepository observationRepository;

    @Autowired
    private ResearchProgramRepository programRepository;

    @Autowired
    private TelescopeRepository telescopeRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. GET - получить все заявки с деталями (исправлено для Java 8)
    @GetMapping
    public List<Map<String, Object>> getAllObservations() {
        List<ObservationRequest> observations = observationRepository.findAll();

        return observations.stream().map(obs -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", obs.getId());
            map.put("objectName", obs.getObjectName());
            map.put("coordinates", obs.getCoordinates());
            map.put("spectralRange", obs.getSpectralRange());
            map.put("requestedStart", obs.getRequestedStart());
            map.put("requestedEnd", obs.getRequestedEnd());
            map.put("priority", obs.getPriority());
            map.put("status", obs.getStatus());
            map.put("resultDescription", obs.getResultDescription());

            // Вместо ID выводим связанные данные с проверкой на null
            map.put("programName", obs.getProgram() != null ? obs.getProgram().getName() : null);
            map.put("programId", obs.getProgram() != null ? obs.getProgram().getId() : null);
            map.put("telescopeName", obs.getTelescope() != null ? obs.getTelescope().getName() : null);
            map.put("telescopeId", obs.getTelescope() != null ? obs.getTelescope().getId() : null);
            map.put("userName", obs.getUser() != null ? obs.getUser().getFullName() : null);
            map.put("userId", obs.getUser() != null ? obs.getUser().getId() : null);
            map.put("userUsername", obs.getUser() != null ? obs.getUser().getUsername() : null);

            return map;
        }).collect(Collectors.toList());
    }

    // 2. GET - получить заявку по ID с деталями (исправлено)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getObservationById(@PathVariable Long id) {
        ObservationRequest observation = observationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка с ID " + id + " не найдена"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", observation.getId());
        response.put("objectName", observation.getObjectName());
        response.put("coordinates", observation.getCoordinates());
        response.put("spectralRange", observation.getSpectralRange());
        response.put("requestedStart", observation.getRequestedStart());
        response.put("requestedEnd", observation.getRequestedEnd());
        response.put("priority", observation.getPriority());
        response.put("status", observation.getStatus());
        response.put("resultDescription", observation.getResultDescription());

        // Программа
        Map<String, Object> programMap = new HashMap<>();
        if (observation.getProgram() != null) {
            programMap.put("id", observation.getProgram().getId());
            programMap.put("name", observation.getProgram().getName());
        }
        response.put("program", programMap);

        // Телескоп
        Map<String, Object> telescopeMap = new HashMap<>();
        if (observation.getTelescope() != null) {
            telescopeMap.put("id", observation.getTelescope().getId());
            telescopeMap.put("name", observation.getTelescope().getName());
            telescopeMap.put("type", observation.getTelescope().getType());
        }
        response.put("telescope", telescopeMap);

        // Пользователь
        Map<String, Object> userMap = new HashMap<>();
        if (observation.getUser() != null) {
            userMap.put("id", observation.getUser().getId());
            userMap.put("fullName", observation.getUser().getFullName());
            userMap.put("username", observation.getUser().getUsername());
        }
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }

    // 3. GET - получить заявки конкретного пользователя (исправлено)
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getObservationsByUser(@PathVariable Long userId) {
        List<ObservationRequest> observations = observationRepository.findByUserId(userId);

        return observations.stream().map(obs -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", obs.getId());
            map.put("objectName", obs.getObjectName());
            map.put("coordinates", obs.getCoordinates());
            map.put("spectralRange", obs.getSpectralRange());
            map.put("requestedStart", obs.getRequestedStart());
            map.put("requestedEnd", obs.getRequestedEnd());
            map.put("priority", obs.getPriority());
            map.put("status", obs.getStatus());
            map.put("resultDescription", obs.getResultDescription());

            map.put("programName", obs.getProgram() != null ? obs.getProgram().getName() : null);
            map.put("telescopeName", obs.getTelescope() != null ? obs.getTelescope().getName() : null);
            map.put("userName", obs.getUser() != null ? obs.getUser().getFullName() : null);

            return map;
        }).collect(Collectors.toList());
    }

    // 4. GET - статистика по заявкам (упрощенная версия без сложных запросов)
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<ObservationRequest> allObservations = observationRepository.findAll();

        // Основная статистика
        stats.put("totalRequests", allObservations.size());
        stats.put("pendingRequests", (int) allObservations.stream()
                .filter(obs -> "PENDING".equals(obs.getStatus())).count());
        stats.put("completedRequests", (int) allObservations.stream()
                .filter(obs -> "COMPLETED".equals(obs.getStatus())).count());

        // Статистика по программам (упрощенная)
        Map<String, Long> programStats = allObservations.stream()
                .filter(obs -> obs.getProgram() != null)
                .collect(Collectors.groupingBy(
                        obs -> obs.getProgram().getName(),
                        Collectors.counting()
                ));
        stats.put("programStats", programStats);

        // Статистика по телескопам (упрощенная)
        Map<String, Long> telescopeStats = allObservations.stream()
                .filter(obs -> obs.getTelescope() != null)
                .collect(Collectors.groupingBy(
                        obs -> obs.getTelescope().getName(),
                        Collectors.counting()
                ));
        stats.put("telescopeStats", telescopeStats);

        // Статистика по статусам
        Map<String, Long> statusStats = allObservations.stream()
                .collect(Collectors.groupingBy(
                        ObservationRequest::getStatus,
                        Collectors.counting()
                ));
        stats.put("statusStats", statusStats);

        return stats;
    }

    // 5. POST - создать новую заявку (оставляем как было)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createObservation(@RequestBody ObservationRequestDto observationDto) {
        // Находим связанные сущности по ID
        ResearchProgram program = programRepository.findById(observationDto.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Программа с ID " + observationDto.getProgramId() + " не найдена"));

        Telescope telescope = telescopeRepository.findById(observationDto.getTelescopeId())
                .orElseThrow(() -> new ResourceNotFoundException("Телескоп с ID " + observationDto.getTelescopeId() + " не найдена"));

        User user = userRepository.findById(observationDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + observationDto.getUserId() + " не найден"));

        ObservationRequest observation = new ObservationRequest();
        observation.setProgram(program);
        observation.setTelescope(telescope);
        observation.setUser(user);
        observation.setObjectName(observationDto.getObjectName());
        observation.setCoordinates(observationDto.getCoordinates());
        observation.setSpectralRange(observationDto.getSpectralRange());
        observation.setRequestedStart(observationDto.getRequestedStart());
        observation.setRequestedEnd(observationDto.getRequestedEnd());
        observation.setPriority(observationDto.getPriority());
        observation.setStatus(observationDto.getStatus() != null ? observationDto.getStatus() : "PENDING");
        observation.setResultDescription(observationDto.getResultDescription());

        ObservationRequest savedObservation = observationRepository.save(observation);

        // Возвращаем созданную заявку с деталями
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedObservation.getId());
        response.put("objectName", savedObservation.getObjectName());
        response.put("status", savedObservation.getStatus());
        response.put("programName", savedObservation.getProgram().getName());
        response.put("telescopeName", savedObservation.getTelescope().getName());
        response.put("userName", savedObservation.getUser().getFullName());
        response.put("message", "Заявка успешно создана");

        return ResponseEntity.ok(response);
    }

    // 6. PUT - обновить заявку
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateObservation(@PathVariable Long id,
                                                                 @RequestBody ObservationRequestDto observationDetails) {
        ObservationRequest observation = observationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка с ID " + id + " не найдена"));

        // Обновляем связанные сущности если изменились ID
        if (observationDetails.getProgramId() != null) {
            ResearchProgram program = programRepository.findById(observationDetails.getProgramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Программа не найдена"));
            observation.setProgram(program);
        }

        if (observationDetails.getTelescopeId() != null) {
            Telescope telescope = telescopeRepository.findById(observationDetails.getTelescopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Телескоп не найден"));
            observation.setTelescope(telescope);
        }

        if (observationDetails.getUserId() != null) {
            User user = userRepository.findById(observationDetails.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            observation.setUser(user);
        }

        if (observationDetails.getObjectName() != null) {
            observation.setObjectName(observationDetails.getObjectName());
        }
        if (observationDetails.getCoordinates() != null) {
            observation.setCoordinates(observationDetails.getCoordinates());
        }
        if (observationDetails.getSpectralRange() != null) {
            observation.setSpectralRange(observationDetails.getSpectralRange());
        }
        if (observationDetails.getRequestedStart() != null) {
            observation.setRequestedStart(observationDetails.getRequestedStart());
        }
        if (observationDetails.getRequestedEnd() != null) {
            observation.setRequestedEnd(observationDetails.getRequestedEnd());
        }
        if (observationDetails.getPriority() != null) {
            observation.setPriority(observationDetails.getPriority());
        }
        if (observationDetails.getStatus() != null) {
            observation.setStatus(observationDetails.getStatus());
        }
        if (observationDetails.getResultDescription() != null) {
            observation.setResultDescription(observationDetails.getResultDescription());
        }

        ObservationRequest updatedObservation = observationRepository.save(observation);

        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedObservation.getId());
        response.put("objectName", updatedObservation.getObjectName());
        response.put("status", updatedObservation.getStatus());
        response.put("message", "Заявка успешно обновлена");

        return ResponseEntity.ok(response);
    }

    // 7. DELETE - удалить заявку
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteObservation(@PathVariable Long id) {
        ObservationRequest observation = observationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка с ID " + id + " не найдена"));

        observationRepository.delete(observation);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Заявка с ID " + id + " успешно удалена");

        return ResponseEntity.ok(response);
    }

    // 8. GET - фильтрация заявок (упрощенная)
    @GetMapping("/filter")
    public List<Map<String, Object>> filterObservations(
            @RequestParam(required = false) Long telescopeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String status) {

        List<ObservationRequest> allObservations = observationRepository.findAll();

        // Применяем фильтры
        return allObservations.stream()
                .filter(obs -> telescopeId == null ||
                        (obs.getTelescope() != null && obs.getTelescope().getId().equals(telescopeId)))
                .filter(obs -> status == null || status.equals(obs.getStatus()))
                .filter(obs -> priority == null || priority.equals(obs.getPriority()))
                .map(obs -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", obs.getId());
                    map.put("objectName", obs.getObjectName());
                    map.put("requestedStart", obs.getRequestedStart());
                    map.put("requestedEnd", obs.getRequestedEnd());
                    map.put("priority", obs.getPriority());
                    map.put("status", obs.getStatus());
                    map.put("programName", obs.getProgram() != null ? obs.getProgram().getName() : null);
                    map.put("telescopeName", obs.getTelescope() != null ? obs.getTelescope().getName() : null);
                    map.put("userName", obs.getUser() != null ? obs.getUser().getFullName() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }

    // DTO для запросов создания/обновления заявок
    public static class ObservationRequestDto {
        private Long programId;
        private Long telescopeId;
        private Long userId;
        private String objectName;
        private String coordinates;
        private String spectralRange;
        private LocalDateTime requestedStart;
        private LocalDateTime requestedEnd;
        private Integer priority;
        private String status;
        private String resultDescription;

        // Геттеры и сеттеры
        public Long getProgramId() { return programId; }
        public void setProgramId(Long programId) { this.programId = programId; }

        public Long getTelescopeId() { return telescopeId; }
        public void setTelescopeId(Long telescopeId) { this.telescopeId = telescopeId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }

        public String getCoordinates() { return coordinates; }
        public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

        public String getSpectralRange() { return spectralRange; }
        public void setSpectralRange(String spectralRange) { this.spectralRange = spectralRange; }

        public LocalDateTime getRequestedStart() { return requestedStart; }
        public void setRequestedStart(LocalDateTime requestedStart) { this.requestedStart = requestedStart; }

        public LocalDateTime getRequestedEnd() { return requestedEnd; }
        public void setRequestedEnd(LocalDateTime requestedEnd) { this.requestedEnd = requestedEnd; }

        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getResultDescription() { return resultDescription; }
        public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }
    }
}