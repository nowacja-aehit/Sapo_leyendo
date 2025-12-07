package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "StockCountSessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCountSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_stock_count_session")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private StockCountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StockCountStatus status = StockCountStatus.OPEN;

    @ManyToMany
    @JoinTable(
        name = "StockCountLocations",
        joinColumns = @JoinColumn(name = "id_stock_count_session"),
        inverseJoinColumns = @JoinColumn(name = "id_location")
    )
    private List<Location> locations;
}
