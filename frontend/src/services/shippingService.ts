import api from './api';

export interface TransportLoad {
    id: number;
    carrierId: number;
    vehiclePlateNumber: string;
    driverName: string;
    driverPhone: string;
    dockId: number;
    status: string;
}

export interface Manifest {
    id: number;
    loadId: number;
    generatedDate: string;
    content: string;
}

export const shippingService = {
    createLoad: async (carrierId: number, vehiclePlateNumber: string, driverName: string, driverPhone: string, dockId: number) => {
        const response = await api.post<TransportLoad>('/shipping/loads', null, {
            params: { carrierId, vehiclePlateNumber, driverName, driverPhone, dockId }
        });
        return response.data;
    },

    assignShipmentToLoad: async (loadId: number, shipmentId: number) => {
        await api.post(`/shipping/loads/${loadId}/assign/${shipmentId}`);
    },

    dispatchLoad: async (loadId: number) => {
        const response = await api.post<Manifest>(`/shipping/loads/${loadId}/dispatch`);
        return response.data;
    }
};
