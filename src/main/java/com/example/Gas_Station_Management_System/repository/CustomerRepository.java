package com.example.Gas_Station_Management_System.repository;

import com.example.Gas_Station_Management_System.entity.Customer;
import com.example.Gas_Station_Management_System.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // EXISTS BY METHODS
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // FIND BY EMAIL
    Optional<Customer> findByEmail(String email);

    // FIND BY LOCATION (using Location object for self-reference)
    List<Customer> findByLocation(Location location);
    Page<Customer> findByLocation(Location location, Pageable pageable);

    // SORTING METHODS
    List<Customer> findAllByOrderByFullNameAsc();
    List<Customer> findAllByOrderByFullNameDesc();
    List<Customer> findAllByOrderByRegistrationDateAsc();
    List<Customer> findAllByOrderByRegistrationDateDesc();
    List<Customer> findAllByOrderByLoyaltyPointsAsc();
    List<Customer> findAllByOrderByLoyaltyPointsDesc();
}
