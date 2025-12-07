package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.OutboundOrder;
import com.mycompany.sapo_leyendo.repository.OutboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OutboundService {

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    public List<OutboundOrder> getAllOutboundOrders() {
        return outboundOrderRepository.findAll();
    }

    public Optional<OutboundOrder> getOutboundOrderById(Integer id) {
        return outboundOrderRepository.findById(id);
    }

    public OutboundOrder saveOutboundOrder(OutboundOrder order) {
        return outboundOrderRepository.save(order);
    }
}
