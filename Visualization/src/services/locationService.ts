import axios from 'axios';

const API_URL = '/api/locations';

const mockZones: Zone[] = [
    { id: 1, name: 'STREFA-A', isSecure: false, isTemperatureControlled: false },
    { id: 2, name: 'STREFA-CHŁODNIA', isSecure: false, isTemperatureControlled: true },
];

const mockTypes: LocationType[] = [
    { id: 1, name: 'PALLET' },
    { id: 2, name: 'SHELF' },
];

const mockLocations: Location[] = [
    { id: 101, name: 'A-01-01', zone: mockZones[0], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'A0101' },
    { id: 102, name: 'A-01-02', zone: mockZones[0], locationType: mockTypes[1], status: 'ACTIVE', barcode: 'A0102' },
];

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
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error("Failed to load locations", error);
        return mockLocations;
    }
};

export const createLocation = async (location: Partial<Location>): Promise<Location> => {
    try {
        const response = await axios.post(API_URL, location);
        return response.data;
    } catch (error) {
        console.error("Failed to create location, using local state only", error);
        return {
            id: Date.now(),
            name: location.name ?? 'LOKALIZACJA-LOCAL',
            zone: location.zone,
            locationType: location.locationType,
            status: location.status ?? 'ACTIVE',
            barcode: location.barcode,
        } as Location;
    }
};

export const getAllZones = async (): Promise<Zone[]> => {
    try {
        const response = await axios.get(`${API_URL}/zones`);
        return response.data;
    } catch (error) {
        console.error("Failed to load zones", error);
        return mockZones;
    }
};

export const getAllLocationTypes = async (): Promise<LocationType[]> => {
    try {
        const response = await axios.get(`${API_URL}/types`);
        return response.data;
    } catch (error) {
        console.error("Failed to load location types", error);
        return mockTypes;
    }
};
