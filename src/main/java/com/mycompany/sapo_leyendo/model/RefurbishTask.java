package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "RefurbishTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefurbishTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_return_item", nullable = false)
    private ReturnItem returnItem;

    @ElementCollection
    @CollectionTable(name = "RefurbishActions", joinColumns = @JoinColumn(name = "id_task"))
    @Column(name = "action")
    private List<String> requiredActions;

    @Enumerated(EnumType.STRING)
    private RefurbishStatus status;
}
