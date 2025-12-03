package com.observatory.observatorysystem.repository;

import com.observatory.observatorysystem.entity.ObservationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRequestRepository extends JpaRepository<ObservationRequest, Long> {

    // Найти заявки конкретного пользователя
    List<ObservationRequest> findByUserId(Long userId);

    // Найти заявки по статусу
    List<ObservationRequest> findByStatus(String status);

    // Подсчитать заявки по статусу
    long countByStatus(String status);

    // Статистика по программам (если нужны сложные запросы)
    @Query("SELECT p.name, COUNT(o) FROM ObservationRequest o JOIN o.program p GROUP BY p.name")
    List<Object[]> countObservationsByProgram();

    // Статистика по телескопам
    @Query("SELECT t.name, COUNT(o) FROM ObservationRequest o JOIN o.telescope t GROUP BY t.name")
    List<Object[]> countObservationsByTelescope();
}