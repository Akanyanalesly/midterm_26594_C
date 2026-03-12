package com.example.Gas_Station_Management_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EntityScan(basePackages = "com.example.Gas_Station_Management_System.entity")
@EnableJpaRepositories(basePackages = "com.example.Gas_Station_Management_System.repository")
public class GasStationManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GasStationManagementSystemApplication.class, args);
		System.out.println("========================================");
		System.out.println("Gas Station Management System Started!");
		System.out.println("========================================");
		System.out.println("API Base URL: http://localhost:8080/api");
		System.out.println("Available Endpoints:");
		System.out.println("  - /api/locations     (Rwanda Hierarchy: Province→District→Sector→Cell→Village)");
		System.out.println("  - /api/customers     (Customer Management - linked to Village)");
		System.out.println("  - /api/loyalty-cards (Loyalty Program)");
		System.out.println("  - /api/discounts     (Discount Programs)");
		System.out.println("  - /api/transactions  (Fuel Transactions)");
		System.out.println("========================================");
	}

}
