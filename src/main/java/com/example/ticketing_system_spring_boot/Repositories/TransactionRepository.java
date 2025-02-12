package com.example.ticketing_system_spring_boot.Repositories;

import com.example.ticketing_system_spring_boot.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
