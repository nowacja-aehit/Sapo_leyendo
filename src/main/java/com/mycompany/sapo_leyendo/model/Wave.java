package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Waves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wave {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_wave")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WaveStatus status = WaveStatus.PLANNED;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "cutoff_time")
    private LocalDateTime cutoffTime;

    @OneToMany(mappedBy = "wave", cascade = CascadeType.ALL)
    private List<PickList> pickLists;
}
