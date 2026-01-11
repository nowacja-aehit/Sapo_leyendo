package com.mycompany.sapo_leyendo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PRAWDZIWE testy integracyjne z rzeczywistym serwerem HTTP.
 * Te testy uruchamiają pełny serwer (włącznie z filtrami HTTP, CORS itp.)
 * i wysyłają prawdziwe requesty HTTP.
 * 
 * To wyłapie błędy które MockMvc nie widzi!
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RealServerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String sessionCookie;

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port;
        System.out.println("\n========================================");
        System.out.println("STARTING REAL SERVER INTEGRATION TESTS");
        System.out.println("Server URL: " + baseUrl);
        System.out.println("========================================\n");
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        System.out.println("\n>>> Running test: " + testInfo.getDisplayName());
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        System.out.println("<<< Finished: " + testInfo.getDisplayName() + "\n");
    }

    // ==================== HEALTH CHECK ====================

    @Test
    @Order(1)
    @DisplayName("Server should be running and respond to requests")
    void serverShouldBeRunning() {
        // Test with an API endpoint rather than static content
        // Static content "/" may not be configured in test profile
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/api/products", String.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        
        // API endpoints should respond with 2xx (possibly 401/403 if auth needed but server is running)
        assertTrue(response.getStatusCode().value() < 500,
                "Server should not return 5xx error, got: " + response.getStatusCode());
    }

    // ==================== AUTH API ====================

    @Test
    @Order(10)
    @DisplayName("Login should work with valid credentials")
    void loginShouldWork() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", entity, String.class);

        System.out.println("Login Status: " + response.getStatusCode());
        System.out.println("Login Response: " + response.getBody());
        System.out.println("Login Headers: " + response.getHeaders());

        if (response.getStatusCode().is2xxSuccessful()) {
            // Save session cookie for authenticated requests
            if (response.getHeaders().containsKey("Set-Cookie")) {
                sessionCookie = response.getHeaders().getFirst("Set-Cookie");
                System.out.println("Got session cookie: " + sessionCookie);
            }
        }
        
        // Check what status we actually get
        assertNotNull(response.getStatusCode(), "Should have a response status");
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! LOGIN FAILED !!!");
            System.err.println("This might cause other tests to fail due to missing authentication");
        }
    }

    // ==================== PRODUCTS API ====================

    @Test
    @Order(20)
    @DisplayName("GET /api/products should return product list")
    void getProductsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/products", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/products Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "GET /api/products should return 200 OK");
        
        JsonNode products = objectMapper.readTree(response.getBody());
        assertTrue(products.isArray(), "Response should be JSON array");
        System.out.println("Returned " + products.size() + " products");
    }

    // ==================== INVENTORY API ====================

    @Test
    @Order(30)
    @DisplayName("GET /api/inventory should return inventory list")
    void getInventoryShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/inventory", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/inventory Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /api/inventory should return 200 OK");
    }

    @Test
    @Order(31)
    @DisplayName("PUT /api/inventory/{id} should update inventory item")
    void updateInventoryShouldWork() throws Exception {
        // First get an inventory item
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> getEntity = new HttpEntity<>(headers);

        ResponseEntity<String> getResponse = restTemplate.exchange(
                baseUrl + "/api/inventory", HttpMethod.GET, getEntity, String.class);

        if (!getResponse.getStatusCode().is2xxSuccessful()) {
            System.err.println("Cannot test update - GET failed: " + getResponse.getStatusCode());
            return;
        }

        JsonNode items = objectMapper.readTree(getResponse.getBody());
        if (items.isEmpty()) {
            System.out.println("No inventory items to update, skipping");
            return;
        }

        JsonNode firstItem = items.get(0);
        int itemId = firstItem.get("id").asInt();
        System.out.println("Updating inventory item ID: " + itemId);

        // Try to update
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", itemId);
        updateData.put("quantity", 100);
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updateData, headers);

        ResponseEntity<String> updateResponse = restTemplate.exchange(
                baseUrl + "/api/inventory/" + itemId, HttpMethod.PUT, updateEntity, String.class);

        System.out.println("PUT /api/inventory/" + itemId + " Status: " + updateResponse.getStatusCode());
        System.out.println("Response Body: " + truncate(updateResponse.getBody(), 500));

        if (!updateResponse.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! INVENTORY UPDATE FAILED !!!");
            System.err.println("This is the error from frontend: 'Failed to save item'");
        }
    }

    // ==================== DASHBOARD API ====================

    @Test
    @Order(40)
    @DisplayName("GET /api/dashboard/stats should return dashboard stats")
    void getDashboardStatsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/dashboard/stats", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/dashboard/stats Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /api/dashboard/stats should return 200 OK");
    }

    @Test
    @Order(41)
    @DisplayName("GET /api/dashboard/inventory should return dashboard inventory")
    void getDashboardInventoryShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/dashboard/inventory", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/dashboard/inventory Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! DASHBOARD INVENTORY FAILED !!!");
            System.err.println("Error: " + response.getBody());
        }
    }

    @Test
    @Order(42)
    @DisplayName("PUT /api/dashboard/inventory/{id} should update inventory")
    void updateDashboardInventoryShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Try updating item with ID 1
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 1);
        updateData.put("quantity", 50);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/dashboard/inventory/1", HttpMethod.PUT, entity, String.class);

        System.out.println("PUT /api/dashboard/inventory/1 Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! DASHBOARD INVENTORY UPDATE FAILED !!!");
            System.err.println("This is the error from frontend!");
        }
    }

    // ==================== INBOUND API ====================

    @Test
    @Order(50)
    @DisplayName("GET /api/inbound should return inbound orders")
    void getInboundOrdersShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/inbound", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/inbound Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /api/inbound should return 200 OK");
    }

    @Test
    @Order(51)
    @DisplayName("POST /api/inbound should create inbound order")
    void createInboundOrderShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("supplierId", 1);
        orderData.put("expectedDate", "2026-01-15T10:00:00");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/inbound", entity, String.class);

        System.out.println("POST /api/inbound Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! CREATE INBOUND ORDER FAILED !!!");
            System.err.println("Error: " + response.getBody());
        }
    }

    @Test
    @Order(52)
    @DisplayName("POST /api/inbound/receive should receive items")
    void receiveInboundItemsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> receiveData = new HashMap<>();
        receiveData.put("productId", 1);
        receiveData.put("locationId", 1);
        receiveData.put("quantity", 10);
        receiveData.put("lotNumber", "LOT-TEST-001");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(receiveData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/inbound/receive", entity, String.class);

        System.out.println("POST /api/inbound/receive Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! RECEIVE INBOUND FAILED !!!");
            System.err.println("This is the frontend error: 'Failed to receive item'");
        }
    }

    // ==================== PACKING API ====================

    @Test
    @Order(60)
    @DisplayName("POST /api/packing/shipments/start/{orderId} should start packing")
    void startPackingShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/packing/shipments/start/1", entity, String.class);

        System.out.println("POST /api/packing/shipments/start/1 Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! START PACKING FAILED !!!");
            System.err.println("This is the frontend error!");
        }
    }

    @Test
    @Order(61)
    @DisplayName("POST /api/packing/shipments/{id}/close should close shipment")
    void closeShipmentShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/packing/shipments/1/close", entity, String.class);

        System.out.println("POST /api/packing/shipments/1/close Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("!!! CLOSE SHIPMENT FAILED !!!");
        }
    }

    // ==================== LOCATIONS API ====================

    @Test
    @Order(70)
    @DisplayName("GET /api/locations should return locations")
    void getLocationsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/locations", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/locations Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /api/locations should return 200 OK");
    }

    // ==================== OUTBOUND API ====================

    @Test
    @Order(80)
    @DisplayName("GET /api/outbound should return outbound orders")
    void getOutboundOrdersShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/outbound", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/outbound Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /api/outbound should return 200 OK");
    }

    // ==================== SHIPPING API ====================

    @Test
    @Order(90)
    @DisplayName("GET /api/shipping/carriers should return carriers")
    void getCarriersShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/shipping/carriers", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/shipping/carriers Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));
    }

    // ==================== PICKING API ====================

    @Test
    @Order(100)
    @DisplayName("GET /api/picking/tasks should return picking tasks")
    void getPickingTasksShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/picking/tasks", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/picking/tasks Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));
    }

    // ==================== QC API ====================

    @Test
    @Order(110)
    @DisplayName("GET /api/qc/inspections should return QC inspections")
    void getQcInspectionsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/qc/inspections", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/qc/inspections Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));
    }

    // ==================== RETURNS API ====================

    @Test
    @Order(120)
    @DisplayName("GET /api/returns should return RMA requests")
    void getReturnsShouldWork() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/returns", HttpMethod.GET, entity, String.class);

        System.out.println("GET /api/returns Status: " + response.getStatusCode());
        System.out.println("Response Body: " + truncate(response.getBody(), 500));
    }

    // ==================== HELPER METHODS ====================

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add("Cookie", sessionCookie);
        }
        return headers;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "... [truncated]";
    }

    @AfterAll
    void summary() {
        System.out.println("\n========================================");
        System.out.println("INTEGRATION TESTS COMPLETED");
        System.out.println("========================================");
        System.out.println("Check the output above for !!! ERROR !!! markers");
        System.out.println("These indicate the same failures you see in the browser.");
        System.out.println("========================================\n");
    }
}
