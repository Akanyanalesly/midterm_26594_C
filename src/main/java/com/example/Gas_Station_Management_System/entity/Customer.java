package com.example.Gas_Station_Management_System.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private LoyaltyCard loyaltyCard;

    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_discounts",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    @JsonIgnore
    private List<FuelDiscount> fuelDiscounts = new ArrayList<>();

 
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FuelTransaction> fuelTransactions = new ArrayList<>();

    // Constructors
    public Customer() {
        this.registrationDate = LocalDateTime.now();
    }

    public Customer(String fullName, String email, String phone, String address, Location location) {
        this();
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

   
    public String getLocationHierarchyPath() {
        if (location == null) {
            return "Unknown";
        }
        StringBuilder path = new StringBuilder(location.getName());
        Location current = location.getParent();
        while (current != null) {
            path.insert(0, current.getName() + " > ");
            current = current.getParent();
        }
        return path.toString();
    }

   
    public Location getProvince() {
        if (location == null) {
            return null;
        }
        Location current = location;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    public LoyaltyCard getLoyaltyCard() {
        return loyaltyCard;
    }

    public void setLoyaltyCard(LoyaltyCard loyaltyCard) {
        this.loyaltyCard = loyaltyCard;
        if (loyaltyCard != null) {
            loyaltyCard.setCustomer(this);
        }
    }

    public List<FuelDiscount> getFuelDiscounts() {
        return fuelDiscounts;
    }

    public void setFuelDiscounts(List<FuelDiscount> fuelDiscounts) {
        this.fuelDiscounts = fuelDiscounts;
    }

    public List<FuelTransaction> getFuelTransactions() {
        return fuelTransactions;
    }

    public void setFuelTransactions(List<FuelTransaction> fuelTransactions) {
        this.fuelTransactions = fuelTransactions;
    }

    // Helper methods for Many-to-Many
    public void addFuelDiscount(FuelDiscount discount) {
        fuelDiscounts.add(discount);
    }

    public void removeFuelDiscount(FuelDiscount discount) {
        fuelDiscounts.remove(discount);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}
