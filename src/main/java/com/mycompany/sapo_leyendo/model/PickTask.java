package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "PickTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pick_task")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_pick_list")
    private PickList pickList;

    @ManyToOne
    @JoinColumn(name = "id_outbound_order")
    private OutboundOrder outboundOrder;

    @ManyToOne
    @JoinColumn(name = "id_product")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "id_location")
    private Location location;

    @Column(name = "quantity_to_pick")
    private Integer quantityToPick;

    @Column(name = "quantity_picked")
    private Integer quantityPicked;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PickTaskStatus status = PickTaskStatus.OPEN;

    @Column(name = "sequence")
    private Integer sequence;
}
