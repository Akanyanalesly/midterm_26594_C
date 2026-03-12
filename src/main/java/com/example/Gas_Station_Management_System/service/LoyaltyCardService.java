package com.example.Gas_Station_Management_System.service;

import com.example.Gas_Station_Management_System.entity.Customer;
import com.example.Gas_Station_Management_System.entity.LoyaltyCard;
import com.example.Gas_Station_Management_System.repository.CustomerRepository;
import com.example.Gas_Station_Management_System.repository.LoyaltyCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class LoyaltyCardService {

    private final LoyaltyCardRepository loyaltyCardRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public LoyaltyCardService(LoyaltyCardRepository loyaltyCardRepository,
                              CustomerRepository customerRepository) {
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.customerRepository = customerRepository;
    }

    
    public LoyaltyCard createLoyaltyCard(Long customerId) {
        
        if (loyaltyCardRepository.existsByCustomerId(customerId)) {
            throw new RuntimeException("Customer already has a loyalty card");
        }
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        LoyaltyCard card = new LoyaltyCard();
        card.setCardNumber(generateCardNumber());
        card.setCustomer(customer);
        card.setTierLevel("BRONZE");
        card.setTotalPoints(0);
        card.setIsActive(true);
        
        return loyaltyCardRepository.save(card);
    }

    
    private String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = "LC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (loyaltyCardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    
    @Transactional(readOnly = true)
    public Optional<LoyaltyCard> getCardById(Long id) {
        return loyaltyCardRepository.findById(id);
    }

    
    @Transactional(readOnly = true)
    public Optional<LoyaltyCard> getCardByNumber(String cardNumber) {
        return loyaltyCardRepository.findByCardNumber(cardNumber);
    }

    
    @Transactional(readOnly = true)
    public Optional<LoyaltyCard> getCardByCustomerId(Long customerId) {
        return loyaltyCardRepository.findByCustomerId(customerId);
    }

    
    @Transactional(readOnly = true)
    public Page<LoyaltyCard> getAllCards(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return loyaltyCardRepository.findAll(pageable);
    }

   
    public LoyaltyCard updateTier(Long id, String tier) {
        LoyaltyCard card = loyaltyCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        
        card.setTierLevel(tier);
        return loyaltyCardRepository.save(card);
    }

    
    public LoyaltyCard addPoints(Long id, Integer points) {
        LoyaltyCard card = loyaltyCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        
        card.setTotalPoints(card.getTotalPoints() + points);
        
       
        updateTierBasedOnPoints(card);
        
        return loyaltyCardRepository.save(card);
    }

    
    private void updateTierBasedOnPoints(LoyaltyCard card) {
        int points = card.getTotalPoints();
        if (points >= 10000) {
            card.setTierLevel("PLATINUM");
        } else if (points >= 5000) {
            card.setTierLevel("GOLD");
        } else if (points >= 1000) {
            card.setTierLevel("SILVER");
        } else {
            card.setTierLevel("BRONZE");
        }
    }

    
    public LoyaltyCard deactivateCard(Long id) {
        LoyaltyCard card = loyaltyCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        
        card.setIsActive(false);
        return loyaltyCardRepository.save(card);
    }

    
    public void deleteCard(Long id) {
        LoyaltyCard card = loyaltyCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        loyaltyCardRepository.delete(card);
    }

    
    @Transactional(readOnly = true)
    public boolean existsByCardNumber(String cardNumber) {
        return loyaltyCardRepository.existsByCardNumber(cardNumber);
    }
}
