import { useState } from "react";
import { Box, Printer, CheckCircle, Package } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { startPacking, createParcel, addItemToParcel, closeShipment, Shipment, Parcel } from "../../services/packingService";

export function PackingView() {
  const [orderId, setOrderId] = useState("");
  const [activeShipment, setActiveShipment] = useState<Shipment | null>(null);
  const [activeParcel, setActiveParcel] = useState<Parcel | null>(null);
  const [scanProduct, setScanProduct] = useState("");
  const [scanQty, setScanQty] = useState(1);

  const handleStartPacking = async () => {
    if (!orderId) return;
    try {
      const shipment = await startPacking(Number(orderId));
      setActiveShipment(shipment);
    } catch (error) {
      console.error("Failed to start packing", error);
      alert("Nie można rozpocząć pakowania dla tego zamówienia.");
    }
  };

  const handleCreateParcel = async () => {
    if (!activeShipment) return;
    try {
      // Hardcoded packing material ID 1 (e.g., Standard Box)
      const parcel = await createParcel(activeShipment.id, 1);
      setActiveParcel(parcel);
      // Refresh shipment to see new parcel
      // In a real app, we'd re-fetch the shipment here
    } catch (error) {
      console.error("Failed to create parcel", error);
    }
  };

  const handlePackItem = async () => {
    if (!activeParcel || !scanProduct) return;
    try {
      // Assuming scanProduct is Product ID for now. In real life, it would be a barcode lookup.
      await addItemToParcel(activeParcel.id, Number(scanProduct), scanQty);
      setScanProduct("");
      setScanQty(1);
      alert("Produkt dodany do paczki!");
    } catch (error) {
      console.error("Failed to pack item", error);
      alert("Błąd dodawania produktu.");
    }
  };

  const handleCloseShipment = async () => {
    if (!activeShipment) return;
    try {
      await closeShipment(activeShipment.id);
      alert("Wysyłka zamknięta. Etykiety wygenerowane.");
      setActiveShipment(null);
      setActiveParcel(null);
      setOrderId("");
    } catch (error) {
      console.error("Failed to close shipment", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Stacja Pakowania (Packing)</h1>
          <p className="text-gray-600">Skanuj produkty i twórz paczki wysyłkowe</p>
        </div>
      </div>

      {!activeShipment ? (
        <Card className="p-8 max-w-md mx-auto text-center">
          <Package size={48} className="mx-auto text-blue-600 mb-4" />
          <h2 className="text-xl font-semibold mb-4">Rozpocznij Pakowanie</h2>
          <div className="flex gap-2">
            <Input 
              placeholder="Zeskanuj ID Zamówienia..." 
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
            />
            <Button onClick={handleStartPacking}>Start</Button>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column: Shipment Info */}
          <div className="space-y-6">
            <Card className="p-6">
              <h3 className="font-semibold text-gray-900 mb-2">Wysyłka #{activeShipment.id}</h3>
              <Badge>{activeShipment.status}</Badge>
              <div className="mt-4 space-y-2">
                <Button className="w-full" variant="outline" onClick={handleCreateParcel}>
                  <Box size={16} className="mr-2" />
                  Nowa Paczka
                </Button>
                <Button className="w-full" variant="default" onClick={handleCloseShipment}>
                  <Printer size={16} className="mr-2" />
                  Drukuj Etykiety i Zakończ
                </Button>
              </div>
            </Card>
          </div>

          {/* Middle Column: Active Parcel */}
          <div className="lg:col-span-2 space-y-6">
            {activeParcel ? (
              <Card className="p-6 border-blue-200 bg-blue-50">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="font-semibold text-blue-900">Aktywna Paczka #{activeParcel.id}</h3>
                  <Badge variant="outline" className="bg-white">Waga: {activeParcel.weightKg} kg</Badge>
                </div>
                
                <div className="flex gap-4 mb-4">
                  <div className="flex-1">
                    <Input 
                      placeholder="Skanuj produkt (ID)..." 
                      value={scanProduct}
                      onChange={(e) => setScanProduct(e.target.value)}
                      autoFocus
                    />
                  </div>
                  <div className="w-24">
                    <Input 
                      type="number" 
                      value={scanQty}
                      onChange={(e) => setScanQty(Number(e.target.value))}
                    />
                  </div>
                  <Button onClick={handlePackItem}>Dodaj</Button>
                </div>

                <div className="bg-white rounded-lg p-4 border">
                  <p className="text-sm text-gray-500 text-center">Zeskanowane produkty pojawią się tutaj...</p>
                </div>
              </Card>
            ) : (
              <div className="h-64 flex items-center justify-center border-2 border-dashed rounded-lg text-gray-400">
                Wybierz lub utwórz paczkę, aby rozpocząć skanowanie.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
