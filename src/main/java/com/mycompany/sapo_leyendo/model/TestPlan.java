package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "TestPlans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test_plan")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "aql_level")
    private Double aqlLevel;

    @ElementCollection
    @CollectionTable(name = "TestPlanSteps", joinColumns = @JoinColumn(name = "id_test_plan"))
    private List<TestStep> steps;
}
