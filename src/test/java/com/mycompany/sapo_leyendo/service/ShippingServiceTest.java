package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.model.*;
import com.mycompany.sapo_leyendo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @InjectMocks
    private ShippingService shippingService;

    @Mock
    private TransportLoadRepository transportLoadRepository;

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ManifestRepository manifestRepository;

    @Mock
    private OutboundOrderRepository outboundOrderRepository;

    @Test
    void shouldCreateLoadWithPlannedStatus() {
        Carrier carrier = new Carrier();
        carrier.setId(10);
        when(carrierRepository.findById(10)).thenReturn(Optional.of(carrier));
        when(transportLoadRepository.save(any(TransportLoad.class))).thenAnswer(invocation -> {
            TransportLoad load = invocation.getArgument(0);
            load.setId(99);
            return load;
        });

        TransportLoad load = shippingService.createLoad(10, "DW12345", "Jan", "111222333", 1);

        assertThat(load.getStatus()).isEqualTo(LoadStatus.PLANNED);
        assertThat(load.getCarrier()).isEqualTo(carrier);
        assertThat(load.getId()).isEqualTo(99);
    }

    @Test
    void shouldAssignShipmentToLoadAndUpdateStatus() {
        TransportLoad load = new TransportLoad();
        load.setId(1);
        load.setStatus(LoadStatus.PLANNED);

        Shipment shipment = new Shipment();
        shipment.setId(2);

        when(transportLoadRepository.findById(1)).thenReturn(Optional.of(load));
        when(shipmentRepository.findById(2)).thenReturn(Optional.of(shipment));

        shippingService.assignShipmentToLoad(1, 2);

        assertThat(shipment.getTransportLoad()).isEqualTo(load);
        assertThat(load.getStatus()).isEqualTo(LoadStatus.LOADING);
        verify(shipmentRepository).save(shipment);
        verify(transportLoadRepository).save(load);
    }

    @Test
    void shouldDispatchLoadAndGenerateManifest() {
        OutboundOrder order = new OutboundOrder();
        order.setId(3);

        Shipment shipment = new Shipment();
        shipment.setId(2);
        shipment.setOutboundOrder(order);
        shipment.setStatus(ShipmentStatus.PACKING);

        TransportLoad load = new TransportLoad();
        load.setId(1);
        load.setStatus(LoadStatus.LOADING);
        load.setShipments(List.of(shipment));

        when(transportLoadRepository.findById(1)).thenReturn(Optional.of(load));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transportLoadRepository.save(any(TransportLoad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(manifestRepository.save(any(Manifest.class))).thenAnswer(invocation -> {
            Manifest manifest = invocation.getArgument(0);
            manifest.setId(5);
            return manifest;
        });

        Manifest manifest = shippingService.dispatchLoad(1);

        assertThat(load.getStatus()).isEqualTo(LoadStatus.DISPATCHED);
        assertThat(manifest.getId()).isEqualTo(5);
        assertThat(manifest.getTotalParcels()).isEqualTo(0);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(shipment.getShippedAt()).isNotNull();
        verify(outboundOrderRepository).save(order);
    }

    @Test
    void shouldFailAssigningToDispatchedLoad() {
        TransportLoad load = new TransportLoad();
        load.setId(1);
        load.setStatus(LoadStatus.DISPATCHED);
        when(transportLoadRepository.findById(1)).thenReturn(Optional.of(load));

        assertThrows(RuntimeException.class, () -> shippingService.assignShipmentToLoad(1, 2));
    }
}
