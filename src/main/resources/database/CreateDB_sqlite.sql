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
DROP TABLE IF EXISTS Zones;
DROP TABLE IF EXISTS LocationTypes;
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
    unit_price REAL NULL,
    min_stock_level INTEGER NULL,
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
    reorder_level INTEGER NULL,
    status TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK(status IN ('AVAILABLE', 'ALLOCATED', 'QC_HOLD', 'BLOCKED', 'DAMAGED')),
    received_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT,
    FOREIGN KEY (id_location) REFERENCES Locations(id_location) ON DELETE RESTRICT,
    FOREIGN KEY (id_uom) REFERENCES UnitOfMeasure(id_uom) ON DELETE RESTRICT,
    
    UNIQUE (id_product, id_location, lpn, batch_number, status)
);


-- ########################################
-- SEKCJA 4: PROCESY - PRZYJĘCIA I WYDANIA
-- ########################################

DROP TABLE IF EXISTS InboundOrderItems;
DROP TABLE IF EXISTS InboundOrders;
DROP TABLE IF EXISTS OutboundOrderItems;
DROP TABLE IF EXISTS OutboundOrders;
DROP TABLE IF EXISTS ReceiptLines;
DROP TABLE IF EXISTS GoodsReceived;

-- Tabela `InboundOrders`
CREATE TABLE InboundOrders (
    id_inbound_order INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number VARCHAR(100) UNIQUE NOT NULL,
    status TEXT NOT NULL DEFAULT 'PLANNED' CHECK(status IN ('PLANNED', 'RECEIVED', 'CANCELLED')),
    expected_date TEXT NULL,
    supplier VARCHAR(255) NULL,
    dock_id INTEGER NULL,
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now'))
);

-- Tabela `InboundOrderItems`
CREATE TABLE InboundOrderItems (
    id_inbound_order_item INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inbound_order INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    quantity_expected INTEGER NOT NULL,
    quantity_received INTEGER NOT NULL DEFAULT 0,
    batch_number VARCHAR(100) NULL,
    FOREIGN KEY (id_inbound_order) REFERENCES InboundOrders(id_inbound_order) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT
);

-- Tabela `OutboundOrders` (Zlecenia Wydania - Nagłówek)
CREATE TABLE OutboundOrders (
    id_outbound_order INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number VARCHAR(100) UNIQUE NOT NULL,
    status TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW', 'ALLOCATED', 'PICKING', 'PACKED', 'SHIPPED', 'CANCELLED')),
    ship_date TEXT NULL,
    destination VARCHAR(255) NULL,
    customer_name VARCHAR(255) NULL,
    priority TEXT NOT NULL DEFAULT 'Medium',
    total_amount REAL NULL,
    items_count INTEGER NULL,
    order_date TEXT NULL,
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    id_user_created INTEGER NULL,
    FOREIGN KEY (id_user_created) REFERENCES Users(id_user) ON DELETE SET NULL
);

-- Tabela `OutboundOrderItems` (Linie Zleceń Wydania)
CREATE TABLE OutboundOrderItems (
    id_outbound_order_item INTEGER PRIMARY KEY AUTOINCREMENT,
    id_outbound_order INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    id_uom INTEGER NOT NULL,
    quantity_ordered REAL NOT NULL,
    quantity_picked REAL NOT NULL DEFAULT 0,
    quantity_shipped REAL NOT NULL DEFAULT 0,
    unit_price REAL NULL,
    line_total REAL NULL,
    sku VARCHAR(100) NULL,
    product_name VARCHAR(255) NULL,
    location_code VARCHAR(50) NULL,
    status TEXT NULL,
    FOREIGN KEY (id_outbound_order) REFERENCES OutboundOrders(id_outbound_order) ON DELETE CASCADE,
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
    id_picking_task INTEGER PRIMARY KEY AUTOINCREMENT,
    id_outbound_order_item INTEGER NOT NULL,
    id_inventory INTEGER NOT NULL,
    quantity_to_pick REAL NOT NULL,
    id_user_assigned INTEGER NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'IN_PROGRESS', 'PICKED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    
    FOREIGN KEY (id_outbound_order_item) REFERENCES OutboundOrderItems(id_outbound_order_item) ON DELETE CASCADE,
    FOREIGN KEY (id_inventory) REFERENCES Inventory(id_inventory) ON DELETE RESTRICT,
    FOREIGN KEY (id_user_assigned) REFERENCES Users(id_user) ON DELETE SET NULL
);


-- ########################################
-- SEKCJA 4c: WYSYŁKI I PRZEWOŹNICY
-- ########################################
DROP TABLE IF EXISTS RefurbishActions;
DROP TABLE IF EXISTS RefurbishTasks;
DROP TABLE IF EXISTS ReturnItems;
DROP TABLE IF EXISTS RmaRequests;
DROP TABLE IF EXISTS ParcelItems;
DROP TABLE IF EXISTS Parcels;
DROP TABLE IF EXISTS PackingMaterials;
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
    id_outbound_order INTEGER NOT NULL,
    id_carrier INTEGER NULL,
    id_load INTEGER NULL,
    tracking_number VARCHAR(100) NULL,
    shipped_at TEXT NULL, -- Data jako TEXT
    total_weight_kg REAL NULL,
    status TEXT NOT NULL DEFAULT 'PACKING' CHECK(status IN ('PACKING', 'PACKED', 'SHIPPED')),
    FOREIGN KEY (id_outbound_order) REFERENCES OutboundOrders(id_outbound_order),
    FOREIGN KEY (id_carrier) REFERENCES Carriers(id_carrier),
    FOREIGN KEY (id_load) REFERENCES TransportLoads(id_load)
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

