
package com.campus.service;

import com.campus.model.Building;
import com.campus.repository.JsonDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CampusService {

    private final JsonDataRepository repository;

    public CampusService(JsonDataRepository repository) {
        this.repository = repository;
    }

    public List<Building> getAllBuildings() {
        return repository.findAllBuildings();
    }

    public List<Building> getBuildingsByCategory(String category) {
        return repository.findAllBuildings().stream()
                .filter(building -> building.getCategory() != null
                        && building.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public Optional<Building> getBuildingById(String id) {
        return repository.findBuildingById(id);
    }

    public void reloadData() {
        repository.loadData();
    }
}
