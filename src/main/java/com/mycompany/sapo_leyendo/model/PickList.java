package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PickLists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pick_list")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_wave")
    private Wave wave;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PickListType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PickListStatus status = PickListStatus.PENDING;

    @Column(name = "container_lpn")
    private String containerLpn;

    @OneToMany(mappedBy = "pickList", cascade = CascadeType.ALL)
    private List<PickTask> pickTasks;
}
