package com.example.Gas_Station_Management_System.repository;

import com.example.Gas_Station_Management_System.entity.Location;
import com.example.Gas_Station_Management_System.entity.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    // EXISTS BY METHODS
    boolean existsByCode(String code);
    boolean existsByName(String name);

    // FIND BY CODE/NAME
    Optional<Location> findByCode(String code);
    Optional<Location> findByName(String name);

    // FIND BY TYPE
    List<Location> findByLocationType(LocationType locationType);
    Page<Location> findByLocationType(LocationType locationType, Pageable pageable);

    // FIND CHILD LOCATIONS (using parent reference)
    List<Location> findByParent(Location parent);
    Page<Location> findByParent(Location parent, Pageable pageable);
    
    // FIND BY TYPE AND PARENT
    List<Location> findByLocationTypeAndParent(LocationType locationType, Location parent);

    // FIND ROOT LOCATIONS (no parent - provinces)
    List<Location> findByParentIsNull();

    // COUNT METHODS
    long countByLocationType(LocationType locationType);
    long countByParent(Location parent);
}
