-- Dane testowe dla profilu test
-- Kategorie
INSERT OR IGNORE INTO ProductCategories (id_category, name) VALUES (1, 'Elektronika');
INSERT OR IGNORE INTO ProductCategories (id_category, name) VALUES (2, 'Meble');

-- Jednostki miary (UOM)
INSERT OR IGNORE INTO UnitsOfMeasure (id_uom, code, name) VALUES (1, 'EA', 'Each');
INSERT OR IGNORE INTO UnitsOfMeasure (id_uom, code, name) VALUES (2, 'PK', 'Pack');

-- Strefy
INSERT OR IGNORE INTO Zones (id_zone, name, is_temperature_controlled, is_secure, allow_mixed_sku) 
VALUES (1, 'A', 0, 0, 1);
INSERT OR IGNORE INTO Zones (id_zone, name, is_temperature_controlled, is_secure, allow_mixed_sku) 
VALUES (2, 'B', 0, 0, 1);

-- Typy lokacji
INSERT OR IGNORE INTO LocationTypes (id_location_type, code, name, description) 
VALUES (1, 'RACK', 'Regał', 'Standardowy regał magazynowy');
INSERT OR IGNORE INTO LocationTypes (id_location_type, code, name, description) 
VALUES (2, 'PALLET', 'Paleta', 'Lokacja paletowa');

-- Lokacje
INSERT OR IGNORE INTO Locations (id_location, name, id_zone, id_location_type, aisle, rack, level, bin, status, is_active) 
VALUES (1, 'A-01-01', 1, 1, '01', '01', '01', '01', 'ACTIVE', 1);

-- Produkty testowe
INSERT OR IGNORE INTO Products (id_product, sku, name, description, id_category, id_base_uom, weight_kg, min_stock_level) 
VALUES (1, 'TEST-001', 'Produkt Testowy 1', 'Opis testowy', 1, 1, 1.5, 10);
