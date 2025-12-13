import { ImageWithFallback } from "./figma/ImageWithFallback";

const stats = [
  {
    value: "500+",
    label: "Klientów na całym świecie",
    description: "Zaufany przez firmy na całym globie",
  },
  {
    value: "2M+",
    label: "Przetwarzanych produktów dziennie",
    description: "Obsługa ogromnych wolumenów",
  },
  {
    value: "35%",
    label: "Wzrost efektywności",
    description: "Średni wskaźnik poprawy",
  },
  {
    value: "24/7",
    label: "Monitoring AI",
    description: "Ciągła optymalizacja",
  },
];

export function Stats() {
  return (
    <section className="py-20 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background Image with Overlay */}
      <div className="absolute inset-0 z-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1664382953403-fc1ac77073a0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxpbnZlbnRvcnklMjBtYW5hZ2VtZW50fGVufDF8fHx8MTc2Mzg4MTExOXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Zarządzanie zapasami"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-blue-900/90"></div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto relative z-10">
        <div className="text-center mb-16">
          <h2 className="text-white mb-4">
            Zaufany przez liderów branży
          </h2>
          <p className="text-blue-100 max-w-2xl mx-auto">
            Nasze rozwiązania magazynowe zasilane AI dostarczają mierzalne rezultaty, 
            które przekształcają operacje biznesowe.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {stats.map((stat) => (
            <div key={stat.label} className="text-center space-y-2">
              <div className="text-white">{stat.value}</div>
              <div className="text-blue-200">{stat.label}</div>
              <p className="text-sm text-blue-300">{stat.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}