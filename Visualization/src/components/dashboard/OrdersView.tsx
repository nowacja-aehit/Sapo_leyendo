import { useState, useEffect } from "react";
import { Search, Filter, Download, Eye, Package, User, Calendar, DollarSign, Plus, Trash2 } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { Order } from "../../data/mockData";
import { fetchOrders, createOrder } from "../../services/api";
import { fetchOutboundOrders, createOutboundOrder, addItemToOrder, fetchOrderItems, OutboundOrder, OutboundOrderItem } from "../../services/outboundService";
import { fetchProducts, Product } from "../../services/productService";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";

interface OrderItemDraft {
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
}

export function OrdersView() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [filterStatus, setFilterStatus] = useState("all");
  const [filterPriority, setFilterPriority] = useState("all");
  const [showFilters, setShowFilters] = useState(false);
  const [items, setItems] = useState<Order[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [orderItems, setOrderItems] = useState<OrderItemDraft[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<string>("");
  const [itemQuantity, setItemQuantity] = useState<number>(1);
  const [itemPrice, setItemPrice] = useState<number>(0);
  const [newOrder, setNewOrder] = useState<Order>({
    id: "",
    orderNumber: "",
    customer: "",
    items: 0,
    total: 0,
    status: "Pending",
    date: new Date().toISOString().split("T")[0],
    priority: "Medium",
  });

  useEffect(() => {
    const loadData = async () => {
      const data = await fetchOrders();
      setItems(data);
      
      // Load products for the dropdown
      try {
        const productData = await fetchProducts();
        setProducts(productData);
      } catch (err) {
        console.error("Failed to load products", err);
      }
    };
    loadData();
  }, []);

  const filteredOrders = items.filter(order => {
    const normalizedTerm = searchTerm.trim().toLowerCase();
    const orderNumber = (order.orderNumber ?? "").toLowerCase();
    const customer = (order.customer ?? "").toLowerCase();
    const status = order.status ?? "";
    const priority = order.priority ?? "";

    const matchesSearch =
      normalizedTerm.length === 0 ||
      orderNumber.includes(normalizedTerm) ||
      customer.includes(normalizedTerm);
    const matchesStatus = filterStatus === "all" || status === filterStatus;
    const matchesPriority = filterPriority === "all" || priority === filterPriority;
    return matchesSearch && matchesStatus && matchesPriority;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Delivered": return "default";
      case "Shipped": return "default";
      case "Processing": return "secondary";
      case "Pending": return "secondary";
      case "Cancelled": return "destructive";
      default: return "default";
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "High": return "destructive";
      case "Medium": return "secondary";
      case "Low": return "default";
      default: return "default";
    }
  };

  const formatCurrency = (value?: number) => `$${(value ?? 0).toLocaleString()}`;

  const handleViewOrder = (order: Order) => {
    setSelectedOrder(order);
  };

  const handleResetFilters = () => {
    setFilterStatus("all");
    setFilterPriority("all");
  };

  const handleAddItemToList = () => {
    if (!selectedProductId) return;
    const product = products.find(p => p.id === Number(selectedProductId));
    if (!product) return;
    
    setOrderItems(prev => [...prev, {
      productId: product.id,
      productName: product.name,
      sku: product.sku,
      quantity: itemQuantity,
      unitPrice: itemPrice || product.unitPrice || 0,
    }]);
    
    setSelectedProductId("");
    setItemQuantity(1);
    setItemPrice(0);
  };

  const handleRemoveItem = (index: number) => {
    setOrderItems(prev => prev.filter((_, i) => i !== index));
  };

  const calculateTotal = () => {
    return orderItems.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  };

  const handleCreateOrder = async () => {
    try {
      // Create the order via outbound API
      const orderData: OutboundOrder = {
        referenceNumber: newOrder.orderNumber || `ORD-${Date.now()}`,
        status: "PLANNED",
        destination: newOrder.customer,
        customerName: newOrder.customer,
        priority: newOrder.priority,
        shipDate: newOrder.date,
      };
      
      const created = await createOutboundOrder(orderData);
      
      // Add items to the order
      for (const item of orderItems) {
        await addItemToOrder(created.id!, {
          product: { id: item.productId },
          quantityOrdered: item.quantity,
          unitPrice: item.unitPrice,
        });
      }
      
      // Refresh the list
      const data = await fetchOrders();
      setItems(data);
      
      setIsDialogOpen(false);
      resetForm();
    } catch (error) {
      console.error("Failed to create order", error);
      // Fallback to local state
      const payload = { 
        ...newOrder, 
        id: crypto.randomUUID(),
        items: orderItems.reduce((sum, i) => sum + i.quantity, 0),
        total: calculateTotal(),
      };
      const created = await createOrder(payload);
      setItems((prev) => [...prev, created]);
      setIsDialogOpen(false);
      resetForm();
    }
  };

  const resetForm = () => {
    setNewOrder({
      id: "",
      orderNumber: "",
      customer: "",
      items: 0,
      total: 0,
      status: "Pending",
      date: new Date().toISOString().split("T")[0],
      priority: "Medium",
    });
    setOrderItems([]);
    setSelectedProductId("");
    setItemQuantity(1);
    setItemPrice(0);
  };

  const advanceStatus = (order: Order) => {
    const flow: Order['status'][] = ["Pending", "Processing", "Shipped", "Delivered"];
    const next = flow[flow.indexOf(order.status) + 1] || order.status;
    setItems((prev) => prev.map(o => o.id === order.id ? { ...o, status: next } : o));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-gray-900 mb-2">Zarządzanie zamówieniami</h1>
          <p className="text-gray-600">Przetwarzaj i śledź zamówienia klientów efektywnie</p>
        </div>
        <Button className="gap-2" onClick={() => setIsDialogOpen(true)}>
          <Plus size={18} />
          Dodaj zamówienie
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <Input
              placeholder="Szukaj po numerze zamówienia lub kliencie..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Button 
            variant="outline" 
            className="gap-2"
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter size={20} />
            Filtruj
          </Button>
          <Button variant="outline" className="gap-2">
            <Download size={20} />
            Eksportuj
          </Button>
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <div className="mt-4 pt-4 border-t border-gray-200 grid sm:grid-cols-2 gap-4">
            <div>
              <label className="text-sm text-gray-700 mb-2 block">Status zamówienia</label>
              <Select value={filterStatus} onValueChange={setFilterStatus}>
                <SelectTrigger>
                  <SelectValue placeholder="Wszystkie statusy" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Wszystkie statusy</SelectItem>
                  <SelectItem value="Pending">Oczekujące</SelectItem>
                  <SelectItem value="Processing">W trakcie</SelectItem>
                  <SelectItem value="Shipped">Wysłane</SelectItem>
                  <SelectItem value="Delivered">Dostarczone</SelectItem>
                  <SelectItem value="Cancelled">Anulowane</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-sm text-gray-700 mb-2 block">Priorytet</label>
              <Select value={filterPriority} onValueChange={setFilterPriority}>
                <SelectTrigger>
                  <SelectValue placeholder="Wszystkie priorytety" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Wszystkie priorytety</SelectItem>
                  <SelectItem value="High">Wysoki</SelectItem>
                  <SelectItem value="Medium">Średni</SelectItem>
                  <SelectItem value="Low">Niski</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="sm:col-span-2 flex gap-2">
              <Button variant="outline" size="sm" onClick={handleResetFilters}>
                Wyczyść filtry
              </Button>
              <div className="text-sm text-gray-600 flex items-center">
                Znaleziono {filteredOrders.length} z {items.length} zamówień
              </div>
            </div>
          </div>
        )}
      </Card>

      {/* Stats */}
      <div className="grid sm:grid-cols-4 gap-4">
        {[
          { status: "Pending", label: "Oczekujące" },
          { status: "Processing", label: "W trakcie" },
          { status: "Shipped", label: "Wysłane" },
          { status: "Delivered", label: "Dostarczone" }
        ].map((item) => {
          const count = items.filter(o => o.status === item.status).length;
          return (
            <Card key={item.status} className="p-4">
              <div className="text-gray-900 mb-1">{count}</div>
              <div className="text-sm text-gray-600">{item.label}</div>
            </Card>
          );
        })}
      </div>

      {/* Orders Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Numer zamówienia</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Klient</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Produkty</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Suma</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Priorytet</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Data</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Akcje</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredOrders.map((order) => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900 font-mono">{order.orderNumber}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900">{order.customer}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-600">{order.items} szt.</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900">{formatCurrency(order.total)}</div>
                  </td>
                  <td className="px-6 py-4">
                    <Badge variant={getStatusColor(order.status)}>
                      {order.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4">
                    <Badge variant={getPriorityColor(order.priority)}>
                      {order.priority}
                    </Badge>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-600">{order.date}</div>
                  </td>
                  <td className="px-6 py-4">
                    <Button variant="ghost" size="sm" className="gap-1" onClick={() => handleViewOrder(order)}>
                      <Eye size={16} />
                      Zobacz
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => advanceStatus(order)}>
                      Zmień status
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Order Details Dialog */}
      <Dialog open={selectedOrder !== null} onOpenChange={() => setSelectedOrder(null)}>
        <DialogContent className="sm:max-w-[700px]">
          <DialogHeader>
            <DialogTitle>Szczegóły zamówienia</DialogTitle>
            <DialogDescription>
              Pełne informacje o zamówieniu {selectedOrder?.orderNumber}
            </DialogDescription>
          </DialogHeader>
          {selectedOrder && (
            <div className="space-y-6">
              {/* Order Header */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="text-sm text-gray-500 mb-1">Numer zamówienia</div>
                  <div className="text-gray-900 font-mono">{selectedOrder.orderNumber}</div>
                </div>
                <div>
                  <div className="text-sm text-gray-500 mb-1">Status</div>
                  <Badge variant={getStatusColor(selectedOrder.status)}>
                    {selectedOrder.status}
                  </Badge>
                </div>
              </div>

              {/* Customer Info */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <User size={18} />
                  Informacje o kliencie
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-gray-500">Nazwa klienta</div>
                    <div className="text-gray-900">{selectedOrder.customer}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Priorytet</div>
                    <Badge variant={getPriorityColor(selectedOrder.priority)}>
                      {selectedOrder.priority}
                    </Badge>
                  </div>
                </div>
              </div>

              {/* Order Details */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <Package size={18} />
                  Szczegóły zamówienia
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-gray-500">Liczba produktów</div>
                    <div className="text-gray-900">{selectedOrder.items} szt.</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Całkowita wartość</div>
                    <div className="text-gray-900">${(selectedOrder.total ?? 0).toLocaleString()}</div>
                  </div>
                </div>
              </div>

              {/* Date Info */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <Calendar size={18} />
                  Informacje o dacie
                </h3>
                <div>
                  <div className="text-sm text-gray-500">Data zamówienia</div>
                  <div className="text-gray-900">{selectedOrder.date}</div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="border-t border-gray-200 pt-4 flex gap-2">
                <Button className="flex-1">Edytuj zamówienie</Button>
                <Button variant="outline" className="flex-1">Drukuj fakturę</Button>
                <Button variant="outline">Kontakt z klientem</Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Nowe zamówienie</DialogTitle>
            <DialogDescription>Wprowadź dane zamówienia i dodaj produkty</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-2">
              <label className="text-sm text-gray-700 col-span-1">Numer</label>
              <Input
                className="col-span-3"
                value={newOrder.orderNumber}
                onChange={(e) => setNewOrder({ ...newOrder, orderNumber: e.target.value })}
                placeholder="ORD-2025-XXXX"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-2">
              <label className="text-sm text-gray-700 col-span-1">Klient</label>
              <Input
                className="col-span-3"
                value={newOrder.customer}
                onChange={(e) => setNewOrder({ ...newOrder, customer: e.target.value })}
                placeholder="Nazwa klienta"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-2">
              <label className="text-sm text-gray-700 col-span-1">Priorytet</label>
              <Select value={newOrder.priority} onValueChange={(value) => setNewOrder({ ...newOrder, priority: value as Order['priority'] })}>
                <SelectTrigger className="col-span-3">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="High">Wysoki</SelectItem>
                  <SelectItem value="Medium">Średni</SelectItem>
                  <SelectItem value="Low">Niski</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-4 items-center gap-2">
              <label className="text-sm text-gray-700 col-span-1">Data wysyłki</label>
              <Input
                className="col-span-3"
                type="date"
                value={newOrder.date}
                onChange={(e) => setNewOrder({ ...newOrder, date: e.target.value })}
              />
            </div>

            {/* Products Section */}
            <div className="border-t pt-4 mt-2">
              <h4 className="text-sm font-medium text-gray-900 mb-3">Produkty w zamówieniu</h4>
              
              {/* Add Product Form */}
              <div className="flex gap-2 mb-3">
                <Select value={selectedProductId} onValueChange={setSelectedProductId}>
                  <SelectTrigger className="flex-1">
                    <SelectValue placeholder="Wybierz produkt..." />
                  </SelectTrigger>
                  <SelectContent>
                    {products.map((product) => (
                      <SelectItem key={product.id} value={String(product.id)}>
                        {product.name} ({product.sku})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Input
                  type="number"
                  min="1"
                  className="w-20"
                  placeholder="Ilość"
                  value={itemQuantity}
                  onChange={(e) => setItemQuantity(parseInt(e.target.value) || 1)}
                />
                <Input
                  type="number"
                  step="0.01"
                  className="w-24"
                  placeholder="Cena"
                  value={itemPrice || ""}
                  onChange={(e) => setItemPrice(parseFloat(e.target.value) || 0)}
                />
                <Button type="button" variant="outline" onClick={handleAddItemToList}>
                  <Plus size={16} />
                </Button>
              </div>

              {/* Items List */}
              {orderItems.length > 0 && (
                <div className="border rounded-lg overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-3 py-2 text-left">Produkt</th>
                        <th className="px-3 py-2 text-left">SKU</th>
                        <th className="px-3 py-2 text-right">Ilość</th>
                        <th className="px-3 py-2 text-right">Cena</th>
                        <th className="px-3 py-2 text-right">Suma</th>
                        <th className="px-3 py-2"></th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {orderItems.map((item, idx) => (
                        <tr key={idx}>
                          <td className="px-3 py-2">{item.productName}</td>
                          <td className="px-3 py-2 text-gray-500">{item.sku}</td>
                          <td className="px-3 py-2 text-right">{item.quantity}</td>
                          <td className="px-3 py-2 text-right">${item.unitPrice.toFixed(2)}</td>
                          <td className="px-3 py-2 text-right font-medium">${(item.quantity * item.unitPrice).toFixed(2)}</td>
                          <td className="px-3 py-2">
                            <Button variant="ghost" size="sm" onClick={() => handleRemoveItem(idx)}>
                              <Trash2 size={14} className="text-red-500" />
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot className="bg-gray-50">
                      <tr>
                        <td colSpan={4} className="px-3 py-2 text-right font-medium">Razem:</td>
                        <td className="px-3 py-2 text-right font-bold">${calculateTotal().toFixed(2)}</td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              )}

              {orderItems.length === 0 && (
                <div className="text-center py-4 text-gray-500 border border-dashed rounded-lg">
                  Dodaj produkty do zamówienia
                </div>
              )}
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => { setIsDialogOpen(false); resetForm(); }}>Anuluj</Button>
            <Button onClick={handleCreateOrder} disabled={orderItems.length === 0}>
              Zapisz zamówienie
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}