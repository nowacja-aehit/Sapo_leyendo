import axios from 'axios';

const API_URL = '/api/shipping';

export interface Carrier {
    id: number;
    name: string;
    serviceType?: string;
    integrationType?: string;
    cutoffTime?: string;
    trackingUrlTemplate?: string;
}

export interface Shipment {
    id?: number;
    outboundOrderId?: number;
    carrierId?: number;
    trackingNumber?: string;
    shippedAt?: string;
    status?: string;
    totalWeightKg?: number;
}

export interface TransportLoad {
    id?: number;
    loadNumber?: string;
    carrierId?: number;
    trailerNumber?: string;
    driverName?: string;
    driverPhone?: string;
    sealNumber?: string;
    status?: string;
    scheduledDeparture?: string;
    actualDeparture?: string;
}

// ===== CARRIERS =====

export const fetchCarriers = async (): Promise<Carrier[]> => {
    const response = await axios.get(`${API_URL}/carriers`);
    return response.data;
};

export const fetchCarrierById = async (id: number): Promise<Carrier> => {
    const response = await axios.get(`${API_URL}/carriers/${id}`);
    return response.data;
};

// ===== SHIPMENTS =====

export const fetchShipments = async (): Promise<Shipment[]> => {
    const response = await axios.get(`${API_URL}/shipments`);
    return response.data;
};

export const fetchShipmentById = async (id: number): Promise<Shipment> => {
    const response = await axios.get(`${API_URL}/shipments/${id}`);
    return response.data;
};

export const createShipment = async (outboundOrderId: number, carrierId: number, trackingNumber?: string): Promise<Shipment> => {
    const response = await axios.post(`${API_URL}/shipments`, null, {
        params: { outboundOrderId, carrierId, trackingNumber }
    });
    return response.data;
};

export const updateShipment = async (id: number, shipment: Partial<Shipment>): Promise<Shipment> => {
    const response = await axios.put(`${API_URL}/shipments/${id}`, shipment);
    return response.data;
};

// ===== LOADS =====

export const fetchLoads = async (): Promise<TransportLoad[]> => {
    const response = await axios.get(`${API_URL}/loads`);
    return response.data;
};

export const createLoad = async (
    carrierId: number,
    trailerNumber?: string,
    driverName?: string,
    driverPhone?: string
): Promise<TransportLoad> => {
    const response = await axios.post(`${API_URL}/loads`, null, {
        params: { carrierId, trailerNumber, driverName, driverPhone }
    });
    return response.data;
};

export const assignShipmentToLoad = async (loadId: number, shipmentId: number): Promise<void> => {
    await axios.post(`${API_URL}/loads/${loadId}/assign/${shipmentId}`);
};

export const dispatchLoad = async (loadId: number): Promise<any> => {
    const response = await axios.post(`${API_URL}/loads/${loadId}/dispatch`);
    return response.data;
};
