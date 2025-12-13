import { useState } from "react";
import { RotateCcw, Search, CheckCircle, AlertTriangle } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { createRmaRequest, receiveReturn, gradeItem, RmaRequest } from "../../services/returnsService";

export function ReturnsView() {
  const [orderId, setOrderId] = useState("");
  const [reason, setReason] = useState("Damaged in Transit");
  const [activeRma, setActiveRma] = useState<RmaRequest | null>(null);
  const [trackingNumber, setTrackingNumber] = useState("");

  const handleCreateRma = async () => {
    if (!orderId) return;
    try {
      const rma = await createRmaRequest(Number(orderId), reason);
      setActiveRma(rma);
      alert(`RMA #${rma.id} utworzone pomyślnie.`);
    } catch (error) {
      console.error("Failed to create RMA", error);
      alert("Błąd tworzenia RMA.");
    }
  };

  const handleReceiveReturn = async () => {
    if (!activeRma || !trackingNumber) return;
    try {
      const updatedRma = await receiveReturn(activeRma.id, trackingNumber);
      setActiveRma(updatedRma);
      alert("Zwrot przyjęty na magazyn.");
    } catch (error) {
      console.error("Failed to receive return", error);
    }
  };

  const handleGradeItem = async (productId: number, grade: string) => {
    if (!activeRma) return;
    try {
      await gradeItem(activeRma.id, productId, grade, "Graded via UI");
      alert("Ocena produktu zapisana.");
    } catch (error) {
      console.error("Failed to grade item", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Obsługa Zwrotów (Returns)</h1>
          <p className="text-gray-600">Twórz zgłoszenia RMA i oceniaj stan zwracanych produktów</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Create RMA Section */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <RotateCcw size={20} />
            Nowe Zgłoszenie RMA
          </h3>
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">ID Zamówienia</label>
              <Input 
                value={orderId}
                onChange={(e) => setOrderId(e.target.value)}
                placeholder="Np. 1001"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Powód Zwrotu</label>
              <Select value={reason} onValueChange={setReason}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Damaged in Transit">Uszkodzone w transporcie</SelectItem>
                  <SelectItem value="Wrong Item Sent">Otrzymano zły produkt</SelectItem>
                  <SelectItem value="Customer Changed Mind">Klient zmienił zdanie</SelectItem>
                  <SelectItem value="Defective">Produkt wadliwy</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button className="w-full" onClick={handleCreateRma}>Utwórz RMA</Button>
          </div>
        </Card>

        {/* Active RMA Processing */}
        {activeRma && (
          <Card className="p-6 bg-gray-50 border-gray-200">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">RMA #{activeRma.id}</h3>
                <p className="text-sm text-gray-500">Zamówienie: {activeRma.outboundOrderId}</p>
              </div>
              <Badge>{activeRma.status}</Badge>
            </div>

            {activeRma.status === 'PENDING' && (
              <div className="space-y-4 border-t pt-4">
                <h4 className="font-medium">Przyjęcie Fizyczne</h4>
                <div className="flex gap-2">
                  <Input 
                    placeholder="Nr listu przewozowego zwrotu" 
                    value={trackingNumber}
                    onChange={(e) => setTrackingNumber(e.target.value)}
                  />
                  <Button onClick={handleReceiveReturn}>Przyjmij</Button>
                </div>
              </div>
            )}

            {activeRma.status === 'RECEIVED' && (
              <div className="space-y-4 border-t pt-4">
                <h4 className="font-medium">Ocena Jakości (Grading)</h4>
                <div className="bg-white p-4 rounded border">
                  <p className="mb-2 font-medium">Produkt ID: 1 (Przykładowy)</p>
                  <div className="flex gap-2">
                    <Button size="sm" variant="outline" className="text-green-600 border-green-200 hover:bg-green-50" onClick={() => handleGradeItem(1, 'A')}>
                      Klasa A (Nowy)
                    </Button>
                    <Button size="sm" variant="outline" className="text-yellow-600 border-yellow-200 hover:bg-yellow-50" onClick={() => handleGradeItem(1, 'B')}>
                      Klasa B (Używany)
                    </Button>
                    <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50" onClick={() => handleGradeItem(1, 'D')}>
                      Klasa D (Złom)
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </Card>
        )}
      </div>
    </div>
  );
}
