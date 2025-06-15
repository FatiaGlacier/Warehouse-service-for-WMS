package com.fatia.warehouseservice.services;

import com.fatia.warehouseservice.entities.FaceDirection;
import com.fatia.warehouseservice.entities.ZoneEntity;
import com.fatia.warehouseservice.entities.ZoneType;
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
    public boolean isChildWithinParentBounds(ZoneEntity child, ZoneEntity parent) {
        int rotationAngle = child.getRotationAngle();

        int drawnWidth = (rotationAngle % 180 == 0) ? child.getWidth() : child.getLength();
        int drawnLength = (rotationAngle % 180 == 0) ? child.getLength() : child.getWidth();

        // Переводимо локальні координати дитини у глобальні
        int childGlobalX = parent.getOriginX() + child.getOriginX();
        int childGlobalY = parent.getOriginY() + child.getOriginY();

        int parentMinX = parent.getOriginX();
        int parentMinY = parent.getOriginY();
        int parentMaxX = parentMinX + parent.getWidth();
        int parentMaxY = parentMinY + parent.getLength();

        int childMaxX = childGlobalX + drawnWidth;
        int childMaxY = childGlobalY + drawnLength;

        return childGlobalX >= parentMinX
                && childGlobalY >= parentMinY
                && childMaxX <= parentMaxX
                && childMaxY <= parentMaxY;
    }

    public static boolean isOverlappingWithOthers(
            ZoneEntity targetZone,
            List<ZoneEntity> siblings) {

        int targetOriginX = targetZone.getOriginX();
        int targetOriginY = targetZone.getOriginY();
        int targetDrawnWidth = targetZone.getRotationAngle() % 180 == 0
                ? targetZone.getWidth()
                : targetZone.getLength();
        int targetDrawnLength = targetZone.getRotationAngle() % 180 == 0
                ? targetZone.getLength()
                : targetZone.getWidth();

        int targetEndX = targetOriginX + targetDrawnWidth;
        int targetEndY = targetOriginY + targetDrawnLength;

        for (ZoneEntity sibling : siblings) {
            if (sibling.getId().equals(targetZone.getId())
                    || targetZone.getId().equals(0L)) {
                continue;
            }

            int siblingOriginX = sibling.getOriginX();
            int siblingOriginY = sibling.getOriginY();
            int siblingDrawnWidth = sibling.getRotationAngle() % 180 == 0
                    ? sibling.getWidth()
                    : sibling.getLength();
            int siblingDrawnLength = sibling.getRotationAngle() % 180 == 0
                    ? sibling.getLength()
                    : sibling.getWidth();

            int siblingEndX = siblingOriginX + siblingDrawnWidth;
            int siblingEndY = siblingOriginY + siblingDrawnLength;

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
                request.getLength())
        ) {
            throw new RuntimeException("Zone with invalid origin");
        }

        List<ZoneEntity> allParentZones = zoneRepository.findByTypeIn(ZoneType.getWarehouseChildrenZones());

        ZoneEntity overLappingNewZoneTestEntity = ZoneEntity
                .builder()
                .id(0L)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .rotationAngle(request.getRotationAngle())
                .build();

        if (isOverlappingWithOthers(
                overLappingNewZoneTestEntity,
                allParentZones
        )) {
            throw new RuntimeException("Zone overlapping with other zones");
        }

        if (!ZoneType.isValidZoneType(request.getType())) {
            throw new RuntimeException("Invalid type " + request.getType());
        }

        String uuid = generateUniqueShortUUID();

        String name = request.getType() + "-" + uuid;

        ZoneEntity entity = ZoneEntity
                .builder()
                .name(name)
                .uuid(uuid)
                .type(ZoneType.valueOf(request.getType().toUpperCase()))
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .description(request.getDescription())
                .rotationAngle(request.getRotationAngle())
                .faceDirection(FaceDirection.valueOf(request.getFaceDirection()))
                .build();

        zoneRepository.saveAndFlush(entity);

        ZoneModel zoneModel = ZoneModel.toModel(entity);
        return zoneModel;
    }

    public ZoneModel addChildZone(AddChildZoneRequest request) {
        if (!ZoneType.isValidZoneType(request.getType())) {
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

        ZoneEntity newChildTestEntity = ZoneEntity
                .builder()
                .id(0L)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .rotationAngle(request.getRotationAngle())
                .build();

        if (!isChildWithinParentBounds(
                newChildTestEntity,
                parentZone
        )) {
            throw new RuntimeException("Child zone out of parent zone bounds");
        }

        if (isOverlappingWithOthers(
                newChildTestEntity,
                parentZone.getChildZones()
        )) {
            throw new RuntimeException("Child zone overlapping with sibling zones");
        }

        ZoneType childType = ZoneType.valueOf(request.getType());
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
                .length(request.getLength())
                .description(request.getDescription())
                .rotationAngle(request.getRotationAngle())
                .faceDirection(FaceDirection.valueOf(request.getFaceDirection()))
                .parentZone(parentZone)
                .build();

        zoneRepository.saveAndFlush(childZone);

        ZoneModel zoneModel = ZoneModel.toModel(childZone);

        return zoneModel;
    }

    public UpdateZoneReponse updateParentZone(Long id, UpdateParentZoneRequest request) {
        List<String> warnings = new ArrayList<>();
        String status = HttpStatus.OK.toString();

        if (!warehouseService.isWithinBounds(
                request.getOriginX(),
                request.getOriginY(),
                request.getWidth(),
                request.getLength())
        ) {
            throw new RuntimeException("Zone with invalid origin");
        }

        if (!ZoneType.isValidZoneType(request.getType())) {
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

            entity.setType(ZoneType.valueOf(request.getType().toUpperCase()));
            warnings.add("Zone type was changed");
            status = "OK_WITH_WARNINGS";
        }

        int oldX = entity.getOriginX();
        int oldY = entity.getOriginY();

        List<ZoneEntity> allParentZones = zoneRepository.findByTypeIn(ZoneType.getWarehouseChildrenZones());

        ZoneEntity overLappingNewParentTestEntity = ZoneEntity
                .builder()
                .id(id)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .build();

        if (!isOverlappingWithOthers(
                overLappingNewParentTestEntity,
                allParentZones
        )) {
            entity.setRotationAngle(request.getRotationAngle());
            entity.setOriginX(request.getOriginX());
            entity.setOriginY(request.getOriginY());
            warnings.add("Zone origin was changed");

            if (entity.getChildZones().isEmpty()) {
                entity.setWidth(request.getWidth());
                entity.setLength(request.getLength());
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

        entity.setFaceDirection(FaceDirection.valueOf(request.getFaceDirection()));
        entity.setDescription(request.getDescription());
        zoneRepository.saveAndFlush(entity);

        ZoneModel zoneModel = ZoneModel.toModel(entity);

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

        ZoneEntity newChildTestEntity = ZoneEntity
                .builder()
                .id(id)
                .originX(request.getOriginX())
                .originY(request.getOriginY())
                .width(request.getWidth())
                .length(request.getLength())
                .rotationAngle(request.getRotationAngle())
                .build();

        if (!isChildWithinParentBounds(
                newChildTestEntity,
                parentZone
        )) {
            throw new RuntimeException("Child zone out of parent zone bounds");
        }

        ZoneEntity childZone = optionalChildZoneEntity.get();

        if (childZone.getShelves().isEmpty()) {


            if (!isOverlappingWithOthers(
                    newChildTestEntity,
                    parentZone.getChildZones()
            )) {
                childZone.setRotationAngle(request.getRotationAngle());
                childZone.setOriginX(request.getOriginX());
                childZone.setOriginY(request.getOriginY());
                childZone.setWidth(request.getWidth());
                childZone.setLength(request.getLength());
                warnings.add("Zone origin and bounds was changed");
            } else {
                warnings.add("Zone overlaps with siblings zone bounds");
                status = "OK_WITH_WARNINGS";
            }
        } else {
            warnings.add("Zone bounds were not changed because zone has " + childZone.getShelves().size() + " shelves");
            status = "OK_WITH_WARNINGS";
        }

        childZone.setFaceDirection(FaceDirection.valueOf(request.getFaceDirection()));
        childZone.setDescription(request.getDescription());
        zoneRepository.saveAndFlush(childZone);

        ZoneModel zoneModel = ZoneModel.toModel(childZone);

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
