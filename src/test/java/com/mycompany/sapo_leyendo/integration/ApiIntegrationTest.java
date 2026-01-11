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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that verify REAL API functionality:
 * - Actual HTTP requests/responses
 * - Real database operations (H2 in-memory)
 * - Data persistence and retrieval
 * 
 * These tests DO NOT use mocks - they test the full stack.
 * Uses H2 in-memory database to avoid SQLite data corruption issues.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private UnitOfMeasureRepository uomRepository;
    
    @Autowired
    private ZoneRepository zoneRepository;

    private static Integer testProductId;
    private static Integer testLocationId;
    private static Integer testInventoryId;
    private static Integer testOrderId;
    
    // Store initialized test data IDs
    private static Integer initializedUomId;
    private static Integer initializedZoneId;
    private static Integer initializedProductId;
    private static Integer initializedLocationId;

    @BeforeEach
    void setupTestData() {
        // Ensure UnitOfMeasure exists
        if (uomRepository.count() == 0) {
            UnitOfMeasure uom = new UnitOfMeasure();
            uom.setId(1);
            uom.setCode("PCS");
            uom.setName("Piece");
            uom = uomRepository.save(uom);
            initializedUomId = uom.getId();
        } else {
            initializedUomId = uomRepository.findAll().get(0).getId();
        }
        
        // Ensure Zone exists
        if (zoneRepository.count() == 0) {
            Zone zone = new Zone();
            zone.setName("TEST-ZONE-" + System.currentTimeMillis());
            zone.setTemperatureControlled(false);
            zone.setSecure(false);
            zone.setAllowMixedSku(true);
            zone = zoneRepository.save(zone);
            initializedZoneId = zone.getId();
        } else {
            initializedZoneId = zoneRepository.findAll().get(0).getId();
        }
        
        // Ensure at least one product exists for tests that need it
        if (productRepository.count() == 0) {
            Product product = new Product();
            product.setSku("INIT-PRODUCT-001");
            product.setName("Initial Test Product");
            product.setIdBaseUom(initializedUomId);
            product.setWeightKg(1.0);
            product.setUnitPrice(new BigDecimal("10.00"));
            product = productRepository.save(product);
            initializedProductId = product.getId();
        } else {
            initializedProductId = productRepository.findAll().get(0).getId();
        }
        
        // Ensure at least one location exists
        if (locationRepository.count() == 0) {
            Zone zone = zoneRepository.findById(initializedZoneId).orElseThrow();
            Location location = new Location();
            location.setName("INIT-LOC-A1-" + System.currentTimeMillis());
            location.setZone(zone);
            location.setAisle("A");
            location.setRack("1");
            location.setLevel("1");
            location.setBin("01");
            location.setActive(true);
            location = locationRepository.save(location);
            initializedLocationId = location.getId();
        } else {
            initializedLocationId = locationRepository.findAll().get(0).getId();
        }
    }

    // ==================== Products API Tests ====================

    @Test
    @Order(1)
    void shouldGetAllProducts() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode products = objectMapper.readTree(content);
        
        assertTrue(products.isArray(), "Response should be an array");
        System.out.println("Products API returned " + products.size() + " products");
    }

    @Test
    @Order(2)
    void shouldCreateProductAndVerifyInDatabase() throws Exception {
        // Create product via API - use the initialized UOM
        Map<String, Object> productData = new HashMap<>();
        productData.put("sku", "TEST-API-" + System.currentTimeMillis());
        productData.put("name", "API Test Product");
        productData.put("weightKg", 1.5);
        productData.put("unitPrice", 99.99);
        productData.put("idBaseUom", initializedUomId); // Use initialized UOM ID

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        testProductId = created.get("id").asInt();
        
        assertNotNull(testProductId, "Created product should have an ID");
        
        // Verify product exists in database
        Product dbProduct = productRepository.findById(testProductId).orElse(null);
        assertNotNull(dbProduct, "Product should exist in database");
        assertEquals("API Test Product", dbProduct.getName());
        
        System.out.println("Created product with ID: " + testProductId);
    }

    @Test
    @Order(3)
    void shouldGetProductById() throws Exception {
        // Use an existing product from DB
        Product existingProduct = productRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(existingProduct, "Should have at least one product in DB");

        mockMvc.perform(get("/api/products/" + existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingProduct.getId()))
                .andExpect(jsonPath("$.name").exists());
    }

    // ==================== Locations API Tests ====================

    @Test
    @Order(10)
    void shouldGetAllLocations() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode locations = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(locations.isArray(), "Response should be an array");
        System.out.println("Locations API returned " + locations.size() + " locations");
    }

    @Test
    @Order(11)
    void shouldCreateLocationAndVerifyInDatabase() throws Exception {
        // First create a zone if needed
        Zone zone = new Zone();
        zone.setName("Test Zone API");
        zone.setAllowMixedSku(true);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("name", "API-TEST-LOC-" + System.currentTimeMillis());
        locationData.put("aisle", "Z");
        locationData.put("rack", "99");
        locationData.put("level", "1");
        locationData.put("bin", "A");
        locationData.put("active", true);

        MvcResult result = mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        testLocationId = created.get("id").asInt();
        
        assertNotNull(testLocationId, "Created location should have an ID");
        
        // Verify in database
        Location dbLocation = locationRepository.findById(testLocationId).orElse(null);
        assertNotNull(dbLocation, "Location should exist in database");
        
        System.out.println("Created location with ID: " + testLocationId);
    }

    // ==================== Dashboard Inventory API Tests ====================

    @Test
    @Order(20)
    void shouldGetDashboardInventory() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/dashboard/inventory")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode inventory = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(inventory.isArray(), "Response should be an array");
        
        if (inventory.size() > 0) {
            JsonNode firstItem = inventory.get(0);
            assertTrue(firstItem.has("id"), "Inventory item should have id");
            assertTrue(firstItem.has("quantity"), "Inventory item should have quantity");
        }
        
        System.out.println("Dashboard inventory API returned " + inventory.size() + " items");
    }

    @Test
    @Order(21)
    void shouldCreateInventoryViaDashboardAPI() throws Exception {
        // Get existing product and location
        Product product = productRepository.findAll().stream().findFirst().orElse(null);
        Location location = locationRepository.findAll().stream().findFirst().orElse(null);
        
        assertNotNull(product, "Need at least one product");
        assertNotNull(location, "Need at least one location");

        Map<String, Object> inventoryData = new HashMap<>();
        inventoryData.put("productId", product.getId());
        inventoryData.put("locationId", location.getId());
        inventoryData.put("quantity", 50);
        inventoryData.put("status", "AVAILABLE");
        inventoryData.put("batchNumber", "BATCH-API-TEST");

        MvcResult result = mockMvc.perform(post("/api/dashboard/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        testInventoryId = created.get("id").asInt();
        
        assertNotNull(testInventoryId, "Created inventory should have an ID");
        
        // Verify in database
        Inventory dbInventory = inventoryRepository.findById(testInventoryId).orElse(null);
        assertNotNull(dbInventory, "Inventory should exist in database");
        assertEquals(50, dbInventory.getQuantity());
        assertEquals("BATCH-API-TEST", dbInventory.getBatchNumber());
        
        System.out.println("Created inventory with ID: " + testInventoryId);
    }

    @Test
    @Order(22)
    void shouldUpdateInventoryViaDashboardAPI() throws Exception {
        // Get an existing inventory item
        Inventory existing = inventoryRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(existing, "Need at least one inventory item");
        
        int newQuantity = existing.getQuantity() + 10;

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", newQuantity);

        mockMvc.perform(put("/api/dashboard/inventory/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(newQuantity));

        // Verify in database
        Inventory updated = inventoryRepository.findById(existing.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(newQuantity, updated.getQuantity());
        
        System.out.println("Updated inventory ID " + existing.getId() + " quantity to " + newQuantity);
    }

    @Test
    @Order(23)
    void shouldDeleteInventoryViaDashboardAPI() throws Exception {
        // Create a new inventory to delete with all required fields
        Product product = productRepository.findAll().stream().findFirst().orElse(null);
        Location location = locationRepository.findAll().stream().findFirst().orElse(null);
        UnitOfMeasure uom = uomRepository.findAll().stream().findFirst().orElse(null);
        
        assertNotNull(product, "Need a product");
        assertNotNull(location, "Need a location");
        assertNotNull(uom, "Need a UOM");
        
        Inventory toDelete = new Inventory();
        toDelete.setProduct(product);
        toDelete.setLocation(location);
        toDelete.setUom(uom);
        toDelete.setQuantity(1);
        toDelete.setStatus(InventoryStatus.AVAILABLE);
        toDelete.setReceivedAt(LocalDateTime.now());
        toDelete = inventoryRepository.save(toDelete);
        
        Integer deleteId = toDelete.getId();

        mockMvc.perform(delete("/api/dashboard/inventory/" + deleteId))
                .andExpect(status().isOk());

        // Verify deleted from database
        assertFalse(inventoryRepository.existsById(deleteId), "Inventory should be deleted");
        
        System.out.println("Deleted inventory with ID: " + deleteId);
    }

    // ==================== Dashboard Orders API Tests ====================

    @Test
    @Order(30)
    void shouldGetDashboardOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/dashboard/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orders = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(orders.isArray(), "Response should be an array");
        System.out.println("Dashboard orders API returned " + orders.size() + " orders");
    }

    @Test
    @Order(31)
    void shouldCreateOrderViaDashboardAPI() throws Exception {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderNumber", "ORD-API-TEST-" + System.currentTimeMillis());
        orderData.put("customer", "API Test Customer");
        orderData.put("total", 199.99);
        orderData.put("priority", "High");

        MvcResult result = mockMvc.perform(post("/api/dashboard/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        String orderId = created.get("id").asText();
        
        assertNotNull(orderId, "Created order should have an ID");
        
        // Dashboard orders endpoint returns mock data with UUID, not database persistence
        // The order is echoed back with generated ID and status but not saved to DB
        System.out.println("Created dashboard order with ID: " + orderId);
    }

    // ==================== Dashboard Shipments API Tests ====================

    @Test
    @Order(40)
    void shouldGetDashboardShipments() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/dashboard/shipments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode shipments = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(shipments.isArray(), "Response should be an array");
        System.out.println("Dashboard shipments API returned " + shipments.size() + " shipments");
    }

    // ==================== Outbound Orders API Tests ====================

    @Test
    @Order(50)
    void shouldGetAllOutboundOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/outbound")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orders = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(orders.isArray(), "Response should be an array");
        System.out.println("Outbound orders API returned " + orders.size() + " orders");
    }

    // ==================== Inbound Orders API Tests ====================

    @Test
    @Order(60)
    void shouldGetAllInboundOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inbound")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orders = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(orders.isArray(), "Response should be an array");
        System.out.println("Inbound orders API returned " + orders.size() + " orders");
    }

    @Test
    @Order(61)
    void shouldCreateInboundOrder() throws Exception {
        Product product = productRepository.findAll().stream().findFirst().orElse(null);
        Location location = locationRepository.findAll().stream().findFirst().orElse(null);
        
        assertNotNull(product, "Need at least one product");
        assertNotNull(location, "Need at least one location");

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderReference", "INB-API-TEST-" + System.currentTimeMillis());
        orderData.put("status", "PLANNED");
        orderData.put("expectedArrival", LocalDateTime.now().plusDays(1).toString());
        orderData.put("supplier", "API Test Supplier");
        orderData.put("dockId", location.getId());

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("product", Map.of("id", product.getId()));
        itemData.put("expectedQuantity", 100);
        itemData.put("batchNumber", "BATCH-API");

        orderData.put("items", java.util.List.of(itemData));

        MvcResult result = mockMvc.perform(post("/api/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        Integer inboundId = created.get("id").asInt();
        
        assertNotNull(inboundId, "Created inbound order should have an ID");
        
        // Verify in database
        assertTrue(inboundOrderRepository.existsById(inboundId), "Inbound order should exist in database");
        
        System.out.println("Created inbound order with ID: " + inboundId);
    }

    // ==================== Returns API Tests ====================

    @Test
    @Order(70)
    void shouldGetAllReturns() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode returns = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(returns.isArray(), "Response should be an array");
        System.out.println("Returns API returned " + returns.size() + " returns");
    }

    // ==================== Shipping API Tests ====================

    @Test
    @Order(80)
    void shouldGetAllCarriers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/shipping/carriers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode carriers = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(carriers.isArray(), "Response should be an array");
        System.out.println("Carriers API returned " + carriers.size() + " carriers");
    }

    // ==================== API Error Handling Tests ====================

    @Test
    @Order(90)
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(91)
    void shouldReturn404ForNonExistentInventory() throws Exception {
        mockMvc.perform(put("/api/dashboard/inventory/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 10}"))
                .andExpect(status().isNotFound());
    }

    // ==================== Data Integrity Tests ====================

    @Test
    @Order(100)
    void shouldMaintainDataIntegrityAcrossRequests() throws Exception {
        // Create a product with all required fields - use initialized UOM
        Map<String, Object> productData = new HashMap<>();
        productData.put("sku", "INTEGRITY-TEST-" + System.currentTimeMillis());
        productData.put("name", "Integrity Test Product");
        productData.put("weightKg", 2.0);
        productData.put("idBaseUom", initializedUomId); // Use initialized UOM ID

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Integer productId = created.get("id").asInt();

        // Fetch the same product
        MvcResult getResult = mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode fetched = objectMapper.readTree(getResult.getResponse().getContentAsString());
        
        assertEquals(productId, fetched.get("id").asInt());
        assertEquals("Integrity Test Product", fetched.get("name").asText());
        
        System.out.println("Data integrity verified for product ID: " + productId);
    }
}
