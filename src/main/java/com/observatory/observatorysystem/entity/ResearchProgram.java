package com.observatory.observatorysystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
//import lombok.Data;

// Аннотации КЛАССА
@Entity                 // Сущность БД
@Table(name = "research_program")  // Название таблицы в БД
//@Data
public class ResearchProgram {

    @Id                 // Это поле - первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент в БД
    private Long id;    // BIGSERIAL в PostgreSQL

    // Обычное поле (столбец в таблице)
    @Column(nullable = false)  // NOT NULL в БД
    private String name;       // VARCHAR(255) в PostgreSQL

    // Поле без @Column - создастся с именем поля
    private String description; // TEXT в PostgreSQL

    // Поле с кастомным именем столбца в БД
    @Column(name = "start_date")  // В БД будет start_date, а не startDate
    private LocalDate startDate;  // DATE в PostgreSQL

    @Column(name = "end_date")
    private LocalDate endDate;

    // Простое поле
    private String status;     // VARCHAR(255) - ACTIVE, COMPLETED, PLANNED

    // Поле для денег/десятичных чисел
    private BigDecimal budget; // DECIMAL(15,2) в PostgreSQL

    @ManyToOne
    @JoinColumn(name = "lead_scientist_id")
    private User leadScientist;

    // ========== ГЕТТЕРЫ ==========
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public BigDecimal getBudget() { return budget; }
    public User getLeadScientist() { return leadScientist; }

    // ========== СЕТТЕРЫ ==========
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setStatus(String status) { this.status = status; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public void setLeadScientist(User leadScientist) { this.leadScientist = leadScientist; }
}
