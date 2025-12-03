package com.observatory.observatorysystem.repository;

import com.observatory.observatorysystem.entity.ResearchProgram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchProgramRepository
        extends JpaRepository<ResearchProgram, Long> {
}