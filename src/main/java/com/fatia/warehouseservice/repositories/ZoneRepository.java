package com.fatia.warehouseservice.repositories;

import com.fatia.warehouseservice.entities.ZoneEntity;
import com.fatia.warehouseservice.entities.ZoneTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<ZoneEntity, Long> {
    Optional<ZoneEntity> findByUuid(String uuid);

    List<ZoneEntity> findByTypeIn(List<ZoneTypes> types);
}
