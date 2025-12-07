# 1. Create Product
$headers = @{ "Content-Type" = "application/json" }
$product = @{ sku="TEST-SKU-001"; name="Test Product"; description="A test product"; category="General"; price=10.0 }
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers $headers -Body ($product | ConvertTo-Json)

# 2. Create Supplier
$supplier = @{ name="Test Supplier"; contactInfo="test@supplier.com"; address="123 Test St" }
Invoke-RestMethod -Uri "http://localhost:8080/api/suppliers" -Method Post -Headers $headers -Body ($supplier | ConvertTo-Json)

# 3. Create Zone
$zone = @{ name="Zone A"; isTemperatureControlled=$false; isSecure=$false; allowMixedSku=$true }
Invoke-RestMethod -Uri "http://localhost:8080/api/zones" -Method Post -Headers $headers -Body ($zone | ConvertTo-Json)

# 4. Create Location Types
$dockType = @{ name="DOCK"; length=10; width=10; height=10; maxWeight=1000; maxVolume=1000 }
Invoke-RestMethod -Uri "http://localhost:8080/api/location-types" -Method Post -Headers $headers -Body ($dockType | ConvertTo-Json)

$shelfType = @{ name="SHELF"; length=1; width=1; height=1; maxWeight=100; maxVolume=100 }
Invoke-RestMethod -Uri "http://localhost:8080/api/location-types" -Method Post -Headers $headers -Body ($shelfType | ConvertTo-Json)

# 5. Create Locations
# We need to fetch the IDs first to be safe, but for this test we'll assume 1 and 2 if the DB was empty.
# Actually, let's fetch them.
$zones = Invoke-RestMethod -Uri "http://localhost:8080/api/zones" -Method Get
$zoneId = $zones[0].id

$types = Invoke-RestMethod -Uri "http://localhost:8080/api/location-types" -Method Get
$dockTypeId = ($types | Where-Object { $_.name -eq "DOCK" }).id
$shelfTypeId = ($types | Where-Object { $_.name -eq "SHELF" }).id

$dockLoc = @{ barcode="LOC-DOCK-01"; zone=@{id=$zoneId}; type=@{id=$dockTypeId}; status="ACTIVE"; aisle="0"; rack="0"; level="0"; bin="0" }
Invoke-RestMethod -Uri "http://localhost:8080/api/locations" -Method Post -Headers $headers -Body ($dockLoc | ConvertTo-Json)

$shelfLoc = @{ barcode="LOC-SHELF-01"; zone=@{id=$zoneId}; type=@{id=$shelfTypeId}; status="ACTIVE"; aisle="1"; rack="1"; level="1"; bin="1" }
Invoke-RestMethod -Uri "http://localhost:8080/api/locations" -Method Post -Headers $headers -Body ($shelfLoc | ConvertTo-Json)

Write-Host "Master Data Created."
