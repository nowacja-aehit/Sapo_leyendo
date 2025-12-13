import { useState, useEffect } from "react";
import { Search, Filter, Download, Eye, Package, User, Calendar, DollarSign } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { Order } from "../../data/mockData";
import { fetchOrders } from "../../services/api";
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

export function OrdersView() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [filterStatus, setFilterStatus] = useState("all");
  const [filterPriority, setFilterPriority] = useState("all");
  const [showFilters, setShowFilters] = useState(false);
  const [items, setItems] = useState<Order[]>([]);

  useEffect(() => {
    const loadData = async () => {
      const data = await fetchOrders();
      setItems(data);
    };
    loadData();
  }, []);

  const filteredOrders = items.filter(order => {
    const matchesSearch = order.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.customer.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === "all" || order.status === filterStatus;
    const matchesPriority = filterPriority === "all" || order.priority === filterPriority;
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

  const handleViewOrder = (order: Order) => {
    setSelectedOrder(order);
  };

  const handleResetFilters = () => {
    setFilterStatus("all");
    setFilterPriority("all");
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-gray-900 mb-2">Zarządzanie zamówieniami</h1>
        <p className="text-gray-600">Przetwarzaj i śledź zamówienia klientów efektywnie</p>
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
                Znaleziono {filteredOrders.length} z {orders.length} zamówień
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
                    <div className="text-sm text-gray-900">${order.total.toLocaleString()}</div>
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
                    <div className="text-gray-900">${selectedOrder.total.toLocaleString()}</div>
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
    </div>
  );
}