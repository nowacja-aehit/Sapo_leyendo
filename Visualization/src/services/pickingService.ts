import axios from 'axios';

const API_URL = '/api/picking';

export interface Wave {
    id: string; // UUID
    status: string;
    createdAt: string;
}

export interface PickTask {
    id: string; // UUID
    waveId: string;
    sourceLocationId: number;
    targetLpn: string;
    productId: number;
    quantityToPick: number;
    quantityPicked: number;
    status: string;
    productName?: string; // Optional, might need to be fetched separately or joined in backend
    locationName?: string; // Optional
}

export const createWave = async (outboundOrderIds: number[]): Promise<Wave> => {
    const response = await axios.post(`${API_URL}/waves`, outboundOrderIds);
    return response.data;
};

export const getPickingTasks = async (waveId: string): Promise<PickTask[]> => {
    const response = await axios.get(`${API_URL}/waves/${waveId}/tasks`);
    return response.data;
};

export const confirmPickTask = async (taskId: string, quantityPicked: number): Promise<void> => {
    await axios.post(`${API_URL}/tasks/${taskId}/confirm`, null, {
        params: { quantityPicked }
    });
};
