package com.mycompany.sapo_leyendo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sapo_leyendo.model.Product;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OutboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateAndFetchOutboundOrder() throws Exception {
        Product product = new Product();
        product.setSku("OUT-SKU-1");
        product.setName("Outbound Test Product");
        product.setIdBaseUom(1);
        product = productRepository.save(product);

        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("referenceNumber", "OUT-INT-001");
        orderPayload.put("status", "NEW");
        orderPayload.put("shipDate", LocalDate.now().plusDays(2).toString());
        orderPayload.put("destination", "Wroclaw");
        orderPayload.put("createdAt", LocalDateTime.now().toString());

        String response = mockMvc.perform(post("/api/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        int outboundId = created.get("id").asInt();

        mockMvc.perform(get("/api/outbound/" + outboundId))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.referenceNumber").value("OUT-INT-001"));
    }
}
