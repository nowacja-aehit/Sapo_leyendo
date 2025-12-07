package com.mycompany.sapo_leyendo;

import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SapoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnProducts() throws Exception {
        // Given
        Product product = new Product();
        product.setSku("TEST-SKU-1");
        product.setName("Test Product");
        product.setIdBaseUom(1);
        productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sku == 'TEST-SKU-1')].name").value("Test Product"));
    }
}
