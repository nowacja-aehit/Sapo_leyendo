package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UnitOfMeasure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure {
    @Id
    @Column(name = "id_uom")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;
}
