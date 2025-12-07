package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.NonConformanceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NonConformanceReportRepository extends JpaRepository<NonConformanceReport, Integer> {
}
