package com.fatia.warehouseservice.controllers;

import com.fatia.warehouseservice.models.ShelfModel;
import com.fatia.warehouseservice.requests.AddShelfRequest;
import com.fatia.warehouseservice.requests.UpdateShelfRequest;
import com.fatia.warehouseservice.responses.UpdateShelfReponse;
import com.fatia.warehouseservice.services.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private ShelfService shelfService;

    @GetMapping("/get-all")
    public ResponseEntity<List<ShelfModel>> getAll() {
        return ResponseEntity.ok(shelfService.getAll());
    }

    @GetMapping("/get-shelf/{id}")
    public ResponseEntity<ShelfModel> getShelfById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(shelfService.getById(id));
    }

    @PostMapping("/add-shelf")
    public ResponseEntity<ShelfModel> addShelf(
            @RequestBody AddShelfRequest request
    ) {
        return ResponseEntity.ok(shelfService.addShelf(request));
    }

    @PatchMapping("/set-column/{shelfId}/{columnId}")
    public ResponseEntity<ShelfModel> connectToColumn(
            @PathVariable Long shelfId,
            @PathVariable Long columnId
    ) {
        return ResponseEntity.ok(shelfService.setColumn(shelfId, columnId));
    }

    //TODO update-shelf
    @PutMapping("/update-shelf/{id}")
    public ResponseEntity<UpdateShelfReponse> updateShelf(
            @PathVariable Long id,
            @RequestBody UpdateShelfRequest request
    ) {
        return ResponseEntity.ok(shelfService.updateShelf(id, request));
    }


    //TODO get-best-shelves

    //TODO delete-shelf
    @DeleteMapping("/delete-shelf/{id}")
    public ResponseEntity<String> deleteShelf(
            @PathVariable Long id
    ) {
        shelfService.deleteShelf(id);
        return ResponseEntity.ok("Shelf with id " + id + " was deleted");
    }
}
