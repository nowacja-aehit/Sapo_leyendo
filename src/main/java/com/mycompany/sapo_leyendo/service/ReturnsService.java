package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReturnsService {

    @Autowired
    private RmaRequestRepository rmaRequestRepository;

    @Autowired
    private ReturnItemRepository returnItemRepository;

    @Autowired
    private RefurbishTaskRepository refurbishTaskRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public RmaRequest createRmaRequest(Integer outboundOrderId, String customerReason) {
        OutboundOrder order = outboundOrderRepository.findById(outboundOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        RmaRequest rma = new RmaRequest();
        rma.setOutboundOrder(order);
        rma.setCustomerReason(customerReason);
        rma.setStatus(RmaStatus.PENDING);
        rma.setCreatedAt(LocalDateTime.now());
        
        return rmaRequestRepository.save(rma);
    }

    @Transactional
    public RmaRequest receiveReturn(Integer rmaId, String trackingNumberIn) {
        RmaRequest rma = rmaRequestRepository.findById(rmaId)
                .orElseThrow(() -> new RuntimeException("RMA not found"));

        rma.setStatus(RmaStatus.RECEIVED);
        rma.setTrackingNumberIn(trackingNumberIn);
        
        return rmaRequestRepository.save(rma);
    }

    @Transactional
    public ReturnItem gradeItem(Integer rmaId, Integer productId, GradingStatus grade, String comment) {
        RmaRequest rma = rmaRequestRepository.findById(rmaId)
                .orElseThrow(() -> new RuntimeException("RMA not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ReturnItem item = new ReturnItem();
        item.setRmaRequest(rma);
        item.setProduct(product);
        item.setGradingStatus(grade);
        item.setInspectorComment(comment);

        // Auto-assign disposition based on grade
        switch (grade) {
            case GRADE_A:
                item.setDisposition(Disposition.RESTOCK);
                break;
            case GRADE_B:
                item.setDisposition(Disposition.REFURBISH);
                break;
            case GRADE_C:
                item.setDisposition(Disposition.VENDOR); // Or REFURBISH depending on policy
                break;
            case SCRAP:
                item.setDisposition(Disposition.TRASH);
                break;
        }

        item = returnItemRepository.save(item);

        // Trigger side effects based on disposition
        if (item.getDisposition() == Disposition.REFURBISH) {
            createRefurbishTask(item);
        }
        
        return item;
    }

    public List<RmaRequest> getAllRmaRequests() {
        return rmaRequestRepository.findAll();
    }

    public Optional<RmaRequest> getRmaRequestById(Integer id) {
        return rmaRequestRepository.findById(id);
    }

    @Transactional
    public void deleteRmaRequest(Integer id) {
        rmaRequestRepository.deleteById(id);
    }

    private void createRefurbishTask(ReturnItem item) {
        RefurbishTask task = new RefurbishTask();
        task.setReturnItem(item);
        task.setStatus(RefurbishStatus.OPEN);
        task.setRequiredActions(List.of("Inspect", "Clean", "Repackage")); // Default actions
        refurbishTaskRepository.save(task);
    }
}
