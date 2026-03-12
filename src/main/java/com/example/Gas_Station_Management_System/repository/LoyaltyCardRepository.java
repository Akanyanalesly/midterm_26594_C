package com.example.Gas_Station_Management_System.repository;

import com.example.Gas_Station_Management_System.entity.LoyaltyCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {

    
    boolean existsByCardNumber(String cardNumber);

    
    boolean existsByCustomerId(Long customerId);

    
    Optional<LoyaltyCard> findByCardNumber(String cardNumber);

    
    Optional<LoyaltyCard> findByCustomerId(Long customerId);

    
    Page<LoyaltyCard> findAll(Pageable pageable);

    
    Page<LoyaltyCard> findByTierLevel(String tierLevel, Pageable pageable);

    /**
     * Find active cards with pagination
     */
    Page<LoyaltyCard> findByIsActiveTrue(Pageable pageable);

    /**
     * Search cards by card number containing keyword
     */
    @Query("SELECT lc FROM LoyaltyCard lc WHERE lc.cardNumber LIKE %:keyword%")
    Page<LoyaltyCard> searchByCardNumberContaining(@Param("keyword") String keyword, Pageable pageable);
}
