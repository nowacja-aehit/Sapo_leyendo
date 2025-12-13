import { Button } from "./ui/button";
import { ArrowRight, Bot } from "lucide-react";
import { ImageWithFallback } from "./figma/ImageWithFallback";

interface HeroProps {
  onGetStarted?: () => void;
}

export function Hero({ onGetStarted }: HeroProps) {
  return (
    <section id="home" className="pt-24 pb-16 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-blue-50 via-white to-gray-50">
      <div className="max-w-7xl mx-auto">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Content */}
          <div className="space-y-8">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-blue-100 text-blue-700 rounded-full">
              <Bot size={20} />
              <span>Rozwiązania zasilane AI</span>
            </div>
            
            <div className="space-y-4">
              <h1 className="text-gray-900">
                Witamy w <span className="text-blue-600">Sap Leyendo</span>
              </h1>
              <p className="text-gray-600 max-w-xl">
                Przyszłość zarządzania magazynem jest tutaj. Doświadcz inteligentnej kontroli zapasów, 
                zautomatyzowanej logistyki i analityki predykcyjnej zasilanej zaawansowaną sztuczną inteligencją.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row gap-4">
              <Button size="lg" className="gap-2" onClick={onGetStarted}>
                Rozpocznij
                <ArrowRight size={20} />
              </Button>
              <Button size="lg" variant="outline">
                Dowiedz się więcej
              </Button>
            </div>

            <div className="flex gap-8 pt-4">
              <div>
                <div className="text-blue-600">99.9%</div>
                <div className="text-sm text-gray-600">Wskaźnik dokładności</div>
              </div>
              <div>
                <div className="text-blue-600">50K+</div>
                <div className="text-sm text-gray-600">Śledzone produkty</div>
              </div>
              <div>
                <div className="text-blue-600">24/7</div>
                <div className="text-sm text-gray-600">Monitoring AI</div>
              </div>
            </div>
          </div>

          {/* Right Image */}
          <div className="relative">
            <div className="relative rounded-2xl overflow-hidden shadow-2xl">
              <ImageWithFallback
                src="https://images.unsplash.com/photo-1619070284836-e850273d69ac?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjB3YXJlaG91c2UlMjBsb2dpc3RpY3N8ZW58MXx8fHwxNzYzODU1MDM1fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
                alt="Nowoczesna logistyka magazynowa"
                className="w-full h-auto"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-blue-900/50 to-transparent"></div>
            </div>
            
            {/* Floating Card */}
            <div className="absolute -bottom-6 -left-6 bg-white rounded-xl shadow-xl p-4 border border-gray-200">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <span className="text-green-600">✓</span>
                </div>
                <div>
                  <div className="text-sm text-gray-900">Śledzenie w czasie rzeczywistym</div>
                  <div className="text-xs text-gray-500">Włączone AI</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}