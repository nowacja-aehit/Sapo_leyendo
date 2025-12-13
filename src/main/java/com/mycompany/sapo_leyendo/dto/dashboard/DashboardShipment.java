package com.mycompany.sapo_leyendo.dto.dashboard;

public record DashboardShipment(
    String id,
    String trackingNumber,
    String destination,
    String carrier,
    String status,
    String estimatedDelivery,
    int items
) {}
