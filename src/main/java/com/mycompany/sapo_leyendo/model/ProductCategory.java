package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ProductCategories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {
    @Id
    @Column(name = "id_category")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_category_id")
    private Integer parentCategoryId;
}
