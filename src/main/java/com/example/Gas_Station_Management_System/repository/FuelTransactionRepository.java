package com.example.Gas_Station_Management_System.repository;

import com.example.Gas_Station_Management_System.entity.FuelTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface FuelTransactionRepository extends JpaRepository<FuelTransaction, Long> {

    /**
     * EXISTS BY METHOD:
     * - Checks if a transaction with the given code exists
     * - Transaction codes should be unique
     */
    boolean existsByTransactionCode(String transactionCode);

    /**
     * Find transaction by code
     */
    Optional<FuelTransaction> findByTransactionCode(String transactionCode);

    /**
     * PAGINATION AND SORTING:
     * - Returns paginated list of all transactions
     */
    Page<FuelTransaction> findAll(Pageable pageable);

    /**
     * Find transactions by customer ID with pagination
     * - Supports the Many-to-One relationship query
     */
    Page<FuelTransaction> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Find transactions by fuel type with pagination
     */
    Page<FuelTransaction> findByFuelType(String fuelType, Pageable pageable);

    /**
     * Find transactions within date range
     */
    @Query("SELECT ft FROM FuelTransaction ft WHERE ft.transactionDate BETWEEN :startDate AND :endDate")
    Page<FuelTransaction> findByTransactionDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find transactions by station location
     */
    Page<FuelTransaction> findByStationLocationContaining(String location, Pageable pageable);

    
    @Query("SELECT SUM(ft.finalAmount) FROM FuelTransaction ft")
    BigDecimal calculateTotalSales();

    
    @Query("SELECT ft.fuelType, SUM(ft.finalAmount) FROM FuelTransaction ft GROUP BY ft.fuelType")
    List<Object[]> calculateSalesByFuelType();

    /**
     * Find transactions by minimum amount
     */
    Page<FuelTransaction> findByFinalAmountGreaterThanEqual(BigDecimal amount, Pageable pageable);
}
