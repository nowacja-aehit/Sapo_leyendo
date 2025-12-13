export interface InventoryItem {
  id: string;
  name: string;
  sku: string;
  category: string;
  quantity: number;
  reorderLevel: number;
  location: string;
  status: "In Stock" | "Low Stock" | "Out of Stock";
  price: number;
  lastUpdated: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  customer: string;
  items: number;
  total: number;
  status: "Pending" | "Processing" | "Shipped" | "Delivered" | "Cancelled";
  date: string;
  priority: "Low" | "Medium" | "High";
}

export interface Shipment {
  id: string;
  trackingNumber: string;
  destination: string;
  carrier: string;
  status: "Preparing" | "In Transit" | "Out for Delivery" | "Delivered";
  estimatedDelivery: string;
  items: number;
}

export const inventoryItems: InventoryItem[] = [
  {
    id: "1",
    name: "Wireless Bluetooth Headphones",
    sku: "WBH-001",
    category: "Electronics",
    quantity: 245,
    reorderLevel: 50,
    location: "A-12-3",
    status: "In Stock",
    price: 79.99,
    lastUpdated: "2025-11-23"
  },
  {
    id: "2",
    name: "Smart Watch Series 5",
    sku: "SWS-005",
    category: "Electronics",
    quantity: 32,
    reorderLevel: 40,
    location: "A-15-2",
    status: "Low Stock",
    price: 299.99,
    lastUpdated: "2025-11-23"
  },
  {
    id: "3",
    name: "USB-C Charging Cable",
    sku: "UCC-100",
    category: "Accessories",
    quantity: 0,
    reorderLevel: 100,
    location: "B-03-1",
    status: "Out of Stock",
    price: 12.99,
    lastUpdated: "2025-11-22"
  },
  {
    id: "4",
    name: "Laptop Backpack Pro",
    sku: "LBP-020",
    category: "Bags",
    quantity: 156,
    reorderLevel: 30,
    location: "C-08-4",
    status: "In Stock",
    price: 59.99,
    lastUpdated: "2025-11-23"
  },
  {
    id: "5",
    name: "Wireless Mouse",
    sku: "WM-045",
    category: "Electronics",
    quantity: 312,
    reorderLevel: 60,
    location: "A-18-1",
    status: "In Stock",
    price: 24.99,
    lastUpdated: "2025-11-23"
  },
  {
    id: "6",
    name: "Mechanical Keyboard RGB",
    sku: "MKR-088",
    category: "Electronics",
    quantity: 48,
    reorderLevel: 50,
    location: "A-18-2",
    status: "Low Stock",
    price: 129.99,
    lastUpdated: "2025-11-23"
  },
  {
    id: "7",
    name: "Phone Case Premium",
    sku: "PCP-150",
    category: "Accessories",
    quantity: 428,
    reorderLevel: 80,
    location: "B-05-3",
    status: "In Stock",
    price: 19.99,
    lastUpdated: "2025-11-22"
  },
  {
    id: "8",
    name: "Portable Power Bank 20000mAh",
    sku: "PPB-200",
    category: "Electronics",
    quantity: 89,
    reorderLevel: 40,
    location: "A-20-1",
    status: "In Stock",
    price: 39.99,
    lastUpdated: "2025-11-23"
  }
];

export const orders: Order[] = [
  {
    id: "1",
    orderNumber: "ORD-2025-1234",
    customer: "TechMart Inc.",
    items: 45,
    total: 3245.50,
    status: "Processing",
    date: "2025-11-23",
    priority: "High"
  },
  {
    id: "2",
    orderNumber: "ORD-2025-1235",
    customer: "Global Electronics",
    items: 120,
    total: 8950.00,
    status: "Shipped",
    date: "2025-11-22",
    priority: "Medium"
  },
  {
    id: "3",
    orderNumber: "ORD-2025-1236",
    customer: "Retail Solutions LLC",
    items: 28,
    total: 1567.80,
    status: "Pending",
    date: "2025-11-23",
    priority: "Low"
  },
  {
    id: "4",
    orderNumber: "ORD-2025-1237",
    customer: "QuickShop Online",
    items: 67,
    total: 4823.90,
    status: "Delivered",
    date: "2025-11-20",
    priority: "High"
  },
  {
    id: "5",
    orderNumber: "ORD-2025-1238",
    customer: "Metro Supplies",
    items: 92,
    total: 6234.20,
    status: "Processing",
    date: "2025-11-23",
    priority: "Medium"
  },
  {
    id: "6",
    orderNumber: "ORD-2025-1239",
    customer: "Digital World",
    items: 15,
    total: 945.75,
    status: "Pending",
    date: "2025-11-23",
    priority: "Low"
  }
];

export const shipments: Shipment[] = [
  {
    id: "1",
    trackingNumber: "TRK-SL-89234",
    destination: "New York, NY",
    carrier: "FedEx Express",
    status: "In Transit",
    estimatedDelivery: "2025-11-25",
    items: 45
  },
  {
    id: "2",
    trackingNumber: "TRK-SL-89235",
    destination: "Los Angeles, CA",
    carrier: "UPS Ground",
    status: "Preparing",
    estimatedDelivery: "2025-11-27",
    items: 28
  },
  {
    id: "3",
    trackingNumber: "TRK-SL-89236",
    destination: "Chicago, IL",
    carrier: "DHL Express",
    status: "Out for Delivery",
    estimatedDelivery: "2025-11-23",
    items: 67
  },
  {
    id: "4",
    trackingNumber: "TRK-SL-89237",
    destination: "Houston, TX",
    carrier: "USPS Priority",
    status: "Delivered",
    estimatedDelivery: "2025-11-22",
    items: 120
  }
];
