package com.mycompany.sapo_leyendo.dto;

import lombok.Data;

@Data
public class LocationCreateRequest {
    private String name;
    private String zone;  // Nazwa strefy jako String
    private Integer locationTypeId;
    private String aisle;
    private String rack;
    private String level;
    private String bin;
    private String shelf;
    private Boolean isActive;
}
