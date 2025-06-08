package com.fatia.warehouseservice.services;

import com.fatia.warehouseservice.entities.ShelfEntity;
import com.fatia.warehouseservice.entities.ZoneEntity;
import com.fatia.warehouseservice.models.ShelfModel;
import com.fatia.warehouseservice.repositories.ShelfRepository;
import com.fatia.warehouseservice.repositories.ZoneRepository;
import com.fatia.warehouseservice.requests.AddShelfRequest;
import com.fatia.warehouseservice.requests.UpdateShelfRequest;
import com.fatia.warehouseservice.responses.UpdateShelfReponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ShelfRepository shelfRepository;

    private final ZoneRepository zoneRepository;

    private final WarehouseService warehouseService;

    public static boolean isShelfWithinColumnBounds(
            int shelfOriginX, int shelfOriginY, int shelfWidth, int shelfLength, int columnWidth, int columnLength) {

        return shelfOriginX >= 0
                && shelfOriginY >= 0
                && (shelfOriginX + shelfWidth) <= columnWidth
                && (shelfOriginY + shelfLength) <= columnLength;
    }

    public List<ShelfModel> getAll() {
        List<ShelfEntity> shelfEntities = shelfRepository.findAll();
        List<ShelfModel> shelfModels = new ArrayList<>();
        for (ShelfEntity shelfEntity : shelfEntities) {
            shelfModels.add(ShelfModel.toModel(shelfEntity));
        }

        return shelfModels;
    }

    public ShelfModel getById(Long id) {
        Optional<ShelfEntity> shelfEntity = shelfRepository.findById(id);
        if (shelfEntity.isEmpty()) {
            throw new RuntimeException("Shelf with id " + id + " not found");
        }

        return ShelfModel.toModel(shelfEntity.get());
    }

    public ShelfModel addShelf(AddShelfRequest request) {
        if (!warehouseService.isWithinBounds(
                request.getOriginX(),
                request.getOriginY(),
                request.getWidth(),
                request.getHeight())
        ) {
            throw new RuntimeException("Shelf with invalid origin");
        }

        String name = "SHELF-NULL-" + request.getLevel();

        ShelfEntity shelfEntity = ShelfEntity
                .builder()
                .name(name)
                .level(request.getLevel())
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .height(request.getHeight())
                .description(request.getDescription())
                .conditions(request.getConditions())
                .build();

        shelfRepository.saveAndFlush(shelfEntity);
        return ShelfModel.toModel(shelfEntity);
    }

    public ShelfModel setColumn(Long shelfId, Long columnId) {
        Optional<ShelfEntity> optionalShelfEntity = shelfRepository.findById(shelfId);
        if (optionalShelfEntity.isEmpty()) {
            throw new RuntimeException("Shelf with id " + shelfId + " not found");
        }

        Optional<ZoneEntity> optionalZoneEntity = zoneRepository.findById(columnId);
        if (optionalZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + columnId + " not found");
        }

        ShelfEntity shelfEntity = optionalShelfEntity.get();
        ZoneEntity zoneEntity = optionalZoneEntity.get();

        if (!zoneEntity.getType().name().equals("COLUMN")) {
            throw new RuntimeException("Zone with id " + columnId + " is not a COLUMN");
        }

        if (!isShelfWithinColumnBounds(
                shelfEntity.getOriginX(), shelfEntity.getOriginY(),
                shelfEntity.getWidth(), shelfEntity.getLength(),
                zoneEntity.getWidth(), zoneEntity.getHeight()
        )) {
            throw new RuntimeException("Shelf with id " + columnId + " out of bounds column " + columnId);
        }

        shelfEntity.setZone(zoneEntity);
        shelfEntity.setName("SHELF-"
                + zoneEntity.getParentZone().getUuid()
                + "-"
                + zoneEntity.getUuid()
                + "-"
                + shelfEntity.getLevel());
        shelfEntity.setActive(true);

        shelfRepository.saveAndFlush(shelfEntity);

        return ShelfModel.toModel(shelfEntity);
    }

    public UpdateShelfReponse updateShelf(Long id, UpdateShelfRequest request) {
        String status = "OK";
        List<String> warnings = new ArrayList<>();

        Optional<ShelfEntity> optionalShelfEntity = shelfRepository.findById(id);
        if (optionalShelfEntity.isEmpty()) {
            throw new RuntimeException("Shelf with id " + id + " not found");
        }

        ShelfEntity shelfEntity = optionalShelfEntity.get();
        ZoneEntity column = shelfEntity.getZone();

        if (!shelfEntity.isOccupied()) {
            if (isShelfWithinColumnBounds(
                    request.getOriginX(), request.getOriginY(),
                    request.getWidth(), request.getLength(),
                    column.getWidth(), column.getHeight()
            )) {
                shelfEntity.setOriginX(request.getOriginX());
                shelfEntity.setOriginY(request.getOriginY());
                shelfEntity.setWidth(request.getWidth());
                shelfEntity.setLength(request.getLength());
                shelfEntity.setHeight(request.getHeight());

                warnings.add("Shelf bounds was updated");
            } else {
                warnings.add("Shelf with id " + id + " out of bounds column " + column.getId());
                status = "OK_WITH_WARNINGS";
            }

            shelfEntity.setLevel(request.getLevel());

        } else {
            warnings.add("Shelf with id " + id + " is occupied");
            status = "OK_WITH_WARNINGS";
        }

        shelfEntity.setDescription(request.getDescription());
        warnings.add("Shelf description was updated");

        shelfEntity.setConditions(request.getConditions());//TODO можливо додати попередження
        warnings.add("Shelf conditions were updated");

        shelfRepository.saveAndFlush(shelfEntity);

        return UpdateShelfReponse
                .builder()
                .shelf(ShelfModel.toModel(shelfEntity))
                .status(status)
                .warnings(warnings)
                .build();
    }

    public void deleteShelf(Long id) {
        Optional<ShelfEntity> optionalShelfEntity = shelfRepository.findById(id);
        if (optionalShelfEntity.isEmpty()) {
            throw new RuntimeException("Shelf with id " + id + " not found");
        }
        ShelfEntity shelfEntity = optionalShelfEntity.get();
        if (shelfEntity.isOccupied()) {
            throw new RuntimeException("Shelf with id " + id + " is occupied");
        }

        shelfRepository.delete(shelfEntity);
    }
}
