package com.example.Gas_Station_Management_System.service;

import com.example.Gas_Station_Management_System.entity.Customer;
import com.example.Gas_Station_Management_System.entity.Location;
import com.example.Gas_Station_Management_System.entity.LocationType;
import com.example.Gas_Station_Management_System.entity.LoyaltyCard;
import com.example.Gas_Station_Management_System.repository.CustomerRepository;
import com.example.Gas_Station_Management_System.repository.LocationRepository;
import com.example.Gas_Station_Management_System.repository.LoyaltyCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LocationRepository locationRepository;
   

    @Autowired
    public CustomerService(CustomerRepository customerRepository, 
                          LocationRepository locationRepository,
                          LoyaltyCardRepository loyaltyCardRepository) {
        this.customerRepository = customerRepository;
        this.locationRepository = locationRepository;

    }

    
    public Customer registerCustomer(Customer customer, UUID villageId) {
        // Validate email uniqueness using existsBy()
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Customer with email '" + customer.getEmail() + "' already exists");
        }
        
        // Validate phone uniqueness
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new RuntimeException("Customer with phone '" + customer.getPhone() + "' already exists");
        }
        
        // Find and set village location
        Location village = locationRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Village not found with id: " + villageId));
        
        // Validate that the location is a VILLAGE
        if (village.getLocationType() != LocationType.VILLAGE) {
            throw new RuntimeException("Customer must be registered to a VILLAGE, not " + village.getLocationType());
        }
        
        customer.setLocation(village);
        
        // Save customer first to get ID
        Customer savedCustomer = customerRepository.save(customer);
        
        // Create loyalty card for customer (One-to-One relationship)
        LoyaltyCard loyaltyCard = new LoyaltyCard();
        loyaltyCard.setCardNumber(generateCardNumber());
        loyaltyCard.setCustomer(savedCustomer);
        loyaltyCard.setTierLevel("BRONZE");
        loyaltyCard.setTotalPoints(0);
        
        savedCustomer.setLoyaltyCard(loyaltyCard);
        
        return customerRepository.save(savedCustomer);
    }

    /**
     * Generate unique card number
     */
    private String generateCardNumber() {
        return "LC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Update customer
     */
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        
        // Check email uniqueness if changed
        if (!customer.getEmail().equals(customerDetails.getEmail()) &&
            customerRepository.existsByEmail(customerDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        customer.setFullName(customerDetails.getFullName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        
        return customerRepository.save(customer);
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    /**
     * PAGINATION AND SORTING:
     * - Returns paginated customer list
     * - Supports sorting by any field
     * - Default 10 items per page as per requirements
     */
    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return customerRepository.findAll(pageable);
    }

    /**
     * Get all customers without pagination
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    
    @Transactional(readOnly = true)
    public List<Customer> getCustomersByLocation(String locationCodeOrName) {
        
        Location location = locationRepository.findByCode(locationCodeOrName)
                .orElseGet(() -> locationRepository.findByName(locationCodeOrName)
                        .orElseThrow(() -> new RuntimeException("Location not found: " + locationCodeOrName)));
        
        if (location.getLocationType() == LocationType.VILLAGE) {
            
            return location.getCustomers();
        } else {
            
            return getAllCustomersUnderLocation(location);
        }
    }

    
    private List<Customer> getAllCustomersUnderLocation(Location location) {
        List<Customer> customers = new ArrayList<>();
        
        for (Location subLocation : location.getSubLocations()) {
            if (subLocation.getLocationType() == LocationType.VILLAGE) {
                customers.addAll(subLocation.getCustomers());
            } else {
                customers.addAll(getAllCustomersUnderLocation(subLocation));
            }
        }
        
        return customers;
    }

    /**
     * SEARCH BY LOCATION with Pagination
     */
    @Transactional(readOnly = true)
    public Page<Customer> getCustomersByLocationPaginated(String locationCodeOrName, int page, int size) {
        List<Customer> customers = getCustomersByLocation(locationCodeOrName);
        // Convert list to page manually
        int start = page * size;
        int end = Math.min(start + size, customers.size());
        List<Customer> pageContent = customers.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, 
            PageRequest.of(page, size), customers.size());
    }

    
    @Transactional(readOnly = true)
    public List<Customer> getCustomersByLocationId(UUID locationId) {
        
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        
        if (location.getLocationType() == LocationType.VILLAGE) {
            return location.getCustomers();
        } else {
            return getAllCustomersUnderLocation(location);
        }
    }

    
    @Transactional(readOnly = true)
    public Optional<Page<Customer>> getCustomersByLocationPaginated(UUID locationId, int page, int size) {
        Optional<Location> locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            Pageable pageable = PageRequest.of(page, size);
            return Optional.of(customerRepository.findByLocation(location, pageable));
        }
        return Optional.empty();
    }


    @Transactional(readOnly = true)
    public List<Customer> getCustomersSortedByName(boolean ascending) {
        return ascending 
            ? customerRepository.findAllByOrderByFullNameAsc() 
            : customerRepository.findAllByOrderByFullNameDesc();
    }



    
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    
    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.findAll().stream()
                .filter(c -> c.getFullName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}
