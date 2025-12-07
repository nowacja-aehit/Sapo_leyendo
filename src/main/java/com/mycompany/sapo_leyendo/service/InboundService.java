package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.InboundOrder;
import com.mycompany.sapo_leyendo.model.InboundOrderItem;
import com.mycompany.sapo_leyendo.model.Receipt;
import com.mycompany.sapo_leyendo.repository.InboundOrderItemRepository;
import com.mycompany.sapo_leyendo.repository.InboundOrderRepository;
import com.mycompany.sapo_leyendo.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InboundService {

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private InboundOrderItemRepository inboundOrderItemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    public List<InboundOrder> getAllInboundOrders() {
        return inboundOrderRepository.findAll();
    }

    public Optional<InboundOrder> getInboundOrderById(Integer id) {
        return inboundOrderRepository.findById(id);
    }

    public InboundOrder saveInboundOrder(InboundOrder order) {
        return inboundOrderRepository.save(order);
    }

    @Transactional
    public Receipt receiveItem(Integer inboundOrderItemId, String lpn, Integer quantity, Long operatorId, String damageCode) {
        InboundOrderItem item = inboundOrderItemRepository.findById(inboundOrderItemId)
                .orElseThrow(() -> new RuntimeException("Inbound Order Item not found"));

        Receipt receipt = new Receipt();
        receipt.setInboundOrderItem(item);
        receipt.setLpn(lpn);
        receipt.setQuantity(quantity);
        receipt.setOperatorId(operatorId);
        receipt.setTimestamp(LocalDateTime.now());
        receipt.setDamageCode(damageCode);

        receiptRepository.save(receipt);

        item.setQuantityReceived(item.getQuantityReceived() + quantity);
        inboundOrderItemRepository.save(item);

        InboundOrder order = item.getInboundOrder();
        if ("PLANNED".equals(order.getStatus()) || "ARRIVED".equals(order.getStatus())) {
            order.setStatus("IN_PROGRESS");
            inboundOrderRepository.save(order);
        }

        return receipt;
    }
}
