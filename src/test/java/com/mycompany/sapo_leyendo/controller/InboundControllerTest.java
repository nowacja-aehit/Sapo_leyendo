package com.mycompany.sapo_leyendo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sapo_leyendo.model.Location;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.repository.LocationRepository;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetInboundOrders() throws Exception {
        mockMvc.perform(get("/api/inbound")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void shouldCreateAndFetchInboundOrderWithItems() throws Exception {
        Product product = new Product();
        product.setSku("INB-SKU-1");
        product.setName("Inbound Test Product");
        product.setIdBaseUom(1);
        product = productRepository.save(product);

        Location dock = locationRepository.findAll().stream().findFirst().orElseGet(() -> {
            Location fallback = new Location();
            fallback.setName("REC-AUTO");
            fallback.setActive(true);
            return locationRepository.save(fallback);
        });

        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("orderReference", "INB-INT-001");
        orderPayload.put("status", "PLANNED");
        orderPayload.put("expectedArrival", LocalDate.now().plusDays(1).toString());
        orderPayload.put("supplier", "Supplier X");
        orderPayload.put("dockId", dock.getId());

        Map<String, Object> itemPayload = new HashMap<>();
        itemPayload.put("product", Map.of("id", product.getId()));
        itemPayload.put("expectedQuantity", 5);
        itemPayload.put("batchNumber", "BATCH-1");

        orderPayload.put("items", java.util.List.of(itemPayload));

        String response = mockMvc.perform(post("/api/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.items[0].expectedQuantity").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        int inboundId = created.get("id").asInt();

        mockMvc.perform(get("/api/inbound/" + inboundId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderReference").value("INB-INT-001"))
            .andExpect(jsonPath("$.items", hasSize(1)));
    }
}
