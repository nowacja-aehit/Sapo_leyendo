import { useState, useEffect } from "react";
import { Package, ShoppingCart, Truck, TrendingUp, AlertTriangle, CheckCircle } from "lucide-react";
import { Card } from "../ui/card";
import { Button } from "../ui/button";
import { ViewType } from "../Dashboard";
import { InventoryItem, Order, Shipment } from "../../data/mockData";
import { fetchInventory, fetchOrders, fetchShipments } from "../../services/api";

interface DashboardOverviewProps {
  onNavigate: (view: ViewType) => void;
}

export function DashboardOverview({ onNavigate }: DashboardOverviewProps) {
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [shipments, setShipments] = useState<Shipment[]>([]);

  useEffect(() => {
    const loadData = async () => {
      const [invData, ordersData, shipmentsData] = await Promise.all([
        fetchInventory(),
        fetchOrders(),
        fetchShipments()
      ]);
      setInventoryItems(invData);
      setOrders(ordersData);
      setShipments(shipmentsData);
    };
    loadData();
  }, []);

  const totalItems = inventoryItems.reduce((sum, item) => sum + item.quantity, 0);
  const lowStockItems = inventoryItems.filter(item => item.status === "Low Stock" || item.status === "Out of Stock").length;
  const activeOrders = orders.filter(order => order.status === "Processing" || order.status === "Pending").length;
  const inTransitShipments = shipments.filter(ship => ship.status === "In Transit" || ship.status === "Out for Delivery").length;

  const stats = [
    {
      icon: Package,
      label: "Całkowite zapasy",
      value: totalItems.toLocaleString(),
      change: "+12%",
      positive: true,
      color: "blue",
      view: "inventory" as ViewType
    },
    {
      icon: ShoppingCart,
      label: "Aktywne zamówienia",
      value: activeOrders.toString(),
      change: "+8%",
      positive: true,
      color: "green",
      view: "orders" as ViewType
    },
    {
      icon: Truck,
      label: "Przesyłki w tranzycie",
      value: inTransitShipments.toString(),
      change: "-3%",
      positive: false,
      color: "purple",
      view: "shipments" as ViewType
    },
    {
      icon: AlertTriangle,
      label: "Alerty niskiego stanu",
      value: lowStockItems.toString(),
      change: "Wymaga uwagi",
      positive: false,
      color: "orange",
      view: "inventory" as ViewType
    }
  ];

  const recentActivity = [
    { icon: CheckCircle, text: "Zamówienie ORD-2025-1234 wysłane pomyślnie", time: "5 min temu", color: "green" },
    { icon: AlertTriangle, text: "Alert niskiego stanu: Kabel ładujący USB-C", time: "12 min temu", color: "orange" },
    { icon: Package, text: "156 produktów przyjętych do magazynu", time: "1 godzinę temu", color: "blue" },
    { icon: TrendingUp, text: "AI przewiduje 15% wzrost popytu", time: "2 godziny temu", color: "purple" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-gray-900 mb-2">Przegląd panelu</h1>
        <p className="text-gray-600">Witaj ponownie! Oto co dzieje się w Twoim magazynie.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <Card 
              key={stat.label}
              className="p-6 hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => onNavigate(stat.view)}
            >
              <div className="flex items-start justify-between mb-4">
                <div className={`w-12 h-12 bg-${stat.color}-100 rounded-lg flex items-center justify-center`}>
                  <Icon className={`text-${stat.color}-600`} size={24} />
                </div>
                <span className={`text-sm ${stat.positive ? 'text-green-600' : 'text-orange-600'}`}>
                  {stat.change}
                </span>
              </div>
              <div className="text-gray-900 mb-1">{stat.value}</div>
              <div className="text-sm text-gray-600">{stat.label}</div>
            </Card>
          );
        })}
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Recent Activity */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Ostatnia aktywność</h2>
          <div className="space-y-4">
            {recentActivity.map((activity, index) => {
              const Icon = activity.icon;
              return (
                <div key={index} className="flex items-start gap-3">
                  <div className={`w-8 h-8 bg-${activity.color}-100 rounded-lg flex items-center justify-center flex-shrink-0`}>
                    <Icon className={`text-${activity.color}-600`} size={16} />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-gray-900">{activity.text}</p>
                    <p className="text-xs text-gray-500">{activity.time}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </Card>

        {/* Quick Actions */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Szybkie akcje</h2>
          <div className="grid grid-cols-2 gap-3">
            <Button 
              variant="outline" 
              className="h-auto py-4 flex-col gap-2"
              onClick={() => onNavigate("inventory")}
            >
              <Package size={24} />
              <span className="text-sm">Zobacz zapasy</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-auto py-4 flex-col gap-2"
              onClick={() => onNavigate("orders")}
            >
              <ShoppingCart size={24} />
              <span className="text-sm">Zarządzaj zamówieniami</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-auto py-4 flex-col gap-2"
              onClick={() => onNavigate("shipments")}
            >
              <Truck size={24} />
              <span className="text-sm">Śledź przesyłki</span>
            </Button>
            <Button 
              variant="outline" 
              className="h-auto py-4 flex-col gap-2"
              onClick={() => onNavigate("ai-insights")}
            >
              <TrendingUp size={24} />
              <span className="text-sm">Wglądy AI</span>
            </Button>
          </div>
        </Card>
      </div>

      {/* Low Stock Alerts */}
      {lowStockItems > 0 && (
        <Card className="p-6 border-orange-200 bg-orange-50">
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-3">
              <AlertTriangle className="text-orange-600 mt-1" size={24} />
              <div>
                <h3 className="text-gray-900 mb-1">Alert niskiego stanu zapasów</h3>
                <p className="text-sm text-gray-600">
                  Masz {lowStockItems} produktów, które wymagają uzupełnienia. Przejrzyj swoje zapasy, aby zapobiec brakom.
                </p>
              </div>
            </div>
            <Button onClick={() => onNavigate("inventory")}>
              Zobacz produkty
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
}