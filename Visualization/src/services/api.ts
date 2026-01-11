import axios from 'axios';
import { InventoryItem, Order, Shipment, inventoryItems, orders, shipments } from '../data/mockData';

// Ensure cookies are sent with every request
axios.defaults.withCredentials = true;

const API_URL = '/api';
const AUTH_URL = '/api/auth';

export const login = async (email: string, password: string): Promise<any> => {
    const response = await axios.post(`${AUTH_URL}/login`, { email, password });
    return response.data;
};

export const fetchInventory = async (): Promise<InventoryItem[]> => {
    try {
        const response = await axios.get(`${API_URL}/dashboard/inventory`);
        // Map backend response to frontend InventoryItem interface
        return response.data.map((item: any) => ({
            id: item.id?.toString() || '',
            name: item.name || '',
            sku: item.sku || '',
            category: item.category || '',
            quantity: item.quantity || 0,
            reorderLevel: item.reorderLevel || 0,
            location: item.location || '',
            status: item.status || 'In Stock',
            price: item.price || 0,
            lastUpdated: item.lastUpdated || new Date().toISOString().split('T')[0]
        }));
    } catch (error) {
        console.error("Failed to fetch inventory", error);
        return inventoryItems; // Fallback to mock data when API is unavailable
    }
};

export const createInventoryItem = async (item: Omit<InventoryItem, 'id'>): Promise<InventoryItem> => {
    // Map frontend item to backend format
    const backendPayload = {
        productId: parseInt(item.sku) || 1, // TODO: Find product by SKU
        locationId: 7, // TODO: Find location by name
        quantity: item.quantity,
        status: item.status === 'In Stock' ? 'AVAILABLE' : 'UNAVAILABLE'
    };
    const response = await axios.post(`${API_URL}/dashboard/inventory`, backendPayload);
    return response.data;
};

export const updateInventoryItem = async (id: string, item: InventoryItem): Promise<InventoryItem> => {
    // Map frontend item to backend format - send only updatable fields
    const backendPayload: Record<string, any> = {
        quantity: item.quantity,
        ...(item.status && { status: item.status === 'In Stock' ? 'AVAILABLE' : 'UNAVAILABLE' }),
        ...(item.price !== undefined && { price: item.price })
    };
    // Add locationId if location string provided (need to resolve by name)
    if (item.location) {
        backendPayload.locationCode = item.location;
    }
    const response = await axios.put(`${API_URL}/dashboard/inventory/${id}`, backendPayload);
    return response.data;
};

export const deleteInventoryItem = async (id: string): Promise<void> => {
    await axios.delete(`${API_URL}/dashboard/inventory/${id}`);
};

export const fetchOrders = async (): Promise<Order[]> => {
    try {
        const response = await axios.get(`${API_URL}/dashboard/orders`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch orders", error);
        return orders; // Fallback to mock data when API is unavailable
    }
};

export const fetchShipments = async (): Promise<Shipment[]> => {
    try {
        const response = await axios.get(`${API_URL}/dashboard/shipments`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch shipments", error);
        return shipments; // Fallback to mock data when API is unavailable
    }
};

export const createOrder = async (order: Order): Promise<Order> => {
    try {
        const response = await axios.post(`${API_URL}/dashboard/orders`, order);
        return response.data;
    } catch (error) {
        console.error("Failed to create order, using local state only", error);
        return order; // Let caller optimistically add to UI when API is blocked
    }
};
