package com.observatory.observatorysystem.entity;

import jakarta.persistence.*;
//import lombok.Data;

@Entity
@Table(name = "telescope")
//@Data
public class Telescope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "is_operational")
    private Boolean isOperational = true;

    private String location;

    // ========== ГЕТТЕРЫ ==========
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public Boolean getIsOperational() { return isOperational; }
    public String getLocation() { return location; }

    // ========== СЕТТЕРЫ ==========
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setIsOperational(Boolean isOperational) { this.isOperational = isOperational; }
    public void setLocation(String location) { this.location = location; }
}