package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Waves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wave")
    private Integer id;

    @Column(name = "wave_number", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WaveStatus status = WaveStatus.CREATED;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "id_user_created")
    private Integer userCreatedId;

    @OneToMany(mappedBy = "wave", cascade = CascadeType.ALL)
    private List<PickList> pickLists;
}
