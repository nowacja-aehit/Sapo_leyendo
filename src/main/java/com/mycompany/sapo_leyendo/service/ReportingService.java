package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.dto.DashboardStats;
import com.mycompany.sapo_leyendo.model.AuditLog;
import com.mycompany.sapo_leyendo.model.KpiMetric;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportingService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private GoodsReceivedRepository goodsReceivedRepository;
    @Autowired
    private OutboundOrderRepository outboundOrderRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private KpiMetricRepository kpiMetricRepository;

    public DashboardStats getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalLocations = locationRepository.count();
        long totalInventoryItems = inventoryRepository.count();
        long totalInboundOrders = goodsReceivedRepository.count();
        long totalOutboundOrders = outboundOrderRepository.count();

        return new DashboardStats(totalProducts, totalLocations, totalInventoryItems, totalInboundOrders, totalOutboundOrders);
    }

    @Transactional
    public void logAudit(String action, String entity, String oldValue, String newValue, String username) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setUsername(username);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    public void recordKpi(String name, Double value, String dimension) {
        KpiMetric kpi = new KpiMetric();
        kpi.setName(name);
        kpi.setValue(value);
        kpi.setDimension(dimension);
        kpi.setTimestamp(LocalDateTime.now());
        kpiMetricRepository.save(kpi);
    }
    
    public List<KpiMetric> getKpiHistory(String name) {
        return kpiMetricRepository.findByNameOrderByTimestampDesc(name);
    }
}
