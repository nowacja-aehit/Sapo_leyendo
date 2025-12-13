package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(name = "GoodsReceived")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceived {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receipt")
    private Integer id;

    @Column(name = "asn_reference")
    private String asnReference;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "status")
    private String status;

    @Column(name = "expected_at")
    private LocalDateTime expectedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
