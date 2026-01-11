import axios from 'axios';

const API_URL = '/api/returns';

// Valid grade values: 'GRADE_A', 'GRADE_B', 'GRADE_C', 'SCRAP'

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
    gradingStatus?: string; // GRADE_A | GRADE_B | GRADE_C | SCRAP
    comment?: string;
}

export const fetchRmaRequests = async (): Promise<RmaRequest[]> => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const fetchRmaRequestById = async (id: number): Promise<RmaRequest> => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
};

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
        params: { productId, grade: grade.toUpperCase(), comment }
    });
    return response.data;
};
