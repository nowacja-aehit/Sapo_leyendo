package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Manifests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_manifest")
    private Integer id;

    @Column(name = "manifest_number", unique = true, nullable = false)
    private String manifestNumber;

    @ManyToOne
    @JoinColumn(name = "id_load")
    private TransportLoad transportLoad;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
