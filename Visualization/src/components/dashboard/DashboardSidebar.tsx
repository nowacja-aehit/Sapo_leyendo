import { LayoutDashboard, Package, ShoppingCart, Truck, BarChart3, Brain, X, ArrowDownToLine, CheckSquare, Box, RotateCcw, ClipboardCheck, MapPin } from "lucide-react";
import { ViewType } from "../Dashboard";

interface DashboardSidebarProps {
  currentView: ViewType;
  onViewChange: (view: ViewType) => void;
  isOpen: boolean;
  onToggle: () => void;
}

const menuItems = [
  { id: "overview" as ViewType, icon: LayoutDashboard, label: "Przegląd" },
  { id: "inventory" as ViewType, icon: Package, label: "Zapasy" },
  { id: "inbound" as ViewType, icon: ArrowDownToLine, label: "Przyjęcia" },
  { id: "picking" as ViewType, icon: CheckSquare, label: "Kompletacja" },
  { id: "packing" as ViewType, icon: Box, label: "Pakowanie" },
  { id: "orders" as ViewType, icon: ShoppingCart, label: "Zamówienia" },
  { id: "shipments" as ViewType, icon: Truck, label: "Przesyłki" },
  { id: "returns" as ViewType, icon: RotateCcw, label: "Zwroty" },
  { id: "qc" as ViewType, icon: ClipboardCheck, label: "Kontrola Jakości" },
  { id: "locations" as ViewType, icon: MapPin, label: "Lokacje" },
  { id: "analytics" as ViewType, icon: BarChart3, label: "Analityka" },
  { id: "ai-insights" as ViewType, icon: Brain, label: "Wglądy AI" },
];

export function DashboardSidebar({ currentView, onViewChange, isOpen, onToggle }: DashboardSidebarProps) {
  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onToggle}
        />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed top-0 left-0 z-50 h-full w-64 bg-white border-r border-gray-200
        transform transition-transform duration-300 lg:translate-x-0
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg flex items-center justify-center">
              <span className="text-white">SL</span>
            </div>
            <div>
              <div className="text-gray-900">Sap Leyendo</div>
              <div className="text-xs text-gray-500">Panel AI</div>
            </div>
          </div>
          <button onClick={onToggle} className="lg:hidden text-gray-500">
            <X size={20} />
          </button>
        </div>

        <nav className="p-4 space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentView === item.id;
            return (
              <button
                key={item.id}
                onClick={() => {
                  onViewChange(item.id);
                  if (window.innerWidth < 1024) onToggle();
                }}
                className={`
                  w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all
                  ${isActive 
                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30' 
                    : 'text-gray-600 hover:bg-gray-100'
                  }
                `}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
          <div className="bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg p-4 text-white">
            <div className="flex items-center gap-2 mb-2">
              <Brain size={20} />
              <span className="text-sm">Status AI</span>
            </div>
            <div className="text-xs text-blue-100">Wszystkie systemy operacyjne</div>
            <div className="mt-2 flex items-center gap-2">
              <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
              <span className="text-xs">Aktywny monitoring</span>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}