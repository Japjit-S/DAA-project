package com.campus.controller;

import com.campus.dto.ApiResponse;
import com.campus.model.Building;
import com.campus.service.CampusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing campus building information and metadata.
 */
@RestController
@RequestMapping("/api/buildings")
public class CampusController {

    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    /**
     * Retrieves all campus buildings.
     */
    @GetMapping
    public ResponseEntity<List<Building>> getAllBuildings(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(campusService.getBuildingsByCategory(category));
        }
        return ResponseEntity.ok(campusService.getAllBuildings());
    }

    /**
     * Retrieves a single building by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBuildingById(@PathVariable String id) {
        return campusService.getBuildingById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Building not found with id: " + id)));
    }

    /**
     * Hot reload data files from disk into memory.
     */
    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<String>> reloadData() {
        campusService.reloadData();
        return ResponseEntity.ok(ApiResponse.success("Campus datasets reloaded from disk", "OK"));
    }
}
