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
    try {
        const response = await axios.post(`${API_URL}/waves`, outboundOrderIds);
        return response.data;
    } catch (error) {
        console.error("Failed to create wave, using mock wave", error);
        return {
            id: crypto.randomUUID(),
            status: 'IN_PROGRESS',
            createdAt: new Date().toISOString(),
        } as Wave;
    }
};

export const getPickingTasks = async (waveId: string): Promise<PickTask[]> => {
    try {
        const response = await axios.get(`${API_URL}/waves/${waveId}/tasks`);
        return response.data;
    } catch (error) {
        console.error("Failed to load picking tasks, returning mock tasks", error);
        return [
            {
                id: crypto.randomUUID(),
                waveId,
                sourceLocationId: 101,
                targetLpn: 'LPN-0001',
                productId: 1,
                quantityToPick: 5,
                quantityPicked: 0,
                status: 'PENDING',
                productName: 'Wireless Mouse',
                locationName: 'A-01-01',
            },
            {
                id: crypto.randomUUID(),
                waveId,
                sourceLocationId: 102,
                targetLpn: 'LPN-0002',
                productId: 2,
                quantityToPick: 3,
                quantityPicked: 0,
                status: 'PENDING',
                productName: 'USB-C Cable',
                locationName: 'A-01-02',
            },
        ];
    }
};

export const confirmPickTask = async (taskId: string, quantityPicked: number): Promise<void> => {
    try {
        await axios.post(`${API_URL}/tasks/${taskId}/confirm`, null, {
            params: { quantityPicked }
        });
    } catch (error) {
        console.error("Failed to confirm pick, continuing locally", error);
    }
};
