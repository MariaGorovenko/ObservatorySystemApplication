package com.observatory.observatorysystem.controller;

import com.observatory.observatorysystem.entity.ResearchProgram;
import com.observatory.observatorysystem.entity.User;
import com.observatory.observatorysystem.repository.ResearchProgramRepository;
import com.observatory.observatorysystem.repository.UserRepository;
import com.observatory.observatorysystem.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    @Autowired
    private ResearchProgramRepository programRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. GET - получить все программы
    @GetMapping
    public List<ResearchProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    // 2. GET - получить программу по ID
    @GetMapping("/{id}")
    public ResponseEntity<ResearchProgram> getProgramById(@PathVariable Long id) {
        ResearchProgram program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Программа с ID " + id + " не найдена"));
        return ResponseEntity.ok(program);
    }

    // 3. POST - создать новую программу
    @PostMapping
    public ResponseEntity<ResearchProgram> createProgram(@RequestBody ProgramRequest programRequest) {
        // Находим ученого по ID
        User leadScientist = userRepository.findById(programRequest.getLeadScientistId())
                .orElseThrow(() -> new ResourceNotFoundException("Ученый с ID " + programRequest.getLeadScientistId() + " не найден"));

        ResearchProgram program = new ResearchProgram();
        program.setName(programRequest.getName());
        program.setDescription(programRequest.getDescription());
        program.setStartDate(programRequest.getStartDate());
        program.setEndDate(programRequest.getEndDate());
        program.setStatus(programRequest.getStatus());
        program.setBudget(programRequest.getBudget());
        program.setLeadScientist(leadScientist);  // Устанавливаем ссылку на пользователя

        ResearchProgram savedProgram = programRepository.save(program);
        return ResponseEntity.ok(savedProgram);
    }

    // 4. PUT - обновить программу
    @PutMapping("/{id}")
    public ResponseEntity<ResearchProgram> updateProgram(@PathVariable Long id,
                                                         @RequestBody ProgramRequest programDetails) {
        ResearchProgram program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Программа с ID " + id + " не найдена"));

        // Если меняется ученый - находим нового
        if (programDetails.getLeadScientistId() != null) {
            User leadScientist = userRepository.findById(programDetails.getLeadScientistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ученый с ID " + programDetails.getLeadScientistId() + " не найден"));
            program.setLeadScientist(leadScientist);
        }

        program.setName(programDetails.getName());
        program.setDescription(programDetails.getDescription());
        program.setStartDate(programDetails.getStartDate());
        program.setEndDate(programDetails.getEndDate());
        program.setStatus(programDetails.getStatus());
        program.setBudget(programDetails.getBudget());

        ResearchProgram updatedProgram = programRepository.save(program);
        return ResponseEntity.ok(updatedProgram);
    }

    // 5. DELETE - удалить программу
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProgram(@PathVariable Long id) {
        ResearchProgram program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Программа с ID " + id + " не найдена"));

        programRepository.delete(program);
        return ResponseEntity.ok("Программа с ID " + id + " успешно удалена");
    }

    // DTO для запросов создания/обновления
    public static class ProgramRequest {
        private String name;
        private String description;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String status;
        private java.math.BigDecimal budget;
        private Long leadScientistId;  // ID ученого вместо строки

        // Геттеры и сеттеры
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }

        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public java.math.BigDecimal getBudget() { return budget; }
        public void setBudget(java.math.BigDecimal budget) { this.budget = budget; }

        public Long getLeadScientistId() { return leadScientistId; }
        public void setLeadScientistId(Long leadScientistId) { this.leadScientistId = leadScientistId; }
    }
}