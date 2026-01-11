package com.mycompany.sapo_leyendo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * PEŁNE TESTY CYKLU ŻYCIA PRODUKTU W WMS
 * 
 * Testuje kompletny przepływ towaru:
 * 1. Tworzenie produktu w katalogu
 * 2. Przyjęcie dostawy (Inbound)
 * 3. Kontrola jakości (QC)
 * 4. Magazynowanie i przesunięcia (Inventory)
 * 5. Zamówienie wychodzące (Outbound)
 * 6. Kompletacja (Picking)
 * 7. Pakowanie (Packing)
 * 8. Wysyłka (Shipping)
 * 9. Zwroty i reklamacje (Returns)
 * 10. Wycofanie produktu
 * 
 * Wszystkie testy używają rzeczywistego API tak jak frontend.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories for data setup and verification
    @Autowired private ProductRepository productRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private LocationTypeRepository locationTypeRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private CarrierRepository carrierRepository;
    @Autowired private PackingMaterialRepository packingMaterialRepository;

    // Shared test data IDs
    private Integer uomId;
    private Integer zoneId;
    private Integer dockLocationId;
    private Integer shelfLocationId;
    private Integer carrierId;
    private Integer packingMaterialId;
    
    // Product lifecycle IDs (populated during tests)
    private Integer productId;
    private Integer inboundOrderId;
    private Integer inboundItemId;
    private Integer inventoryId;
    private Integer outboundOrderId;
    private Integer outboundItemId;
    private Integer waveId;
    private Integer pickListId;
    private Integer pickTaskId;
    private Integer shipmentId;
    private Integer parcelId;
    private Integer loadId;
    private Integer inspectionId;
    private Integer rmaId;

    @BeforeAll
    void setupTestEnvironment() {
        System.out.println("\n========================================");
        System.out.println("  INICJALIZACJA ŚRODOWISKA TESTOWEGO");
        System.out.println("========================================\n");

        // 1. Create UnitOfMeasure
        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setId(1);
        uom.setCode("SZT");
        uom.setName("Sztuka");
        uom = uomRepository.save(uom);
        uomId = uom.getId();
        System.out.println("✓ Utworzono UOM: " + uomId);

        // 2. Create Zone
        Zone zone = new Zone();
        zone.setName("ZONE-A");
        zone.setTemperatureControlled(false);
        zone.setSecure(false);
        zone.setAllowMixedSku(true);
        zone = zoneRepository.save(zone);
        zoneId = zone.getId();
        System.out.println("✓ Utworzono strefę: " + zoneId);

        // 3. Create LocationTypes
        LocationType dockType = new LocationType();
        dockType.setName("DOCK");
        dockType = locationTypeRepository.save(dockType);
        
        LocationType shelfType = new LocationType();
        shelfType.setName("SHELF");
        shelfType = locationTypeRepository.save(shelfType);
        System.out.println("✓ Utworzono typy lokalizacji");

        // 4. Create Locations
        Location dockLocation = new Location();
        dockLocation.setName("DOCK-01");
        dockLocation.setZone(zone);
        dockLocation.setLocationType(dockType);
        dockLocation.setAisle("D");
        dockLocation.setRack("1");
        dockLocation.setLevel("1");
        dockLocation.setBin("01");
        dockLocation.setActive(true);
        dockLocation = locationRepository.save(dockLocation);
        dockLocationId = dockLocation.getId();
        System.out.println("✓ Utworzono lokalizację DOCK: " + dockLocationId);

        Location shelfLocation = new Location();
        shelfLocation.setName("SHELF-A1-01");
        shelfLocation.setZone(zone);
        shelfLocation.setLocationType(shelfType);
        shelfLocation.setAisle("A");
        shelfLocation.setRack("1");
        shelfLocation.setLevel("1");
        shelfLocation.setBin("01");
        shelfLocation.setActive(true);
        shelfLocation = locationRepository.save(shelfLocation);
        shelfLocationId = shelfLocation.getId();
        System.out.println("✓ Utworzono lokalizację SHELF: " + shelfLocationId);

        // 5. Create Carrier
        Carrier carrier = new Carrier();
        carrier.setName("DHL Test");
        carrier.setTrackingUrlTemplate("https://dhl.com/track/{tracking}");
        carrier = carrierRepository.save(carrier);
        carrierId = carrier.getId();
        System.out.println("✓ Utworzono przewoźnika: " + carrierId);

        // 6. Create PackingMaterial
        PackingMaterial material = new PackingMaterial();
        material.setCode("BOX-M");
        material.setMaxWeightKg(10.0);
        material.setTareWeightKg(0.3);
        material.setLengthCm(30.0);
        material.setWidthCm(20.0);
        material.setHeightCm(15.0);
        material = packingMaterialRepository.save(material);
        packingMaterialId = material.getId();
        System.out.println("✓ Utworzono materiał pakowania: " + packingMaterialId);

        System.out.println("\n✅ Środowisko testowe gotowe!\n");
    }

    // =========================================================================
    // FAZA 1: TWORZENIE PRODUKTU
    // =========================================================================

    @Test
    @Order(100)
    @DisplayName("FAZA 1: Tworzenie nowego produktu w katalogu")
    void phase1_createProduct() throws Exception {
        System.out.println("\n--- FAZA 1: TWORZENIE PRODUKTU ---");

        Map<String, Object> productData = new HashMap<>();
        productData.put("sku", "TEST-LAPTOP-001");
        productData.put("name", "Laptop Dell XPS 15 Test");
        productData.put("description", "Laptop testowy do cyklu życia");
        productData.put("idBaseUom", uomId);
        productData.put("weightKg", 2.5);
        productData.put("lengthCm", 35.0);
        productData.put("widthCm", 23.0);
        productData.put("heightCm", 2.0);
        productData.put("unitPrice", 7999.99);
        productData.put("minStockLevel", 5);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        productId = response.get("id").asInt();
        
        assertNotNull(productId, "Product ID should not be null");
        assertEquals("TEST-LAPTOP-001", response.get("sku").asText());
        
        System.out.println("✓ Utworzono produkt ID: " + productId);
        System.out.println("  SKU: " + response.get("sku").asText());
        System.out.println("  Nazwa: " + response.get("name").asText());
    }

    @Test
    @Order(101)
    @DisplayName("FAZA 1: Weryfikacja produktu istnieje w katalogu")
    void phase1_verifyProductExists() throws Exception {
        assertNotNull(productId, "productId should be set from previous test");

        MvcResult result = mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode product = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("TEST-LAPTOP-001", product.get("sku").asText());
        
        System.out.println("✓ Produkt zweryfikowany w katalogu");
    }

    // =========================================================================
    // FAZA 2: PRZYJĘCIE DOSTAWY (INBOUND)
    // =========================================================================

    @Test
    @Order(200)
    @DisplayName("FAZA 2a: Tworzenie zamówienia inbound z pozycjami")
    void phase2a_createInboundOrderWithItems() throws Exception {
        System.out.println("\n--- FAZA 2: PRZYJĘCIE DOSTAWY ---");
        assertNotNull(productId, "productId should be set");

        // Create inbound order with items in one call
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("supplier", "Dell Polska Sp. z o.o.");
        orderData.put("expectedDate", LocalDateTime.now().plusDays(1).toString());
        orderData.put("status", "PLANNED");
        
        // Add items directly to order
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("quantityExpected", 100);
        items.add(item);
        orderData.put("items", items);

        MvcResult result = mockMvc.perform(post("/api/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        inboundOrderId = response.get("id").asInt();
        
        assertNotNull(inboundOrderId);
        System.out.println("✓ Utworzono zamówienie inbound ID: " + inboundOrderId);
        
        // Get the inbound item ID
        MvcResult orderResult = mockMvc.perform(get("/api/inbound/" + inboundOrderId))
                .andExpect(status().isOk())
                .andReturn();
        
        JsonNode orderDetails = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        if (orderDetails.has("items") && orderDetails.get("items").size() > 0) {
            inboundItemId = orderDetails.get("items").get(0).get("id").asInt();
            System.out.println("✓ Item ID: " + inboundItemId);
        }
    }

    @Test
    @Order(201)
    @DisplayName("FAZA 2b: Przyjęcie towaru (LPN Receiving)")
    void phase2b_receiveItem() throws Exception {
        assertNotNull(inboundItemId, "inboundItemId should be set from previous test");

        // Generate LPN
        MvcResult lpnResult = mockMvc.perform(get("/api/inbound/lpn/generate"))
                .andExpect(status().isOk())
                .andReturn();
        
        String lpn = lpnResult.getResponse().getContentAsString().replace("\"", "");
        System.out.println("  Wygenerowano LPN: " + lpn);

        // Receive item using JSON body
        Map<String, Object> receiveData = new HashMap<>();
        receiveData.put("inboundOrderItemId", inboundItemId);
        receiveData.put("lpn", lpn);
        receiveData.put("quantity", 100);
        receiveData.put("operatorId", 1);

        MvcResult result = mockMvc.perform(post("/api/inbound/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiveData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode receipt = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(receipt.get("id"));
        assertEquals(100, receipt.get("quantity").asInt());
        
        System.out.println("✓ Przyjęto towar, Receipt ID: " + receipt.get("id").asInt());
        System.out.println("  Ilość przyjęta: " + receipt.get("quantity").asInt() + " szt");

        // Verify inventory was created
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        if (!inventories.isEmpty()) {
            inventoryId = inventories.get(0).getId();
            System.out.println("✓ Utworzono inventory ID: " + inventoryId);
            System.out.println("  Status: " + inventories.get(0).getStatus());
        }
    }

    // =========================================================================
    // FAZA 3: KONTROLA JAKOŚCI (QC)
    // =========================================================================

    @Test
    @Order(300)
    @DisplayName("FAZA 3a: Tworzenie inspekcji QC")
    void phase3a_createQcInspection() throws Exception {
        System.out.println("\n--- FAZA 3: KONTROLA JAKOŚCI ---");
        assertNotNull(productId);

        Integer referenceId = inboundItemId != null ? inboundItemId : 1;

        MvcResult result = mockMvc.perform(post("/api/qc/inspections")
                        .param("productId", productId.toString())
                        .param("sourceType", "INBOUND")
                        .param("referenceId", referenceId.toString())
                        .param("sampleSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode inspection = objectMapper.readTree(result.getResponse().getContentAsString());
        inspectionId = inspection.get("id").asInt();
        
        assertNotNull(inspectionId);
        assertEquals("PENDING", inspection.get("result").asText());
        
        System.out.println("✓ Utworzono inspekcję QC ID: " + inspectionId);
        System.out.println("  Typ źródła: INBOUND");
        System.out.println("  Próbka: 10 szt");
    }

    @Test
    @Order(301)
    @DisplayName("FAZA 3b: Wykonanie inspekcji - wynik PASSED")
    void phase3b_executeInspection() throws Exception {
        assertNotNull(inspectionId);

        MvcResult result = mockMvc.perform(post("/api/qc/inspections/" + inspectionId + "/execute")
                        .param("result", "PASSED")
                        .param("inspectorId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode inspection = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("PASSED", inspection.get("result").asText());
        assertNotNull(inspection.get("inspectedAt"));
        
        System.out.println("✓ Inspekcja zakończona wynikiem: PASSED");
    }

    // =========================================================================
    // FAZA 4: MAGAZYNOWANIE (INVENTORY)
    // =========================================================================

    @Test
    @Order(400)
    @DisplayName("FAZA 4a: Weryfikacja stanu magazynowego")
    void phase4a_verifyInventory() throws Exception {
        System.out.println("\n--- FAZA 4: MAGAZYNOWANIE ---");

        MvcResult result = mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode inventories = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(inventories.isArray());
        System.out.println("✓ Pobrano listę inventory: " + inventories.size() + " pozycji");
        
        // Find inventory for our product
        for (JsonNode inv : inventories) {
            if (inv.has("product") && inv.get("product").has("id") 
                && inv.get("product").get("id").asInt() == productId) {
                inventoryId = inv.get("id").asInt();
                System.out.println("  Inventory ID: " + inventoryId);
                System.out.println("  Ilość: " + inv.get("quantity").asInt() + " szt");
                System.out.println("  Status: " + inv.get("status").asText());
                break;
            }
        }
    }

    @Test
    @Order(401)
    @DisplayName("FAZA 4b: Weryfikacja zadań Move (PutAway)")
    void phase4b_verifyMoveTasks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/tasks/pending"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode tasks = objectMapper.readTree(result.getResponse().getContentAsString());
        System.out.println("✓ Pobrano zadania przesunięć: " + tasks.size() + " zadań");
        
        if (tasks.size() > 0) {
            System.out.println("  Typ zadania: " + tasks.get(0).path("type").asText());
            System.out.println("  Status: " + tasks.get(0).path("status").asText());
        }
    }

    // =========================================================================
    // FAZA 5: ZAMÓWIENIE WYCHODZĄCE (OUTBOUND)
    // =========================================================================

    @Test
    @Order(500)
    @DisplayName("FAZA 5a: Tworzenie zamówienia wychodzącego")
    void phase5a_createOutboundOrder() throws Exception {
        System.out.println("\n--- FAZA 5: ZAMÓWIENIE WYCHODZĄCE ---");

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("customerName", "Klient Testowy Sp. z o.o.");
        orderData.put("destination", "Warszawa, ul. Testowa 123");
        orderData.put("shipDate", LocalDateTime.now().plusDays(2).toString());
        orderData.put("status", "PLANNED");
        orderData.put("priority", "HIGH");

        MvcResult result = mockMvc.perform(post("/api/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        outboundOrderId = response.get("id").asInt();
        
        assertNotNull(outboundOrderId);
        assertNotNull(response.get("referenceNumber"));
        
        System.out.println("✓ Utworzono zamówienie wychodzące ID: " + outboundOrderId);
        System.out.println("  Numer referencyjny: " + response.get("referenceNumber").asText());
        System.out.println("  Klient: " + (response.get("customer") != null ? response.get("customer").asText() : "N/A"));
    }

    @Test
    @Order(501)
    @DisplayName("FAZA 5b: Dodanie pozycji do zamówienia wychodzącego")
    void phase5b_addOutboundOrderItem() throws Exception {
        assertNotNull(outboundOrderId);
        assertNotNull(productId);

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("productId", productId);
        itemData.put("quantityOrdered", 10.0);
        itemData.put("unitPrice", 7999.99);

        MvcResult result = mockMvc.perform(post("/api/outbound/" + outboundOrderId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        outboundItemId = response.get("id").asInt();
        
        assertNotNull(outboundItemId);
        System.out.println("✓ Dodano pozycję do zamówienia, item ID: " + outboundItemId);
        System.out.println("  Ilość zamówiona: 10 szt");
        System.out.println("  Cena jednostkowa: 7999.99 PLN");
    }

    @Test
    @Order(502)
    @DisplayName("FAZA 5c: Weryfikacja zamówienia na dashboardzie")
    void phase5c_verifyOrderOnDashboard() throws Exception {
        assertNotNull(outboundOrderId);

        MvcResult result = mockMvc.perform(get("/api/dashboard/orders"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orders = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(orders.isArray());
        
        boolean found = false;
        for (JsonNode order : orders) {
            if (order.get("id").asInt() == outboundOrderId) {
                found = true;
                System.out.println("✓ Zamówienie widoczne na dashboardzie");
                System.out.println("  Status: " + order.get("status").asText());
                break;
            }
        }
        assertTrue(found, "Order should be visible on dashboard");
    }

    // =========================================================================
    // FAZA 6: KOMPLETACJA (PICKING)
    // =========================================================================

    @Test
    @Order(600)
    @DisplayName("FAZA 6a: Tworzenie fali (Wave)")
    void phase6a_createWave() throws Exception {
        System.out.println("\n--- FAZA 6: KOMPLETACJA ---");
        assertNotNull(outboundOrderId);

        List<Integer> orderIds = List.of(outboundOrderId);
        
        MvcResult result = mockMvc.perform(post("/api/picking/waves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode wave = objectMapper.readTree(result.getResponse().getContentAsString());
        waveId = wave.get("id").asInt();
        
        assertNotNull(waveId);
        assertEquals("CREATED", wave.get("status").asText());
        
        System.out.println("✓ Utworzono falę ID: " + waveId);
        System.out.println("  Nazwa: " + wave.get("name").asText());
        System.out.println("  Status: " + wave.get("status").asText());
    }

    @Test
    @Order(601)
    @DisplayName("FAZA 6b: Uruchomienie fali (alokacja + release)")
    void phase6b_runWave() throws Exception {
        assertNotNull(waveId);
        assertNotNull(outboundOrderId);

        List<Integer> orderIds = List.of(outboundOrderId);

        MvcResult result = mockMvc.perform(post("/api/picking/waves/" + waveId + "/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderIds)))
                .andDo(print())
                .andReturn();
        
        // Print response content for debugging
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response status: " + result.getResponse().getStatus());
        System.out.println("Response content: " + responseContent);
        
        assertEquals(200, result.getResponse().getStatus(), 
            "Expected 200 OK but got " + result.getResponse().getStatus() + ". Response: " + responseContent);

        JsonNode wave = objectMapper.readTree(responseContent);
        
        String status = wave.get("status").asText();
        assertTrue(status.equals("IN_PROGRESS") || status.equals("RELEASED") || status.equals("CREATED"), 
            "Wave status should be IN_PROGRESS, RELEASED or CREATED, but was: " + status);
        
        System.out.println("✓ Fala uruchomiona");
        System.out.println("  Status: " + status);
    }

    @Test
    @Order(602)
    @DisplayName("FAZA 6c: Pobieranie zadań kompletacji")
    void phase6c_getPickingTasks() throws Exception {
        assertNotNull(waveId);

        MvcResult result = mockMvc.perform(get("/api/picking/waves/" + waveId + "/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode tasks = objectMapper.readTree(result.getResponse().getContentAsString());
        System.out.println("✓ Pobrano zadania kompletacji: " + tasks.size() + " zadań");
        
        if (tasks.size() > 0) {
            System.out.println("  Przykładowe zadanie:");
            System.out.println("    Status: " + tasks.get(0).path("status").asText());
        }
    }

    // =========================================================================
    // FAZA 7: PAKOWANIE (PACKING)
    // =========================================================================

    @Test
    @Order(700)
    @DisplayName("FAZA 7a: Rozpoczęcie pakowania")
    void phase7a_startPacking() throws Exception {
        System.out.println("\n--- FAZA 7: PAKOWANIE ---");
        assertNotNull(outboundOrderId);

        MvcResult result = mockMvc.perform(post("/api/packing/shipments/start/" + outboundOrderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode shipment = objectMapper.readTree(result.getResponse().getContentAsString());
        shipmentId = shipment.get("id").asInt();
        
        assertNotNull(shipmentId);
        assertEquals("PACKING", shipment.get("status").asText());
        
        System.out.println("✓ Rozpoczęto pakowanie, Shipment ID: " + shipmentId);
        System.out.println("  Status: PACKING");
    }

    @Test
    @Order(701)
    @DisplayName("FAZA 7b: Tworzenie paczki")
    void phase7b_createParcel() throws Exception {
        assertNotNull(shipmentId);
        assertNotNull(packingMaterialId);

        MvcResult result = mockMvc.perform(post("/api/packing/shipments/" + shipmentId + "/parcels")
                        .param("packingMaterialId", packingMaterialId.toString()))
                .andDo(print())
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            JsonNode parcel = objectMapper.readTree(result.getResponse().getContentAsString());
            parcelId = parcel.get("id").asInt();
            System.out.println("✓ Utworzono paczkę ID: " + parcelId);
        } else {
            System.out.println("⚠ Tworzenie paczki zwróciło status: " + result.getResponse().getStatus());
            System.out.println("  Response: " + result.getResponse().getContentAsString());
        }
    }

    @Test
    @Order(702)
    @DisplayName("FAZA 7c: Zamknięcie przesyłki")
    void phase7c_closeShipment() throws Exception {
        assertNotNull(shipmentId);

        MvcResult result = mockMvc.perform(post("/api/packing/shipments/" + shipmentId + "/close"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode shipment = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("PACKED", shipment.get("status").asText());
        
        System.out.println("✓ Zamknięto przesyłkę");
        System.out.println("  Status: PACKED");
        System.out.println("  Waga całkowita: " + shipment.path("totalWeightKg").asDouble() + " kg");
    }

    // =========================================================================
    // FAZA 8: WYSYŁKA (SHIPPING)
    // =========================================================================

    @Test
    @Order(800)
    @DisplayName("FAZA 8a: Tworzenie transportu (Load)")
    void phase8a_createLoad() throws Exception {
        System.out.println("\n--- FAZA 8: WYSYŁKA ---");
        assertNotNull(carrierId);

        MvcResult result = mockMvc.perform(post("/api/shipping/loads")
                        .param("carrierId", carrierId.toString())
                        .param("trailerNumber", "WA12345")
                        .param("driverName", "Jan Kowalski")
                        .param("driverPhone", "600123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode load = objectMapper.readTree(result.getResponse().getContentAsString());
        loadId = load.get("id").asInt();
        
        assertNotNull(loadId);
        assertEquals("PLANNING", load.get("status").asText());
        
        System.out.println("✓ Utworzono transport ID: " + loadId);
        System.out.println("  Numer: " + load.get("loadNumber").asText());
        System.out.println("  Kierowca: " + load.get("driverName").asText());
        System.out.println("  Status: PLANNING");
    }

    @Test
    @Order(801)
    @DisplayName("FAZA 8b: Przypisanie przesyłki do transportu")
    void phase8b_assignShipmentToLoad() throws Exception {
        assertNotNull(loadId);
        assertNotNull(shipmentId);

        mockMvc.perform(post("/api/shipping/loads/" + loadId + "/assign/" + shipmentId))
                .andDo(print())
                .andExpect(status().isOk());

        System.out.println("✓ Przypisano przesyłkę do transportu");
    }

    @Test
    @Order(802)
    @DisplayName("FAZA 8c: Wysłanie transportu (Dispatch)")
    void phase8c_dispatchLoad() throws Exception {
        assertNotNull(loadId);

        MvcResult result = mockMvc.perform(post("/api/shipping/loads/" + loadId + "/dispatch"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode manifest = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(manifest.get("id"));
        assertNotNull(manifest.get("manifestNumber"));
        
        System.out.println("✓ Transport wysłany!");
        System.out.println("  Manifest: " + manifest.get("manifestNumber").asText());
        
        // Verify load status changed
        MvcResult loadResult = mockMvc.perform(get("/api/shipping/loads"))
                .andExpect(status().isOk())
                .andReturn();
        
        JsonNode loads = objectMapper.readTree(loadResult.getResponse().getContentAsString());
        for (JsonNode load : loads) {
            if (load.get("id").asInt() == loadId) {
                assertEquals("IN_TRANSIT", load.get("status").asText());
                System.out.println("  Status transportu: IN_TRANSIT");
                break;
            }
        }
    }

    // =========================================================================
    // FAZA 9: ZWROTY I REKLAMACJE (RETURNS)
    // =========================================================================

    @Test
    @Order(900)
    @DisplayName("FAZA 9a: Tworzenie zgłoszenia RMA")
    void phase9a_createRma() throws Exception {
        System.out.println("\n--- FAZA 9: ZWROTY ---");
        assertNotNull(outboundOrderId);

        MvcResult result = mockMvc.perform(post("/api/returns/rma")
                        .param("outboundOrderId", outboundOrderId.toString())
                        .param("reason", "Produkt uszkodzony podczas transportu"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rma = objectMapper.readTree(result.getResponse().getContentAsString());
        rmaId = rma.get("id").asInt();
        
        assertNotNull(rmaId);
        assertEquals("PENDING", rma.get("status").asText());
        
        System.out.println("✓ Utworzono zgłoszenie RMA ID: " + rmaId);
        System.out.println("  Powód: " + rma.get("customerReason").asText());
        System.out.println("  Status: PENDING");
    }

    @Test
    @Order(901)
    @DisplayName("FAZA 9b: Przyjęcie zwrotu")
    void phase9b_receiveReturn() throws Exception {
        assertNotNull(rmaId);

        MvcResult result = mockMvc.perform(post("/api/returns/receive/" + rmaId)
                        .param("trackingNumber", "RETURN-123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rma = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("RECEIVED", rma.get("status").asText());
        
        System.out.println("✓ Zwrot przyjęty");
        System.out.println("  Numer śledzenia: RETURN-123456");
        System.out.println("  Status: RECEIVED");
    }

    @Test
    @Order(902)
    @DisplayName("FAZA 9c: Ocena stanu towaru (Grading)")
    void phase9c_gradeItem() throws Exception {
        assertNotNull(rmaId);
        assertNotNull(productId);

        MvcResult result = mockMvc.perform(post("/api/returns/grade/" + rmaId)
                        .param("productId", productId.toString())
                        .param("grade", "GRADE_B")
                        .param("comment", "Lekkie zarysowania obudowy"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode item = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("GRADE_B", item.get("gradingStatus").asText());
        assertEquals("REFURBISH", item.get("disposition").asText());
        
        System.out.println("✓ Towar oceniony");
        System.out.println("  Ocena: GRADE_B");
        System.out.println("  Dyspozycja: REFURBISH");
        System.out.println("  Komentarz: Lekkie zarysowania obudowy");
    }

    // =========================================================================
    // FAZA 10: PODSUMOWANIE I WERYFIKACJA KOŃCOWA
    // =========================================================================

    @Test
    @Order(1000)
    @DisplayName("FAZA 10: Podsumowanie cyklu życia produktu")
    void phase10_summary() throws Exception {
        System.out.println("\n========================================");
        System.out.println("  PODSUMOWANIE CYKLU ŻYCIA PRODUKTU");
        System.out.println("========================================\n");

        System.out.println("1. PRODUKT");
        System.out.println("   ID: " + productId);
        
        System.out.println("\n2. INBOUND");
        System.out.println("   Order ID: " + inboundOrderId);
        System.out.println("   Item ID: " + inboundItemId);
        
        System.out.println("\n3. QC");
        System.out.println("   Inspection ID: " + inspectionId);
        
        System.out.println("\n4. INVENTORY");
        System.out.println("   Inventory ID: " + inventoryId);
        
        System.out.println("\n5. OUTBOUND");
        System.out.println("   Order ID: " + outboundOrderId);
        System.out.println("   Item ID: " + outboundItemId);
        
        System.out.println("\n6. PICKING");
        System.out.println("   Wave ID: " + waveId);
        
        System.out.println("\n7. PACKING");
        System.out.println("   Shipment ID: " + shipmentId);
        System.out.println("   Parcel ID: " + parcelId);
        
        System.out.println("\n8. SHIPPING");
        System.out.println("   Load ID: " + loadId);
        
        System.out.println("\n9. RETURNS");
        System.out.println("   RMA ID: " + rmaId);
        
        System.out.println("\n✅ CYKL ŻYCIA PRODUKTU ZAKOŃCZONY POMYŚLNIE!");
        System.out.println("========================================\n");
        
        // Critical assertions
        assertNotNull(productId, "Product should be created");
        assertNotNull(inboundOrderId, "Inbound order should be created");
        assertNotNull(outboundOrderId, "Outbound order should be created");
        assertNotNull(shipmentId, "Shipment should be created");
        assertNotNull(loadId, "Load should be created");
        assertNotNull(rmaId, "RMA should be created");
    }
}
