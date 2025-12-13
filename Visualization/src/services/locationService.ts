import axios from 'axios';

const API_URL = '/api/locations';

export interface Location {
    id: number;
    name: string;
    zone?: Zone;
    locationType?: LocationType;
    barcode?: string;
    status: string;
}

export interface Zone {
    id: number;
    name: string;
    isTemperatureControlled: boolean;
    isSecure: boolean;
}

export interface LocationType {
    id: number;
    name: string;
    maxWeight?: number;
}

export const getAllLocations = async (): Promise<Location[]> => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const createLocation = async (location: Partial<Location>): Promise<Location> => {
    const response = await axios.post(API_URL, location);
    return response.data;
};

export const getAllZones = async (): Promise<Zone[]> => {
    const response = await axios.get(`${API_URL}/zones`);
    return response.data;
};

export const getAllLocationTypes = async (): Promise<LocationType[]> => {
    const response = await axios.get(`${API_URL}/types`);
    return response.data;
};
