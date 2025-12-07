package com.mycompany.sapo_leyendo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "RmaRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RmaRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rma")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_outbound_order", nullable = false)
    private OutboundOrder outboundOrder;

    @Column(name = "customer_reason")
    private String customerReason;

    @Enumerated(EnumType.STRING)
    private RmaStatus status;

    @Column(name = "tracking_number_in")
    private String trackingNumberIn;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rmaRequest", cascade = CascadeType.ALL)
    private List<ReturnItem> returnItems;
}
