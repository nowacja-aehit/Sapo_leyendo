package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ReturnItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_return_item")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_rma", nullable = false)
    private RmaRequest rmaRequest;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_status")
    private GradingStatus gradingStatus;

    @Enumerated(EnumType.STRING)
    private Disposition disposition;

    @Column(name = "inspector_comment")
    private String inspectorComment;
}
