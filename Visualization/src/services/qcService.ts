import axios from 'axios';

const API_URL = '/api/qc';

// Valid sourceType values: 'INBOUND', 'RETURN', 'INVENTORY', 'PRODUCTION'
// Valid result values: 'PENDING', 'PASSED', 'FAILED', 'CONDITIONAL'

export interface QcInspection {
    id: number;
    productId: number;
    sourceType: string; // INBOUND | RETURN | INVENTORY | PRODUCTION
    referenceId: number;
    status: string;
    result?: string; // PENDING | PASSED | FAILED | CONDITIONAL
    sampleSize: number;
}

export interface NonConformanceReport {
    id: number;
    inspectionId: number;
    defectType: string;
    description: string;
    status: string;
}

export const createInspection = async (productId: number, sourceType: string, referenceId: number, sampleSize: number): Promise<QcInspection> => {
    const response = await axios.post(`${API_URL}/inspections`, null, {
        params: { productId, sourceType: sourceType.toUpperCase(), referenceId, sampleSize }
    });
    return response.data;
};

export const executeInspection = async (inspectionId: number, result: string, inspectorId: number): Promise<QcInspection> => {
    const response = await axios.post(`${API_URL}/inspections/${inspectionId}/execute`, null, {
        params: { result: result.toUpperCase(), inspectorId }
    });
    return response.data;
};

export const createNcr = async (inspectionId: number, defectType: string, description: string): Promise<NonConformanceReport> => {
    const response = await axios.post(`${API_URL}/inspections/${inspectionId}/ncr`, [], {
        params: { defectType, description }
    });
    return response.data;
};
