package com.example.ticketing_system_spring_boot;

import com.example.ticketing_system_spring_boot.Controller.TicketingSystemController;
import com.example.ticketing_system_spring_boot.Entities.Configuration;
import com.example.ticketing_system_spring_boot.Repositories.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketingSystemController.class)
class TicketingSystemSpringBootApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationRepository configurationRepository;

    @Test
    public void testSaveConfiguration() throws Exception {
        String configJson = "{\"maxTicketCapacity\":100,\"customerRetrievalRate\":5,\"ticketReleaseRate\":10}";

        Configuration mockConfig = new Configuration();
        mockConfig.setMaxTicketCapacity(100);
        mockConfig.setCustomerRetrievalRate(5);
        mockConfig.setTicketReleaseRate(10);

        when(configurationRepository.save(mockConfig)).thenReturn(mockConfig);

        mockMvc.perform(post("/api/configuration/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configJson))
                .andExpect(status().isOk());
    }
}

