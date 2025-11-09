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
DELETE FROM InventoryTransactions;
DELETE FROM ShipmentLines;
DELETE FROM Shipments;
DELETE FROM PickingTasks;
DELETE FROM UOM_Conversions;
DELETE FROM OrderLines;
DELETE FROM ReceiptLines;
DELETE FROM Inventory;
DELETE FROM WmsOrders;
DELETE FROM GoodsReceived;
DELETE FROM Products;
DELETE FROM ProductCategories;
DELETE FROM UserRoles;
DELETE FROM Users;
DELETE FROM Roles;
DELETE FROM Locations;
DELETE FROM UnitOfMeasure;
DELETE FROM Carriers;

-- Resetujemy sekwencje AUTOINCREMENT
DELETE FROM sqlite_sequence WHERE name IN (
    'InventoryTransactions', 'ShipmentLines', 'Shipments', 'PickingTasks', 
    'OrderLines', 'ReceiptLines', 'Inventory', 'WmsOrders', 'GoodsReceived', 
    'Products', 'ProductCategories', 'Users', 'Locations'
);

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

-- Tabela `Locations` (Lokalizacje Magazynowe) - 15 rekordów (id_location będzie AUTOINCREMENT)
INSERT INTO Locations (location_code, location_type) VALUES
('REC-01', 'RECEIVING_BAY'),
('REC-02', 'RECEIVING_BAY'),
('SHIP-01', 'SHIPPING_BAY'),
('SHIP-02', 'SHIPPING_BAY'),
('QUAR-01', 'QUARANTINE'),
('BUF-01', 'BUFFER'),
('A-01-01', 'STORAGE'),
('A-01-02', 'STORAGE'),
('A-02-01', 'STORAGE'),
('A-02-02', 'STORAGE'),
('B-01-01', 'STORAGE'),
('B-01-02', 'STORAGE'),
('P-A-01', 'PICKING'), -- Strefa pickingowa dla regału A
('P-A-02', 'PICKING'),
('P-B-01', 'PICKING'); -- Strefa pickingowa dla regału B

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
INSERT INTO Users (login, password_hash, first_name, last_name, is_active) VALUES
('admin', '$2a$10$3...hash...', 'Adam', 'Min', 1),
('manager', '$2a$10$3...hash...', 'Marta', 'Nager', 1),
('jkowalski', '$2a$10$3...hash...', 'Jan', 'Kowalski', 1),
('anowak', '$2a$10$3...hash...', 'Anna', 'Nowak', 1),
('pzielinski', '$2a$10$3...hash...', 'Piotr', 'Zieliński', 1),
('kwisniewska', '$2a$10$3...hash...', 'Katarzyna', 'Wiśniewska', 1),
('mwojcik', '$2a$10$3...hash...', 'Marek', 'Wójcik', 1),
('tkaminski', '$2a$10$3...hash...', 'Tomasz', 'Kamiński', 1),
('olewandowska', '$2a$10$3...hash...', 'Olga', 'Lewandowska', 1),
('jnowicki', '$2a$10$3...hash...', 'Janusz', 'Nowicki', 0); -- Nieaktywny

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
INSERT INTO Products (sku, name, id_category, id_base_uom, weight_kg, length_cm, width_cm, height_cm) VALUES
('LAP-DEL-XPS15', 'Laptop Dell XPS 15', 5, 1, 2.5, 40, 30, 5),
('IPH-15-PRO', 'Apple iPhone 15 Pro 256GB', 6, 1, 0.4, 20, 12, 3),
('TSH-XYZ-L-BLK', 'T-Shirt XYZ Czarny L', 7, 1, 0.3, 30, 20, 2),
('JNS-WRG-32-32', 'Jeansy Wrangler 32/32', 8, 1, 0.8, 40, 30, 5),
('HAM-BCH-01', 'Młotek Bosch 500g', 9, 1, 0.6, 25, 10, 4),
('WAT-ZRO-05-PAL', 'Woda Źródlana 0.5L (Paleta)', 10, 7, 300, 120, 80, 150),
('WAT-ZRO-05-KAR', 'Woda Źródlana 0.5L (Karton)', 10, 6, 6.5, 30, 20, 15),
('WAT-ZRO-05-SZT', 'Woda Źródlana 0.5L', 10, 1, 0.55, 6, 6, 22),
('KBL-HDMI-2M', 'Kabel HDMI 2m', 6, 1, 0.1, 15, 15, 2),
('COF-LAV-1KG', 'Kawa Lavazza 1kg ziarno', 4, 1, 1.0, 20, 10, 8),
('PRL-ARI-5KG', 'Proszek do prania Ariel 5kg', 3, 2, 5.0, 30, 25, 10),
('PAP-A4-RYS', 'Papier A4 500 szt. (Rysa)', 3, 8, 2.5, 30, 21, 5),
('MYD-DOV-100G', 'Mydło Dove 100g', 3, 1, 0.1, 10, 6, 3),
('TV-SAM-55Q', 'Telewizor Samsung 55" QLED', 5, 1, 25.0, 140, 80, 15),
('MIK-SAM-1000W', 'Mikrofalówka Samsung 1000W', 11, 1, 12.0, 50, 40, 35);