-- Tabela `PackingMaterials`
CREATE TABLE PackingMaterials (
    id_packing_material INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    widthCm REAL,
    heightCm REAL,
    lengthCm REAL,
    tareWeightKg REAL,
    maxWeightKg REAL
);

-- Tabela `Parcels`
CREATE TABLE Parcels (
    id_parcel INTEGER PRIMARY KEY AUTOINCREMENT,
    id_shipment INTEGER NOT NULL,
    id_packing_material INTEGER,
    weight_kg REAL,
    tracking_sub_number VARCHAR(100),
    FOREIGN KEY (id_shipment) REFERENCES Shipments(id_shipment) ON DELETE CASCADE,
    FOREIGN KEY (id_packing_material) REFERENCES PackingMaterials(id_packing_material)
);

-- Tabela `ParcelItems`
CREATE TABLE ParcelItems (
    id_parcel_item INTEGER PRIMARY KEY AUTOINCREMENT,
    id_parcel INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY (id_parcel) REFERENCES Parcels(id_parcel) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES Products(id_product)
);


-- ########################################
-- SEKCJA 4d: ZWROTY
-- ########################################

CREATE TABLE RmaRequests (
    id_rma INTEGER PRIMARY KEY AUTOINCREMENT,
    id_outbound_order INTEGER NOT NULL,
    customer_reason TEXT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'APPROVED', 'RECEIVED', 'COMPLETED', 'REJECTED')),
    tracking_number_in VARCHAR(100) NULL,
    created_at TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%S', 'now')),
    FOREIGN KEY (id_outbound_order) REFERENCES OutboundOrders(id_outbound_order) ON DELETE CASCADE
);

CREATE TABLE ReturnItems (
    id_return_item INTEGER PRIMARY KEY AUTOINCREMENT,
    id_rma INTEGER NOT NULL,
    id_product INTEGER NOT NULL,
    serial_number VARCHAR(100) NULL,
    grading_status TEXT NULL CHECK(grading_status IN ('GRADE_A', 'GRADE_B', 'GRADE_C', 'SCRAP')),
    disposition TEXT NULL CHECK(disposition IN ('RESTOCK', 'REFURBISH', 'VENDOR', 'TRASH')),
    inspector_comment TEXT NULL,
    FOREIGN KEY (id_rma) REFERENCES RmaRequests(id_rma) ON DELETE CASCADE,
    FOREIGN KEY (id_product) REFERENCES Products(id_product) ON DELETE RESTRICT
);

CREATE TABLE RefurbishTasks (
    id_task INTEGER PRIMARY KEY AUTOINCREMENT,
    id_return_item INTEGER NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'OPEN' CHECK(status IN ('OPEN', 'IN_PROGRESS', 'DONE')),
    FOREIGN KEY (id_return_item) REFERENCES ReturnItems(id_return_item) ON DELETE CASCADE
);

CREATE TABLE RefurbishActions (
    id_action INTEGER PRIMARY KEY AUTOINCREMENT,
    id_task INTEGER NOT NULL,
    action TEXT NOT NULL,
    FOREIGN KEY (id_task) REFERENCES RefurbishTasks(id_task) ON DELETE CASCADE
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
CREATE INDEX IF NOT EXISTS idx_locations_code ON Locations(barcode);
CREATE INDEX IF NOT EXISTS idx_inventory_product ON Inventory(id_product);
CREATE INDEX IF NOT EXISTS idx_inventory_location ON Inventory(id_location);
CREATE INDEX IF NOT EXISTS idx_inventory_lpn ON Inventory(lpn);
CREATE INDEX IF NOT EXISTS idx_transactions_product ON InventoryTransactions(id_product);
CREATE INDEX IF NOT EXISTS idx_transactions_time ON InventoryTransactions(timestamp);
CREATE INDEX IF NOT EXISTS idx_transactions_reference ON InventoryTransactions(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_order_line ON PickingTasks(id_outbound_order_item);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_inventory ON PickingTasks(id_inventory);
CREATE INDEX IF NOT EXISTS idx_picking_tasks_status ON PickingTasks(status);
CREATE INDEX IF NOT EXISTS idx_shipments_order ON Shipments(id_outbound_order);
CREATE INDEX IF NOT EXISTS idx_shipments_carrier ON Shipments(id_carrier);
CREATE INDEX IF NOT EXISTS idx_shipments_load ON Shipments(id_load);
CREATE INDEX IF NOT EXISTS idx_shipment_lines_shipment ON ShipmentLines(id_shipment);