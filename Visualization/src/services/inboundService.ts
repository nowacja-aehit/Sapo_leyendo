import axios from 'axios';

const API_URL = '/api/inbound';

export interface InboundOrder {
    id: number;
    orderReference: string;
    expectedArrival: string;
    status: string;
    supplier: string;
    items: InboundOrderItem[];
}

export interface InboundOrderItem {
    id: number;
    productName: string;
    sku: string;
    expectedQuantity: number;
    receivedQuantity: number;
}

export interface ReceiveItemRequest {
    inboundOrderItemId: number;
    lpn: string;
    quantity: number;
    operatorId: number;
    damageCode?: string;
}

export const fetchInboundOrders = async (): Promise<InboundOrder[]> => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const receiveItem = async (request: ReceiveItemRequest): Promise<any> => {
    const response = await axios.post(`${API_URL}/receive`, request);
    return response.data;
};

export const generateLpn = async (): Promise<string> => {
    const response = await axios.get(`${API_URL}/lpn/generate`);
    return response.data;
};
