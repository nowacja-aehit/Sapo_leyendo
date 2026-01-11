package com.mycompany.sapo_leyendo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.sapo_leyendo.converter.LocalDateTimeStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "OutboundOrders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outbound_order")
    private Integer id;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "status", nullable = false)
    private String status = "NEW"; // NEW, ALLOCATED, PICKING, PACKED, SHIPPED, CANCELLED

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "ship_date")
    private LocalDateTime shipDate;

    // Setter that accepts date string (e.g., "2026-01-10") from frontend
    @com.fasterxml.jackson.annotation.JsonSetter("shipDate")
    public void setShipDateFromString(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                if (dateStr.contains("T")) {
                    this.shipDate = LocalDateTime.parse(dateStr);
                } else {
                    this.shipDate = LocalDate.parse(dateStr).atStartOfDay();
                }
            } catch (Exception e) {
                this.shipDate = null;
            }
        }
    }

    @Column(name = "destination")
    private String destination;

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonIgnore
    @Column(name = "customer_name")
    private String customerName;

    @JsonIgnore
    @Column(name = "priority")
    private String priority = "Medium";

    @JsonIgnore
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @JsonIgnore
    @Column(name = "items_count")
    private Integer itemsCount;

    @JsonIgnore
    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    
    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL)
    private List<OutboundOrderItem> items;

    @JsonProperty("orderNumber")
    public String getOrderNumber() {
        return referenceNumber;
    }

    @JsonProperty("customer")
    public String getCustomer() {
        return customerName;
    }

    @JsonProperty("items")
    public Integer getTotalItems() {
        return itemsCount != null ? itemsCount : (items != null ? (int) items.stream().mapToDouble(item -> item.getQuantityOrdered() != null ? item.getQuantityOrdered() : 0.0).sum() : 0);
    }

    @JsonProperty("total")
    public BigDecimal getOrderTotal() {
        if (totalAmount != null) {
            return totalAmount;
        }
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> Optional.ofNullable(item.getLineTotal())
                        .orElseGet(() -> Optional.ofNullable(item.getUnitPrice())
                                .map(price -> price.multiply(BigDecimal.valueOf(Optional.ofNullable(item.getQuantityOrdered()).orElse(0.0))))
                                .orElse(BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @JsonProperty("priority")
    public String getPriorityLabel() {
        return priority != null ? priority : "Medium";
    }

    @JsonProperty("date")
    public String getOrderDateLabel() {
        LocalDate date = orderDate != null
                ? orderDate.toLocalDate()
                : (createdAt != null ? createdAt.toLocalDate() : LocalDate.now());
        return date.toString();
    }
}
