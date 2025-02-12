package com.example.ticketing_system_spring_boot.service;

import com.example.ticketing_system_spring_boot.Entities.Customer;
import com.example.ticketing_system_spring_boot.Entities.Transaction;
import com.example.ticketing_system_spring_boot.Repositories.CustomerRepository;
import com.example.ticketing_system_spring_boot.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final TicketPool ticketPool;
    private final List<Thread> consumerThreads = new ArrayList<>();
    private volatile boolean running = false;

    @Autowired
    private CustomerRepository consumerRepository;

    @Autowired
    private TransactionRepository ticketTransactionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public CustomerService(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
    }

    public void startConsumers(int consumerCount, int retrievalRate) {
        if (running) {
            throw new IllegalStateException("Customers are already running.");
        }

        running = true;

        for (int i = 1; i <= consumerCount; i++) {
            Customer consumer = new Customer();
            consumer.setName("Customer-" + i + "-" + System.nanoTime());
            consumerRepository.save(consumer);

            Long consumerId = consumer.getId();

            Thread consumerThread = new Thread(() -> runConsumer(retrievalRate, consumerId));
            consumerThreads.add(consumerThread);
            consumerThread.start();
        }
    }

    private void runConsumer(int retrievalRate, Long consumerId) {
        while (running) {
            try {
                Map<String, Object> ticketDetails = ticketPool.retrieveTicketDetails();
                if (ticketDetails == null) {
                    logger.warn("Error: Retrieved ticketDetails is null!");
                    continue;
                }

                // Safely convert ticketId and vendorId to Long
                Long ticketId = ((Number) ticketDetails.get("ticketId")).longValue();
                Long vendorId = ((Number) ticketDetails.get("vendorId")).longValue();

                if (ticketId == null || ticketId == 0 || vendorId == null) {
                    System.err.println("Error: Invalid ticketDetails. ticketId: " + ticketId + ", vendorId: " + vendorId);
                    continue;
                }

                System.out.println("Consumer " + consumerId + " purchased ticket: " + ticketId);

                Transaction transaction = new Transaction();
                transaction.setTicketId(ticketId.intValue());
                transaction.setConsumerId(consumerId);
                transaction.setVendorId(vendorId);
                ticketTransactionRepository.save(transaction);

                messagingTemplate.convertAndSend("/topic/simulation", Map.of(
                        "type", "ticketBought",
                        "consumerId", consumerId,
                        "ticketId", ticketId
                ));

                Thread.sleep(retrievalRate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stopConsumers() {
        running = false;
        synchronized (ticketPool) {
            ticketPool.notifyAll(); // Wake up all waiting threads
        }
        consumerThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumerThreads.clear();
    }
}
