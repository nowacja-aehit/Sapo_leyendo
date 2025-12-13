/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  j0now
 * Created: 9 lis 2025
 */

-- Ustawienia początkowe dla SQLite
PRAGMA foreign_keys = OFF; -- Wyłącz sprawdzanie kluczy obcych na czas tworzenia tabel

-- ########################################
-- SEKCJA 1: UŻYTKOWNICY I UPRAWNIENIA (RBAC)
-- ########################################

DROP TABLE IF EXISTS UserRoles;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Roles;

-- Tabela `Users` (Użytkownicy)
CREATE TABLE Users (
    id_user INTEGER PRIMARY KEY AUTOINCREMENT,
    login VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT 1
);

-- Tabela `Roles` (Role)
CREATE TABLE Roles (
    id_role INTEGER PRIMARY KEY, -- ID jest podawane w skrypcie FillDatabase, więc bez AUTOINCREMENT
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Tabela `UserRoles` (Tabela łącząca)
CREATE TABLE UserRoles (
    id_user INTEGER NOT NULL,
    id_role INTEGER NOT NULL,
    PRIMARY KEY (id_user, id_role),
    FOREIGN KEY (id_user) REFERENCES Users(id_user) ON DELETE CASCADE,
    FOREIGN KEY (id_role) REFERENCES Roles(id_role) ON DELETE CASCADE
);

DROP TABLE IF EXISTS RolePermissions;
DROP TABLE IF EXISTS Permissions;

-- Tabela `Permissions` (Uprawnienia/Transakcje)
CREATE TABLE Permissions (
    id_permission INTEGER PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL, -- np. 'INBOUND_READ', 'INBOUND_WRITE'
    module VARCHAR(50) NOT NULL,       -- np. 'INBOUND', 'OUTBOUND'
    description TEXT
);

-- Tabela `RolePermissions` (Uprawnienia Ról)
CREATE TABLE RolePermissions (
    id_role INTEGER NOT NULL,
    id_permission INTEGER NOT NULL,
    PRIMARY KEY (id_role, id_permission),
    FOREIGN KEY (id_role) REFERENCES Roles(id_role) ON DELETE CASCADE,
    FOREIGN KEY (id_permission) REFERENCES Permissions(id_permission) ON DELETE CASCADE
);


-- ########################################
-- SEKCJA 2: RDZEŃ - PRODUKTY I LOKALIZACJE
-- ########################################

DROP TABLE IF EXISTS UOM_Conversions;
DROP TABLE IF EXISTS Inventory;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Locations;
DROP TABLE IF EXISTS UnitOfMeasure;
DROP TABLE IF EXISTS ProductCategories;

-- Tabela `UnitOfMeasure` (Jednostki Miary)
CREATE TABLE UnitOfMeasure (
    id_uom INTEGER PRIMARY KEY, -- ID jest podawane w skrypcie FillDatabase
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL
);

-- Tabela `ProductCategories` (Kategorie Produktów)
CREATE TABLE ProductCategories (
    id_category INTEGER PRIMARY KEY, -- ID jest podawane w skrypcie FillDatabase
    name VARCHAR(100) NOT NULL,
    parent_category_id INTEGER NULL,
    FOREIGN KEY (parent_category_id) REFERENCES ProductCategories(id_category) ON DELETE SET NULL
);

-- Tabela `Products` (Produkty)
CREATE TABLE Products (
    id_product INTEGER PRIMARY KEY AUTOINCREMENT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    id_category INTEGER NULL,
    id_base_uom INTEGER NOT NULL,
    weight_kg REAL NULL,
    length_cm REAL NULL,
    width_cm REAL NULL,
    height_cm REAL NULL,
    FOREIGN KEY (id_category) REFERENCES ProductCategories(id_category) ON DELETE SET NULL,
    FOREIGN KEY (id_base_uom) REFERENCES UnitOfMeasure(id_uom) ON DELETE RESTRICT
);

-- Tabela `UOM_Conversions`
CREATE TABLE UOM_Conversions (
    id_product INTEGER NOT NULL,
    id_uom_from INTEGER NOT NULL,
    id_uom_to INTEGER NOT NULL,
    conversion_factor REAL NOT NULL,
    PRIMARY KEY (id_product, id_uom_from, id_uom_to),
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE CASCADE,
    FOREIGN KEY (id_uom_from) REFERENCES UnitOfMeasure(id_uom) ON DELETE CASCADE,
    FOREIGN KEY (id_uom_to) REFERENCES UnitOfMeasure(id_uom) ON DELETE CASCADE
);

-- Tabela `Zones` (Strefy Magazynowe)
CREATE TABLE Zones (
    id_zone INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    is_temperature_controlled BOOLEAN DEFAULT 0,
    is_secure BOOLEAN DEFAULT 0,
    allow_mixed_sku BOOLEAN DEFAULT 1
);

-- Tabela `LocationTypes` (Typy Lokalizacji)
CREATE TABLE LocationTypes (
    id_location_type INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    max_weight REAL NULL,
    max_volume REAL NULL,
    length REAL NULL,
    width REAL NULL,
    height REAL NULL
);

-- Tabela `Locations` (Lokalizacje Magazynowe)
CREATE TABLE Locations (
    id_location INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    id_zone INTEGER NULL,
    id_location_type INTEGER NULL,
    barcode VARCHAR(100) NULL,
    aisle VARCHAR(10) NULL,
    rack VARCHAR(10) NULL,
    level VARCHAR(10) NULL,
    bin VARCHAR(10) NULL,
    pick_sequence INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (id_zone) REFERENCES Zones(id_zone) ON DELETE SET NULL,
    FOREIGN KEY (id_location_type) REFERENCES LocationTypes(id_location_type) ON DELETE SET NULL
);



-- ########################################
-- SEKCJA 3: ZARZĄDZANIE STANEM (SALDO)
-- ########################################

-- Tabela `Inventory` (Stany Magazynowe)
CREATE TABLE Inventory (
    id_inventory INTEGER PRIMARY KEY AUTOINCREMENT,
    id_product INTEGER NOT NULL,
    id_location INTEGER NOT NULL,
    id_uom INTEGER NOT NULL,
    quantity REAL NOT NULL,
    lpn VARCHAR(50) NULL,
    batch_number VARCHAR(100) NULL,
    expiry_date TEXT NULL, -- Używamy TEXT dla dat
    status TEXT NOT NULL DEFAULT 'Available' CHECK(status IN ('Available', 'Quarantined', 'Damaged', 'Reserved')),
    received_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT,
    FOREIGN KEY (id_location) REFERENCES Locations(id_location) ON DELETE RESTRICT,
    FOREIGN KEY (id_uom) REFERENCES UnitOfMeasure(id_uom) ON DELETE RESTRICT,
    
    UNIQUE (id_product, id_location, lpn, batch_number, status)
);


-- ########################################
-- SEKCJA 4: PROCESY - PRZYJĘCIA I WYDANIA
-- ########################################

DROP TABLE IF EXISTS OrderLines;
DROP TABLE IF EXISTS WmsOrders;
DROP TABLE IF EXISTS ReceiptLines;
DROP TABLE IF EXISTS GoodsReceived;

-- Tabela `WmsOrders` (Zlecenia Wydania - Nagłówek)
CREATE TABLE WmsOrders (
    id_order INTEGER PRIMARY KEY AUTOINCREMENT,
    order_reference VARCHAR(100) UNIQUE NOT NULL,
    status TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW', 'ALLOCATED', 'PICKING', 'PACKED', 'SHIPPED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    id_user_created INTEGER NULL,
    FOREIGN KEY (id_user_created) REFERENCES Users(id_user) ON DELETE SET NULL
);

-- Tabela `OrderLines` (Linie Zleceń Wydania)
CREATE TABLE OrderLines (
    id_line INTEGER PRIMARY KEY AUTOINCREMENT,
    id_order INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    id_uom INTEGER NOT NULL,
    quantity_ordered REAL NOT NULL,
    quantity_picked REAL NOT NULL DEFAULT 0,
    quantity_shipped REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (id_order) REFERENCES WmsOrders(id_order) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT,
    FOREIGN KEY (id_uom) REFERENCES UnitOfMeasure(id_uom) ON DELETE RESTRICT
);

-- Tabela `GoodsReceived` (Zlecenia Przyjęcia - Nagłówek)
CREATE TABLE GoodsReceived (
    id_receipt INTEGER PRIMARY KEY AUTOINCREMENT,
    asn_reference VARCHAR(100) NULL,
    supplier VARCHAR(200) NULL,
    status TEXT NOT NULL DEFAULT 'EXPECTED' CHECK(status IN ('EXPECTED', 'RECEIVING', 'RECEIVED', 'CANCELLED')),
    expected_at TEXT NULL, -- Data jako TEXT
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    id_user_created INTEGER NULL,
    FOREIGN KEY (id_user_created) REFERENCES Users(id_user) ON DELETE SET NULL
);

-- Tabela `ReceiptLines` (Linie Przyjęć)
CREATE TABLE ReceiptLines (
    id_line INTEGER PRIMARY KEY AUTOINCREMENT,
    id_receipt INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    id_uom INTEGER NOT NULL,
    quantity_expected REAL NOT NULL,
    quantity_received REAL NOT NULL DEFAULT 0,
    batch_number VARCHAR(100) NULL,
    expiry_date TEXT NULL, -- Data jako TEXT
    FOREIGN KEY (id_receipt) REFERENCES GoodsReceived(id_receipt) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT,
    FOREIGN KEY (id_uom) REFERENCES UnitOfMeasure(id_uom) ON DELETE RESTRICT
);


-- ########################################
-- SEKCJA 4b: ALOKACJA I ZADANIA
-- ########################################
DROP TABLE IF EXISTS PickingTasks;

-- Tabela `PickingTasks`
CREATE TABLE PickingTasks (
    id_task INTEGER PRIMARY KEY AUTOINCREMENT,
    id_order_line INTEGER NOT NULL,
    id_inventory INTEGER NOT NULL,
    quantity_to_pick REAL NOT NULL,
    id_user_assigned INTEGER NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'IN_PROGRESS', 'PICKED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    
    FOREIGN KEY (id_order_line) REFERENCES OrderLines(id_line) ON DELETE CASCADE,
    FOREIGN KEY (id_inventory) REFERENCES Inventory(id_inventory) ON DELETE RESTRICT,
    FOREIGN KEY (id_user_assigned) REFERENCES Users(id_user) ON DELETE SET NULL
);


-- ########################################
-- SEKCJA 4c: WYSYŁKI I PRZEWOŹNICY
-- ########################################
DROP TABLE IF EXISTS ShipmentLines;
DROP TABLE IF EXISTS Shipments;
DROP TABLE IF EXISTS Carriers;

-- Tabela `Carriers`
CREATE TABLE Carriers (
    id_carrier INTEGER PRIMARY KEY, -- ID podawane w skrypcie
    name VARCHAR(100) NOT NULL,
    tracking_url_template VARCHAR(255) NULL
);

-- Tabela `Shipments`
CREATE TABLE Shipments (
    id_shipment INTEGER PRIMARY KEY AUTOINCREMENT,
    id_order INTEGER NOT NULL,
    id_carrier INTEGER NULL,
    tracking_number VARCHAR(100) NULL,
    shipped_at TEXT NULL, -- Data jako TEXT
    status TEXT NOT NULL DEFAULT 'PACKING' CHECK(status IN ('PACKING', 'READY_TO_SHIP', 'SHIPPED')),
    FOREIGN KEY (id_order) REFERENCES WmsOrders(id_order),
    FOREIGN KEY (id_carrier) REFERENCES Carriers(id_carrier)
);

-- Tabela `ShipmentLines`
CREATE TABLE ShipmentLines (
    id_shipment_line INTEGER PRIMARY KEY AUTOINCREMENT,
    id_shipment INTEGER NOT NULL,
    id_inventory INTEGER NULL,
    id_product INTEGER NOT NULL,
    quantity_shipped REAL NOT NULL,
    FOREIGN KEY (id_shipment) REFERENCES Shipments(id_shipment) ON DELETE CASCADE,
    FOREIGN KEY (id_inventory) REFERENCES Inventory(id_inventory),
    FOREIGN KEY (id_product) REFERENCES Products(id_product)
);


-- ########################################
-- SEKCJA 5: AUDYT - DZIENNIK TRANSAKCJI
-- ########################################

DROP TABLE IF EXISTS InventoryTransactions;

-- Tabela `InventoryTransactions` (Transakcje Magazynowe)
CREATE TABLE InventoryTransactions (
    id_transaction INTEGER PRIMARY KEY AUTOINCREMENT,
    id_product INTEGER NOT NULL,
    id_user INTEGER NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('RECEIPT', 'SHIPMENT', 'MOVE', 'ADJUST_IN', 'ADJUST_OUT', 'STATUS_CHANGE')),
    reference_type VARCHAR(50) NULL,
    reference_id INTEGER NULL, -- Zmienione z BIGINT na INTEGER
    from_location_id INTEGER NULL,
    to_location_id INTEGER NULL,
    lpn VARCHAR(50) NULL,
    batch_number VARCHAR(100) NULL,
    quantity_change REAL NOT NULL,
    new_status TEXT NULL CHECK(new_status IN ('Available', 'Quarantined', 'Damaged', 'Reserved')),
    timestamp TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT,
    FOREIGN KEY (id_user) REFERENCES Users(id_user) ON DELETE SET NULL,
    FOREIGN KEY (from_location_id) REFERENCES Locations(id_location) ON DELETE RESTRICT,
    FOREIGN KEY (to_location_id) REFERENCES Locations(id_location) ON DELETE RESTRICT
);

