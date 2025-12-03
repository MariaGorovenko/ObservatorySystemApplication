package com.observatory.observatorysystem.repository;

import com.observatory.observatorysystem.entity.Telescope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelescopeRepository
        extends JpaRepository<Telescope, Long> {
}