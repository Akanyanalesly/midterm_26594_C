package com.example.Gas_Station_Management_System.service;

import com.example.Gas_Station_Management_System.entity.Customer;
import com.example.Gas_Station_Management_System.entity.FuelTransaction;
import com.example.Gas_Station_Management_System.repository.CustomerRepository;
import com.example.Gas_Station_Management_System.repository.FuelTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class FuelTransactionService {

    private final FuelTransactionRepository fuelTransactionRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public FuelTransactionService(FuelTransactionRepository fuelTransactionRepository,
                                  CustomerRepository customerRepository) {
        this.fuelTransactionRepository = fuelTransactionRepository;
        this.customerRepository = customerRepository;
    }

    
    public FuelTransaction createTransaction(FuelTransaction transaction, Long customerId) {
        
        String transactionCode = generateTransactionCode();
        while (fuelTransactionRepository.existsByTransactionCode(transactionCode)) {
            transactionCode = generateTransactionCode();
        }
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        transaction.setTransactionCode(transactionCode);
        transaction.setCustomer(customer);
        transaction.setTransactionDate(LocalDateTime.now());
        
        
        if (transaction.getFinalAmount() == null && transaction.getTotalAmount() != null) {
            transaction.setFinalAmount(transaction.getTotalAmount()
                    .subtract(transaction.getDiscountApplied() != null ? transaction.getDiscountApplied() : BigDecimal.ZERO));
        }
        
        return fuelTransactionRepository.save(transaction);
    }

    
    private String generateTransactionCode() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    
    @Transactional(readOnly = true)
    public Optional<FuelTransaction> getTransactionById(Long id) {
        return fuelTransactionRepository.findById(id);
    }

    
    @Transactional(readOnly = true)
    public Optional<FuelTransaction> getTransactionByCode(String code) {
        return fuelTransactionRepository.findByTransactionCode(code);
    }

    /**
     * Get all transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<FuelTransaction> getAllTransactions(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return fuelTransactionRepository.findAll(pageable);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelTransaction> getTransactionsByCustomer(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return fuelTransactionRepository.findByCustomerId(customerId, pageable);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelTransaction> getTransactionsByFuelType(String fuelType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fuelTransactionRepository.findByFuelType(fuelType, pageable);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fuelTransactionRepository.findByTransactionDateBetween(startDate, endDate, pageable);
    }

    
    @Transactional(readOnly = true)
    public Page<FuelTransaction> getTransactionsByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fuelTransactionRepository.findByStationLocationContaining(location, pageable);
    }

    
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalSales() {
        BigDecimal total = fuelTransactionRepository.calculateTotalSales();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate sales by fuel type
     */
    @Transactional(readOnly = true)
    public List<Object[]> calculateSalesByFuelType() {
        return fuelTransactionRepository.calculateSalesByFuelType();
    }

    /**
     * Delete transaction
     */
    public void deleteTransaction(Long id) {
        FuelTransaction transaction = fuelTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        fuelTransactionRepository.delete(transaction);
    }

    
    @Transactional(readOnly = true)
    public boolean existsByTransactionCode(String code) {
        return fuelTransactionRepository.existsByTransactionCode(code);
    }
}
