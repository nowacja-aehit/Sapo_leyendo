package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.dto.DashboardStats;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    public DashboardStats getDashboardStats() {
        return new DashboardStats(
            productRepository.count(),
            locationRepository.count(),
            inventoryRepository.count(),
            inboundOrderRepository.count(),
            outboundOrderRepository.count()
        );
    }
}
