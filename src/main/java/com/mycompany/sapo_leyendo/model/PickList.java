package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PickLists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pick_list")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_wave")
    @JsonIgnore
    private Wave wave;

    @Column(name = "pick_list_number", unique = true, nullable = false)
    private String pickListNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PickListStatus status = PickListStatus.PENDING;

    @Column(name = "id_user_assigned")
    private Integer assignedUserId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pickList", cascade = CascadeType.ALL)
    private List<PickTask> pickTasks;
}
