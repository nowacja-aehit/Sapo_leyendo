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
    const response = await axios.post(`${API_URL}/shipments/start/${outboundOrderId}`);
    return response.data;
};

export const createParcel = async (shipmentId: number, packingMaterialId: number): Promise<Parcel> => {
    const response = await axios.post(`${API_URL}/shipments/${shipmentId}/parcels`, null, {
        params: { packingMaterialId }
    });
    return response.data;
};

export const addItemToParcel = async (parcelId: number, productId: number, quantity: number): Promise<Parcel> => {
    const response = await axios.post(`${API_URL}/parcels/${parcelId}/items`, null, {
        params: { productId, quantity }
    });
    return response.data;
};

export const closeShipment = async (shipmentId: number): Promise<Shipment> => {
    const response = await axios.post(`${API_URL}/shipments/${shipmentId}/close`);
    return response.data;
};
