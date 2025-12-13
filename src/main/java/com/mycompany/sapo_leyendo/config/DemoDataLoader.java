package com.mycompany.sapo_leyendo.config;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Ładuje przykładowe dane demonstracyjne dla profilu MySQL.
 * Dane są ładowane tylko jeśli baza jest pusta.
 */
@Component
@Profile("mysql")
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final CarrierRepository carrierRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final OutboundOrderRepository outboundOrderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("📦 Dane demo już istnieją, pomijam inicjalizację");
            return;
        }

        log.info("🚀 Ładowanie danych demonstracyjnych...");

        // 1. Permissions
        Permission pInboundRead = createPermission(1, "INBOUND_READ", "INBOUND", "Podgląd przyjęć");
        Permission pInboundWrite = createPermission(2, "INBOUND_WRITE", "INBOUND", "Tworzenie i edycja przyjęć");
        Permission pOutboundRead = createPermission(3, "OUTBOUND_READ", "OUTBOUND", "Podgląd wydań");
        Permission pOutboundWrite = createPermission(4, "OUTBOUND_WRITE", "OUTBOUND", "Tworzenie i edycja wydań");
        Permission pInventoryRead = createPermission(5, "INVENTORY_READ", "INVENTORY", "Podgląd stanów magazynowych");
        Permission pInventoryAdjust = createPermission(6, "INVENTORY_ADJUST", "INVENTORY", "Korekty stanów magazynowych");
        Permission pReportView = createPermission(7, "REPORT_VIEW", "REPORTING", "Dostęp do raportów i dashboardu");
        Permission pUserMgmt = createPermission(8, "USER_MANAGEMENT", "ADMIN", "Zarządzanie użytkownikami");

        // 2. Roles with permissions
        Role roleAdmin = createRole("ADMIN", "Administrator Systemu - pełne uprawnienia",
                Set.of(pInboundRead, pInboundWrite, pOutboundRead, pOutboundWrite, pInventoryRead, pInventoryAdjust, pReportView, pUserMgmt));
        Role roleManager = createRole("MANAGER", "Kierownik Magazynu - podgląd, raporty, zarządzanie procesami",
                Set.of(pInboundRead, pOutboundRead, pInventoryRead, pInventoryAdjust, pReportView));
        Role roleReceiver = createRole("RECEIVER", "Magazynier - obsługa strefy przyjęć",
                Set.of(pInboundRead, pInboundWrite, pInventoryRead));
        Role rolePicker = createRole("PICKER", "Magazynier - kompletacja zamówień",
                Set.of(pOutboundRead, pInventoryRead));
        Role rolePacker = createRole("PACKER", "Magazynier - pakowanie i obsługa strefy wydań",
                Set.of(pOutboundRead, pOutboundWrite));

        // 3. Users (password: password - BCrypt hash)
        String passwordHash = "$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim";
        createUser("admin", "admin@example.com", passwordHash, "Adam", "Min", true, Set.of(roleAdmin));
        createUser("manager", "manager@example.com", passwordHash, "Marta", "Nager", true, Set.of(roleManager));
        createUser("jkowalski", "jkowalski@example.com", passwordHash, "Jan", "Kowalski", true, Set.of(roleReceiver, rolePicker));
        createUser("anowak", "anowak@example.com", passwordHash, "Anna", "Nowak", true, Set.of(rolePicker, rolePacker));
        createUser("pzielinski", "pzielinski@example.com", passwordHash, "Piotr", "Zieliński", true, Set.of(roleReceiver));
        createUser("kwisniewska", "kwisniewska@example.com", passwordHash, "Katarzyna", "Wiśniewska", true, Set.of(rolePicker));
        createUser("mwojcik", "mwojcik@example.com", passwordHash, "Marek", "Wójcik", true, Set.of(rolePacker));
        createUser("tkaminski", "tkaminski@example.com", passwordHash, "Tomasz", "Kamiński", true, Set.of(roleReceiver, rolePicker, rolePacker));
        createUser("olewandowska", "olewandowska@example.com", passwordHash, "Olga", "Lewandowska", true, Set.of(roleManager));
        createUser("jnowicki", "jnowicki@example.com", passwordHash, "Janusz", "Nowicki", false, Set.of(rolePicker));

        // 4. Zones
        Zone zoneGeneral = createZone("General Storage", false, false, true);
        Zone zoneCold = createZone("Cold Storage", true, false, false);
        Zone zoneSecure = createZone("Secure Cage", false, true, false);
        Zone zoneReceiving = createZone("Receiving", false, false, true);
        Zone zoneShipping = createZone("Shipping", false, false, true);

        // 5. Location Types
        LocationType ltPallet = createLocationType("Standard Pallet Rack", 1000.0, 2.0, 1.2, 1.0, 1.5);
        LocationType ltBin = createLocationType("Small Bin", 20.0, 0.1, 0.4, 0.3, 0.3);
        LocationType ltFloor = createLocationType("Floor Location", 5000.0, 10.0, 2.0, 2.0, 2.5);
        LocationType ltDock = createLocationType("Dock Door", null, null, null, null, null);

        // 6. Locations
        createLocation("REC-01", ltDock, zoneReceiving);
        createLocation("REC-02", ltDock, zoneReceiving);
        createLocation("SHIP-01", ltDock, zoneShipping);
        createLocation("SHIP-02", ltDock, zoneShipping);
        createLocation("QUAR-01", ltFloor, zoneSecure);
        createLocation("BUF-01", ltFloor, zoneGeneral);
        Location locA0101 = createLocation("A-01-01", ltPallet, zoneGeneral);
        Location locA0102 = createLocation("A-01-02", ltPallet, zoneGeneral);
        Location locA0201 = createLocation("A-02-01", ltPallet, zoneGeneral);
        createLocation("A-02-02", ltPallet, zoneGeneral);
        createLocation("B-01-01", ltPallet, zoneGeneral);
        createLocation("B-01-02", ltPallet, zoneGeneral);
        createLocation("P-A-01", ltBin, zoneGeneral);
        createLocation("P-A-02", ltBin, zoneGeneral);
        createLocation("P-B-01", ltBin, zoneGeneral);

        // 7. Units of Measure
        UnitOfMeasure uomSzt = createUom(1, "SZT", "Sztuka");
        createUom(2, "KG", "Kilogram");
        createUom(3, "M", "Metr");
        createUom(4, "M2", "Metr kwadratowy");
        createUom(5, "M3", "Metr sześcienny");
        createUom(6, "KAR", "Karton");
        UnitOfMeasure uomPal = createUom(7, "PAL", "Paleta");
        createUom(8, "OPAK", "Opakowanie");

        // 8. Carriers
        createCarrier("DHL Express", "https://www.dhl.com/pl-pl/home/tracking.html?tracking-number=");
        createCarrier("DPD Polska", "https://tracktrace.dpd.com.pl/parcelDetails?p1=");
        createCarrier("UPS Polska", "https://www.ups.com/track?loc=pl_PL&tracknum=");
        createCarrier("InPost Paczkomaty", "https://inpost.pl/sledzenie-przesylek?number=");
        createCarrier("FedEx", "https://www.fedex.com/pl-pl/tracking.html?trackingNumbers=");

        // 9. Products
        Product laptop = createProduct("LAP-DEL-XPS15", "Laptop Dell XPS 15", 5, 1, 2.5, 40.0, 30.0, 5.0);
        Product iphone = createProduct("IPH-15-PRO", "Apple iPhone 15 Pro 256GB", 6, 1, 0.4, 20.0, 12.0, 3.0);
        Product tshirt = createProduct("TSH-XYZ-L-BLK", "T-Shirt XYZ Czarny L", 7, 1, 0.3, 30.0, 20.0, 2.0);
        createProduct("JNS-WRG-32-32", "Jeansy Wrangler 32/32", 8, 1, 0.8, 40.0, 30.0, 5.0);
        createProduct("HAM-BCH-01", "Młotek Bosch 500g", 9, 1, 0.6, 25.0, 10.0, 4.0);
        createProduct("WAT-ZRO-05-PAL", "Woda Źródlana 0.5L (Paleta)", 10, 7, 300.0, 120.0, 80.0, 150.0);
        createProduct("WAT-ZRO-05-KAR", "Woda Źródlana 0.5L (Karton)", 10, 6, 6.5, 30.0, 20.0, 15.0);
        createProduct("WAT-ZRO-05-SZT", "Woda Źródlana 0.5L", 10, 1, 0.55, 6.0, 6.0, 22.0);
        Product hdmiCable = createProduct("KBL-HDMI-2M", "Kabel HDMI 2m", 6, 1, 0.1, 15.0, 15.0, 2.0);
        createProduct("COF-LAV-1KG", "Kawa Lavazza 1kg ziarno", 4, 1, 1.0, 20.0, 10.0, 8.0);
        createProduct("PRL-ARI-5KG", "Proszek do prania Ariel 5kg", 3, 2, 5.0, 30.0, 25.0, 10.0);
        createProduct("PAP-A4-RYS", "Papier A4 500 szt. (Rysa)", 3, 8, 2.5, 30.0, 21.0, 5.0);
        createProduct("MYD-DOV-100G", "Mydło Dove 100g", 3, 1, 0.1, 10.0, 6.0, 3.0);
        Product tv = createProduct("TV-SAM-55Q", "Telewizor Samsung 55\" QLED", 5, 1, 25.0, 140.0, 80.0, 15.0);
        createProduct("MIK-SAM-1000W", "Mikrofalówka Samsung 1000W", 11, 1, 12.0, 50.0, 40.0, 35.0);

        // 10. Inventory
        createInventory(laptop, locA0101, uomSzt, 10);
        createInventory(iphone, locA0101, uomSzt, 20);
        createInventory(hdmiCable, locA0102, uomSzt, 50);
        createInventory(tv, locA0201, uomSzt, 5);

        // 11. Inbound Orders
        createInboundOrder("INB-2025-001", "PLANNED", "Acme Corp", LocalDate.of(2025, 12, 25));
        createInboundOrder("INB-2025-002", "RECEIVED", "Global Supplies", LocalDate.of(2025, 12, 20));
        createInboundOrder("INB-2025-003", "IN_TRANSIT", "Tech Parts Ltd", LocalDate.of(2025, 12, 18));

        // 12. Outbound Orders
        createOutboundOrder("ORD-2025-001", "NEW", "Warsaw, Poland", LocalDate.of(2025, 12, 20));
        createOutboundOrder("ORD-2025-002", "PICKING", "Berlin, Germany", LocalDate.of(2025, 12, 21));
        createOutboundOrder("ORD-2025-003", "SHIPPED", "Paris, France", LocalDate.of(2025, 12, 10));

        log.info("✅ Dane demonstracyjne załadowane pomyślnie!");
        log.info("   📊 {} ról, {} użytkowników, {} produktów, {} lokalizacji",
                roleRepository.count(), userRepository.count(), productRepository.count(), locationRepository.count());
    }

    // Helper methods
    private Permission createPermission(int id, String name, String module, String description) {
        Permission p = new Permission();
        p.setId(id);
        p.setName(name);
        p.setModule(module);
        p.setDescription(description);
        return permissionRepository.save(p);
    }

    private Role createRole(String name, String description, Set<Permission> permissions) {
        Role r = new Role();
        r.setRoleName(name);
        r.setDescription(description);
        r.setPermissions(permissions);
        return roleRepository.save(r);
    }

    private User createUser(String login, String email, String passwordHash, String firstName, String lastName, boolean active, Set<Role> roles) {
        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setActive(active);
        u.setRoles(roles);
        return userRepository.save(u);
    }

    private Zone createZone(String name, boolean tempControlled, boolean secure, boolean allowMixed) {
        Zone z = new Zone();
        z.setName(name);
        z.setTemperatureControlled(tempControlled);
        z.setSecure(secure);
        z.setAllowMixedSku(allowMixed);
        return zoneRepository.save(z);
    }

    private LocationType createLocationType(String name, Double maxWeight, Double maxVolume, Double length, Double width, Double height) {
        LocationType lt = new LocationType();
        lt.setName(name);
        lt.setMaxWeight(maxWeight);
        lt.setMaxVolume(maxVolume);
        lt.setLength(length);
        lt.setWidth(width);
        lt.setHeight(height);
        return locationTypeRepository.save(lt);
    }

    private Location createLocation(String name, LocationType type, Zone zone) {
        Location loc = new Location();
        loc.setName(name);
        loc.setLocationType(type);
        loc.setZone(zone);
        loc.setStatus(LocationStatus.ACTIVE);
        loc.setActive(true);
        return locationRepository.save(loc);
    }

    private UnitOfMeasure createUom(int id, String code, String name) {
        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setId(id);
        uom.setCode(code);
        uom.setName(name);
        return unitOfMeasureRepository.save(uom);
    }

    private Carrier createCarrier(String name, String trackingUrl) {
        Carrier c = new Carrier();
        c.setName(name);
        c.setTrackingUrlTemplate(trackingUrl);
        return carrierRepository.save(c);
    }

    private Product createProduct(String sku, String name, Integer categoryId, Integer uomId, Double weight, Double length, Double width, Double height) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setIdBaseUom(uomId);
        p.setWeightKg(weight);
        p.setLengthCm(length);
        p.setWidthCm(width);
        p.setHeightCm(height);
        return productRepository.save(p);
    }

    private Inventory createInventory(Product product, Location location, UnitOfMeasure uom, int quantity) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setLocation(location);
        inv.setUom(uom);
        inv.setQuantity(quantity);
        inv.setStatus(InventoryStatus.AVAILABLE);
        return inventoryRepository.save(inv);
    }

    private InboundOrder createInboundOrder(String refNumber, String status, String supplier, LocalDate expectedDate) {
        InboundOrder order = new InboundOrder();
        order.setReferenceNumber(refNumber);
        order.setStatus(status);
        order.setSupplier(supplier);
        order.setExpectedDate(expectedDate);
        return inboundOrderRepository.save(order);
    }

    private OutboundOrder createOutboundOrder(String refNumber, String status, String destination, LocalDate shipDate) {
        OutboundOrder order = new OutboundOrder();
        order.setReferenceNumber(refNumber);
        order.setStatus(status);
        order.setDestination(destination);
        order.setShipDate(shipDate);
        return outboundOrderRepository.save(order);
    }
}
