import { useState, useEffect } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { ArrowLeft, Bot } from "lucide-react";
import { login } from "../services/api";

interface LoginPageProps {
  onLogin: () => void;
  onBack: () => void;
}

export function LoginPage({ onLogin, onBack }: LoginPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const savedEmail = localStorage.getItem("rememberedEmail");
    if (savedEmail) {
      setEmail(savedEmail);
      setRememberMe(true);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    
    try {
      await login(email, password);
      
      if (rememberMe) {
        localStorage.setItem("rememberedEmail", email);
      } else {
        localStorage.removeItem("rememberedEmail");
      }

      onLogin();
    } catch (err) {
      console.error("Login failed", err);
      setError("Nieprawidłowy email lub hasło");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-gray-50 flex items-center justify-center px-4">
      <Button
        variant="ghost"
        onClick={onBack}
        className="absolute top-4 left-4 gap-2"
      >
        <ArrowLeft size={20} />
        Powrót do strony głównej
      </Button>

      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-4">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-blue-800 rounded-2xl flex items-center justify-center">
              <Bot className="text-white" size={32} />
            </div>
          </div>
          <h1 className="text-gray-900 mb-2">Witaj ponownie</h1>
          <p className="text-gray-600">Zaloguj się, aby uzyskać dostęp do panelu magazynowego zasilanego AI</p>
        </div>

        <div className="bg-white rounded-2xl shadow-xl p-8 border border-gray-200">
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="p-3 text-sm text-red-500 bg-red-50 rounded-lg">
                {error}
              </div>
            )}
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm text-gray-700">
                Adres e-mail
              </label>
              <Input
                id="email"
                type="email"
                placeholder="admin@sapleyendo.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="password" className="text-sm text-gray-700">
                Hasło
              </label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center gap-2 text-gray-600">
                <input 
                  type="checkbox" 
                  className="rounded" 
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                Zapamiętaj mnie
              </label>
              <a href="#" className="text-blue-600 hover:text-blue-700">
                Zapomniałeś hasła?
              </a>
            </div>

            <Button type="submit" size="lg" className="w-full">
              Zaloguj się
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-gray-600">
            Nie masz konta?{" "}
            <a href="#" className="text-blue-600 hover:text-blue-700">
              Skontaktuj się z działem sprzedaży
            </a>
          </div>
        </div>

        <div className="mt-6 text-center text-xs text-gray-500">
          Dane demonstracyjne: Użyj dowolnego e-maila i hasła, aby się zalogować
        </div>
      </div>
    </div>
  );
}