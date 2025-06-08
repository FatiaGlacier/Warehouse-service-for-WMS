package com.fatia.warehouseservice.controllers;

import com.fatia.warehouseservice.models.ZoneModel;
import com.fatia.warehouseservice.requests.AddChildZoneRequest;
import com.fatia.warehouseservice.requests.AddParentZoneRequest;
import com.fatia.warehouseservice.requests.UpdateChildZoneRequest;
import com.fatia.warehouseservice.requests.UpdateParentZoneRequest;
import com.fatia.warehouseservice.responses.UpdateZoneReponse;
import com.fatia.warehouseservice.services.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/zone")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping("/get-all")
    public ResponseEntity<List<ZoneModel>> getAll() {
        return ResponseEntity.ok(zoneService.getAll());
    }

    @GetMapping("/get-zone/{id}")
    public ResponseEntity<ZoneModel> getZoneById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(zoneService.getById(id));
    }

    @PostMapping("/add-parent-zone")
    public ResponseEntity<ZoneModel> addParentZone(
            @RequestBody AddParentZoneRequest request
    ) {
        return ResponseEntity.ok(zoneService.addParentZone(request));
    }

    @PostMapping("/add-child-zone")
    public ResponseEntity<ZoneModel> addChildZone(
            @RequestBody AddChildZoneRequest request
    ) {
        return ResponseEntity.ok(zoneService.addChildZone(request));
    }

    @PutMapping("/update-parent-zone/{id}")
    public ResponseEntity<UpdateZoneReponse> updateZone(
            @RequestParam Long id,
            @RequestBody UpdateParentZoneRequest request
    ) {
        return ResponseEntity.ok(zoneService.updateParentZone(id, request));
    }

    @PutMapping("/update-child-zone/{id}")
    public ResponseEntity<UpdateZoneReponse> updateZone(
            @RequestParam Long id,
            @RequestBody UpdateChildZoneRequest request
    ) {
        return ResponseEntity.ok(zoneService.updateChildZone(id, request));
    }

    @DeleteMapping("/delete-zone/{id}")
    public ResponseEntity<String> deleteZone(
            @PathVariable Long id
    ) {
        zoneService.deleteZoneById(id);
        return ResponseEntity.ok("Deleted zone with id " + id);
    }
}
