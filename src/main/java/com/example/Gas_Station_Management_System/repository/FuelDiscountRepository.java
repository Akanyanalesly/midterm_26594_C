package com.example.Gas_Station_Management_System.repository;

import com.example.Gas_Station_Management_System.entity.FuelDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface FuelDiscountRepository extends JpaRepository<FuelDiscount, Long> {

    
    boolean existsByDiscountCode(String discountCode);

    
    boolean existsByDiscountName(String discountName);

    
    Optional<FuelDiscount> findByDiscountCode(String discountCode);

    /**
     * PAGINATION AND SORTING:
     * - Returns paginated list of all discounts
     */
    Page<FuelDiscount> findAll(Pageable pageable);

    /**
     * Find active discounts with pagination
     */
    Page<FuelDiscount> findByIsActiveTrue(Pageable pageable);

    /**
     * Find discounts by applicable fuel type
     */
    @Query("SELECT fd FROM FuelDiscount fd WHERE fd.applicableFuelTypes LIKE %:fuelType%")
    Page<FuelDiscount> findByApplicableFuelType(@Param("fuelType") String fuelType, Pageable pageable);

    /**
     * Find currently active discounts (within date range)
     */
    @Query("SELECT fd FROM FuelDiscount fd WHERE fd.isActive = true AND fd.startDate <= :now AND fd.endDate >= :now")
    List<FuelDiscount> findCurrentlyActiveDiscounts(@Param("now") LocalDateTime now);

    /**
     * Find discounts assigned to a specific customer
     * - Supports the Many-to-Many relationship query
     */
    @Query("SELECT fd FROM FuelDiscount fd JOIN fd.customers c WHERE c.id = :customerId")
    List<FuelDiscount> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Search discounts by name containing keyword
     */
    @Query("SELECT fd FROM FuelDiscount fd WHERE LOWER(fd.discountName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FuelDiscount> searchByNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
