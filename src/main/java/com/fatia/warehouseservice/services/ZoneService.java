package com.fatia.warehouseservice.services;

import com.fatia.warehouseservice.entities.ZoneEntity;
import com.fatia.warehouseservice.entities.ZoneTypes;
import com.fatia.warehouseservice.models.ZoneModel;
import com.fatia.warehouseservice.repositories.ZoneRepository;
import com.fatia.warehouseservice.requests.AddChildZoneRequest;
import com.fatia.warehouseservice.requests.AddParentZoneRequest;
import com.fatia.warehouseservice.requests.UpdateChildZoneRequest;
import com.fatia.warehouseservice.requests.UpdateParentZoneRequest;
import com.fatia.warehouseservice.responses.UpdateZoneReponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    private final WarehouseService warehouseService;

    //For generating unique short UUID
    private String generateUniqueShortUUID() {
        String uuid;
        do {
            uuid = UUID.randomUUID().toString().substring(0, 8);
        } while (zoneRepository.findByUuid(uuid).isPresent());
        return uuid;
    }

    //For checkin is child object within parent object bounds
    public boolean isChildWithinParentBounds(
            int childOriginX, int childOriginY, int childWidth, int childLength,
            int parentWidth, int parentLength) {

        return childOriginX >= 0
                && childOriginY >= 0
                && (childOriginX + childWidth) <= parentWidth
                && (childOriginY + childLength) <= +parentLength;
    }

    public static boolean isOverlappingWithOthers(
            ZoneEntity targetZone,
            List<ZoneEntity> siblings) {
        int targetOriginX = targetZone.getOriginX();
        int targetEndX = targetOriginX + targetZone.getWidth();
        int targetOriginY = targetZone.getOriginY();
        int targetEndY = targetOriginY + targetZone.getHeight();

        for (ZoneEntity sibling : siblings) {
            if (sibling.getId().equals(targetZone.getId())
                    || targetZone.getId().equals(0L)) {
                continue;
            }

            int siblingOriginX = sibling.getOriginX();
            int siblingEndX = siblingOriginX + sibling.getWidth();
            int siblingOriginY = sibling.getOriginY();
            int siblingEndY = siblingOriginY + sibling.getHeight();

            boolean xOverlap = targetOriginX < siblingEndX && targetEndX > siblingOriginX;
            boolean yOverlap = targetOriginY < siblingEndY && targetEndY > siblingOriginY;

            if (xOverlap && yOverlap) {
                return true;
            }
        }

        return false;
    }

    public List<ZoneModel> getAll() {
        List<ZoneEntity> entities = zoneRepository.findAll();
        List<ZoneModel> models = new ArrayList<>();
        for (ZoneEntity entity : entities) {
            models.add(ZoneModel.toModel(entity));
        }

        return models;
    }

    public ZoneModel getById(Long id) {
        Optional<ZoneEntity> entity = zoneRepository.findById(id);
        if (entity.isEmpty()) {
            throw new RuntimeException("Zone with id " + id + " not found");
        }

        return ZoneModel.toModel(entity.get());
    }

    public ZoneModel addParentZone(AddParentZoneRequest request) {
        if (!warehouseService.isWithinBounds(
                request.getOriginX(),
                request.getOriginY(),
                request.getWidth(),
                request.getHeight())
        ) {
            throw new RuntimeException("Zone with invalid origin");
        }

        List<ZoneEntity> allParentZones = zoneRepository.findByTypeIn(ZoneTypes.getWarehouseChildrenZones());

        ZoneEntity overLappingNewZoneTestEntity = ZoneEntity
                .builder()
                .id(0L)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .height(request.getHeight())
                .build();

        if (isOverlappingWithOthers(
                overLappingNewZoneTestEntity,
                allParentZones
        )) {
            throw new RuntimeException("Zone overlapping with other zones");
        }

        if (!ZoneTypes.isValidZoneType(request.getType())) {
            throw new RuntimeException("Invalid type " + request.getType());
        }

        String uuid = generateUniqueShortUUID();

        String name = request.getType() + "-" + uuid;

        ZoneEntity entity = ZoneEntity
                .builder()
                .name(name)
                .uuid(uuid)
                .type(ZoneTypes.valueOf(request.getType().toUpperCase()))
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .height(request.getHeight())
                .description(request.getDescription())
                .build();

        zoneRepository.saveAndFlush(entity);

        return ZoneModel.toModel(entity);
    }

    public ZoneModel addChildZone(AddChildZoneRequest request) {
        if (!ZoneTypes.isValidZoneType(request.getType())) {
            throw new RuntimeException("Invalid type " + request.getType());
        }

        if (request.getParentZoneId() == null) {
            throw new RuntimeException("Parent zone id is null");
        }

        Optional<ZoneEntity> optionalZoneEntity = zoneRepository.findById(request.getParentZoneId());
        if (optionalZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + request.getParentZoneId() + " not found");
        }

        ZoneEntity parentZone = optionalZoneEntity.get();

        if (!isChildWithinParentBounds(
                request.getOriginX(), request.getOriginY(),
                request.getWidth(), request.getHeight(),
                parentZone.getWidth(), parentZone.getHeight()
        )) {
            throw new RuntimeException("Child zone out of parent zone bounds");
        }

        ZoneEntity overLappingNewChildTestEntity = ZoneEntity
                .builder()
                .id(0L)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .height(request.getHeight())
                .build();

        if (isOverlappingWithOthers(
                overLappingNewChildTestEntity,
                parentZone.getChildZones()
        )) {
            throw new RuntimeException("Child zone overlapping with sibling zones");
        }

        ZoneTypes childType = ZoneTypes.valueOf(request.getType());
        if (!parentZone.getType().getAllowedChildren().contains(childType)) {
            throw new RuntimeException("Child zone with type " + request.getType()
                    + " is not allowed to be child of " + parentZone.getType());
        }

        String uuid = generateUniqueShortUUID();

        String name = request.getType()
                + "-"
                + parentZone.getUuid()
                + "-" + uuid;

        ZoneEntity childZone = ZoneEntity
                .builder()
                .name(name)
                .uuid(uuid)
                .type(childType)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .height(request.getHeight())
                .description(request.getDescription())
                .parentZone(parentZone)
                .build();

        zoneRepository.saveAndFlush(childZone);

        return ZoneModel.toModel(childZone);
    }

    public UpdateZoneReponse updateParentZone(Long id, UpdateParentZoneRequest request) {
        List<String> warnings = new ArrayList<>();
        String status = HttpStatus.OK.toString();

        if (!warehouseService.isWithinBounds(
                request.getOriginX(),
                request.getOriginY(),
                request.getWidth(),
                request.getHeight())
        ) {
            throw new RuntimeException("Zone with invalid origin");
        }

        if (!ZoneTypes.isValidZoneType(request.getType())) {
            throw new RuntimeException("Invalid type " + request.getType());
        }

        Optional<ZoneEntity> optionalZoneEntity = zoneRepository.findById(id);
        if (optionalZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + id + " not found");
        }

        ZoneEntity entity = optionalZoneEntity.get();

        if (!entity.getType().name().equals(request.getType())) {
            String newName = request.getType() + "-" + entity.getUuid();
            entity.setName(newName);

            entity.setType(ZoneTypes.valueOf(request.getType().toUpperCase()));
            warnings.add("Zone type was changed");
            status = "OK_WITH_WARNINGS";
        }

        int oldX = entity.getOriginX();
        int oldY = entity.getOriginY();

        List<ZoneEntity> allParentZones = zoneRepository.findByTypeIn(ZoneTypes.getWarehouseChildrenZones());

        ZoneEntity overLappingNewParentTestEntity = ZoneEntity
                .builder()
                .id(id)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .height(request.getHeight())
                .build();

        if (!isOverlappingWithOthers(
                overLappingNewParentTestEntity,
                allParentZones
        )) {
            entity.setOriginX(request.getOriginX());
            entity.setOriginY(request.getOriginY());
            warnings.add("Zone origin was changed");

            if (entity.getChildZones().isEmpty()) {
                entity.setWidth(request.getWidth());
                entity.setHeight(request.getHeight());
                warnings.add("Bounds was changed");
            } else {
                warnings.add("Bounds was not changed. Zone has " + entity.getChildZones().size() + " child zones");
                status = "OK_WITH_WARNINGS";

                int dx = request.getOriginX() - oldX;
                int dy = request.getOriginY() - oldY;

                if (dx != 0 || dy != 0) {
                    for (ZoneEntity child : entity.getChildZones()) {
                        child.setOriginX(child.getOriginX() + dx);
                        child.setOriginY(child.getOriginY() + dy);
                    }
                    warnings.add("Child zones were shifted with new parent origin");
                }
            }
        } else {
            warnings.add("Zone overlaps with siblings zone bounds");
            status = "OK_WITH_WARNINGS";
        }

        entity.setDescription(request.getDescription());
        zoneRepository.saveAndFlush(entity);

        return UpdateZoneReponse
                .builder()
                .zone(ZoneModel.toModel(entity))
                .status(status)
                .warnings(warnings)
                .build();
    }

    public UpdateZoneReponse updateChildZone(Long id, UpdateChildZoneRequest request) {
        List<String> warnings = new ArrayList<>();
        String status = HttpStatus.OK.toString();

        Optional<ZoneEntity> optionalChildZoneEntity = zoneRepository.findById(id);
        if (optionalChildZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + id + " not found");
        }

        if (request.getParentZoneId() == null) {
            throw new RuntimeException("Parent zone id is null");
        }

        Optional<ZoneEntity> optionalParentZoneEntity = zoneRepository.findById(request.getParentZoneId());
        if (optionalParentZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + request.getParentZoneId() + " not found");
        }

        ZoneEntity parentZone = optionalParentZoneEntity.get();

        if (!isChildWithinParentBounds(
                request.getOriginX(), request.getOriginY(),
                request.getWidth(), request.getHeight(),
                parentZone.getWidth(), parentZone.getHeight()
        )) {
            throw new RuntimeException("Child zone out of parent zone bounds");
        }

        ZoneEntity childZone = optionalChildZoneEntity.get();

        if (childZone.getShelves().isEmpty()) {
            ZoneEntity overLappingNewChildTestEntity = ZoneEntity
                    .builder()
                    .id(id)
                    .originX(request.getOriginX())
                    .originY(request.getOriginY())
                    .width(request.getWidth())
                    .height(request.getHeight())
                    .build();

            if (!isOverlappingWithOthers(
                    overLappingNewChildTestEntity,
                    parentZone.getChildZones()
            )) {
                childZone.setOriginX(request.getOriginX());
                childZone.setOriginY(request.getOriginY());
                childZone.setWidth(request.getWidth());
                childZone.setHeight(request.getHeight());
                warnings.add("Zone origin and bounds was changed");
            } else {
                warnings.add("Zone overlaps with siblings zone bounds");
                status = "OK_WITH_WARNINGS";
            }
        } else {
            warnings.add("Zone bounds were not changed because zone has " + childZone.getShelves().size() + " shelves");
            status = "OK_WITH_WARNINGS";
        }

        childZone.setDescription(request.getDescription());
        zoneRepository.saveAndFlush(childZone);

        return UpdateZoneReponse
                .builder()
                .zone(ZoneModel.toModel(childZone))
                .status(status)
                .warnings(warnings)
                .build();
    }

    public void deleteZoneById(Long id) {
        Optional<ZoneEntity> optionalZoneEntity = zoneRepository.findById(id);
        if (optionalZoneEntity.isEmpty()) {
            throw new RuntimeException("Zone with id " + id + " not found");
        }

        ZoneEntity entity = optionalZoneEntity.get();

        if (!entity.getShelves().isEmpty()
                || !entity.getChildZones().isEmpty()) {
            throw new RuntimeException("Zone cannot ne deleted because it has dependent elements");
        }

        zoneRepository.delete(entity);
    }
}
