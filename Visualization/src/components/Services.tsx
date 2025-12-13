import { ImageWithFallback } from "./figma/ImageWithFallback";
import { Check } from "lucide-react";
import { Button } from "./ui/button";

const services = [
  "Zarządzanie zapasami zasilane AI",
  "Automatyczne realizowanie zamówień",
  "Predykcyjne prognozowanie popytu",
  "Śledzenie i monitorowanie w czasie rzeczywistym",
  "Inteligentna kontrola jakości",
  "Inteligentna optymalizacja tras",
];

export function Services() {
  return (
    <section id="services" className="py-20 px-4 sm:px-6 lg:px-8 bg-gray-50">
      <div className="max-w-7xl mx-auto">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Image */}
          <div className="relative order-2 lg:order-1">
            <div className="rounded-2xl overflow-hidden shadow-xl">
              <ImageWithFallback
                src="https://images.unsplash.com/photo-1761195696590-3490ea770aa1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3YXJlaG91c2UlMjB0ZWNobm9sb2d5JTIwYXV0b21hdGlvbnxlbnwxfHx8fDE3NjM4ODc5MTB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
                alt="Technologia i automatyzacja magazynowa"
                className="w-full h-auto"
              />
            </div>
            
            {/* Floating Stats */}
            <div className="absolute -top-6 -right-6 bg-white rounded-xl shadow-xl p-6 border border-gray-200">
              <div className="text-center">
                <div className="text-blue-600">40%</div>
                <div className="text-xs text-gray-600">Redukcja kosztów</div>
              </div>
            </div>
          </div>

          {/* Right Content */}
          <div className="space-y-6 order-1 lg:order-2">
            <div className="space-y-4">
              <h2 className="text-gray-900">
                Kompleksowe rozwiązania magazynowe
              </h2>
              <p className="text-gray-600">
                Nasze usługi zasilane AI obejmują każdy aspekt nowoczesnego zarządzania magazynem, 
                od przyjęcia do wysyłki, z inteligentną automatyzacją na każdym kroku.
              </p>
            </div>

            <div className="space-y-3">
              {services.map((service) => (
                <div key={service} className="flex items-start gap-3">
                  <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                    <Check className="text-blue-600" size={16} />
                  </div>
                  <span className="text-gray-700">{service}</span>
                </div>
              ))}
            </div>

            <div className="pt-4">
              <Button size="lg">Poznaj wszystkie usługi</Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}