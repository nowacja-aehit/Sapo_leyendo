import { useState, useEffect } from "react";
import { RotateCcw, Search, CheckCircle, AlertTriangle, List } from "lucide-react";
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
import { createRmaRequest, receiveReturn, gradeItem, RmaRequest, fetchRmaRequests } from "../../services/returnsService";
import { fetchOutboundOrders, OutboundOrder } from "../../services/outboundService";
import { fetchProducts, Product } from "../../services/productService";

export function ReturnsView() {
  const [orderId, setOrderId] = useState("");
  const [reason, setReason] = useState("Damaged in Transit");
  const [activeRma, setActiveRma] = useState<RmaRequest | null>(null);
  const [trackingNumber, setTrackingNumber] = useState("");
  const [orders, setOrders] = useState<OutboundOrder[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [rmaList, setRmaList] = useState<RmaRequest[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<string>("");
  const [showList, setShowList] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [orderData, productData, rmaData] = await Promise.all([
          fetchOutboundOrders(),
          fetchProducts(),
          fetchRmaRequests()
        ]);
        setOrders(orderData.filter(o => o.status === "SHIPPED" || o.status === "DELIVERED" || o.status === "Shipped" || o.status === "Delivered"));
        setProducts(productData);
        setRmaList(rmaData);
      } catch (err) {
        console.error("Failed to load data", err);
      }
    };
    loadData();
  }, []);

  const handleCreateRma = async () => {
    if (!orderId) return;
    try {
      const rma = await createRmaRequest(Number(orderId), reason);
      setActiveRma(rma);
      setRmaList(prev => [...prev, rma]);
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
      setRmaList(prev => prev.map(r => r.id === updatedRma.id ? updatedRma : r));
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

  const handleSelectRma = (rma: RmaRequest) => {
    setActiveRma(rma);
    setShowList(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Obsługa Zwrotów (Returns)</h1>
          <p className="text-gray-600">Twórz zgłoszenia RMA i oceniaj stan zwracanych produktów</p>
        </div>
        <Button variant="outline" className="gap-2" onClick={() => setShowList(!showList)}>
          <List size={16} />
          {showList ? "Ukryj listę RMA" : "Pokaż listę RMA"}
        </Button>
      </div>

      {/* RMA List */}
      {showList && rmaList.length > 0 && (
        <Card className="p-4">
          <h3 className="font-semibold mb-3">Historia zgłoszeń RMA</h3>
          <div className="divide-y">
            {rmaList.map((rma) => (
              <div key={rma.id} className="py-2 flex justify-between items-center">
                <div>
                  <span className="font-mono">RMA #{rma.id}</span>
                  <span className="text-gray-500 ml-2">- Zamówienie #{rma.outboundOrderId}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Badge>{rma.status}</Badge>
                  <Button size="sm" variant="outline" onClick={() => handleSelectRma(rma)}>
                    Wybierz
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Create RMA Section */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <RotateCcw size={20} />
            Nowe Zgłoszenie RMA
          </h3>
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Zamówienie</label>
              <Select value={orderId} onValueChange={setOrderId}>
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
            <Button className="w-full" onClick={handleCreateRma} disabled={!orderId}>Utwórz RMA</Button>
          </div>
        </Card>

        {/* Active RMA Processing */}
        {activeRma && (
          <Card className="p-6 bg-gray-50 border-gray-200">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">RMA #{activeRma.id}</h3>
                <p className="text-sm text-gray-500">Zamówienie: #{activeRma.outboundOrderId}</p>
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
                <div className="space-y-2 mb-3">
                  <label className="text-sm font-medium">Wybierz produkt do oceny</label>
                  <Select value={selectedProductId} onValueChange={setSelectedProductId}>
                    <SelectTrigger>
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
                </div>
                {selectedProductId && (
                  <div className="bg-white p-4 rounded border">
                    <p className="mb-2 font-medium">Produkt: {products.find(p => p.id === Number(selectedProductId))?.name}</p>
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline" className="text-green-600 border-green-200 hover:bg-green-50" onClick={() => handleGradeItem(Number(selectedProductId), 'GRADE_A')}>
                        Klasa A (Nowy)
                      </Button>
                      <Button size="sm" variant="outline" className="text-yellow-600 border-yellow-200 hover:bg-yellow-50" onClick={() => handleGradeItem(Number(selectedProductId), 'GRADE_B')}>
                        Klasa B (Używany)
                      </Button>
                      <Button size="sm" variant="outline" className="text-orange-600 border-orange-200 hover:bg-orange-50" onClick={() => handleGradeItem(Number(selectedProductId), 'GRADE_C')}>
                        Klasa C (Uszkodzony)
                      </Button>
                      <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50" onClick={() => handleGradeItem(Number(selectedProductId), 'SCRAP')}>
                        Złom
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </Card>
        )}
      </div>
    </div>
  );
}
