package com.example.Gas_Station_Management_System.controller;

import com.example.Gas_Station_Management_System.entity.Location;
import com.example.Gas_Station_Management_System.entity.LocationType;
import com.example.Gas_Station_Management_System.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    
    @PostMapping("/parent")
    public ResponseEntity<?> createParentLocation(@RequestBody Location location) {
        try {
            Location savedLocation = locationService.saveParentLocation(location);
            return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @PostMapping("/child")
    public ResponseEntity<?> createChildLocation(
            @RequestParam UUID parentId,
            @RequestBody Location location) {
        try {
            Location savedLocation = locationService.saveChildLocation(location, parentId);
            return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<Location> locations = locationService.getAllLocations(page, size, sortBy, direction);
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    
     
     
    @GetMapping("/all")
    public ResponseEntity<List<Location>> getAllLocationsNoPagination() {
        List<Location> locations = locationService.getAllLocations();
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable UUID id) {
        return locationService.getLocationById(id)
                .map(location -> new ResponseEntity<>(location, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getLocationByCode(@PathVariable String code) {
        return locationService.getLocationByCode(code)
                .map(location -> new ResponseEntity<>(location, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLocationByName(@PathVariable String name) {
        return locationService.getLocationByName(name)
                .map(location -> new ResponseEntity<>(location, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable UUID id, @RequestBody Location location) {
        try {
            Location updatedLocation = locationService.updateLocation(id, location);
            return new ResponseEntity<>(updatedLocation, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable UUID id) {
        try {
            locationService.deleteLocation(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Location and all its sub-locations deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Location>> getLocationsByType(@PathVariable String type) {
        try {
            LocationType locationType = LocationType.valueOf(type.toUpperCase());
            List<Location> locations = locationService.getLocationsByType(locationType);
            return new ResponseEntity<>(locations, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid location type. Valid types: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE");
            return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/type/{type}/paged")
    public ResponseEntity<?> getLocationsByTypePaged(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            LocationType locationType = LocationType.valueOf(type.toUpperCase());
            Page<Location> locations = locationService.getLocationsByType(locationType, page, size);
            return new ResponseEntity<>(locations, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid location type. Valid types: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE");
            return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/provinces")
    public ResponseEntity<List<Location>> getAllProvinces() {
        List<Location> provinces = locationService.getAllProvinces();
        return new ResponseEntity<>(provinces, HttpStatus.OK);
    }

    
    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildLocations(@PathVariable UUID parentId) {
        Optional<List<Location>> children = locationService.getChildLocations(parentId);
        return children.map(locations -> new ResponseEntity<>(locations, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/{parentId}/children/paged")
    public ResponseEntity<?> getChildLocationsPaged(
            @PathVariable UUID parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Optional<Page<Location>> children = locationService.getChildLocations(parentId, page, size);
        return children.map(locations -> new ResponseEntity<>(locations, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/{id}/hierarchy-path")
    public ResponseEntity<?> getLocationHierarchyPath(@PathVariable UUID id) {
        Optional<String> path = locationService.getLocationHierarchyPath(id);
        if (path.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("hierarchyPath", path.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    
    @GetMapping("/{id}/all-children")
    public ResponseEntity<?> getAllSubLocations(@PathVariable UUID id) {
        Optional<List<Location>> subLocations = locationService.getAllSubLocations(id);
        return subLocations.map(locations -> new ResponseEntity<>(locations, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/{id}/villages")
    public ResponseEntity<List<Location>> getAllVillagesUnderLocation(@PathVariable UUID id) {
        List<Location> villages = locationService.getAllVillagesUnderLocation(id);
        return new ResponseEntity<>(villages, HttpStatus.OK);
    }

    
    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam String keyword) {
        List<Location> locations = locationService.searchLocations(keyword);
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    
    @GetMapping("/search-by-type")
    public ResponseEntity<?> searchLocationsByType(
            @RequestParam String type,
            @RequestParam String keyword) {
        try {
            LocationType locationType = LocationType.valueOf(type.toUpperCase());
            List<Location> locations = locationService.searchLocationsByType(locationType, keyword);
            return new ResponseEntity<>(locations, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid location type. Valid types: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE");
            return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Boolean>> existsByCode(@PathVariable String code) {
        boolean exists = locationService.existsByCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    @GetMapping("/statistics")
    public ResponseEntity<LocationService.LocationStatistics> getLocationStatistics() {
        LocationService.LocationStatistics stats = locationService.getLocationStatistics();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}
