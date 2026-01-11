package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "DockAppointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DockAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dock_appointment")
    private Integer id;

    @Column(name = "dock_id", nullable = false)
    private Integer dockId;

    @OneToOne
    @JoinColumn(name = "id_inbound_order")
    private InboundOrder inboundOrder;

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "carrier_name")
    private String carrierName;
}