-- Włączamy z powrotem sprawdzanie kluczy obcych
PRAGMA foreign_keys = ON;

-- ########################################
-- SEKCJA 6: INDEKSY
-- ########################################

CREATE INDEX IF NOT EXISTS idx_products_sku ON Products(sku);
CREATE INDEX IF NOT EXISTS idx_locations_code ON Locations(location_code);
CREATE INDEX IF NOT EXISTS idx_inventory_product ON Inventory(id_product);
CREATE INDEX IF NOT EXISTS idx_inventory_location ON Inventory(id_location);
CREATE INDEX IF NOT EXISTS idx_inventory_lpn ON Inventory(lpn);
CREATE INDEX IF NOT EXISTS idx_transactions_product ON InventoryTransactions(id_product);
CREATE INDEX IF NOT EXISTS idx_transactions_time ON InventoryTransactions(timestamp);
CREATE INDEX IF NOT EXISTS idx_transactions_reference ON InventoryTransactions(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_order_line ON PickingTasks(id_order_line);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_inventory ON PickingTasks(id_inventory);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_status ON PickingTasks(status);
CREATE INDEX IF NOT EXISTS idx_shipments_order ON Shipments(id_order);
CREATE INDEX IF NOT EXISTS idx_shipments_carrier ON Shipments(id_carrier);
CREATE INDEX IF NOT EXISTS idx_shipment_lines_shipment ON ShipmentLines(id_shipment);