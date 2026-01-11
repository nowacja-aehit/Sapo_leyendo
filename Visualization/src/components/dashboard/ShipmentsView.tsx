import { useState, useEffect } from "react";
import { Truck, Package, MapPin, Calendar, Navigation, Plus } from "lucide-react";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Shipment } from "../../data/mockData";
import { fetchShipments } from "../../services/api";
import { fetchCarriers, createShipment as createShipmentApi, Carrier } from "../../services/shippingService";
import { fetchOutboundOrders, OutboundOrder } from "../../services/outboundService";
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

export function ShipmentsView() {
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);
  const [trackingView, setTrackingView] = useState<Shipment | null>(null);
  const [items, setItems] = useState<Shipment[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [carriers, setCarriers] = useState<Carrier[]>([]);
  const [orders, setOrders] = useState<OutboundOrder[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState<string>("");
  const [selectedCarrierId, setSelectedCarrierId] = useState<string>("");
  const [newShipment, setNewShipment] = useState<Shipment>({
    id: 0,
    trackingNumber: "",
    destination: "",
    carrier: "",
    status: "PACKING",
    estimatedDelivery: new Date().toISOString().split("T")[0],
    items: 0,
  });

  useEffect(() => {
    const loadData = async () => {
      const data = await fetchShipments();
      setItems(data);
      
      // Load carriers and orders for dropdowns
      try {
        const [carrierData, orderData] = await Promise.all([
          fetchCarriers(),
          fetchOutboundOrders()
        ]);
        setCarriers(carrierData);
        setOrders(orderData.filter(o => o.status === "PLANNED" || o.status === "PICKED"));
      } catch (err) {
        console.error("Failed to load carriers/orders", err);
      }
    };
    loadData();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PACKING": return "secondary";
      case "PACKED": return "default";
      case "SHIPPED": return "default";
      default: return "default";
    }
  };

  const handleTrack = (shipment: Shipment) => {
    setTrackingView(shipment);
  };

  const handleDetails = (shipment: Shipment) => {
    setSelectedShipment(shipment);
  };

  const handleAddShipment = async () => {
    if (!selectedOrderId || !selectedCarrierId) {
      alert("Wybierz zamówienie i przewoźnika");
      return;
    }
    
    try {
      const created = await createShipmentApi(
        Number(selectedOrderId),
        Number(selectedCarrierId),
        newShipment.trackingNumber || undefined
      );
      
      // Refresh shipments list
      const data = await fetchShipments();
      setItems(data);
      
      setIsDialogOpen(false);
      resetForm();
    } catch (error) {
      console.error("Failed to create shipment via API", error);
      // Fallback to local state
      const carrier = carriers.find(c => c.id === Number(selectedCarrierId));
      const created = { 
        ...newShipment, 
        id: Date.now(),
        carrier: carrier?.name || newShipment.carrier,
        trackingNumber: newShipment.trackingNumber || `TRK-${Date.now()}`
      };
      setItems((prev) => [...prev, created]);
      setIsDialogOpen(false);
      resetForm();
    }
  };

  const resetForm = () => {
    setNewShipment({
      id: 0,
      trackingNumber: "",
      destination: "",
      carrier: "",
      status: "PACKING",
      estimatedDelivery: new Date().toISOString().split("T")[0],
      items: 0,
    });
    setSelectedOrderId("");
    setSelectedCarrierId("");
  };

  const advanceStatus = (shipment: Shipment) => {
    const flow: Shipment['status'][] = ["PACKING", "PACKED", "SHIPPED"];
    const next = flow[flow.indexOf(shipment.status) + 1] || shipment.status;
    setItems((prev) => prev.map(s => s.id === shipment.id ? { ...s, status: next } : s));
  };

  const getTrackingSteps = (status: string) => {
    const allSteps = [
      { label: "Pakowanie", status: "PACKING", completed: true },
      { label: "Spakowane", status: "PACKED", completed: status === "PACKED" || status === "SHIPPED" },
      { label: "Wysłane", status: "SHIPPED", completed: status === "SHIPPED" },
    ];
    return allSteps;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-gray-900 mb-2">Śledzenie przesyłek</h1>
          <p className="text-gray-600">Monitoruj wszystkie przesyłki w czasie rzeczywistym dzięki śledzeniu zasilanemu AI</p>
        </div>
        <Button className="gap-2" onClick={() => setIsDialogOpen(true)}>
          <Plus size={16} /> Dodaj przesyłkę
        </Button>
      </div>

      {/* Stats */}
      <div className="grid sm:grid-cols-3 gap-4">
        {[
          { status: "PACKING", label: "Pakowanie" },
          { status: "PACKED", label: "Spakowane" },
          { status: "SHIPPED", label: "Wysłane" }
        ].map((item) => {
          const count = items.filter(s => s.status === item.status).length;
          return (
            <Card key={item.status} className="p-4">
              <div className="text-gray-900 mb-1">{count}</div>
              <div className="text-sm text-gray-600">{item.label}</div>
            </Card>
          );
        })}
      </div>

      {/* Shipments Grid */}
      <div className="grid lg:grid-cols-2 gap-6">
        {items.map((shipment) => (
          <Card key={shipment.id} className="p-6">
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Truck className="text-blue-600" size={24} />
                </div>
                <div>
                  <div className="text-sm text-gray-900 font-mono">{shipment.trackingNumber}</div>
                  <div className="text-xs text-gray-500">{shipment.carrier}</div>
                </div>
              </div>
              <Badge variant={getStatusColor(shipment.status)}>
                {shipment.status}
              </Badge>
            </div>

            <div className="space-y-3 mb-4">
              <div className="flex items-center gap-2 text-sm">
                <MapPin className="text-gray-400" size={16} />
                <span className="text-gray-600">Przeznaczenie:</span>
                <span className="text-gray-900">{shipment.destination}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Package className="text-gray-400" size={16} />
                <span className="text-gray-600">Produkty:</span>
                <span className="text-gray-900">{shipment.items}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <span className="text-gray-600">Przewid. dostawa:</span>
                <span className="text-gray-900">{shipment.estimatedDelivery}</span>
              </div>
            </div>

            {/* Progress Bar */}
            <div className="mb-4">
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-blue-600 rounded-full transition-all"
                  style={{
                    width: shipment.status === "Delivered" ? "100%" :
                           shipment.status === "Out for Delivery" ? "75%" :
                           shipment.status === "In Transit" ? "50%" : "25%"
                  }}
                />
              </div>
            </div>

            <div className="flex gap-2">
              <Button variant="outline" size="sm" className="flex-1" onClick={() => handleTrack(shipment)}>
                Śledź
              </Button>
              <Button variant="outline" size="sm" className="flex-1" onClick={() => handleDetails(shipment)}>
                Szczegóły
              </Button>
              <Button variant="outline" size="sm" onClick={() => advanceStatus(shipment)}>
                Aktualizuj status
              </Button>
            </div>
          </Card>
        ))}
      </div>

      {/* Tracking Dialog */}
      <Dialog open={trackingView !== null} onOpenChange={() => setTrackingView(null)}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>Śledź przesyłkę</DialogTitle>
            <DialogDescription>
              Aktualny status przesyłki {trackingView?.trackingNumber}
            </DialogDescription>
          </DialogHeader>
          {trackingView && (
            <div className="space-y-6">
              {/* Current Status */}
              <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
                    <Navigation className="text-white" size={24} />
                  </div>
                  <div>
                    <div className="text-sm text-blue-600">Aktualny status</div>
                    <div className="text-gray-900">{trackingView.status}</div>
                  </div>
                </div>
              </div>

              {/* Tracking Steps */}
              <div className="space-y-4">
                {getTrackingSteps(trackingView.status).map((step, index) => (
                  <div key={index} className="flex items-start gap-4">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                      step.completed ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-400'
                    }`}>
                      {step.completed ? '✓' : index + 1}
                    </div>
                    <div className="flex-1 pt-1">
                      <div className={`${step.completed ? 'text-gray-900' : 'text-gray-400'}`}>
                        {step.label}
                      </div>
                      {step.completed && step.status === trackingView.status && (
                        <div className="text-xs text-blue-600 mt-1">Aktualny etap</div>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {/* Shipment Info */}
              <div className="border-t border-gray-200 pt-4 space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Przewoźnik</span>
                  <span className="text-gray-900">{trackingView.carrier}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Przeznaczenie</span>
                  <span className="text-gray-900">{trackingView.destination}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Przewidywana dostawa</span>
                  <span className="text-gray-900">{trackingView.estimatedDelivery}</span>
                </div>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Details Dialog */}
      <Dialog open={selectedShipment !== null} onOpenChange={() => setSelectedShipment(null)}>
        <DialogContent className="sm:max-w-[700px]">
          <DialogHeader>
            <DialogTitle>Szczegóły przesyłki</DialogTitle>
            <DialogDescription>
              Pełne informacje o przesyłce {selectedShipment?.trackingNumber}
            </DialogDescription>
          </DialogHeader>
          {selectedShipment && (
            <div className="space-y-6">
              {/* Header */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="text-sm text-gray-500 mb-1">Numer śledzenia</div>
                  <div className="text-gray-900 font-mono">{selectedShipment.trackingNumber}</div>
                </div>
                <div>
                  <div className="text-sm text-gray-500 mb-1">Status</div>
                  <Badge variant={getStatusColor(selectedShipment.status)}>
                    {selectedShipment.status}
                  </Badge>
                </div>
              </div>

              {/* Carrier Info */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <Truck size={18} />
                  Informacje o przewoźniku
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-gray-500">Firma przewozowa</div>
                    <div className="text-gray-900">{selectedShipment.carrier}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Metoda wysyłki</div>
                    <div className="text-gray-900">Standard Express</div>
                  </div>
                </div>
              </div>

              {/* Destination Info */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <MapPin size={18} />
                  Informacje o dostawie
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-gray-500">Przeznaczenie</div>
                    <div className="text-gray-900">{selectedShipment.destination}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Liczba produktów</div>
                    <div className="text-gray-900">{selectedShipment.items}</div>
                  </div>
                </div>
              </div>

              {/* Timeline */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                  <Calendar size={18} />
                  Harmonogram
                </h3>
                <div>
                  <div className="text-sm text-gray-500">Przewidywana dostawa</div>
                  <div className="text-gray-900">{selectedShipment.estimatedDelivery}</div>
                </div>
              </div>

              {/* Actions */}
              <div className="border-t border-gray-200 pt-4 flex gap-2">
                <Button className="flex-1">Zgłoś problem</Button>
                <Button variant="outline" className="flex-1">Zmień adres</Button>
                <Button variant="outline">Kontakt z przewoźnikiem</Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Dodaj przesyłkę</DialogTitle>
            <DialogDescription>Utwórz przesyłkę dla zamówienia wychodzącego</DialogDescription>
          </DialogHeader>
          <div className="grid gap-3 py-4">
            <div>
              <label className="text-sm font-medium mb-1 block">Zamówienie *</label>
              <Select value={selectedOrderId} onValueChange={setSelectedOrderId}>
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz zamówienie..." />
                </SelectTrigger>
                <SelectContent>
                  {orders.map((order) => (
                    <SelectItem key={order.id} value={String(order.id)}>
                      {order.referenceNumber || order.orderNumber} - {order.customerName || order.customer}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-sm font-medium mb-1 block">Przewoźnik *</label>
              <Select value={selectedCarrierId} onValueChange={setSelectedCarrierId}>
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz przewoźnika..." />
                </SelectTrigger>
                <SelectContent>
                  {carriers.map((carrier) => (
                    <SelectItem key={carrier.id} value={String(carrier.id)}>
                      {carrier.name} ({carrier.serviceType || "Standard"})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-sm font-medium mb-1 block">Numer śledzenia (opcjonalnie)</label>
              <Input
                placeholder="Zostanie wygenerowany automatycznie"
                value={newShipment.trackingNumber}
                onChange={(e) => setNewShipment({ ...newShipment, trackingNumber: e.target.value })}
              />
            </div>
            <div>
              <label className="text-sm font-medium mb-1 block">Przeznaczenie</label>
              <Input
                placeholder="Adres dostawy"
                value={newShipment.destination}
                onChange={(e) => setNewShipment({ ...newShipment, destination: e.target.value })}
              />
            </div>
            <div>
              <label className="text-sm font-medium mb-1 block">Przewidywana dostawa</label>
              <Input
                type="date"
                value={newShipment.estimatedDelivery}
                onChange={(e) => setNewShipment({ ...newShipment, estimatedDelivery: e.target.value })}
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => { setIsDialogOpen(false); resetForm(); }}>Anuluj</Button>
            <Button onClick={handleAddShipment} disabled={!selectedOrderId || !selectedCarrierId}>
              Utwórz przesyłkę
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}