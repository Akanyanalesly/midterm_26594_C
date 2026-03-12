package com.example.Gas_Station_Management_System.controller;

import com.example.Gas_Station_Management_System.entity.FuelDiscount;
import com.example.Gas_Station_Management_System.service.FuelDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "*")
public class FuelDiscountController {

    private final FuelDiscountService fuelDiscountService;

    @Autowired
    public FuelDiscountController(FuelDiscountService fuelDiscountService) {
        this.fuelDiscountService = fuelDiscountService;
    }

    
    @PostMapping
    public ResponseEntity<?> createDiscount(@RequestBody FuelDiscount discount) {
        try {
            FuelDiscount savedDiscount = fuelDiscountService.createDiscount(discount);
            return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping
    public ResponseEntity<Page<FuelDiscount>> getAllDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "discountName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<FuelDiscount> discounts = fuelDiscountService.getAllDiscounts(page, size, sortBy, direction);
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiscountById(@PathVariable Long id) {
        return fuelDiscountService.getDiscountById(id)
                .map(discount -> new ResponseEntity<>(discount, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getDiscountByCode(@PathVariable String code) {
        return fuelDiscountService.getDiscountByCode(code)
                .map(discount -> new ResponseEntity<>(discount, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiscount(@PathVariable Long id, @RequestBody FuelDiscount discount) {
        try {
            FuelDiscount updatedDiscount = fuelDiscountService.updateDiscount(id, discount);
            return new ResponseEntity<>(updatedDiscount, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiscount(@PathVariable Long id) {
        try {
            fuelDiscountService.deleteDiscount(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Discount deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/active")
    public ResponseEntity<Page<FuelDiscount>> getActiveDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FuelDiscount> discounts = fuelDiscountService.getActiveDiscounts(page, size);
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    
    @GetMapping("/currently-active")
    public ResponseEntity<List<FuelDiscount>> getCurrentlyActiveDiscounts() {
        List<FuelDiscount> discounts = fuelDiscountService.getCurrentlyActiveDiscounts();
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    
    @PostMapping("/{discountId}/assign")
    public ResponseEntity<?> assignDiscountToCustomer(@PathVariable Long discountId, 
                                                       @RequestParam Long customerId) {
        try {
            FuelDiscount discount = fuelDiscountService.assignDiscountToCustomer(discountId, customerId);
            return new ResponseEntity<>(discount, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @PostMapping("/{discountId}/remove")
    public ResponseEntity<?> removeDiscountFromCustomer(@PathVariable Long discountId, 
                                                         @RequestParam Long customerId) {
        try {
            FuelDiscount discount = fuelDiscountService.removeDiscountFromCustomer(discountId, customerId);
            return new ResponseEntity<>(discount, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

   
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FuelDiscount>> getDiscountsByCustomer(@PathVariable Long customerId) {
        List<FuelDiscount> discounts = fuelDiscountService.getDiscountsByCustomer(customerId);
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    
    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Boolean>> existsByDiscountCode(@PathVariable String code) {
        boolean exists = fuelDiscountService.existsByDiscountCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
