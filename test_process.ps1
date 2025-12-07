$headers = @{ "Content-Type" = "application/json" }

# 1. Create Inbound Order
# Need Supplier ID
$suppliers = Invoke-RestMethod -Uri "http://localhost:8080/api/suppliers" -Method Get
$supplierId = $suppliers[0].id

# Need Dock Location ID
$locations = Invoke-RestMethod -Uri "http://localhost:8080/api/locations" -Method Get
$dockId = ($locations | Where-Object { $_.barcode -eq "LOC-DOCK-01" }).id

$order = @{ 
    orderNumber="ORD-001"; 
    supplierId=$supplierId; 
    dockId=$dockId; 
    expectedDeliveryDate=(Get-Date).ToString("yyyy-MM-ddTHH:mm:ss"); 
    status="CREATED" 
}
$createdOrder = Invoke-RestMethod -Uri "http://localhost:8080/api/inbound-orders" -Method Post -Headers $headers -Body ($order | ConvertTo-Json)
$orderId = $createdOrder.id
Write-Host "Order Created: $orderId"

# 2. Add Item to Order
# Need Product ID
$products = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
$productId = $products[0].id

$item = @{
    productId=$productId;
    expectedQuantity=10;
    receivedQuantity=0;
    status="PENDING"
}
# The API for adding items might be different. Let's check InboundOrderController.
# Assuming POST /api/inbound-orders/{id}/items
Invoke-RestMethod -Uri "http://localhost:8080/api/inbound-orders/$orderId/items" -Method Post -Headers $headers -Body ($item | ConvertTo-Json)
Write-Host "Item Added to Order."

# 3. Receive Item
# This should trigger the Put-away logic.
# We need the Item ID.
$updatedOrder = Invoke-RestMethod -Uri "http://localhost:8080/api/inbound-orders/$orderId" -Method Get
$itemId = $updatedOrder.items[0].id

$receipt = @{
    inboundOrderItemId=$itemId;
    quantity=5;
    lpn="LPN-001";
    damageCode="";
    operatorId=1
}
Invoke-RestMethod -Uri "http://localhost:8080/api/inbound-orders/receive" -Method Post -Headers $headers -Body ($receipt | ConvertTo-Json)
Write-Host "Item Received."

# 4. Verify Move Task Creation
Start-Sleep -Seconds 2
$tasks = Invoke-RestMethod -Uri "http://localhost:8080/api/move-tasks" -Method Get
$putAwayTask = $tasks | Where-Object { $_.type -eq "PUTAWAY" -and $_.status -eq "PENDING" }

if ($putAwayTask) {
    Write-Host "Put-away Task Created: $($putAwayTask.id)"
    
    # 5. Complete the Task
    Invoke-RestMethod -Uri "http://localhost:8080/api/move-tasks/$($putAwayTask.id)/complete" -Method Post -Headers $headers
    Write-Host "Task Completed."
    
    # 6. Verify Inventory at Shelf
    $inventory = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method Get
    $shelfItem = $inventory | Where-Object { $_.lpn -eq "LPN-001" }
    
    if ($shelfItem.location.barcode -eq "LOC-SHELF-01") {
        Write-Host "SUCCESS: Inventory moved to Shelf!"
    } else {
        Write-Host "FAILURE: Inventory is at $($shelfItem.location.barcode)"
    }
} else {
    Write-Host "FAILURE: No Put-away task found."
}