-- Tabela `UOM_Conversions` (Przeliczniki JEDNOSTEK MIAR)
-- ID Produktów bazują na kolejności INSERT powyżej
INSERT INTO UOM_Conversions (id_product, id_uom_from, id_uom_to, conversion_factor) VALUES
(6, 7, 6, 48),    -- Woda PAL -> KAR (1 Paleta = 48 Kartonów)
(6, 7, 1, 288),   -- Woda PAL -> SZT (1 Paleta = 288 Sztuk)
(7, 6, 1, 6),     -- Woda KAR -> SZT (1 Karton = 6 Sztuk)
(12, 6, 8, 5),    -- Papier KAR -> OPAK (1 Karton = 5 Ryz)
(12, 7, 6, 40),   -- Papier PAL -> KAR (1 Paleta = 40 Kartonów)
(12, 7, 8, 200),  -- Papier PAL -> OPAK (1 Paleta = 200 Ryz)
(13, 6, 1, 24);   -- Mydło KAR -> SZT (1 Karton = 24 Sztuki)

-- Tabela `WmsOrders` (Zlecenia Wydania) - 10 rekordów
INSERT INTO WmsOrders (order_reference, status, id_user_created) VALUES
('ORD-2025-001', 'SHIPPED', 1),
('ORD-2025-002', 'PACKED', 2),
('ORD-2025-003', 'PICKING', 2),
('ORD-2025-004', 'ALLOCATED', 1),
('ORD-2025-005', 'NEW', 1),
('ORD-2025-006', 'NEW', 2),
('ORD-2025-007', 'CANCELLED', 1),
('ORD-2025-008', 'SHIPPED', 2),
('ORD-2025-009', 'PICKING', 2),
('ORD-2025-010', 'NEW', 1);

-- Tabela `GoodsReceived` (Zlecenia Przyjęcia) - 10 rekordów
INSERT INTO GoodsReceived (asn_reference, supplier, status, expected_at, id_user_created) VALUES
('ASN-2025-001', 'Dell Polska', 'RECEIVED', '2025-11-01', 3),
('ASN-2025-002', 'Apple Polska', 'RECEIVED', '2025-11-01', 5),
('ASN-2025-003', 'Hurtownia XYZ', 'RECEIVED', '2025-11-02', 3),
('ASN-2025-004', 'Dostawca Wody', 'RECEIVED', '2025-11-03', 5),
('ASN-2025-005', 'Procter&Gamble', 'RECEIVING', '2025-11-09', 3),
('ASN-2025-006', 'Hurtownia Kawa', 'EXPECTED', '2025-11-10', 3),
('ASN-2025-007', 'Samsung Polska', 'RECEIVED', '2025-11-04', 5),
('ASN-2025-008', 'Hurtownia Odzież', 'RECEIVED', '2025-11-05', 3),
('ASN-2025-009', 'Hurtownia Dom', 'EXPECTED', '2025-11-12', 3),
('ASN-2025-010', 'Dostawca Wody', 'RECEIVED', '2025-11-08', 5);

-- ########################################
-- SEKCJA 3: DANE PROCESOWE (POZIOM 2)
-- ########################################

-- Tabela `OrderLines` (Linie Zleceń Wydania)
INSERT INTO OrderLines (id_order, id_product, id_uom, quantity_ordered, quantity_picked, quantity_shipped) VALUES
(1, 1, 1, 1, 1, 1), -- ORD-001 (SHIPPED)
(1, 2, 1, 1, 1, 1), -- ORD-001 (SHIPPED)
(2, 3, 1, 5, 5, 0), -- ORD-002 (PACKED)
(3, 8, 1, 2, 1, 0), -- ORD-003 (PICKING)
(3, 9, 1, 3, 0, 0), -- ORD-003 (PICKING)
(4, 10, 1, 10, 0, 0), -- ORD-004 (ALLOCATED)
(4, 13, 1, 20, 0, 0), -- ORD-004 (ALLOCATED)
(5, 14, 1, 1, 0, 0), -- ORD-005 (NEW)
(6, 15, 1, 1, 0, 0), -- ORD-006 (NEW)
(8, 5, 1, 2, 2, 2), -- ORD-008 (SHIPPED)
(9, 12, 8, 10, 5, 0); -- ORD-009 (PICKING)

