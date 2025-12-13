import { useState, useEffect } from "react";
import { Truck, Package, MapPin, Calendar, Navigation } from "lucide-react";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Shipment } from "../../data/mockData";
import { fetchShipments } from "../../services/api";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog";

export function ShipmentsView() {
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);
  const [trackingView, setTrackingView] = useState<Shipment | null>(null);
  const [items, setItems] = useState<Shipment[]>([]);

  useEffect(() => {
    const loadData = async () => {
      const data = await fetchShipments();
      setItems(data);
    };
    loadData();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Delivered": return "default";
      case "In Transit": return "default";
      case "Out for Delivery": return "secondary";
      case "Preparing": return "secondary";
      default: return "default";
    }
  };

  const handleTrack = (shipment: Shipment) => {
    setTrackingView(shipment);
  };

  const handleDetails = (shipment: Shipment) => {
    setSelectedShipment(shipment);
  };

  const getTrackingSteps = (status: string) => {
    const allSteps = [
      { label: "Przygotowanie", status: "Preparing", completed: true },
      { label: "W tranzycie", status: "In Transit", completed: status !== "Preparing" },
      { label: "W dostawie", status: "Out for Delivery", completed: status === "Out for Delivery" || status === "Delivered" },
      { label: "Dostarczone", status: "Delivered", completed: status === "Delivered" },
    ];
    return allSteps;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-gray-900 mb-2">Śledzenie przesyłek</h1>
        <p className="text-gray-600">Monitoruj wszystkie przesyłki w czasie rzeczywistym dzięki śledzeniu zasilanemu AI</p>
      </div>

      {/* Stats */}
      <div className="grid sm:grid-cols-4 gap-4">
        {[
          { status: "Preparing", label: "Przygotowanie" },
          { status: "In Transit", label: "W tranzycie" },
          { status: "Out for Delivery", label: "W dostawie" },
          { status: "Delivered", label: "Dostarczone" }
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
    </div>
  );
}