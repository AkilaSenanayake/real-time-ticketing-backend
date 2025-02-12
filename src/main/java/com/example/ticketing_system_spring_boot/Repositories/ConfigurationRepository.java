package com.example.ticketing_system_spring_boot.Repositories;

import com.example.ticketing_system_spring_boot.Entities.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    @Query("SELECT c FROM Configuration c WHERE c.id = 1")
    Optional<Configuration> findSingletonConfiguration();
}
