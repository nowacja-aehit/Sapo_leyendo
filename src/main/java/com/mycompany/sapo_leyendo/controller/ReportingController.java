package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.DashboardStats;
import com.mycompany.sapo_leyendo.model.KpiMetric;
import com.mycompany.sapo_leyendo.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(reportingService.getDashboardStats());
    }

    @GetMapping("/kpi/{name}")
    public ResponseEntity<List<KpiMetric>> getKpiHistory(@PathVariable String name) {
        return ResponseEntity.ok(reportingService.getKpiHistory(name));
    }
}
