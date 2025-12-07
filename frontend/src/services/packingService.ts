import api from './api';

export interface Shipment {
    id: number;
    outboundOrderId: number;
    status: string;
    trackingNumber: string;
}

export interface Parcel {
    id: number;
    shipmentId: number;
    packingMaterialId: number;
    weight: number;
    trackingNumber: string;
}

export const packingService = {
    startPacking: async (outboundOrderId: number) => {
        const response = await api.post<Shipment>(`/packing/shipments/start/${outboundOrderId}`);
        return response.data;
    },

    createParcel: async (shipmentId: number, packingMaterialId: number) => {
        const response = await api.post<Parcel>(`/packing/shipments/${shipmentId}/parcels`, null, {
            params: { packingMaterialId }
        });
        return response.data;
    },

    addItemToParcel: async (parcelId: number, productId: number, quantity: number) => {
        const response = await api.post<Parcel>(`/packing/parcels/${parcelId}/items`, null, {
            params: { productId, quantity }
        });
        return response.data;
    },

    closeShipment: async (shipmentId: number) => {
        const response = await api.post<Shipment>(`/packing/shipments/${shipmentId}/close`);
        return response.data;
    }
};
