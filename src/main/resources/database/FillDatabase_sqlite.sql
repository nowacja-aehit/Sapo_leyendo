/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  j0now
 * Created: 9 lis 2025
 */

-- ##################################################################
-- SKRYPT WYPEŁNIAJACY BAZĘ WMS v2 DANYMI PRZYKŁADOWYMI (WERSJA SQLite)
-- ##################################################################
PRAGMA foreign_keys = OFF; -- Wyłączamy sprawdzanie kluczy na czas ładowania danych

-- ########################################
-- SEKCJA 0: CZYSZCZENIE DANYCH
-- ########################################
-- Używamy DELETE zamiast TRUNCATE (TRUNCATE nie istnieje w SQLite)
-- DELETE FROM InventoryTransactions;
-- DELETE FROM ShipmentLines;
-- DELETE FROM Shipments;
-- DELETE FROM PickingTasks;
-- DELETE FROM UOM_Conversions;
-- DELETE FROM OrderLines;
-- DELETE FROM ReceiptLines;
-- DELETE FROM Inventory;
-- DELETE FROM WmsOrders;
-- DELETE FROM GoodsReceived;
DELETE FROM Products;
DELETE FROM ProductCategories;
DELETE FROM RolePermissions;
DELETE FROM Permissions;
DELETE FROM UserRoles;
DELETE FROM Users;
DELETE FROM Roles;
DELETE FROM Locations;
DELETE FROM Zones;
DELETE FROM LocationTypes;
DELETE FROM UnitOfMeasure;
DELETE FROM Carriers;

-- Resetujemy sekwencje AUTOINCREMENT
-- DELETE FROM sqlite_sequence WHERE name IN (
--    'Products', 'Users', 'Roles'
-- );

-- ########################################
-- SEKCJA 1: SŁOWNIKI (BEZ ZALEŻNOŚCI)
-- ########################################

-- Tabela `Roles` (Role) - Zostawiamy apostrofy
INSERT INTO Roles (id_role, role_name, description) VALUES
(1, 'ADMIN', 'Administrator Systemu - pełne uprawnienia'),
(2, 'MANAGER', 'Kierownik Magazynu - podgląd, raporty, zarządzanie procesami'),
(3, 'RECEIVER', 'Magazynier - obsługa strefy przyjęć'),
(4, 'PICKER', 'Magazynier - kompletacja zamówień'),
(5, 'PACKER', 'Magazynier - pakowanie i obsługa strefy wydań');

-- Tabela `Permissions` (Uprawnienia)
INSERT INTO Permissions (id_permission, name, module, description) VALUES
(1, 'INBOUND_READ', 'INBOUND', 'Podgląd przyjęć'),
(2, 'INBOUND_WRITE', 'INBOUND', 'Tworzenie i edycja przyjęć'),
(3, 'OUTBOUND_READ', 'OUTBOUND', 'Podgląd wydań'),
(4, 'OUTBOUND_WRITE', 'OUTBOUND', 'Tworzenie i edycja wydań'),
(5, 'INVENTORY_READ', 'INVENTORY', 'Podgląd stanów magazynowych'),
(6, 'INVENTORY_ADJUST', 'INVENTORY', 'Korekty stanów magazynowych'),
(7, 'REPORT_VIEW', 'REPORTING', 'Dostęp do raportów i dashboardu'),
(8, 'USER_MANAGEMENT', 'ADMIN', 'Zarządzanie użytkownikami');

-- Tabela `RolePermissions` (Uprawnienia Ról)
INSERT INTO RolePermissions (id_role, id_permission) VALUES
-- ADMIN: Wszystko
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
-- MANAGER: Wszystko do odczytu + raporty + korekty
(2, 1), (2, 3), (2, 5), (2, 6), (2, 7),
-- RECEIVER: Tylko Inbound
(3, 1), (3, 2), (3, 5),
-- PICKER: Tylko Outbound (odczyt) + Inventory (odczyt)
(4, 3), (4, 5),
-- PACKER: Tylko Outbound
(5, 3), (5, 4);

