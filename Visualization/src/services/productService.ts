import axios from 'axios';

const API_URL = '/api/products';

export interface Product {
    id: number;
    name: string;
    sku: string;
    description?: string;
    category?: string;
    unitPrice?: number;
    weight?: number;
    length?: number;
    width?: number;
    height?: number;
    barcode?: string;
    active?: boolean;
}

export const fetchProducts = async (): Promise<Product[]> => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const fetchProductById = async (id: number): Promise<Product> => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
};

export const createProduct = async (product: Omit<Product, 'id'>): Promise<Product> => {
    // Map to ProductCreateRequest format expected by backend
    const payload = {
        sku: product.sku,
        name: product.name,
        description: product.description,
        category: product.category,
        minStock: product.weight ? Math.floor(product.weight) : 0, // Temporary mapping
    };
    const response = await axios.post(API_URL, payload);
    return response.data;
};

export const updateProduct = async (id: number, product: Product): Promise<Product> => {
    const response = await axios.put(`${API_URL}/${id}`, product);
    return response.data;
};

export const deleteProduct = async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/${id}`);
};
