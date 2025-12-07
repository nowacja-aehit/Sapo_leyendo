package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.InboundOrder;
import com.mycompany.sapo_leyendo.repository.InboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InboundService {

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    public List<InboundOrder> getAllInboundOrders() {
        return inboundOrderRepository.findAll();
    }

    public Optional<InboundOrder> getInboundOrderById(Integer id) {
        return inboundOrderRepository.findById(id);
    }

    public InboundOrder saveInboundOrder(InboundOrder order) {
        return inboundOrderRepository.save(order);
    }
}
