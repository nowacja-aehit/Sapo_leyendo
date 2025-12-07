package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zone")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "is_temperature_controlled")
    private boolean isTemperatureControlled;

    @Column(name = "is_secure")
    private boolean isSecure;

    @Column(name = "allow_mixed_sku")
    private boolean allowMixedSku;
}
