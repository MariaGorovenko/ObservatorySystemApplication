package com.observatory.observatorysystem.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
//import lombok.Data;

@Entity
@Table(name = "observation_request")
//@Data
public class ObservationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)  // внешний ключ в БД
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ResearchProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telescope_id", nullable = false)  // внешний ключ в БД
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Telescope telescope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    private String coordinates;

    @Column(name = "spectral_range")
    private String spectralRange;

    @Column(name = "requested_start")
    private LocalDateTime requestedStart;  // TIMESTAMP в БД

    @Column(name = "requested_end")
    private LocalDateTime requestedEnd;

    private Integer priority;  // 1-высокий, 2-средний, 3-низкий

    private String status;     // PENDING, APPROVED, SCHEDULED, COMPLETED, CANCELLED

    @Column(name = "result_description")
    private String resultDescription;

    // ========== ГЕТТЕРЫ ==========
    public Long getId() { return id; }
    public ResearchProgram getProgram() { return program; }
    public Telescope getTelescope() { return telescope; }
    public User getUser() { return user; }
    public String getObjectName() { return objectName; }
    public String getCoordinates() { return coordinates; }
    public String getSpectralRange() { return spectralRange; }
    public LocalDateTime getRequestedStart() { return requestedStart; }
    public LocalDateTime getRequestedEnd() { return requestedEnd; }
    public Integer getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getResultDescription() { return resultDescription; }

    // ========== СЕТТЕРЫ ==========
    public void setId(Long id) { this.id = id; }
    public void setProgram(ResearchProgram program) { this.program = program; }
    public void setTelescope(Telescope telescope) { this.telescope = telescope; }
    public void setUser(User user) { this.user = user; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
    public void setSpectralRange(String spectralRange) { this.spectralRange = spectralRange; }
    public void setRequestedStart(LocalDateTime requestedStart) { this.requestedStart = requestedStart; }
    public void setRequestedEnd(LocalDateTime requestedEnd) { this.requestedEnd = requestedEnd; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }
    public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }
}