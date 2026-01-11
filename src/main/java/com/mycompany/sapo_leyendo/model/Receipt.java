package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receipt")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_inbound_order_item", nullable = false)
    private InboundOrderItem inboundOrderItem;

    @Column(name = "lpn", nullable = false)
    private String lpn;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "operator_id")
    private Long operatorId;

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "damage_code")
    private String damageCode;
}
