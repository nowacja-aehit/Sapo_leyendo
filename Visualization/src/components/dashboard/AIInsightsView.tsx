import { useState } from "react";
import { Brain, TrendingUp, AlertTriangle, Lightbulb, Zap, Target, CheckCircle } from "lucide-react";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "../ui/dialog";

interface Insight {
  icon: any;
  title: string;
  description: string;
  impact: string;
  color: string;
  recommendation: string;
}

export function AIInsightsView() {
  const [selectedInsight, setSelectedInsight] = useState<Insight | null>(null);
  const [actionDialog, setActionDialog] = useState<Insight | null>(null);

  const insights: Insight[] = [
    {
      icon: TrendingUp,
      title: "Prognoza popytu",
      description: "AI przewiduje 15% wzrost popytu na kategorię Elektronika w ciągu najbliższych 2 tygodni.",
      impact: "Wysoki",
      color: "blue",
      recommendation: "Rozważ zwiększenie poziomów zapasów dla bezprzewodowych słuchawek i smartwatchy."
    },
    {
      icon: AlertTriangle,
      title: "Alert zapasów",
      description: "Kable ładujące USB-C są niedostępne. Dane historyczne pokazują wysoki popyt w weekendy.",
      impact: "Krytyczny",
      color: "red",
      recommendation: "Pilne uzupełnienie zalecane. Złóż pilne zamówienie u dostawcy."
    },
    {
      icon: Lightbulb,
      title: "Możliwość optymalizacji",
      description: "Reorganizacja produktów w Sekcji A może skrócić czas kompletacji o 18%.",
      impact: "Średni",
      color: "purple",
      recommendation: "Zaplanuj reorganizację magazynu w godzinach mniejszego ruchu."
    },
    {
      icon: Zap,
      title: "Zysk efektywności",
      description: "Optymalizacja tras zasilana AI skróciła średni czas przetwarzania z 2.8 do 2.3 dni.",
      impact: "Pozytywny",
      color: "green",
      recommendation: "Kontynuuj obecne strategie optymalizacji."
    },
    {
      icon: Target,
      title: "Wzorzec sezonowy",
      description: "Analiza pokazuje 23% wzrost sprzedaży plecaków na laptopy w okresie listopad-grudzień.",
      impact: "Średni",
      color: "orange",
      recommendation: "Przygotuj zwiększone zapasy na nadchodzący sezonowy popyt."
    },
  ];

  const predictions = [
    { item: "Bezprzewodowe słuchawki Bluetooth", currentStock: 245, predicted: 412, confidence: 94 },
    { item: "Smartwatch Series 5", currentStock: 32, predicted: 87, confidence: 89 },
    { item: "Klawiatura mechaniczna RGB", currentStock: 48, predicted: 102, confidence: 91 },
    { item: "Przenośny powerbank 20000mAh", currentStock: 89, predicted: 145, confidence: 87 },
  ];

  const getImpactColor = (impact: string) => {
    switch (impact) {
      case "Krytyczny": return "destructive";
      case "Wysoki": return "default";
      case "Średni": return "secondary";
      case "Pozytywny": return "default";
      default: return "default";
    }
  };

  const handleLearnMore = (insight: Insight) => {
    setSelectedInsight(insight);
  };

  const handleTakeAction = (insight: Insight) => {
    setActionDialog(insight);
  };

  const handleConfirmAction = () => {
    // W prawdziwej aplikacji tutaj byłoby wywołanie API
    console.log("Akcja zatwierdzona dla:", actionDialog?.title);
    setActionDialog(null);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-gray-900 mb-2">Wglądy i prognozy AI</h1>
        <p className="text-gray-600">Zaawansowana analityka i rekomendacje zasilane uczeniem maszynowym</p>
      </div>

      {/* AI Status */}
      <Card className="p-6 bg-gradient-to-br from-blue-600 to-blue-800 text-white">
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 bg-white/20 rounded-xl flex items-center justify-center">
              <Brain size={32} />
            </div>
            <div>
              <h2 className="mb-2">Status systemu AI</h2>
              <p className="text-blue-100 mb-4">
                Wszystkie modele AI są aktywne i nieprzerwanie analizują dane magazynowe. 
                Ostatnia aktualizacja modelu: 2 godziny temu.
              </p>
              <div className="flex gap-4 text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                  <span>Analityka predykcyjna: Aktywna</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                  <span>Rozpoznawanie wzorców: Aktywne</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                  <span>Optymalizacja: Aktywna</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Insights Grid */}
      <div className="space-y-4">
        <h2 className="text-gray-900">Wglądy wygenerowane przez AI</h2>
        {insights.map((insight, index) => {
          const Icon = insight.icon;
          return (
            <Card key={index} className="p-6">
              <div className="flex items-start gap-4">
                <div className={`w-12 h-12 bg-${insight.color}-100 rounded-lg flex items-center justify-center flex-shrink-0`}>
                  <Icon className={`text-${insight.color}-600`} size={24} />
                </div>
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="text-gray-900">{insight.title}</h3>
                    <Badge variant={getImpactColor(insight.impact)}>
                      Wpływ: {insight.impact}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-3">{insight.description}</p>
                  <div className="bg-gray-50 rounded-lg p-3 mb-3">
                    <div className="text-sm text-gray-900 mb-1">Rekomendacja AI:</div>
                    <p className="text-sm text-gray-600">{insight.recommendation}</p>
                  </div>
                  <div className="flex gap-2">
                    <Button size="sm" onClick={() => handleTakeAction(insight)}>
                      Podejmij działanie
                    </Button>
                    <Button size="sm" variant="outline" onClick={() => handleLearnMore(insight)}>
                      Dowiedz się więcej
                    </Button>
                  </div>
                </div>
              </div>
            </Card>
          );
        })}
      </div>

      {/* Demand Predictions */}
      <Card className="p-6">
        <h2 className="text-gray-900 mb-4">Prognozy popytu (Kolejne 7 dni)</h2>
        <div className="space-y-4">
          {predictions.map((pred, index) => (
            <div key={index}>
              <div className="flex items-center justify-between mb-2">
                <div>
                  <div className="text-sm text-gray-900">{pred.item}</div>
                  <div className="text-xs text-gray-500">
                    Aktualnie: {pred.currentStock} → Prognoza: {pred.predicted} jednostek
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-sm text-gray-900">{pred.confidence}%</div>
                  <div className="text-xs text-gray-500">Pewność</div>
                </div>
              </div>
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-blue-600 rounded-full"
                  style={{ width: `${pred.confidence}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Learn More Dialog */}
      <Dialog open={selectedInsight !== null} onOpenChange={() => setSelectedInsight(null)}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>{selectedInsight?.title}</DialogTitle>
            <DialogDescription>
              Szczegółowa analiza i rekomendacje AI
            </DialogDescription>
          </DialogHeader>
          {selectedInsight && (
            <div className="space-y-6">
              {/* Impact Badge */}
              <div className="flex items-center justify-between">
                <Badge variant={getImpactColor(selectedInsight.impact)} className="text-sm">
                  Wpływ: {selectedInsight.impact}
                </Badge>
                <div className={`w-12 h-12 bg-${selectedInsight.color}-100 rounded-lg flex items-center justify-center`}>
                  {selectedInsight.icon && <selectedInsight.icon className={`text-${selectedInsight.color}-600`} size={24} />}
                </div>
              </div>

              {/* Description */}
              <div>
                <h3 className="text-gray-900 mb-2">Opis</h3>
                <p className="text-gray-600">{selectedInsight.description}</p>
              </div>

              {/* Recommendation */}
              <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                <h3 className="text-gray-900 mb-2">Rekomendacja AI</h3>
                <p className="text-gray-600">{selectedInsight.recommendation}</p>
              </div>

              {/* Additional Insights */}
              <div>
                <h3 className="text-gray-900 mb-3">Dodatkowe informacje</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li className="flex items-start gap-2">
                    <CheckCircle className="text-green-600 mt-0.5 flex-shrink-0" size={16} />
                    <span>Analiza oparta na danych z ostatnich 6 miesięcy</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <CheckCircle className="text-green-600 mt-0.5 flex-shrink-0" size={16} />
                    <span>Uwzględnia trendy sezonowe i wzorce zakupowe</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <CheckCircle className="text-green-600 mt-0.5 flex-shrink-0" size={16} />
                    <span>Model AI zaktualizowany 2 godziny temu</span>
                  </li>
                </ul>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedInsight(null)}>
              Zamknij
            </Button>
            <Button onClick={() => {
              setSelectedInsight(null);
              if (selectedInsight) handleTakeAction(selectedInsight);
            }}>
              Podejmij działanie
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Take Action Dialog */}
      <Dialog open={actionDialog !== null} onOpenChange={() => setActionDialog(null)}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Potwierdź działanie</DialogTitle>
            <DialogDescription>
              Czy chcesz wdrożyć rekomendację AI?
            </DialogDescription>
          </DialogHeader>
          {actionDialog && (
            <div className="space-y-4">
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="text-sm text-gray-900 mb-2">{actionDialog.title}</div>
                <p className="text-sm text-gray-600">{actionDialog.recommendation}</p>
              </div>

              <div className="border-l-4 border-blue-600 pl-4 py-2">
                <p className="text-sm text-gray-600">
                  System automatycznie utworzy zadania i powiadomienia dla odpowiednich zespołów.
                </p>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setActionDialog(null)}>
              Anuluj
            </Button>
            <Button onClick={handleConfirmAction}>
              Potwierdź i wdróż
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}