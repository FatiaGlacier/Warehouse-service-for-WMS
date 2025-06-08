package com.fatia.warehouseservice.repositories;

import com.fatia.warehouseservice.entities.ShelfEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<ShelfEntity, Long> {
}
