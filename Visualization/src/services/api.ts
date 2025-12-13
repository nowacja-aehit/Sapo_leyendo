import axios from 'axios';
import { InventoryItem, Order, Shipment } from '../data/mockData';

// Ensure cookies are sent with every request
axios.defaults.withCredentials = true;

const API_URL = '/api/dashboard';
const AUTH_URL = '/api/auth';

export const login = async (email: string, password: string): Promise<any> => {
    const response = await axios.post(`${AUTH_URL}/login`, { email, password });
    return response.data;
};

export const fetchInventory = async (): Promise<InventoryItem[]> => {
    try {
        const response = await axios.get(`${API_URL}/inventory`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch inventory", error);
        return [];
    }
};

export const createInventoryItem = async (item: Omit<InventoryItem, 'id'>): Promise<InventoryItem> => {
    const response = await axios.post(`${API_URL}/inventory`, item);
    return response.data;
};

export const updateInventoryItem = async (id: string, item: InventoryItem): Promise<InventoryItem> => {
    const response = await axios.put(`${API_URL}/inventory/${id}`, item);
    return response.data;
};

export const deleteInventoryItem = async (id: string): Promise<void> => {
    await axios.delete(`${API_URL}/inventory/${id}`);
};

export const fetchOrders = async (): Promise<Order[]> => {
    try {
        const response = await axios.get(`${API_URL}/orders`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch orders", error);
        return [];
    }
};

export const fetchShipments = async (): Promise<Shipment[]> => {
    try {
        const response = await axios.get(`${API_URL}/shipments`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch shipments", error);
        return [];
    }
};