-- Tabela `ReceiptLines` (Linie Zleceń Przyjęcia)
INSERT INTO ReceiptLines (id_receipt, id_product, id_uom, quantity_expected, quantity_received, batch_number, expiry_date) VALUES
(1, 1, 1, 10, 10, 'B-DELL-001', NULL),
(2, 2, 1, 20, 20, 'B-APP-001', NULL),
(3, 3, 1, 50, 50, 'B-TSH-001', NULL),
(3, 4, 1, 30, 30, 'B-JNS-001', NULL),
(4, 6, 7, 2, 2, 'B-WAT-001', '2026-11-01'),
(5, 11, 2, 100, 50, 'B-ARI-001', '2027-01-01'), -- Dopiero przyjmowane (50 ze 100)
(6, 10, 1, 40, 0, NULL, NULL), -- Oczekiwane
(7, 14, 1, 5, 5, 'B-SAM-TV-001', NULL),
(7, 15, 1, 8, 8, 'B-SAM-MIK-001', NULL),
(8, 3, 1, 100, 100, 'B-TSH-002', NULL),
(10, 7, 6, 50, 50, 'B-WAT-002', '2026-12-01');

-- Tabela `Inventory` (Stany Magazynowe - SALDO)
INSERT INTO Inventory (id_product, id_location, id_uom, quantity, lpn, batch_number, expiry_date, status) VALUES
-- Produkty z przyjęć 1, 2, 7 (ID Lokacji: A-01-01=7, A-01-02=8, A-02-01=9, A-02-02=10)
(1, 7, 1, 10, 'LPN-D-001', 'B-DELL-001', NULL, 'Available'), 
(2, 8, 1, 20, 'LPN-A-001', 'B-APP-001', NULL, 'Available'), 
(14, 9, 1, 5, 'LPN-S-001', 'B-SAM-TV-001', NULL, 'Available'), 
(15, 10, 1, 8, 'LPN-S-002', 'B-SAM-MIK-001', NULL, 'Available'),
-- Produkty z przyjęcia 3 i 8 (Odzież) (ID Lokacji: B-01-01=11, B-01-02=12)
(3, 11, 1, 150, 'LPN-T-001', 'B-TSH-001', NULL, 'Available'), 
(4, 12, 1, 30, 'LPN-J-001', 'B-JNS-001', NULL, 'Available'), 
-- Produkty z przyjęcia 4 i 10 (Woda) (ID Lokacji: P-A-01=13)
(6, 7, 7, 2, 'PAL-001', 'B-WAT-001', '2026-11-01', 'Available'), 
(7, 13, 6, 50, NULL, 'B-WAT-002', '2026-12-01', 'Available'), 
-- Stan po częściowym przyjęciu 5 (Ariel) (ID Lokacji: REC-01=1)
(11, 1, 2, 50, 'LPN-ARI-001', 'B-ARI-001', '2027-01-01', 'Quarantined'),
-- Dodatkowy stock (z "innej" dostawy) (ID Lokacji: P-A-02=14, P-B-01=15)
(8, 13, 1, 100, NULL, 'B-WAT-XTRA', '2026-05-01', 'Available'), 
(9, 14, 1, 50, NULL, 'B-KBL-001', NULL, 'Available'), 
(10, 11, 1, 20, 'LPN-K-001', 'B-LAV-001', '2026-10-01', 'Available'),
(12, 15, 8, 200, 'PAL-PAP-001', 'B-PAP-001', NULL, 'Available'), 
(13, 15, 1, 500, NULL, 'B-MYD-001', '2028-01-01', 'Available');

-- ########################################
-- SEKCJA 4: LOGIKA REALIZACJI (POZIOM 3)
-- ########################################
-- ID Poniżej bazują na kolejności INSERTów do tabel Inventory i OrderLines

-- Tabela `PickingTasks` (Zadania Kompletacji / Alokacje)
INSERT INTO PickingTasks (id_order_line, id_inventory, quantity_to_pick, id_user_assigned, status) VALUES
-- Zadania do ORD-003 (Status: PICKING)
(4, 10, 2, 4, 'PICKED'),   -- OrderLine 4 (Woda 2szt) -> z Inventory 10 (100szt Wody)
(5, 11, 3, 4, 'IN_PROGRESS'), -- OrderLine 5 (Kabel 3szt) -> z Inventory 11 (50szt Kabla)
-- Zadania do ORD-004 (Status: ALLOCATED)
(6, 12, 10, NULL, 'PENDING'), -- OrderLine 6 (Kawa 10szt) -> z Inventory 12 (20szt Kawy)
(7, 14, 20, NULL, 'PENDING'), -- OrderLine 7 (Mydło 20szt) -> z Inventory 14 (500szt Mydła)
-- Zadania do ORD-009 (Status: PICKING)
(11, 13, 5, 6, 'PICKED');     -- OrderLine 11 (Papier 10ryz) -> z Inventory 13 (200ryz Papieru)

