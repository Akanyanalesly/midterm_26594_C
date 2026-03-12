package com.example.Gas_Station_Management_System.service;

import com.example.Gas_Station_Management_System.entity.Customer;
import com.example.Gas_Station_Management_System.entity.FuelDiscount;
import com.example.Gas_Station_Management_System.repository.CustomerRepository;
import com.example.Gas_Station_Management_System.repository.FuelDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class FuelDiscountService {

    private final FuelDiscountRepository fuelDiscountRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public FuelDiscountService(FuelDiscountRepository fuelDiscountRepository,
                               CustomerRepository customerRepository) {
        this.fuelDiscountRepository = fuelDiscountRepository;
        this.customerRepository = customerRepository;
    }

    
    public FuelDiscount createDiscount(FuelDiscount discount) {
        if (fuelDiscountRepository.existsByDiscountCode(discount.getDiscountCode())) {
            throw new RuntimeException("Discount with code '" + discount.getDiscountCode() + "' already exists");
        }
        
        return fuelDiscountRepository.save(discount);
    }

    
    public FuelDiscount updateDiscount(Long id, FuelDiscount discountDetails) {
        FuelDiscount discount = fuelDiscountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));
        
        if (!discount.getDiscountCode().equals(discountDetails.getDiscountCode()) &&
            fuelDiscountRepository.existsByDiscountCode(discountDetails.getDiscountCode())) {
            throw new RuntimeException("Discount code already exists");
        }
        
        discount.setDiscountCode(discountDetails.getDiscountCode());
        discount.setDiscountName(discountDetails.getDiscountName());
        discount.setDescription(discountDetails.getDescription());
        discount.setDiscountPercentage(discountDetails.getDiscountPercentage());
        discount.setDiscountAmount(discountDetails.getDiscountAmount());
        discount.setMinimumPurchase(discountDetails.getMinimumPurchase());
        discount.setStartDate(discountDetails.getStartDate());
        discount.setEndDate(discountDetails.getEndDate());
        discount.setApplicableFuelTypes(discountDetails.getApplicableFuelTypes());
        discount.setIsActive(discountDetails.getIsActive());
        
        return fuelDiscountRepository.save(discount);
    }

    
    @Transactional(readOnly = true)
    public Optional<FuelDiscount> getDiscountById(Long id) {
        return fuelDiscountRepository.findById(id);
    }

    
    @Transactional(readOnly = true)
    public Optional<FuelDiscount> getDiscountByCode(String code) {
        return fuelDiscountRepository.findByDiscountCode(code);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelDiscount> getAllDiscounts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return fuelDiscountRepository.findAll(pageable);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelDiscount> getActiveDiscounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fuelDiscountRepository.findByIsActiveTrue(pageable);
    }

    
    @Transactional(readOnly = true)
    public List<FuelDiscount> getCurrentlyActiveDiscounts() {
        return fuelDiscountRepository.findCurrentlyActiveDiscounts(LocalDateTime.now());
    }

    
    public FuelDiscount assignDiscountToCustomer(Long discountId, Long customerId) {
        FuelDiscount discount = fuelDiscountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + discountId));
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        discount.addCustomer(customer);
        return fuelDiscountRepository.save(discount);
    }

    /**
     * MANY-TO-MANY RELATIONSHIP:
     * Remove discount from customer
     * - Removes customer from discount's customer list
     * - Join table 'customer_discounts' is updated automatically
     */
    public FuelDiscount removeDiscountFromCustomer(Long discountId, Long customerId) {
        FuelDiscount discount = fuelDiscountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + discountId));
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        discount.removeCustomer(customer);
        return fuelDiscountRepository.save(discount);
    }

    
    @Transactional(readOnly = true)
    public List<FuelDiscount> getDiscountsByCustomer(Long customerId) {
        return fuelDiscountRepository.findByCustomerId(customerId);
    }

    
    public void deleteDiscount(Long id) {
        FuelDiscount discount = fuelDiscountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));
        fuelDiscountRepository.delete(discount);
    }

    
    @Transactional(readOnly = true)
    public boolean existsByDiscountCode(String code) {
        return fuelDiscountRepository.existsByDiscountCode(code);
    }
}
