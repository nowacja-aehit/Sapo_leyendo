import axios from 'axios';

const API_URL = '/api/outbound';

export interface OutboundOrderItem {
    id?: number;
    product?: { id: number };
    productName?: string;
    sku?: string;
    quantityOrdered: number;
    quantityPicked?: number;
    quantityShipped?: number;
    unitPrice?: number;
    lineTotal?: number;
    status?: string;
    locationCode?: string;
}

export interface OutboundOrder {
    id?: number;
    referenceNumber: string;
    status: string;
    shipDate?: string;
    destination?: string;
    customerName?: string;
    priority?: string;
    totalAmount?: number;
    itemsCount?: number;
    orderDate?: string;
    items?: OutboundOrderItem[];
    // Frontend computed properties
    orderNumber?: string;
    customer?: string;
    total?: number;
}

// ===== ORDERS =====

export const fetchOutboundOrders = async (): Promise<OutboundOrder[]> => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const fetchOutboundOrderById = async (id: number): Promise<OutboundOrder> => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
};

export const createOutboundOrder = async (order: Omit<OutboundOrder, 'id'>): Promise<OutboundOrder> => {
    const response = await axios.post(API_URL, order);
    return response.data;
};

export const updateOutboundOrder = async (id: number, order: OutboundOrder): Promise<OutboundOrder> => {
    const response = await axios.put(`${API_URL}/${id}`, order);
    return response.data;
};

export const deleteOutboundOrder = async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/${id}`);
};

// ===== ORDER ITEMS =====

export const fetchOrderItems = async (orderId: number): Promise<OutboundOrderItem[]> => {
    const response = await axios.get(`${API_URL}/${orderId}/items`);
    return response.data;
};

export const addItemToOrder = async (orderId: number, item: OutboundOrderItem): Promise<OutboundOrderItem> => {
    const response = await axios.post(`${API_URL}/${orderId}/items`, item);
    return response.data;
};

export const updateOrderItem = async (orderId: number, itemId: number, item: OutboundOrderItem): Promise<OutboundOrderItem> => {
    const response = await axios.put(`${API_URL}/${orderId}/items/${itemId}`, item);
    return response.data;
};

export const removeItemFromOrder = async (orderId: number, itemId: number): Promise<void> => {
    await axios.delete(`${API_URL}/${orderId}/items/${itemId}`);
};
