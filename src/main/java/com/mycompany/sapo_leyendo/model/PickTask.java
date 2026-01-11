package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PickTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pick_task")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_pick_list")
    private PickList pickList;

    @ManyToOne
    @JoinColumn(name = "id_outbound_order_item", nullable = false)
    private OutboundOrderItem outboundOrderItem;

    @ManyToOne
    @JoinColumn(name = "id_inventory", nullable = false)
    private Inventory inventory;

    @Column(name = "quantity_to_pick", nullable = false)
    private Double quantityToPick;

    @Column(name = "quantity_picked")
    private Double quantityPicked = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PickTaskStatus status = PickTaskStatus.PENDING;

    @Column(name = "pick_sequence")
    private Integer pickSequence = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;
}
