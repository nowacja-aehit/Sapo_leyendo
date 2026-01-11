package com.mycompany.sapo_leyendo.dto;

import lombok.Data;

@Data
public class ProductCreateRequest {
    private String sku;
    private String name;
    private String description;
    private String category;  // Opcjonalne, może być null
    private Integer minStock;
    private Integer maxStock;
}
