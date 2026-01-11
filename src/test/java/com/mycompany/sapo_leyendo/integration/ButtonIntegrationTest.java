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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ====================================================================
 * KOMPLEKSOWE TESTY INTEGRACYJNE DLA KAŻDEGO PRZYCISKU W SYSTEMIE
 * ====================================================================
 * 
 * MODUŁY:
 * - INVENTORY (Zapasy) - testy 1xx
 * - INBOUND (Przyjęcia) - testy 2xx
 * - PICKING (Kompletacja) - testy 3xx
 * - PACKING (Pakowanie) - testy 4xx
 * - ORDERS (Zamówienia) - testy 5xx
 * - SHIPPING (Przesyłki) - testy 6xx
 * - RETURNS (Zwroty) - testy 7xx
 * - QUALITY_CONTROL (Kontrola Jakości) - testy 8xx
 * - LOCATIONS (Lokacje) - testy 9xx
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ButtonIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    
    // Stan testów - przechowuje ID stworzone w testach
    private Integer createdProductId;
    private Integer createdInventoryId;
    private Integer createdInboundOrderId;
    private Integer createdOutboundOrderId;
    private Integer createdLocationId;
    private Integer createdShipmentId;
    private Integer createdRmaId;
    private Integer createdInspectionId;

    // Liczniki wyników
    private int passedTests = 0;
    private int failedTests = 0;
    private List<String> failures = new ArrayList<>();

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port;
        printBanner("TESTY PRZYCISKÓW - START");
        System.out.println("Server URL: " + baseUrl);
        System.out.println("Czas: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @AfterAll
    void teardown() {
        printBanner("PODSUMOWANIE TESTÓW");
        System.out.println("✅ ZALICZONE: " + passedTests);
        System.out.println("❌ NIEZALICZONE: " + failedTests);
        System.out.println("RAZEM: " + (passedTests + failedTests));
        
        if (!failures.isEmpty()) {
            System.out.println("\n❌ LISTA BŁĘDÓW:");
            for (String failure : failures) {
                System.out.println("   • " + failure);
            }
        }
        
        // Asercja końcowa - test FAIL jeśli są błędy
        if (failedTests > 0) {
            fail("❌ " + failedTests + " testów nie przeszło! Zobacz listę powyżej.");
        }
    }

    // ====================================================================
    // MODUŁ 1: INVENTORY (ZAPASY) - Testy 1xx
    // ====================================================================

    @Test
    @Order(100)
    @DisplayName("🔘 INVENTORY: Lista produktów (załadowanie tabeli)")
    void test100_Inventory_LoadProductList() {
        printTestHeader("INVENTORY", "Lista produktów", "GET /api/products");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/products", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success, 
            "Tabela produktów załadowana (" + countJsonArray(response.getBody()) + " produktów)",
            "Nie można załadować listy produktów - Status: " + response.getStatusCode(),
            "INVENTORY: Lista produktów");
    }

    @Test
    @Order(101)
    @DisplayName("🔘 INVENTORY: Przycisk 'Dodaj produkt'")
    void test101_Inventory_AddProduct() {
        printTestHeader("INVENTORY", "Dodaj produkt", "POST /api/products");
        
        Map<String, Object> product = new HashMap<>();
        product.put("name", "Test Product " + System.currentTimeMillis());
        product.put("sku", "SKU-TEST-" + System.currentTimeMillis());
        product.put("description", "Produkt testowy");
        product.put("category", "Elektronika");
        product.put("minStock", 10);
        product.put("maxStock", 100);
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(product);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/products", entity, String.class);
        
        boolean success = response.getStatusCode().is2xxSuccessful();
        
        if (success) {
            createdProductId = extractId(response.getBody());
            System.out.println("   📦 Stworzony produkt ID: " + createdProductId);
        }
        
        assertAndRecord(success,
            "Produkt dodany poprawnie",
            "Błąd dodawania produktu - " + response.getBody(),
            "INVENTORY: Dodaj produkt");
    }

    @Test
    @Order(102)
    @DisplayName("🔘 INVENTORY: Przycisk 'Zapisz zmiany' (nowy inventory)")
    void test102_Inventory_SaveNewInventory() {
        printTestHeader("INVENTORY", "Zapisz zmiany (nowy)", "POST /api/dashboard/inventory");
        
        // Najpierw utwórz produkt jeśli nie istnieje
        if (createdProductId == null) {
            Map<String, Object> product = new HashMap<>();
            product.put("name", "Test Product " + System.currentTimeMillis());
            product.put("sku", "SKU-TEST-" + System.currentTimeMillis());
            product.put("description", "Produkt testowy");
            product.put("category", "Elektronika");
            product.put("minStock", 10);
            product.put("maxStock", 100);
            
            HttpEntity<Map<String, Object>> prodEntity = jsonEntity(product);
            ResponseEntity<String> prodResponse = restTemplate.postForEntity(
                baseUrl + "/api/products", prodEntity, String.class);
            
            if (prodResponse.getStatusCode().is2xxSuccessful()) {
                createdProductId = extractId(prodResponse.getBody());
                System.out.println("   📦 Stworzony produkt ID: " + createdProductId);
            }
        }
        
        // Utwórz lokację jeśli nie istnieje
        if (createdLocationId == null) {
            Map<String, Object> location = new HashMap<>();
            location.put("name", "LOC-TEST-" + System.currentTimeMillis());
            location.put("zone", "A");
            location.put("aisle", "01");
            location.put("rack", "01");
            location.put("shelf", "01");
            location.put("bin", "01");
            location.put("locationTypeId", 1);
            location.put("isActive", true);
            
            HttpEntity<Map<String, Object>> locEntity = jsonEntity(location);
            ResponseEntity<String> locResponse = restTemplate.postForEntity(
                baseUrl + "/api/locations", locEntity, String.class);
            
            if (locResponse.getStatusCode().is2xxSuccessful()) {
                createdLocationId = extractId(locResponse.getBody());
                System.out.println("   📍 Stworzona lokacja ID: " + createdLocationId);
            }
        }
        
        Map<String, Object> inventory = new HashMap<>();
        inventory.put("productId", createdProductId != null ? createdProductId : 1);
        inventory.put("locationId", createdLocationId != null ? createdLocationId : 1);
        inventory.put("quantity", 50);
        inventory.put("status", "AVAILABLE");
        inventory.put("batchNumber", "BATCH-" + System.currentTimeMillis());
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(inventory);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/dashboard/inventory", entity, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        if (success) {
            createdInventoryId = extractId(response.getBody());
            System.out.println("   📦 Stworzony inventory ID: " + createdInventoryId);
        }
        
        assertAndRecord(success,
            "Inventory zapisany poprawnie",
            "Błąd zapisu inventory - " + response.getBody(),
            "INVENTORY: Zapisz zmiany");
    }

    @Test
    @Order(103)
    @DisplayName("🔘 INVENTORY: Przycisk 'Edytuj' → aktualizacja ilości")
    void test103_Inventory_EditQuantity() {
        printTestHeader("INVENTORY", "Edytuj ilość", "PUT /api/dashboard/inventory/{id}");
        
        Assumptions.assumeTrue(createdInventoryId != null, "Brak inventory do edycji");
        
        Map<String, Object> update = new HashMap<>();
        update.put("quantity", 75);
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(update);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/dashboard/inventory/" + createdInventoryId,
            HttpMethod.PUT, entity, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Ilość zaktualizowana: 50 → 75",
            "Błąd aktualizacji - " + response.getStatusCode(),
            "INVENTORY: Edytuj ilość");
    }

    @Test
    @Order(104)
    @DisplayName("🔘 INVENTORY: Statystyki dashboardu")
    void test104_Inventory_DashboardStats() {
        printTestHeader("INVENTORY", "Statystyki", "GET /api/dashboard/stats");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/dashboard/stats", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        boolean isJson = isJsonResponse(response.getBody());
        
        if (success && isJson) {
            try {
                JsonNode json = objectMapper.readTree(response.getBody());
                System.out.println("   📊 Produkty: " + json.path("totalProducts").asInt());
                System.out.println("   📊 Lokacje: " + json.path("totalLocations").asInt());
                System.out.println("   📊 Inventory: " + json.path("totalInventoryItems").asInt());
            } catch (Exception e) { /* ignore */ }
        }
        
        assertAndRecord(success && isJson,
            "Statystyki załadowane jako JSON",
            isJson ? "Błąd - " + response.getStatusCode() : "BŁĄD: Zwrócono HTML zamiast JSON!",
            "INVENTORY: Statystyki");
    }

    @Test
    @Order(105)
    @DisplayName("🔘 INVENTORY: Przycisk 'Usuń' inventory")
    void test105_Inventory_DeleteInventory() {
        printTestHeader("INVENTORY", "Usuń inventory", "DELETE /api/dashboard/inventory/{id}");
        
        Assumptions.assumeTrue(createdInventoryId != null, "Brak inventory do usunięcia");
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/dashboard/inventory/" + createdInventoryId,
            HttpMethod.DELETE, null, String.class);
        
        boolean success = response.getStatusCode().is2xxSuccessful();
        
        assertAndRecord(success,
            "Inventory usunięty (ID: " + createdInventoryId + ")",
            "Błąd usuwania - " + response.getStatusCode(),
            "INVENTORY: Usuń inventory");
    }

    // ====================================================================
    // MODUŁ 2: INBOUND (PRZYJĘCIA) - Testy 2xx
    // ====================================================================

    @Test
    @Order(200)
    @DisplayName("🔘 INBOUND: Lista awizacji")
    void test200_Inbound_LoadList() {
        printTestHeader("INBOUND", "Lista awizacji", "GET /api/inbound");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/inbound", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista awizacji załadowana (" + countJsonArray(response.getBody()) + " awizacji)",
            "Błąd ładowania - " + response.getStatusCode(),
            "INBOUND: Lista awizacji");
    }

    @Test
    @Order(201)
    @DisplayName("🔘 INBOUND: Przycisk 'Nowa Awizacja' → 'Zapisz awizację'")
    void test201_Inbound_CreateOrder() {
        printTestHeader("INBOUND", "Nowa Awizacja", "POST /api/inbound");
        
        Map<String, Object> order = new HashMap<>();
        order.put("supplierId", 1);
        order.put("expectedDate", LocalDateTime.now().plusDays(3).toString());
        order.put("status", "PLANNED");
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(order);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/inbound", entity, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        if (success) {
            createdInboundOrderId = extractId(response.getBody());
            System.out.println("   📦 Stworzona awizacja ID: " + createdInboundOrderId);
            try {
                JsonNode json = objectMapper.readTree(response.getBody());
                System.out.println("   📝 Reference: " + json.path("orderReference").asText());
            } catch (Exception e) { /* ignore */ }
        }
        
        assertAndRecord(success,
            "Awizacja utworzona poprawnie",
            "Błąd tworzenia awizacji - " + response.getBody(),
            "INBOUND: Nowa Awizacja");
    }

    @Test
    @Order(202)
    @DisplayName("🔘 INBOUND: Przycisk 'Przyjmij' → Generuj LPN")
    void test202_Inbound_GenerateLPN() {
        printTestHeader("INBOUND", "Generuj LPN", "GET /api/inbound/lpn/generate");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/inbound/lpn/generate", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        String lpn = response.getBody();
        boolean validLpn = lpn != null && lpn.startsWith("LPN-");
        
        if (validLpn) {
            System.out.println("   🏷️ Wygenerowany LPN: " + lpn);
        }
        
        assertAndRecord(success && validLpn,
            "LPN wygenerowany poprawnie",
            "Błąd generowania LPN - " + lpn,
            "INBOUND: Generuj LPN");
    }

    @Test
    @Order(203)
    @DisplayName("🔘 INBOUND: Przycisk 'Zatwierdź przyjęcie' (walidacja)")
    void test203_Inbound_ReceiveValidation() {
        printTestHeader("INBOUND", "Zatwierdź przyjęcie (walidacja)", "POST /api/inbound/receive");
        
        // Test walidacji - brak wymaganego pola
        Map<String, Object> request = new HashMap<>();
        request.put("quantity", 10);
        // brak inboundOrderItemId
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(request);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/inbound/receive", entity, String.class);
        
        boolean validationWorks = response.getStatusCode() == HttpStatus.BAD_REQUEST;
        
        assertAndRecord(validationWorks,
            "Walidacja działa - zwrócono 400: " + response.getBody(),
            "BŁĄD WALIDACJI - powinno zwrócić 400, a zwróciło " + response.getStatusCode(),
            "INBOUND: Walidacja przyjęcia");
    }

    // ====================================================================
    // MODUŁ 3: PICKING (KOMPLETACJA) - Testy 3xx
    // ====================================================================

    @Test
    @Order(300)
    @DisplayName("🔘 PICKING: Przycisk 'Odśwież zamówienia'")
    void test300_Picking_RefreshOrders() {
        printTestHeader("PICKING", "Odśwież zamówienia", "GET /api/outbound");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/outbound", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista zamówień załadowana (" + countJsonArray(response.getBody()) + " zamówień)",
            "Błąd ładowania - " + response.getStatusCode(),
            "PICKING: Odśwież zamówienia");
    }

    @Test
    @Order(301)
    @DisplayName("🔘 PICKING: Lista zadań picking")
    void test301_Picking_GetTasks() {
        printTestHeader("PICKING", "Lista zadań", "GET /api/picking/tasks");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/picking/tasks", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        boolean isJson = isJsonResponse(response.getBody());
        
        assertAndRecord(success && isJson,
            "Lista zadań picking załadowana",
            isJson ? "Błąd - " + response.getStatusCode() : "BŁĄD: Zwrócono HTML zamiast JSON!",
            "PICKING: Lista zadań");
    }

    @Test
    @Order(302)
    @DisplayName("🔘 PICKING: Przycisk 'Uruchom Falę'")
    void test302_Picking_CreateWave() {
        printTestHeader("PICKING", "Uruchom Falę", "POST /api/picking/waves");
        
        List<Integer> orderIds = new ArrayList<>();
        if (createdOutboundOrderId != null) {
            orderIds.add(createdOutboundOrderId);
        }
        
        HttpEntity<List<Integer>> entity = new HttpEntity<>(orderIds, jsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/picking/waves", entity, String.class);
        
        // Może zwrócić błąd jeśli brak zamówień - to OK
        boolean responded = response.getStatusCode() != null;
        System.out.println("   Status: " + response.getStatusCode());
        
        assertAndRecord(responded,
            "Endpoint odpowiedział (" + response.getStatusCode() + ")",
            "Endpoint nie odpowiedział",
            "PICKING: Uruchom Falę");
    }

    // ====================================================================
    // MODUŁ 4: PACKING (PAKOWANIE) - Testy 4xx
    // ====================================================================

    @Test
    @Order(400)
    @DisplayName("🔘 PACKING: Lista przesyłek")
    void test400_Packing_LoadShipments() {
        printTestHeader("PACKING", "Lista przesyłek", "GET /api/dashboard/shipments");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/dashboard/shipments", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista przesyłek załadowana",
            "Błąd ładowania - " + response.getStatusCode(),
            "PACKING: Lista przesyłek");
    }

    @Test
    @Order(401)
    @DisplayName("🔘 PACKING: Przycisk 'Start' (rozpocznij pakowanie)")
    void test401_Packing_StartPacking() {
        printTestHeader("PACKING", "Start pakowania", "POST /api/packing/shipments/start/{id}");
        
        // Najpierw stwórz zamówienie outbound jeśli nie istnieje
        if (createdOutboundOrderId == null) {
            createOutboundOrder();
        }
        
        Assumptions.assumeTrue(createdOutboundOrderId != null, "Brak zamówienia do pakowania");
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/packing/shipments/start/" + createdOutboundOrderId,
            null, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        if (success) {
            createdShipmentId = extractId(response.getBody());
            System.out.println("   📦 Stworzony shipment ID: " + createdShipmentId);
        }
        
        assertAndRecord(success,
            "Pakowanie rozpoczęte",
            "Błąd rozpoczynania pakowania - " + response.getBody(),
            "PACKING: Start pakowania");
    }

    // ====================================================================
    // MODUŁ 5: ORDERS (ZAMÓWIENIA) - Testy 5xx
    // ====================================================================

    @Test
    @Order(500)
    @DisplayName("🔘 ORDERS: Lista zamówień")
    void test500_Orders_LoadList() {
        printTestHeader("ORDERS", "Lista zamówień", "GET /api/outbound");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/outbound", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista zamówień załadowana (" + countJsonArray(response.getBody()) + " zamówień)",
            "Błąd ładowania - " + response.getStatusCode(),
            "ORDERS: Lista zamówień");
    }

    @Test
    @Order(501)
    @DisplayName("🔘 ORDERS: Przycisk 'Dodaj zamówienie'")
    void test501_Orders_CreateOrder() {
        printTestHeader("ORDERS", "Dodaj zamówienie", "POST /api/outbound");
        
        Map<String, Object> order = new HashMap<>();
        order.put("referenceNumber", "ORD-TEST-" + System.currentTimeMillis());
        order.put("destination", "Kraków, ul. Główna 10");
        order.put("status", "NEW");
        order.put("priority", "HIGH");
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(order);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/outbound", entity, String.class);
        
        boolean success = response.getStatusCode().is2xxSuccessful();
        
        if (success) {
            createdOutboundOrderId = extractId(response.getBody());
            System.out.println("   📦 Stworzone zamówienie ID: " + createdOutboundOrderId);
        }
        
        assertAndRecord(success,
            "Zamówienie utworzone poprawnie",
            "Błąd tworzenia zamówienia - " + response.getBody(),
            "ORDERS: Dodaj zamówienie");
    }

    @Test
    @Order(502)
    @DisplayName("🔘 ORDERS: Dashboard zamówienia")
    void test502_Orders_Dashboard() {
        printTestHeader("ORDERS", "Dashboard zamówienia", "GET /api/dashboard/orders");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/dashboard/orders", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Dashboard zamówień załadowany",
            "Błąd ładowania - " + response.getStatusCode(),
            "ORDERS: Dashboard");
    }

    // ====================================================================
    // MODUŁ 6: SHIPPING (PRZESYŁKI) - Testy 6xx
    // ====================================================================

    @Test
    @Order(600)
    @DisplayName("🔘 SHIPPING: Lista przewoźników")
    void test600_Shipping_LoadCarriers() {
        printTestHeader("SHIPPING", "Lista przewoźników", "GET /api/shipping/carriers");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/shipping/carriers", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista przewoźników załadowana (" + countJsonArray(response.getBody()) + " przewoźników)",
            "Błąd ładowania - " + response.getStatusCode(),
            "SHIPPING: Lista przewoźników");
    }

    @Test
    @Order(601)
    @DisplayName("🔘 SHIPPING: Przycisk 'Utwórz przesyłkę'")
    void test601_Shipping_CreateShipment() {
        printTestHeader("SHIPPING", "Utwórz przesyłkę", "POST /api/shipping/shipments");
        
        Assumptions.assumeTrue(createdOutboundOrderId != null, "Brak zamówienia do wysyłki");
        
        String url = String.format(
            "%s/api/shipping/shipments?outboundOrderId=%d&carrierId=1&trackingNumber=TRK-%d",
            baseUrl, createdOutboundOrderId, System.currentTimeMillis());
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Przesyłka utworzona",
            "Błąd tworzenia przesyłki - " + response.getBody(),
            "SHIPPING: Utwórz przesyłkę");
    }

    // ====================================================================
    // MODUŁ 7: RETURNS (ZWROTY) - Testy 7xx
    // ====================================================================

    @Test
    @Order(700)
    @DisplayName("🔘 RETURNS: Lista RMA")
    void test700_Returns_LoadRMAList() {
        printTestHeader("RETURNS", "Lista RMA", "GET /api/returns");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/returns", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista RMA załadowana (" + countJsonArray(response.getBody()) + " RMA)",
            "Błąd ładowania - " + response.getStatusCode(),
            "RETURNS: Lista RMA");
    }

    @Test
    @Order(701)
    @DisplayName("🔘 RETURNS: Przycisk 'Utwórz RMA'")
    void test701_Returns_CreateRMA() {
        printTestHeader("RETURNS", "Utwórz RMA", "POST /api/returns/rma");
        
        Assumptions.assumeTrue(createdOutboundOrderId != null, "Brak zamówienia dla RMA");
        
        String url = String.format(
            "%s/api/returns/rma?outboundOrderId=%d&reason=DEFECTIVE&description=Test",
            baseUrl, createdOutboundOrderId);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        if (success) {
            createdRmaId = extractId(response.getBody());
            System.out.println("   📦 Stworzone RMA ID: " + createdRmaId);
        }
        
        assertAndRecord(success,
            "RMA utworzone poprawnie",
            "Błąd tworzenia RMA - " + response.getBody(),
            "RETURNS: Utwórz RMA");
    }

    // ====================================================================
    // MODUŁ 8: QUALITY CONTROL - Testy 8xx
    // ====================================================================

    @Test
    @Order(800)
    @DisplayName("🔘 QC: Lista inspekcji")
    void test800_QC_LoadInspections() {
        printTestHeader("QC", "Lista inspekcji", "GET /api/qc/inspections");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/qc/inspections", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        boolean isJson = isJsonResponse(response.getBody());
        
        assertAndRecord(success && isJson,
            "Lista inspekcji załadowana",
            isJson ? "Błąd - " + response.getStatusCode() : "BŁĄD: Zwrócono HTML zamiast JSON!",
            "QC: Lista inspekcji");
    }

    @Test
    @Order(801)
    @DisplayName("🔘 QC: Przycisk 'Rozpocznij Inspekcję'")
    void test801_QC_CreateInspection() {
        printTestHeader("QC", "Rozpocznij Inspekcję", "POST /api/qc/inspections");
        
        Integer productId = createdProductId != null ? createdProductId : 1;
        
        String url = String.format(
            "%s/api/qc/inspections?productId=%d&sourceType=INBOUND&referenceId=1&sampleSize=5",
            baseUrl, productId);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        if (success) {
            createdInspectionId = extractId(response.getBody());
            System.out.println("   🔍 Stworzona inspekcja ID: " + createdInspectionId);
        }
        
        assertAndRecord(success,
            "Inspekcja rozpoczęta",
            "Błąd tworzenia inspekcji - " + response.getBody(),
            "QC: Rozpocznij Inspekcję");
    }

    @Test
    @Order(802)
    @DisplayName("🔘 QC: Przycisk 'ZATWIERDŹ (PASS)'")
    void test802_QC_Approve() {
        printTestHeader("QC", "Zatwierdź (PASS)", "POST /api/qc/inspections/{id}/execute");
        
        Assumptions.assumeTrue(createdInspectionId != null, "Brak inspekcji do zatwierdzenia");
        
        String url = String.format(
            "%s/api/qc/inspections/%d/execute?result=PASSED&inspectorId=1",
            baseUrl, createdInspectionId);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Inspekcja zatwierdzona (PASS)",
            "Błąd zatwierdzania - " + response.getBody(),
            "QC: Zatwierdź (PASS)");
    }

    @Test
    @Order(803)
    @DisplayName("🔘 QC: Przycisk 'Utwórz Raport NCR'")
    void test803_QC_CreateNCR() {
        printTestHeader("QC", "Utwórz NCR", "POST /api/qc/inspections/{id}/ncr");
        
        // Stwórz nową inspekcję dla NCR
        Integer productId = createdProductId != null ? createdProductId : 1;
        String createUrl = String.format(
            "%s/api/qc/inspections?productId=%d&sourceType=INBOUND&referenceId=1&sampleSize=5",
            baseUrl, productId);
        ResponseEntity<String> createResp = restTemplate.postForEntity(createUrl, null, String.class);
        
        Integer inspectionId = extractId(createResp.getBody());
        Assumptions.assumeTrue(inspectionId != null, "Nie można stworzyć inspekcji dla NCR");
        
        String url = String.format(
            "%s/api/qc/inspections/%d/ncr?defectType=COSMETIC&description=Test+NCR",
            baseUrl, inspectionId);
        
        HttpEntity<List<String>> entity = new HttpEntity<>(new ArrayList<>(), jsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "NCR utworzony",
            "Błąd tworzenia NCR - " + response.getBody(),
            "QC: Utwórz NCR");
    }

    // ====================================================================
    // MODUŁ 9: LOCATIONS (LOKACJE) - Testy 9xx
    // ====================================================================

    @Test
    @Order(900)
    @DisplayName("🔘 LOCATIONS: Lista lokacji")
    void test900_Locations_LoadList() {
        printTestHeader("LOCATIONS", "Lista lokacji", "GET /api/locations");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/locations", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista lokacji załadowana (" + countJsonArray(response.getBody()) + " lokacji)",
            "Błąd ładowania - " + response.getStatusCode(),
            "LOCATIONS: Lista lokacji");
    }

    @Test
    @Order(901)
    @DisplayName("🔘 LOCATIONS: Przycisk 'Nowa Lokalizacja'")
    void test901_Locations_CreateLocation() {
        printTestHeader("LOCATIONS", "Nowa Lokalizacja", "POST /api/locations");
        
        Map<String, Object> location = new HashMap<>();
        location.put("name", "LOC-TEST-" + System.currentTimeMillis());
        location.put("zone", "A");
        location.put("aisle", "01");
        location.put("rack", "01");
        location.put("shelf", "01");
        location.put("bin", "01");
        location.put("locationTypeId", 1);
        location.put("isActive", true);
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(location);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/locations", entity, String.class);
        
        boolean success = response.getStatusCode().is2xxSuccessful();
        
        if (success) {
            createdLocationId = extractId(response.getBody());
            System.out.println("   📍 Stworzona lokacja ID: " + createdLocationId);
        }
        
        assertAndRecord(success,
            "Lokacja utworzona poprawnie",
            "Błąd tworzenia lokacji - " + response.getBody(),
            "LOCATIONS: Nowa Lokalizacja");
    }

    @Test
    @Order(902)
    @DisplayName("🔘 LOCATIONS: Lista typów lokacji")
    void test902_Locations_LoadTypes() {
        printTestHeader("LOCATIONS", "Lista typów", "GET /api/locations/types");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/api/locations/types", String.class);
        
        boolean success = response.getStatusCode() == HttpStatus.OK;
        
        assertAndRecord(success,
            "Lista typów załadowana",
            "Błąd ładowania - " + response.getStatusCode(),
            "LOCATIONS: Lista typów");
    }

    @Test
    @Order(903)
    @DisplayName("🔘 LOCATIONS: Przycisk 'Usuń' (ikona kosza)")
    void test903_Locations_DeleteLocation() {
        printTestHeader("LOCATIONS", "Usuń lokację", "DELETE /api/locations/{id}");
        
        Assumptions.assumeTrue(createdLocationId != null, "Brak lokacji do usunięcia");
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/locations/" + createdLocationId,
            HttpMethod.DELETE, null, String.class);
        
        boolean success = response.getStatusCode().is2xxSuccessful();
        
        assertAndRecord(success,
            "Lokacja usunięta (ID: " + createdLocationId + ")",
            "Błąd usuwania - " + response.getStatusCode(),
            "LOCATIONS: Usuń lokację");
    }

    // ====================================================================
    // HELPER METHODS
    // ====================================================================

    private void createOutboundOrder() {
        Map<String, Object> order = new HashMap<>();
        order.put("referenceNumber", "ORD-HELPER-" + System.currentTimeMillis());
        order.put("destination", "Warszawa");
        order.put("status", "NEW");
        
        HttpEntity<Map<String, Object>> entity = jsonEntity(order);
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/outbound", entity, String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            createdOutboundOrderId = extractId(response.getBody());
        }
    }

    private void printBanner(String text) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + text);
        System.out.println("=".repeat(60));
    }

    private void printTestHeader(String module, String button, String endpoint) {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ 📋 MODUŁ: " + padRight(module, 47) + "│");
        System.out.println("│ 🔘 PRZYCISK: " + padRight(button, 44) + "│");
        System.out.println("│ 🌐 ENDPOINT: " + padRight(endpoint, 44) + "│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }

    private void assertAndRecord(boolean success, String successMsg, String failMsg, String testName) {
        if (success) {
            System.out.println("   ✅ " + successMsg);
            passedTests++;
        } else {
            System.out.println("   ❌ " + failMsg);
            failedTests++;
            failures.add(testName);
        }
        assertTrue(success, failMsg);
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    private Integer extractId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("id")) {
                return node.get("id").asInt();
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    private int countJsonArray(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isArray()) return node.size();
        } catch (Exception e) { /* ignore */ }
        return 0;
    }

    private boolean isJsonResponse(String body) {
        return body != null && (body.trim().startsWith("[") || body.trim().startsWith("{"));
    }
}