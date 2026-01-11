package com.mycompany.sapo_leyendo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReturnsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleFullRmaFlow() throws Exception {
        Product product = new Product();
        product.setSku("RET-SKU-1");
        product.setName("Returnable Product");
        product.setIdBaseUom(1);
        product = productRepository.save(product);

        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setReferenceNumber("OUT-RMA-1");
        outboundOrder.setStatus("SHIPPED");
        outboundOrder.setShipDate(LocalDateTime.now().minusDays(1));
        outboundOrder.setCreatedAt(LocalDateTime.now());
        outboundOrder = outboundOrderRepository.save(outboundOrder);

        String rmaResponse = mockMvc.perform(post("/api/returns/rma")
                        .param("outboundOrderId", outboundOrder.getId().toString())
                        .param("reason", "Damaged carton")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode rmaJson = objectMapper.readTree(rmaResponse);
        int rmaId = rmaJson.get("id").asInt();

        mockMvc.perform(post("/api/returns/receive/" + rmaId)
                        .param("trackingNumber", "TRACK-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.trackingNumberIn").value("TRACK-123"));

        mockMvc.perform(post("/api/returns/grade/" + rmaId)
                        .param("productId", product.getId().toString())
                        .param("grade", "GRADE_B")
                        .param("comment", "Minor scratches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradingStatus").value("GRADE_B"))
                .andExpect(jsonPath("$.disposition").value("REFURBISH"));
    }
}
