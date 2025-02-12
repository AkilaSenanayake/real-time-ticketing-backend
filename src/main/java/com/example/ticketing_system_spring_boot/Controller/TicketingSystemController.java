package com.example.ticketing_system_spring_boot.Controller;

import com.example.ticketing_system_spring_boot.Entities.Configuration;
import com.example.ticketing_system_spring_boot.Repositories.ConfigurationRepository;
import com.example.ticketing_system_spring_boot.service.TicketPool;
import com.example.ticketing_system_spring_boot.service.TicketingService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/ticketing")
public class TicketingSystemController {
    private final TicketingService ticketingService;
    private final ConfigurationRepository configurationRepository;
    private final TicketPool ticketPool;

    @Autowired
    public TicketingSystemController(TicketingService ticketingService, ConfigurationRepository configurationRepository, TicketPool ticketPool) {
        this.ticketingService = ticketingService;
        this.configurationRepository = configurationRepository;
        this.ticketPool = ticketPool;
    }
    // Save or Update Configuration
    @PostMapping("/configuration")
    @Transactional
    public ResponseEntity<Configuration> saveConfiguration(@Valid @RequestBody Configuration systemConfiguration) {
        Optional<Configuration> existingConfig = configurationRepository.findSingletonConfiguration();
        if (existingConfig.isPresent()) {
            Configuration config = existingConfig.get();
            config.setMaxTicketCapacity(systemConfiguration.getMaxTicketCapacity());
            config.setCustomerRetrievalRate(systemConfiguration.getCustomerRetrievalRate());
            config.setTicketReleaseRate(systemConfiguration.getTicketReleaseRate());
            return ResponseEntity.ok(configurationRepository.save(config));
        } else {
            systemConfiguration.setId(1L);
            return ResponseEntity.ok(configurationRepository.save(systemConfiguration));
        }
    }

    // Retrieve Current Configuration
    @GetMapping("/current")
    public Configuration getCurrentConfiguration() {
        return configurationRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Configuration not found!"));
    }

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation(
            @RequestParam int vendorCount,
            @RequestParam int consumerCount) {
        // Retrieve the configuration from the database
        Configuration configuration = configurationRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Configuration not found!"));

        // Use the configuration parameters from the database
        int ticketReleaseRate = configuration.getTicketReleaseRate();
        int retrievalRate = configuration.getCustomerRetrievalRate();

        // Update TicketPool's maxCapacity to the latest value from the database
        ticketPool.initializeSimulation(); // Fetches the latest maxCapacity and prepares the pool

        // Pass all necessary parameters to start the simulation
        ticketingService.startSimulation(vendorCount, ticketReleaseRate, consumerCount, retrievalRate);

        return ResponseEntity.ok("Simulation started with updated configuration.");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopSimulation() {
        ticketingService.stopSimulation();
        return ResponseEntity.ok("Simulation stopped");
    }
}
