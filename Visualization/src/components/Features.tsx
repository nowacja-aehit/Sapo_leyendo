import { Brain, BarChart3, ShieldCheck, Zap, Package, TrendingUp } from "lucide-react";

const features = [
  {
    icon: Brain,
    title: "Inteligencja zasilana AI",
    description: "Zaawansowane algorytmy uczenia maszynowego optymalizują każdy aspekt operacji magazynowych w czasie rzeczywistym.",
  },
  {
    icon: BarChart3,
    title: "Analityka predykcyjna",
    description: "Prognozuj popyt, optymalizuj poziomy zapasów i zapobiegaj brakom towaru, zanim się pojawią.",
  },
  {
    icon: ShieldCheck,
    title: "Zwiększone bezpieczeństwo",
    description: "Wielowarstwowe systemy bezpieczeństwa AI chronią Twoje zapasy dzięki rozpoznawaniu twarzy i wykrywaniu anomalii.",
  },
  {
    icon: Zap,
    title: "Automatyczne przetwarzanie",
    description: "Błyskawiczne przetwarzanie zamówień z automatycznym sortowaniem, pakowaniem i wysyłką.",
  },
  {
    icon: Package,
    title: "Inteligentne zapasy",
    description: "System inteligentnego śledzenia zapewnia widoczność każdego produktu w magazynie w czasie rzeczywistym.",
  },
  {
    icon: TrendingUp,
    title: "Optymalizacja wydajności",
    description: "Algorytmy ciągłego uczenia zwiększają efektywność i obniżają koszty operacyjne w czasie.",
  },
];

export function Features() {
  return (
    <section id="features" className="py-20 px-4 sm:px-6 lg:px-8 bg-white">
      <div className="max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-gray-900 mb-4">
            Zasilane sztuczną inteligencją
          </h2>
          <p className="text-gray-600">
            Nasza platforma napędzana AI przekształca tradycyjne operacje magazynowe w 
            inteligentne, samooptymalizujące się systemy dostosowujące się do Twoich potrzeb biznesowych.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature) => {
            const Icon = feature.icon;
            return (
              <div
                key={feature.title}
                className="group p-6 rounded-xl border border-gray-200 hover:border-blue-600 hover:shadow-lg transition-all duration-300"
              >
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 group-hover:bg-blue-600 transition-colors">
                  <Icon className="text-blue-600 group-hover:text-white transition-colors" size={24} />
                </div>
                <h3 className="text-gray-900 mb-2">{feature.title}</h3>
                <p className="text-sm text-gray-600">{feature.description}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}