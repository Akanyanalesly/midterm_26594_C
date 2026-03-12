package com.example.Gas_Station_Management_System.controller;

import com.example.Gas_Station_Management_System.entity.FuelTransaction;
import com.example.Gas_Station_Management_System.service.FuelTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class FuelTransactionController {

    private final FuelTransactionService fuelTransactionService;

    @Autowired
    public FuelTransactionController(FuelTransactionService fuelTransactionService) {
        this.fuelTransactionService = fuelTransactionService;
    }

    
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody FuelTransaction transaction, 
                                                @RequestParam Long customerId) {
        try {
            FuelTransaction savedTransaction = fuelTransactionService.createTransaction(transaction, customerId);
            return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

   
    @GetMapping
    public ResponseEntity<Page<FuelTransaction>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Page<FuelTransaction> transactions = fuelTransactionService.getAllTransactions(page, size, sortBy, direction);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

   
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return fuelTransactionService.getTransactionById(id)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getTransactionByCode(@PathVariable String code) {
        return fuelTransactionService.getTransactionByCode(code)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<FuelTransaction>> getTransactionsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FuelTransaction> transactions = fuelTransactionService.getTransactionsByCustomer(customerId, page, size);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    
    @GetMapping("/fuel-type/{fuelType}")
    public ResponseEntity<Page<FuelTransaction>> getTransactionsByFuelType(
            @PathVariable String fuelType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FuelTransaction> transactions = fuelTransactionService.getTransactionsByFuelType(fuelType, page, size);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    
    @GetMapping("/date-range")
    public ResponseEntity<Page<FuelTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FuelTransaction> transactions = fuelTransactionService.getTransactionsByDateRange(startDate, endDate, page, size);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    
    @GetMapping("/location")
    public ResponseEntity<Page<FuelTransaction>> getTransactionsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FuelTransaction> transactions = fuelTransactionService.getTransactionsByLocation(location, page, size);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        try {
            fuelTransactionService.deleteTransaction(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transaction deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    
    @GetMapping("/reports/total-sales")
    public ResponseEntity<Map<String, BigDecimal>> getTotalSales() {
        BigDecimal totalSales = fuelTransactionService.calculateTotalSales();
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalSales", totalSales);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    @GetMapping("/reports/sales-by-fuel-type")
    public ResponseEntity<List<Object[]>> getSalesByFuelType() {
        List<Object[]> sales = fuelTransactionService.calculateSalesByFuelType();
        return new ResponseEntity<>(sales, HttpStatus.OK);
    }

    
    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Boolean>> existsByTransactionCode(@PathVariable String code) {
        boolean exists = fuelTransactionService.existsByTransactionCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
