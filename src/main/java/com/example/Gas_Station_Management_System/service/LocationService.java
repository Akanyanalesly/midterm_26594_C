package com.example.Gas_Station_Management_System.service;

import com.example.Gas_Station_Management_System.entity.Location;
import com.example.Gas_Station_Management_System.entity.LocationType;
import com.example.Gas_Station_Management_System.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Save parent level location (Province - no parent)
     */
    public Location saveParentLocation(Location location) {
        if (locationRepository.existsByCode(location.getCode())) {
            throw new RuntimeException("Location with code '" + location.getCode() + "' already exists");
        }
        // Province has no parent
        location.setParent(null);
        return locationRepository.save(location);
    }

    /**
     * Save child location with parent reference
     * - Pass parent UUID to establish self-reference
     */
    public Location saveChildLocation(Location location, UUID parentId) {
        if (locationRepository.existsByCode(location.getCode())) {
            throw new RuntimeException("Location with code '" + location.getCode() + "' already exists");
        }
        
        Optional<Location> parentOpt = locationRepository.findById(parentId);
        if (parentOpt.isPresent()) {
            location.setParent(parentOpt.get());
        } else {
            throw new RuntimeException("Parent location not found with id: " + parentId);
        }
        
        return locationRepository.save(location);
    }

    /**
     * Save child location by passing parent object directly
     */
    public Location saveChildWithParent(Location location, Location parent) {
        if (locationRepository.existsByCode(location.getCode())) {
            throw new RuntimeException("Location with code '" + location.getCode() + "' already exists");
        }
        location.setParent(parent);
        return locationRepository.save(location);
    }

   
    public Location updateLocation(UUID id, Location locationDetails) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        
        
        if (!location.getCode().equals(locationDetails.getCode()) &&
            locationRepository.existsByCode(locationDetails.getCode())) {
            throw new RuntimeException("Location code already exists");
        }
        
        location.setCode(locationDetails.getCode());
        location.setName(locationDetails.getName());
        location.setDescription(locationDetails.getDescription());
        
        // Only allow parent change if provided
        if (locationDetails.getParent() != null) {
            location.setParent(locationDetails.getParent());
        }
        
        return locationRepository.save(location);
    }

   

    public Optional<Location> getLocationById(UUID id) {
       if (!locationRepository.existsById(id)) {
           return Optional.empty();
       }

        return locationRepository.findById(id);
    }

  
    public Optional<Location> getLocationByCode(String code) {
        if (!locationRepository.existsByCode(code)) {
            return Optional.empty();
        }
        return locationRepository.findByCode(code);
    }

  
    public Optional<Location> getLocationByName(String name) {
        if (!locationRepository.existsByName(name)) {
            return Optional.empty();
        }
        return locationRepository.findByName(name);
    }
  
    public Page<Location> getAllLocations(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return locationRepository.findAll(pageable);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getLocationsByType(LocationType type) {
        return locationRepository.findByLocationType(type);
    }

    public Page<Location> getLocationsByType(LocationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationRepository.findByLocationType(type, pageable);
    }

    public Optional<List<Location>> getChildLocations(UUID parentId) {
        Optional<Location> parentOpt = locationRepository.findById(parentId);
        return parentOpt.map(locationRepository::findByParent);
    }

    public Optional<Page<Location>> getChildLocations(UUID parentId, int page, int size) {
        Optional<Location> parentOpt = locationRepository.findById(parentId);
        Pageable pageable = PageRequest.of(page, size);
        return parentOpt.map(parent -> locationRepository.findByParent(parent, pageable));
    }


    public List<Location> getAllProvinces() {
        return locationRepository.findByParentIsNull();
    }


    public Optional<String> getLocationHierarchyPath(UUID locationId) {
        Optional<Location> locationOpt = locationRepository.findById(locationId);
        return locationOpt.map(location -> {
            StringBuilder path = new StringBuilder(location.getName());
            Location current = location.getParent();
            while (current != null) {
                path.insert(0, current.getName() + " > ");
                current = current.getParent();
            }
            return path.toString();
        });
    }

    
    @Transactional(readOnly = true)
    public Optional<List<Location>> getAllSubLocations(UUID locationId) {
        Optional<Location> locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isPresent()) {
            List<Location> allSubLocations = new ArrayList<>();
            collectSubLocations(locationOpt.get(), allSubLocations);
            return Optional.of(allSubLocations);
        }
        return Optional.empty();
    }

    
    private void collectSubLocations(Location location, List<Location> result) {
        for (Location subLocation : location.getSubLocations()) {
            result.add(subLocation);
            collectSubLocations(subLocation, result);
        }
    }


    @Transactional(readOnly = true)
    public List<Location> getAllVillagesUnderLocation(UUID locationId) {
        Optional<List<Location>> allSubLocations = getAllSubLocations(locationId);
        return allSubLocations.map(locations -> locations.stream()
                .filter(l -> l.getLocationType() == LocationType.VILLAGE)
                .toList()).orElse(List.of());
    }



    public boolean existsByCode(String code) {
        return locationRepository.existsByCode(code);
    }

    public void deleteLocation(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        locationRepository.delete(location);
    }

    public List<Location> searchLocations(String keyword) {
        return locationRepository.findAll().stream()
                .filter(l -> l.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public List<Location> searchLocationsByType(LocationType type, String keyword) {
        return locationRepository.findByLocationType(type).stream()
                .filter(l -> l.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public LocationStatistics getLocationStatistics() {
        LocationStatistics stats = new LocationStatistics();
        stats.setProvinceCount(locationRepository.countByLocationType(LocationType.PROVINCE));
        stats.setDistrictCount(locationRepository.countByLocationType(LocationType.DISTRICT));
        stats.setSectorCount(locationRepository.countByLocationType(LocationType.SECTOR));
        stats.setCellCount(locationRepository.countByLocationType(LocationType.CELL));
        stats.setVillageCount(locationRepository.countByLocationType(LocationType.VILLAGE));
        return stats;
    }

    public static class LocationStatistics {
        private long provinceCount;
        private long districtCount;
        private long sectorCount;
        private long cellCount;
        private long villageCount;

        public long getProvinceCount() { return provinceCount; }
        public void setProvinceCount(long provinceCount) { this.provinceCount = provinceCount; }
        public long getDistrictCount() { return districtCount; }
        public void setDistrictCount(long districtCount) { this.districtCount = districtCount; }
        public long getSectorCount() { return sectorCount; }
        public void setSectorCount(long sectorCount) { this.sectorCount = sectorCount; }
        public long getCellCount() { return cellCount; }
        public void setCellCount(long cellCount) { this.cellCount = cellCount; }
        public long getVillageCount() { return villageCount; }
        public void setVillageCount(long villageCount) { this.villageCount = villageCount; }
    }

}
