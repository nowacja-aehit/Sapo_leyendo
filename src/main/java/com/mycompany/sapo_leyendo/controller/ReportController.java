package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.DashboardStats;
import com.mycompany.sapo_leyendo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public DashboardStats getDashboardStats() {
        return reportService.getDashboardStats();
    }
}