-- Tabela `Shipments` (Przesyłki / Paczki)
INSERT INTO Shipments (id_order, id_carrier, tracking_number, shipped_at, status) VALUES
(1, 1, 'DHL-TRACK-12345', '2025-11-05 14:30:00', 'SHIPPED'),
(2, 4, NULL, NULL, 'READY_TO_SHIP'), -- Czeka na kuriera InPost
(8, 2, 'DPD-98765-PL', '2025-11-06 16:00:00', 'SHIPPED');

-- Tabela `ShipmentLines` (Zawartość Przesyłek)
INSERT INTO ShipmentLines (id_shipment, id_product, quantity_shipped) VALUES
(1, 1, 1), -- Paczka 1: 1x Laptop
(1, 2, 1), -- Paczka 1: 1x iPhone
(2, 3, 5), -- Paczka 2: 5x T-Shirt
(3, 5, 2); -- Paczka 3: 2x Młotek

-- ########################################
-- SEKCJA 5: DZIENNIK TRANSAKCJI (AUDYT)
-- ########################################

-- Tabela `InventoryTransactions` (Dziennik Operacji)
INSERT INTO InventoryTransactions (id_product, id_user, transaction_type, reference_type, reference_id, to_location_id, lpn, batch_number, quantity_change) VALUES
(1, 3, 'RECEIPT', 'ReceiptLines', 1, 7, 'LPN-D-001', 'B-DELL-001', 10.0000),
(2, 5, 'RECEIPT', 'ReceiptLines', 2, 8, 'LPN-A-001', 'B-APP-001', 20.000),
(14, 5, 'RECEIPT', 'ReceiptLines', 8, 9, 'LPN-S-001', 'B-SAM-TV-001', 5.0000),
(15, 5, 'RECEIPT', 'ReceiptLines', 9, 10, 'LPN-S-002', 'B-SAM-MIK-001', 8.0000),
(3, 3, 'RECEIPT', 'ReceiptLines', 3, 11, 'LPN-T-001', 'B-TSH-001', 50.0000),
(4, 3, 'RECEIPT', 'ReceiptLines', 4, 12, 'LPN-J-001', 'B-JNS-001', 30.0000),
(3, 3, 'RECEIPT', 'ReceiptLines', 10, 11, 'LPN-T-001', 'B-TSH-002', 100.0000),
(6, 5, 'RECEIPT', 'ReceiptLines', 5, 7, 'PAL-001', 'B-WAT-001', 2.0000),
(7, 5, 'RECEIPT', 'ReceiptLines', 11, 13, NULL, 'B-WAT-002', 50.0000),
(11, 3, 'RECEIPT', 'ReceiptLines', 6, 1, 'LPN-ARI-001', 'B-ARI-001', 50.0000);

INSERT INTO InventoryTransactions (id_product, id_user, transaction_type, reference_type, reference_id, to_location_id, lpn, batch_number, quantity_change, new_status) VALUES
(11, 2, 'STATUS_CHANGE', 'Manual', NULL, 1, 'LPN-ARI-001', 'B-ARI-001', 0, 'Quarantined');

INSERT INTO InventoryTransactions (id_product, id_user, transaction_type, from_location_id, to_location_id, lpn, quantity_change) VALUES
(8, 4, 'MOVE', 7, 13, NULL, 0);

INSERT INTO InventoryTransactions (id_product, id_user, transaction_type, reference_type, reference_id, from_location_id, quantity_change) VALUES
(1, 1, 'SHIPMENT', 'ShipmentLines', 1, 7, -1.0000),
(2, 1, 'SHIPMENT', 'ShipmentLines', 2, 8, -1.0000),
(5, 1, 'SHIPMENT', 'ShipmentLines', 4, 9, -2.0000),
(8, 4, 'SHIPMENT', 'PickingTasks', 1, 13, -2.0000),
(12, 6, 'SHIPMENT', 'PickingTasks', 5, 15, -5.0000);

-- Zakończenie skryptu
PRAGMA foreign_keys = ON; -- Włączamy z powrotem sprawdzanie kluczy