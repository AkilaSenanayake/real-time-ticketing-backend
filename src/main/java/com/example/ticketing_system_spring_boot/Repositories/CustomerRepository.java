package com.example.ticketing_system_spring_boot.Repositories;

import com.example.ticketing_system_spring_boot.Entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