-- Tabela `Zones`
INSERT INTO Zones (id_zone, name, is_temperature_controlled, is_secure, allow_mixed_sku) VALUES
(1, 'General Storage', 0, 0, 1),
(2, 'Cold Storage', 1, 0, 0),
(3, 'Secure Cage', 0, 1, 0),
(4, 'Receiving', 0, 0, 1),
(5, 'Shipping', 0, 0, 1);

-- Tabela `LocationTypes`
INSERT INTO LocationTypes (id_location_type, name, max_weight, max_volume, length, width, height) VALUES
(1, 'Standard Pallet Rack', 1000.0, 2.0, 1.2, 1.0, 1.5),
(2, 'Small Bin', 20.0, 0.1, 0.4, 0.3, 0.3),
(3, 'Floor Location', 5000.0, 10.0, 2.0, 2.0, 2.5),
(4, 'Dock Door', NULL, NULL, NULL, NULL, NULL);

-- Tabela `UnitOfMeasure` (Jednostki Miary)
INSERT INTO UnitOfMeasure (id_uom, code, name) VALUES
(1, 'SZT', 'Sztuka'),
(2, 'KG', 'Kilogram'),
(3, 'M', 'Metr'),
(4, 'M2', 'Metr kwadratowy'),
(5, 'M3', 'Metr sześcienny'),
(6, 'KAR', 'Karton'),
(7, 'PAL', 'Paleta'),
(8, 'OPAK', 'Opakowanie');

-- Tabela `Carriers` (Przewoźnicy)
INSERT INTO Carriers (id_carrier, name, tracking_url_template) VALUES
(1, 'DHL Express', 'https://www.dhl.com/pl-pl/home/tracking.html?tracking-number='),
(2, 'DPD Polska', 'https://tracktrace.dpd.com.pl/parcelDetails?p1='),
(3, 'UPS Polska', 'https://www.ups.com/track?loc=pl_PL&tracknum='),
(4, 'InPost Paczkomaty', 'https://inpost.pl/sledzenie-przesylek?number='),
(5, 'FedEx', 'https://www.fedex.com/pl-pl/tracking.html?trackingNumbers=');

-- Tabela `Locations` (Lokalizacje Magazynowe)
INSERT INTO Locations (name, id_location_type, id_zone) VALUES
('REC-01', 4, 4),
('REC-02', 4, 4),
('SHIP-01', 4, 5),
('SHIP-02', 4, 5),
('QUAR-01', 3, 3),
('BUF-01', 3, 1),
('A-01-01', 1, 1),
('A-01-02', 1, 1),
('A-02-01', 1, 1),
('A-02-02', 1, 1),
('B-01-01', 1, 1),
('B-01-02', 1, 1),
('P-A-01', 2, 1),
('P-A-02', 2, 1),
('P-B-01', 2, 1);

-- Tabela `ProductCategories` (Kategorie Produktów) - 11 rekordów
INSERT INTO ProductCategories (id_category, name, parent_category_id) VALUES
(1, 'Elektronika', NULL),
(2, 'Odzież', NULL),
(3, 'Dom i Ogród', NULL),
(4, 'Spożywcze', NULL),
(5, 'Laptopy i Komputery', 1),
(6, 'Telefony i Akcesoria', 1),
(7, 'Koszulki', 2),
(8, 'Spodnie', 2),
(9, 'Narzędzia', 3),
(10, 'Napoje', 4),
(11, 'AGD', 1);

