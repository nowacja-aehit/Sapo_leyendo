import { Facebook, Twitter, Linkedin, Instagram } from "lucide-react";

const footerLinks = {
  Firma: ["O nas", "Kariera", "Prasa", "Blog"],
  Rozwiązania: ["Zarządzanie zapasami", "Realizacja zamówień", "Analityka", "Integracja"],
  Zasoby: ["Dokumentacja", "Odniesienie API", "Studia przypadków", "Wsparcie"],
  Prawne: ["Polityka prywatności", "Warunki usługi", "Bezpieczeństwo", "Zgodność"],
};

export function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="grid md:grid-cols-2 lg:grid-cols-6 gap-8 mb-8">
          {/* Brand */}
          <div className="lg:col-span-2">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg flex items-center justify-center">
                <span className="text-white">SL</span>
              </div>
              <div>
                <div className="text-white">Sap Leyendo</div>
                <div className="text-xs text-gray-400">Magazyn zasilany AI</div>
              </div>
            </div>
            <p className="text-sm text-gray-400 mb-4">
              Przekształcamy operacje magazynowe za pomocą najnowocześniejszej technologii sztucznej inteligencji.
            </p>
            <div className="flex gap-3">
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-lg flex items-center justify-center hover:bg-blue-600 transition-colors">
                <Facebook size={18} />
              </a>
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-lg flex items-center justify-center hover:bg-blue-600 transition-colors">
                <Twitter size={18} />
              </a>
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-lg flex items-center justify-center hover:bg-blue-600 transition-colors">
                <Linkedin size={18} />
              </a>
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-lg flex items-center justify-center hover:bg-blue-600 transition-colors">
                <Instagram size={18} />
              </a>
            </div>
          </div>

          {/* Links */}
          {Object.entries(footerLinks).map(([category, links]) => (
            <div key={category}>
              <h4 className="text-white mb-4">{category}</h4>
              <ul className="space-y-2">
                {links.map((link) => (
                  <li key={link}>
                    <a href="#" className="text-sm text-gray-400 hover:text-blue-400 transition-colors">
                      {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-gray-800 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-sm text-gray-400">
              © 2025 Sap Leyendo. Wszelkie prawa zastrzeżone.
            </p>
            <p className="text-sm text-gray-400">
              Zasilane zaawansowaną technologią AI
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}