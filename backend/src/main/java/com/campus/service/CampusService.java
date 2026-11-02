package com.campus.service;

import com.campus.model.Building;
import com.campus.repository.JsonDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Layer: Business logic for campus facilities and buildings.
 */
@Service
public class CampusService {

    private final JsonDataRepository repository;

    public CampusService(JsonDataRepository repository) {
        this.repository = repository;
    }

    public List<Building> getAllBuildings() {
        return repository.findAllBuildings();
    }

    public Optional<Building> getBuildingById(String id) {
        return repository.findBuildingById(id);
    }

    public List<Building> getBuildingsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return getAllBuildings();
        }
        return repository.findAllBuildings().stream()
                .filter(b -> category.equalsIgnoreCase(b.getCategory()))
                .collect(Collectors.toList());
    }
}
