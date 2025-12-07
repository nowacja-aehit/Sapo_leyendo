package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "ReportDefinitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_report")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "query_template", columnDefinition = "TEXT")
    private String queryTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visualization_type")
    private VisualizationType visualizationType;

    @Column(name = "required_permission")
    private String requiredPermission;
    
    // Parameters could be a separate entity or a JSON string, simplifying for now
    @Column(name = "parameters_json")
    private String parametersJson;
}
