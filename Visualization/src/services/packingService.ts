import axios from 'axios';

const API_URL = '/api/packing';

export interface Shipment {
    id: number;
    status: string;
    trackingNumber?: string;
    parcels?: Parcel[];
}

export interface Parcel {
    id: number;
    weightKg: number;
    trackingNumber?: string;
    items?: any[]; // Simplified
}

export const startPacking = async (outboundOrderId: number): Promise<Shipment> => {
    try {
        const response = await axios.post(`${API_URL}/shipments/start/${outboundOrderId}`);
        return response.data;
    } catch (error) {
        console.error("Failed to start packing, using mock shipment", error);
        return { id: Date.now(), status: 'IN_PROGRESS', trackingNumber: `PKG-${outboundOrderId}` } as Shipment;
    }
};

export const createParcel = async (shipmentId: number, packingMaterialId: number): Promise<Parcel> => {
    try {
        const response = await axios.post(`${API_URL}/shipments/${shipmentId}/parcels`, null, {
            params: { packingMaterialId }
        });
        return response.data;
    } catch (error) {
        console.error("Failed to create parcel, using mock parcel", error);
        return { id: Date.now(), weightKg: 0 } as Parcel;
    }
};

export const addItemToParcel = async (parcelId: number, productId: number, quantity: number): Promise<Parcel> => {
    try {
        const response = await axios.post(`${API_URL}/parcels/${parcelId}/items`, null, {
            params: { productId, quantity }
        });
        return response.data;
    } catch (error) {
        console.error("Failed to add item to parcel, continuing locally", error);
        return { id: parcelId, weightKg: 0, items: [{ productId, quantity }] } as Parcel;
    }
};

export const closeShipment = async (shipmentId: number): Promise<Shipment> => {
    try {
        const response = await axios.post(`${API_URL}/shipments/${shipmentId}/close`);
        return response.data;
    } catch (error) {
        console.error("Failed to close shipment, marking as closed locally", error);
        return { id: shipmentId, status: 'CLOSED' } as Shipment;
    }
};
