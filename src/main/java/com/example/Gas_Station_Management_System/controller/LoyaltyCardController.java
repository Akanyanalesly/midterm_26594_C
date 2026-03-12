package com.example.Gas_Station_Management_System.controller;

import com.example.Gas_Station_Management_System.entity.LoyaltyCard;
import com.example.Gas_Station_Management_System.service.LoyaltyCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/loyalty-cards")
@CrossOrigin(origins = "*")
public class LoyaltyCardController {

    private final LoyaltyCardService loyaltyCardService;

    @Autowired
    public LoyaltyCardController(LoyaltyCardService loyaltyCardService) {
        this.loyaltyCardService = loyaltyCardService;
    }

    
    @PostMapping
    public ResponseEntity<?> createLoyaltyCard(@RequestParam Long customerId) {
        try {
            LoyaltyCard card = loyaltyCardService.createLoyaltyCard(customerId);
            return new ResponseEntity<>(card, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping
    public ResponseEntity<Page<LoyaltyCard>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "cardNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<LoyaltyCard> cards = loyaltyCardService.getAllCards(page, size, sortBy, direction);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCardById(@PathVariable Long id) {
        return loyaltyCardService.getCardById(id)
                .map(card -> new ResponseEntity<>(card, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/number/{cardNumber}")
    public ResponseEntity<?> getCardByNumber(@PathVariable String cardNumber) {
        return loyaltyCardService.getCardByNumber(cardNumber)
                .map(card -> new ResponseEntity<>(card, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCardByCustomerId(@PathVariable Long customerId) {
        return loyaltyCardService.getCardByCustomerId(customerId)
                .map(card -> new ResponseEntity<>(card, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @PutMapping("/{id}/tier")
    public ResponseEntity<?> updateTier(@PathVariable Long id, @RequestParam String tier) {
        try {
            LoyaltyCard card = loyaltyCardService.updateTier(id, tier);
            return new ResponseEntity<>(card, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @PutMapping("/{id}/points")
    public ResponseEntity<?> addPoints(@PathVariable Long id, @RequestParam Integer points) {
        try {
            LoyaltyCard card = loyaltyCardService.addPoints(id, points);
            return new ResponseEntity<>(card, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCard(@PathVariable Long id) {
        try {
            LoyaltyCard card = loyaltyCardService.deactivateCard(id);
            return new ResponseEntity<>(card, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        try {
            loyaltyCardService.deleteCard(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Loyalty card deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/exists/number/{cardNumber}")
    public ResponseEntity<Map<String, Boolean>> existsByCardNumber(@PathVariable String cardNumber) {
        boolean exists = loyaltyCardService.existsByCardNumber(cardNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
