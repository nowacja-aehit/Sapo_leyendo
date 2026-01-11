package com.mycompany.sapo_leyendo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateAndDeleteLocationHierarchy() throws Exception {
        String zoneResponse = mockMvc.perform(post("/api/locations/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "ZONE-INT",
                                "allowMixedSku", true,
                                "isSecure", false
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode zoneJson = objectMapper.readTree(zoneResponse);
        int zoneId = zoneJson.get("id").asInt();

        String typeResponse = mockMvc.perform(post("/api/locations/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "TYPE-INT"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode typeJson = objectMapper.readTree(typeResponse);
        int typeId = typeJson.get("id").asInt();

        String locationResponse = mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "LOC-INT-1",
                                "zone", "ZONE-INT",
                                "locationTypeId", typeId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("LOC-INT-1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode locationJson = objectMapper.readTree(locationResponse);
        int locationId = locationJson.get("id").asInt();

        mockMvc.perform(delete("/api/locations/" + locationId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/locations/" + locationId))
                .andExpect(status().isNotFound());
    }
}
