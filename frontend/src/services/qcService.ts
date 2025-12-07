import api from './api';

export interface QcInspection {
    id: number;
    productId: number;
    sourceType: string;
    referenceId: number;
    sampleSize: number;
    result: string;
    inspectorId: number;
    inspectionDate: string;
}

export interface NonConformanceReport {
    id: number;
    inspectionId: number;
    defectType: string;
    description: string;
    photos: string; // JSON string
}

export const qcService = {
    createInspection: async (productId: number, sourceType: string, referenceId: number, sampleSize: number) => {
        const response = await api.post<QcInspection>('/qc/inspections', null, {
            params: { productId, sourceType, referenceId, sampleSize }
        });
        return response.data;
    },

    executeInspection: async (inspectionId: number, result: string, inspectorId: number) => {
        const response = await api.post<QcInspection>(`/qc/inspections/${inspectionId}/execute`, null, {
            params: { result, inspectorId }
        });
        return response.data;
    },

    createNcr: async (inspectionId: number, defectType: string, description: string, photos: string[]) => {
        const response = await api.post<NonConformanceReport>(`/qc/inspections/${inspectionId}/ncr`, photos, {
            params: { defectType, description }
        });
        return response.data;
    }
};
