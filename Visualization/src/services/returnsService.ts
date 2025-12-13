import axios from 'axios';

const API_URL = '/api/returns';

export interface RmaRequest {
    id: number;
    outboundOrderId: number;
    reason: string;
    status: string;
    trackingNumber?: string;
    items?: ReturnItem[];
}

export interface ReturnItem {
    id: number;
    productId: number;
    quantity: number;
    gradingStatus?: string;
    comment?: string;
}

export const createRmaRequest = async (outboundOrderId: number, reason: string): Promise<RmaRequest> => {
    const response = await axios.post(`${API_URL}/rma`, null, {
        params: { outboundOrderId, reason }
    });
    return response.data;
};

export const receiveReturn = async (rmaId: number, trackingNumber: string): Promise<RmaRequest> => {
    const response = await axios.post(`${API_URL}/receive/${rmaId}`, null, {
        params: { trackingNumber }
    });
    return response.data;
};

export const gradeItem = async (rmaId: number, productId: number, grade: string, comment?: string): Promise<ReturnItem> => {
    const response = await axios.post(`${API_URL}/grade/${rmaId}`, null, {
        params: { productId, grade, comment }
    });
    return response.data;
};
