package com.mycompany.sapo_leyendo.dto;

import lombok.Data;

@Data
public class ReceiveItemRequest {
    private Integer inboundOrderItemId;
    private String lpn;
    private Integer quantity;
    private Long operatorId;
    private String damageCode;
}
