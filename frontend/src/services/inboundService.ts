import api from './api';

export interface InboundOrder {
    id: number;
    supplierId: number;
    orderDate: string;
    status: string;
    expectedDeliveryDate: string;
    dockId: number;
    items: InboundOrderItem[];
}

export interface InboundOrderItem {
    id: number;
    productId: number;
    quantityOrdered: number;
    quantityReceived: number;
    batchNumber: string;
}

export interface Receipt {
    id: number;
    inboundOrderItemId: number;
    lpn: string;
    quantity: number;
    operatorId: number;
    timestamp: string;
    damageCode: string;
}

export interface DockAppointment {
    id: number;
    inboundOrderId: number;
    dockId: number;
    startTime: string;
    endTime: string;
    carrierName: string;
}

export const inboundService = {
    getAllInboundOrders: async () => {
        const response = await api.get<InboundOrder[]>('/inbound');
        return response.data;
    },

    getInboundOrderById: async (id: number) => {
        const response = await api.get<InboundOrder>(`/inbound/${id}`);
        return response.data;
    },

    createInboundOrder: async (order: Partial<InboundOrder>) => {
        const response = await api.post<InboundOrder>('/inbound', order);
        return response.data;
    },

    receiveItem: async (inboundOrderItemId: number, lpn: string, quantity: number, operatorId: number, damageCode?: string) => {
        const response = await api.post<Receipt>('/inbound/receive', {
            inboundOrderItemId,
            lpn,
            quantity,
            operatorId,
            damageCode
        });
        return response.data;
    },

    scheduleDock: async (inboundOrderId: number, dockId: number, startTime: string, endTime: string, carrierName: string) => {
        const response = await api.post<DockAppointment>('/inbound/appointments', null, {
            params: { inboundOrderId, dockId, startTime, endTime, carrierName }
        });
        return response.data;
    },

    generateLpn: async () => {
        const response = await api.get<string>('/inbound/lpn/generate');
        return response.data;
    }
};
