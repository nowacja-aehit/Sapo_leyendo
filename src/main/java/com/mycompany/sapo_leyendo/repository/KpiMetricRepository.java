package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.KpiMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KpiMetricRepository extends JpaRepository<KpiMetric, Integer> {
    List<KpiMetric> findByNameOrderByTimestampDesc(String name);
}
