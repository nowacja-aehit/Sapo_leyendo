import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";
import { Mail, Phone, MapPin } from "lucide-react";

export function Contact() {
  return (
    <section id="contact" className="py-20 px-4 sm:px-6 lg:px-8 bg-white">
      <div className="max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-gray-900 mb-4">
            Skontaktuj się z nami
          </h2>
          <p className="text-gray-600">
            Gotowy, aby przekształcić operacje magazynowe dzięki AI? 
            Skontaktuj się z nami już dziś, aby uzyskać spersonalizowaną konsultację.
          </p>
        </div>

        <div className="grid lg:grid-cols-2 gap-12">
          {/* Contact Form */}
          <div className="bg-gray-50 rounded-2xl p-8">
            <form className="space-y-6">
              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label htmlFor="name" className="text-sm text-gray-700">
                    Imię i nazwisko
                  </label>
                  <Input id="name" placeholder="Twoje imię i nazwisko" />
                </div>
                <div className="space-y-2">
                  <label htmlFor="email" className="text-sm text-gray-700">
                    E-mail
                  </label>
                  <Input id="email" type="email" placeholder="twoj@email.com" />
                </div>
              </div>
              
              <div className="space-y-2">
                <label htmlFor="company" className="text-sm text-gray-700">
                  Firma
                </label>
                <Input id="company" placeholder="Nazwa Twojej firmy" />
              </div>

              <div className="space-y-2">
                <label htmlFor="message" className="text-sm text-gray-700">
                  Wiadomość
                </label>
                <Textarea 
                  id="message" 
                  placeholder="Opowiedz nam o swoich potrzebach magazynowych..."
                  rows={6}
                />
              </div>

              <Button type="submit" size="lg" className="w-full">
                Wyślij wiadomość
              </Button>
            </form>
          </div>

          {/* Contact Information */}
          <div className="space-y-8">
            <div>
              <h3 className="text-gray-900 mb-6">Informacje kontaktowe</h3>
              <div className="space-y-6">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Mail className="text-blue-600" size={24} />
                  </div>
                  <div>
                    <div className="text-gray-900">E-mail</div>
                    <p className="text-gray-600">contact@sapleyendo.com</p>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Phone className="text-blue-600" size={24} />
                  </div>
                  <div>
                    <div className="text-gray-900">Telefon</div>
                    <p className="text-gray-600">+1 (555) 123-4567</p>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <MapPin className="text-blue-600" size={24} />
                  </div>
                  <div>
                    <div className="text-gray-900">Lokalizacja</div>
                    <p className="text-gray-600">
                      ul. Logistyczna 123<br />
                      Dzielnica Innowacji<br />
                      Tech City, TC 12345
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-blue-600 to-blue-800 rounded-2xl p-8 text-white">
              <h3 className="mb-4">Dlaczego warto wybrać Sap Leyendo?</h3>
              <ul className="space-y-3 text-sm text-blue-100">
                <li>• Wiodąca w branży technologia AI</li>
                <li>• Wsparcie klienta 24/7</li>
                <li>• Skalowalne rozwiązania dla każdej wielkości</li>
                <li>• Udowodniony ROI w ciągu 6 miesięcy</li>
                <li>• Bezproblemowa integracja z istniejącymi systemami</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}