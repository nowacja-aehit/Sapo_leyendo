package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "MoveTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_move_task")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoveTaskType type;

    @ManyToOne
    @JoinColumn(name = "id_inventory")
    private Inventory inventory;

    @ManyToOne
    @JoinColumn(name = "id_source_location")
    private Location sourceLocation;

    @ManyToOne
    @JoinColumn(name = "id_target_location")
    private Location targetLocation;

    @Column(name = "priority")
    private Integer priority; // 1-10

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoveTaskStatus status = MoveTaskStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
