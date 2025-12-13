import axios from 'axios';

const API_URL = '/api/locations';

const mockZones: Zone[] = [
    { id: 1, name: 'STREFA-A', isSecure: false, isTemperatureControlled: false },
    { id: 2, name: 'STREFA-B', isSecure: false, isTemperatureControlled: false },
    { id: 3, name: 'STREFA-C', isSecure: false, isTemperatureControlled: false },
    { id: 4, name: 'STREFA-CHŁODNIA', isSecure: false, isTemperatureControlled: true },
];

const mockTypes: LocationType[] = [
    { id: 1, name: 'PALLET' },
    { id: 2, name: 'SHELF' },
];

const mockLocations: Location[] = [
    { id: 101, name: 'A-12-3', zone: mockZones[0], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'A123' },
    { id: 102, name: 'A-15-2', zone: mockZones[0], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'A152' },
    { id: 103, name: 'B-03-1', zone: mockZones[1], locationType: mockTypes[1], status: 'ACTIVE', barcode: 'B031' },
    { id: 104, name: 'C-08-4', zone: mockZones[2], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'C084' },
    { id: 105, name: 'A-18-1', zone: mockZones[0], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'A181' },
    { id: 106, name: 'A-18-2', zone: mockZones[0], locationType: mockTypes[1], status: 'ACTIVE', barcode: 'A182' },
    { id: 107, name: 'B-05-3', zone: mockZones[1], locationType: mockTypes[1], status: 'ACTIVE', barcode: 'B053' },
    { id: 108, name: 'A-20-1', zone: mockZones[0], locationType: mockTypes[0], status: 'ACTIVE', barcode: 'A201' },
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

export const deleteLocation = async (id: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/${id}`);
    } catch (error) {
        console.error("Failed to delete location, removing locally", error);
        // No-op: caller should optimistically remove from UI
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
