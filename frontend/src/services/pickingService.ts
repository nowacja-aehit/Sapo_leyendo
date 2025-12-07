import api from './api';

export interface PickingTask {
    id: string;
    waveId: string;
    outboundOrderId: number;
    productId: number;
    sourceLocationId: number;
    targetLpn: string;
    quantityToPick: number;
    quantityPicked: number;
    status: 'OPEN' | 'PICKED';
}

export interface Wave {
    id: string;
    status: string;
    createdDate: string;
}

export const pickingService = {
    createWave: async (outboundOrderIds: number[]) => {
        const response = await api.post<Wave>('/picking/waves', outboundOrderIds);
        return response.data;
    },

    runWave: async (waveId: string, outboundOrderIds: number[]) => {
        const response = await api.post<Wave>(`/picking/waves/${waveId}/run`, outboundOrderIds);
        return response.data;
    },

    getPickingTasks: async (waveId: string) => {
        const response = await api.get<PickingTask[]>(`/picking/waves/${waveId}/tasks`);
        return response.data;
    },

    confirmTask: async (taskId: string, quantityPicked: number) => {
        await api.post(`/picking/tasks/${taskId}/confirm`, null, {
            params: { quantityPicked }
        });
    }
};
