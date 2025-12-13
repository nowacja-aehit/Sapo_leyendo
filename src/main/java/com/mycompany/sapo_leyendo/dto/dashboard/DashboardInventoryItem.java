package com.mycompany.sapo_leyendo.dto.dashboard;

public record DashboardInventoryItem(
    String id,
    String name,
    String sku,
    String category,
    int quantity,
    int reorderLevel,
    String location,
    String status,
    double price,
    String lastUpdated
) {}
