import { useState } from "react";
import { ClipboardCheck, AlertOctagon, CheckCircle, XCircle } from "lucide-react";
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

export function QualityControlView() {
  const [productId, setProductId] = useState("");
  const [refId, setRefId] = useState("");
  const [activeInspection, setActiveInspection] = useState<QcInspection | null>(null);
  const [ncrDescription, setNcrDescription] = useState("");

  const handleCreateInspection = async () => {
    if (!productId || !refId) return;
    try {
      // Hardcoded sample size 5 for demo
      const inspection = await createInspection(Number(productId), 'INBOUND', Number(refId), 5);
      setActiveInspection(inspection);
    } catch (error) {
      console.error("Failed to create inspection", error);
      alert("Błąd tworzenia inspekcji.");
    }
  };

  const handlePass = async () => {
    if (!activeInspection) return;
    try {
      await executeInspection(activeInspection.id, 'PASS', 1); // Inspector ID 1
      alert("Inspekcja zakończona wynikiem POZYTYWNYM.");
      setActiveInspection(null);
    } catch (error) {
      console.error("Failed to pass inspection", error);
    }
  };

  const handleFail = async () => {
    if (!activeInspection) return;
    try {
      await executeInspection(activeInspection.id, 'FAIL', 1);
      // Don't clear activeInspection yet, allow NCR creation
      alert("Inspekcja zakończona wynikiem NEGATYWNYM. Utwórz raport NCR.");
    } catch (error) {
      console.error("Failed to fail inspection", error);
    }
  };

  const handleCreateNcr = async () => {
    if (!activeInspection) return;
    try {
      await createNcr(activeInspection.id, 'DAMAGED', ncrDescription);
      alert("Raport niezgodności (NCR) utworzony.");
      setActiveInspection(null);
      setNcrDescription("");
    } catch (error) {
      console.error("Failed to create NCR", error);
    }
  };

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
            <div className="space-y-2">
              <label className="text-sm font-medium">ID Produktu</label>
              <Input 
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
                placeholder="Np. 101"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">ID Referencyjne (Dostawa/Zwrot)</label>
              <Input 
                value={refId}
                onChange={(e) => setRefId(e.target.value)}
                placeholder="Np. 500"
              />
            </div>
            <Button className="w-full" onClick={handleCreateInspection}>Rozpocznij Inspekcję</Button>
          </div>
        </Card>

        {/* Active Inspection Execution */}
        {activeInspection && (
          <Card className="p-6 border-blue-200 bg-blue-50">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-lg font-semibold text-blue-900">Inspekcja #{activeInspection.id}</h3>
                <p className="text-sm text-blue-700">Produkt: {activeInspection.productId} | Próbka: {activeInspection.sampleSize} szt.</p>
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
                <Button className="flex-1 bg-green-600 hover:bg-green-700" onClick={handlePass}>
                  <CheckCircle size={18} className="mr-2" />
                  ZATWIERDŹ (PASS)
                </Button>
                <Button className="flex-1 bg-red-600 hover:bg-red-700" onClick={handleFail}>
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
                  <Button variant="outline" className="w-full border-red-200 text-red-700 hover:bg-red-50" onClick={handleCreateNcr}>
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
