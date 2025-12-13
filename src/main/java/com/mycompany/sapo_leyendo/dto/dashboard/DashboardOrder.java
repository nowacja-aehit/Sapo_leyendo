package com.mycompany.sapo_leyendo.dto.dashboard;

public record DashboardOrder(
    String id,
    String orderNumber,
    String customer,
    int items,
    double total,
    String status,
    String date,
    String priority
) {}
