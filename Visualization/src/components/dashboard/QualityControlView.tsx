import { useState, useEffect } from "react";
import { ClipboardCheck, AlertOctagon, CheckCircle, XCircle, Package } from "lucide-react";
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
import { createInspection, executeInspection, createNcr, QcInspection } from "../../services/qcService";
import { fetchProducts, Product } from "../../services/productService";
import { fetchInboundOrders, InboundOrder } from "../../services/inboundService";
import { fetchRmaRequests, RmaRequest } from "../../services/returnsService";

type InspectionType = 'INBOUND' | 'RETURN';

export function QualityControlView() {
  const [products, setProducts] = useState<Product[]>([]);
  const [inboundOrders, setInboundOrders] = useState<InboundOrder[]>([]);
  const [rmaRequests, setRmaRequests] = useState<RmaRequest[]>([]);

  const [selectedProductId, setSelectedProductId] = useState("");
  const [inspectionType, setInspectionType] = useState<InspectionType>('INBOUND');
  const [selectedRefId, setSelectedRefId] = useState("");
  const [sampleSize, setSampleSize] = useState(5);

  const [activeInspection, setActiveInspection] = useState<QcInspection | null>(null);
  const [ncrDescription, setNcrDescription] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [productsData, inboundData, rmaData] = await Promise.all([
        fetchProducts(),
        fetchInboundOrders(),
        fetchRmaRequests()
      ]);
      setProducts(productsData);
      setInboundOrders(inboundData.filter((o: InboundOrder) => o.status === 'PENDING' || o.status === 'RECEIVING'));
      setRmaRequests(rmaData.filter((r: RmaRequest) => r.status === 'PENDING' || r.status === 'RECEIVED'));
    } catch (error) {
      console.error("Failed to load data", error);
    }
  };

  // Get reference options based on inspection type
  const referenceOptions = inspectionType === 'INBOUND' ? inboundOrders : rmaRequests;

  const handleCreateInspection = async () => {
    if (!selectedProductId || !selectedRefId) {
      alert("Wybierz produkt i referencję do inspekcji.");
      return;
    }
    setLoading(true);
    try {
      const inspection = await createInspection(
        Number(selectedProductId), 
        inspectionType, 
        Number(selectedRefId), 
        sampleSize
      );
      setActiveInspection(inspection);
    } catch (error) {
      console.error("Failed to create inspection", error);
      alert("Błąd tworzenia inspekcji.");
    } finally {
      setLoading(false);
    }
  };

  const handlePass = async () => {
    if (!activeInspection) return;
    setLoading(true);
    try {
      await executeInspection(activeInspection.id, 'PASSED', 1); // Inspector ID 1
      alert("Inspekcja zakończona wynikiem POZYTYWNYM.");
      setActiveInspection(null);
      resetForm();
    } catch (error) {
      console.error("Failed to pass inspection", error);
    } finally {
      setLoading(false);
    }
  };

  const handleFail = async () => {
    if (!activeInspection) return;
    setLoading(true);
    try {
      await executeInspection(activeInspection.id, 'FAILED', 1);
      // Don't clear activeInspection yet, allow NCR creation
      alert("Inspekcja zakończona wynikiem NEGATYWNYM. Utwórz raport NCR.");
    } catch (error) {
      console.error("Failed to fail inspection", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNcr = async () => {
    if (!activeInspection) return;
    setLoading(true);
    try {
      await createNcr(activeInspection.id, 'COSMETIC', ncrDescription); // Valid: DIMENSIONAL, COSMETIC, FUNCTIONAL, PACKAGING, DOCUMENTATION, OTHER
      alert("Raport niezgodności (NCR) utworzony.");
      setActiveInspection(null);
      setNcrDescription("");
      resetForm();
    } catch (error) {
      console.error("Failed to create NCR", error);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setSelectedProductId("");
    setSelectedRefId("");
    setSampleSize(5);
  };

  // Get selected product info for display
  const selectedProduct = products.find(p => String(p.id) === selectedProductId);

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Kontrola Jakości (QC)</h1>
          <p className="text-gray-600">Zarządzaj inspekcjami i raportami niezgodności</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* New Inspection Form */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <ClipboardCheck size={20} />
            Nowa Inspekcja
          </h3>
          <div className="space-y-4">
            {/* Product Dropdown */}
            <div className="space-y-2">
              <label className="text-sm font-medium flex items-center gap-2">
                <Package size={16} />
                Produkt do inspekcji
              </label>
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

            {/* Inspection Type */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Typ inspekcji</label>
              <Select value={inspectionType} onValueChange={(v) => { setInspectionType(v as InspectionType); setSelectedRefId(""); }}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="INBOUND">Przyjęcie dostawy</SelectItem>
                  <SelectItem value="RETURN">Zwrot (RMA)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Reference Dropdown - based on type */}
            <div className="space-y-2">
              <label className="text-sm font-medium">
                {inspectionType === 'INBOUND' ? 'Zamówienie inbound' : 'Zgłoszenie RMA'}
              </label>
              <Select value={selectedRefId} onValueChange={setSelectedRefId}>
                <SelectTrigger>
                  <SelectValue placeholder={inspectionType === 'INBOUND' ? 'Wybierz dostawę...' : 'Wybierz RMA...'} />
                </SelectTrigger>
                <SelectContent>
                  {inspectionType === 'INBOUND' ? (
                    inboundOrders.map((order) => (
                      <SelectItem key={order.id} value={String(order.id)}>
                        {order.orderReference} - {order.supplier} ({order.status})
                      </SelectItem>
                    ))
                  ) : (
                    rmaRequests.map((rma) => (
                      <SelectItem key={rma.id} value={String(rma.id)}>
                        RMA #{rma.id} - {rma.reason} ({rma.status})
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>

            {/* Sample Size */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Wielkość próbki</label>
              <Input 
                type="number"
                min="1"
                value={sampleSize}
                onChange={(e) => setSampleSize(parseInt(e.target.value) || 1)}
                placeholder="Ilość sztuk do sprawdzenia"
              />
            </div>

            <Button 
              className="w-full" 
              onClick={handleCreateInspection}
              disabled={!selectedProductId || !selectedRefId || loading}
            >
              {loading ? 'Tworzenie...' : 'Rozpocznij Inspekcję'}
            </Button>
          </div>
        </Card>

        {/* Active Inspection Execution */}
        {activeInspection && (
          <Card className="p-6 border-blue-200 bg-blue-50">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Inspekcja #{activeInspection.id}</h3>
                <p className="text-sm text-blue-700">
                  Produkt: {selectedProduct?.name || `ID ${activeInspection.productId}`} | Próbka: {activeInspection.sampleSize} szt.
                </p>
              </div>
              <Badge>{activeInspection.status}</Badge>
            </div>

            <div className="space-y-6">
              <div className="bg-white p-4 rounded border">
                <h4 className="font-medium mb-2">Lista kontrolna</h4>
                <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                  <li>Sprawdź opakowanie zewnętrzne</li>
                  <li>Zweryfikuj kod kreskowy</li>
                  <li>Sprawdź kompletność zestawu</li>
                  <li>Szukaj widocznych uszkodzeń</li>
                </ul>
              </div>

              <div className="flex gap-4">
                <Button className="flex-1 bg-green-600 hover:bg-green-700" onClick={handlePass} disabled={loading}>
                  <CheckCircle size={18} className="mr-2" />
                  ZATWIERDŹ (PASS)
                </Button>
                <Button className="flex-1 bg-red-600 hover:bg-red-700" onClick={handleFail} disabled={loading}>
                  <XCircle size={18} className="mr-2" />
                  ODRZUĆ (FAIL)
                </Button>
              </div>

              {/* NCR Form - Only show if failed or user wants to report defect */}
              <div className="border-t pt-4 mt-4">
                <h4 className="font-medium mb-2 text-red-800 flex items-center gap-2">
                  <AlertOctagon size={16} />
                  Zgłoś Niezgodność (NCR)
                </h4>
                <div className="space-y-2">
                  <Input 
                    placeholder="Opis uszkodzenia / wady..." 
                    value={ncrDescription}
                    onChange={(e) => setNcrDescription(e.target.value)}
                  />
                  <Button variant="outline" className="w-full border-red-200 text-red-700 hover:bg-red-50" onClick={handleCreateNcr} disabled={loading || !ncrDescription}>
                    Utwórz Raport NCR
                  </Button>
                </div>
              </div>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
}