-- Tabela `Users` (Użytkownicy) - 10 rekordów (id_user będzie AUTOINCREMENT)
INSERT INTO Users (login, email, password_hash, first_name, last_name, is_active) VALUES
('admin', 'admin@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Adam', 'Min', 1), -- password: password
('manager', 'manager@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Marta', 'Nager', 1),
('jkowalski', 'jkowalski@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Jan', 'Kowalski', 1),
('anowak', 'anowak@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Anna', 'Nowak', 1),
('pzielinski', 'pzielinski@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Piotr', 'Zieliński', 1),
('kwisniewska', 'kwisniewska@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Katarzyna', 'Wiśniewska', 1),
('mwojcik', 'mwojcik@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Marek', 'Wójcik', 1),
('tkaminski', 'tkaminski@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Tomasz', 'Kamiński', 1),
('olewandowska', 'olewandowska@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Olga', 'Lewandowska', 1),
('jnowicki', 'jnowicki@example.com', '$2a$10$Crwcf/gQEjIrUgNieWKfZOKMyC1kLlSwYaV4zep0rJy2A3SNDhpim', 'Janusz', 'Nowicki', 0); -- Nieaktywny

-- ########################################
-- SEKCJA 2: TABELE Z ZALEŻNOŚCIAMI (POZIOM 1)
-- ########################################
-- UWAGA: Poniższe ID bazują na kolejności INSERTów powyżej (np. user 'admin' ma ID 1, 'manager' ma ID 2)
-- Jest to kruche, ale zgodne z oryginalnym skryptem FillDatabase.

-- Tabela `UserRoles` (Przypisania ról)
INSERT INTO UserRoles (id_user, id_role) VALUES
(1, 1), -- Admin
(2, 2), -- Manager
(3, 3), (3, 4), -- Kowalski (Receiver i Picker)
(4, 4), (4, 5), -- Nowak (Picker i Packer)
(5, 3), -- Zieliński (Receiver)
(6, 4), -- Wiśniewska (Picker)
(7, 5), -- Wójcik (Packer)
(8, 3), (8, 4), (8, 5), -- Kamiński (wszystkie role magazynowe)
(9, 2), -- Lewandowska (drugi Manager)
(10, 4); -- Nowicki (nieaktywny Picker)

-- Tabela `Products` (Produkty) - 15 rekordów (id_product będzie AUTOINCREMENT)
INSERT INTO Products (sku, name, id_category, id_base_uom, weight_kg, length_cm, width_cm, height_cm, unit_price, min_stock_level) VALUES
('LAP-DEL-XPS15', 'Laptop Dell XPS 15', 5, 1, 2.5, 40, 30, 5, 7200.00, 5),
('IPH-15-PRO', 'Apple iPhone 15 Pro 256GB', 6, 1, 0.4, 20, 12, 3, 5400.00, 10),
('TSH-XYZ-L-BLK', 'T-Shirt XYZ Czarny L', 7, 1, 0.3, 30, 20, 2, 49.90, 50),
('JNS-WRG-32-32', 'Jeansy Wrangler 32/32', 8, 1, 0.8, 40, 30, 5, 199.00, 20),
('HAM-BCH-01', 'Mlotek Bosch 500g', 9, 1, 0.6, 25, 10, 4, 149.00, 15),
('WAT-ZRO-05-PAL', 'Woda Zrodlana 0.5L (Paleta)', 10, 7, 300, 120, 80, 150, 1200.00, 5),
('WAT-ZRO-05-KAR', 'Woda Zrodlana 0.5L (Karton)', 10, 6, 6.5, 30, 20, 15, 45.00, 30),
('WAT-ZRO-05-SZT', 'Woda Zrodlana 0.5L', 10, 1, 0.55, 6, 6, 22, 2.50, 200),
('KBL-HDMI-2M', 'Kabel HDMI 2m', 6, 1, 0.1, 15, 15, 2, 29.90, 40),
('COF-LAV-1KG', 'Kawa Lavazza 1kg ziarno', 4, 1, 1.0, 20, 10, 8, 89.00, 25),
('PRL-ARI-5KG', 'Proszek do prania Ariel 5kg', 3, 2, 5.0, 30, 25, 10, 79.00, 20),
('PAP-A4-RYS', 'Papier A4 500 szt. (Rysa)', 3, 8, 2.5, 30, 21, 5, 19.00, 60),
('MYD-DOV-100G', 'Mydlo Dove 100g', 3, 1, 0.1, 10, 6, 3, 8.99, 100),
('TV-SAM-55Q', 'Telewizor Samsung 55" QLED', 5, 1, 25.0, 140, 80, 15, 3999.00, 3),
('MIK-SAM-1000W', 'Mikrofalowka Samsung 1000W', 11, 1, 12.0, 50, 40, 35, 799.00, 5);

