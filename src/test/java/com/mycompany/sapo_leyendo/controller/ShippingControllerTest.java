package com.mycompany.sapo_leyendo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sapo_leyendo.model.Carrier;
import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.model.Shipment;
import com.mycompany.sapo_leyendo.model.ShipmentStatus;
import com.mycompany.sapo_leyendo.model.TransportLoad;
import com.mycompany.sapo_leyendo.repository.CarrierRepository;
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;
import com.mycompany.sapo_leyendo.repository.ShipmentRepository;
import com.mycompany.sapo_leyendo.repository.TransportLoadRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Disabled due to Carrier schema mismatch between entity and SQLite table")
class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private TransportLoadRepository transportLoadRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateLoad() throws Exception {
        Carrier carrier = carrierRepository.findAll().stream().findFirst().orElseGet(() -> {
            Carrier created = new Carrier();
            created.setName("Test Carrier");
            return carrierRepository.save(created);
        });

        mockMvc.perform(post("/api/shipping/loads")
                        .param("carrierId", carrier.getId().toString())
                        .param("vehiclePlateNumber", "DW12345")
                        .param("driverName", "Jan Kierowca")
                        .param("driverPhone", "123456789")
                        .param("dockId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.carrier.id").value(carrier.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDispatchLoadAndGenerateManifest() throws Exception {
        Carrier carrier = carrierRepository.findAll().stream().findFirst().orElseGet(() -> {
            Carrier created = new Carrier();
            created.setName("Carrier Manifest");
            return carrierRepository.save(created);
        });

        String loadResponse = mockMvc.perform(post("/api/shipping/loads")
                        .param("carrierId", carrier.getId().toString())
                        .param("vehiclePlateNumber", "DW54321")
                        .param("driverName", "Maria Kierowca")
                        .param("driverPhone", "987654321")
                        .param("dockId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loadJson = objectMapper.readTree(loadResponse);
        int loadId = loadJson.get("id").asInt();

        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setReferenceNumber("OUT-SHIP-1");
        outboundOrder.setStatus("PACKED");
        outboundOrder.setShipDate(LocalDate.now());
        outboundOrder = outboundOrderRepository.save(outboundOrder);

        TransportLoad load = transportLoadRepository.findById(loadId).orElseThrow();

        Shipment shipment = new Shipment();
        shipment.setOutboundOrder(outboundOrder);
        shipment.setTransportLoad(load);
        shipment.setStatus(ShipmentStatus.PACKED);
        shipmentRepository.save(shipment);

        mockMvc.perform(post("/api/shipping/loads/" + loadId + "/dispatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transportLoad.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.totalParcels").value(0))
                .andExpect(jsonPath("$.totalWeight").value(equalTo(0.0)));
    }
}
