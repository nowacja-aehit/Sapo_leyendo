package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PickingTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_picking_task")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_outbound_order_item", nullable = false)
    private OutboundOrderItem outboundOrderItem;

    @ManyToOne
    @JoinColumn(name = "id_inventory", nullable = false)
    private Inventory inventory;

    @Column(name = "quantity_to_pick", nullable = false)
    private Double quantityToPick;

    @ManyToOne
    @JoinColumn(name = "id_user_assigned")
    private User userAssigned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickingTaskStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