-- Tabela `OutboundOrders`
INSERT INTO OutboundOrders (reference_number, status, ship_date, destination, id_user_created, customer_name, priority, total_amount, items_count, order_date) VALUES
('ORD-2025-001', 'NEW', '2025-12-20 00:00:00.000', 'Warsaw, Poland', 1, 'Tech Solutions Sp. z o.o.', 'High', 14459.80, 4, '2025-12-18 00:00:00.000'),
('ORD-2025-002', 'PICKING', '2025-12-21 00:00:00.000', 'Berlin, Germany', 1, 'Berlin Retail GmbH', 'Medium', 27000.00, 5, '2025-12-19 00:00:00.000'),
('ORD-2025-003', 'SHIPPED', '2025-12-10 00:00:00.000', 'Paris, France', 1, 'Paris Boutique SARL', 'Low', 3999.00, 1, '2025-12-05 00:00:00.000');

-- Tabela `OutboundOrderItems`
INSERT INTO OutboundOrderItems (id_outbound_order, id_product, id_uom, quantity_ordered, quantity_picked, quantity_shipped, unit_price, line_total, sku, product_name, location_code, status) VALUES
(1, 1, 1, 2, 0, 0, 7200.00, 14400.00, 'LAP-DEL-XPS15', 'Laptop Dell XPS 15', 'A-01-01', 'Zaplanowany'),
(1, 9, 1, 2, 0, 0, 29.90, 59.80, 'KBL-HDMI-2M', 'Kabel HDMI 2m', 'A-01-02', 'Zaplanowany'),
(2, 2, 1, 5, 2, 0, 5400.00, 27000.00, 'IPH-15-PRO', 'Apple iPhone 15 Pro 256GB', 'A-01-01', 'Picking'),
(3, 14, 1, 1, 1, 1, 3999.00, 3999.00, 'TV-SAM-55Q', 'Telewizor Samsung 55" QLED', 'A-02-01', 'Wyslany');

-- Tabela `InboundOrders`
INSERT INTO InboundOrders (reference_number, status, expected_date, supplier, dock_id) VALUES
('INB-2025-001', 'PLANNED', '2025-12-25 00:00:00.000', 'Acme Corp', 1),
('INB-2025-002', 'RECEIVED', '2025-12-20 00:00:00.000', 'Global Supplies', 2);

-- Tabela `InboundOrderItems`
INSERT INTO InboundOrderItems (id_inbound_order, id_product, quantity_expected, quantity_received) VALUES
(1, 1, 10, 0),
(1, 2, 5, 0),
(2, 3, 20, 20);

-- Tabela `Inventory`
INSERT INTO Inventory (id_product, id_location, id_uom, quantity, status) VALUES
(1, 7, 1, 10, 'AVAILABLE'), -- Laptops in A-01-01
(2, 7, 1, 20, 'AVAILABLE'), -- iPhones in A-01-01
(9, 8, 1, 50, 'AVAILABLE'), -- HDMI Cables in A-01-02
(14, 9, 1, 5, 'AVAILABLE'); -- TVs in A-02-01

-- Tabela `Shipments`
INSERT INTO Shipments (id_outbound_order, id_carrier, tracking_number, shipped_at, status) VALUES
(3, 1, 'DHL-123456789', '2025-12-10 10:00:00.000', 'SHIPPED');

-- Zakończenie skryptu
PRAGMA foreign_keys = ON; -- Włączamy z powrotem sprawdzanie kluczy
