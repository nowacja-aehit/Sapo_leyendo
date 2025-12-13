import { useState } from "react";
import { DashboardSidebar } from "./dashboard/DashboardSidebar";
import { DashboardHeader } from "./dashboard/DashboardHeader";
import { DashboardOverview } from "./dashboard/DashboardOverview";
import { InventoryView } from "./dashboard/InventoryView";
import { OrdersView } from "./dashboard/OrdersView";
import { AnalyticsView } from "./dashboard/AnalyticsView";
import { AIInsightsView } from "./dashboard/AIInsightsView";
import { ShipmentsView } from "./dashboard/ShipmentsView";
import { InboundView } from "./dashboard/InboundView";
import { PickingView } from "./dashboard/PickingView";
import { PackingView } from "./dashboard/PackingView";
import { ReturnsView } from "./dashboard/ReturnsView";
import { QualityControlView } from "./dashboard/QualityControlView";
import { LocationView } from "./dashboard/LocationView";

interface DashboardProps {
  onLogout: () => void;
}

export type ViewType = "overview" | "inventory" | "orders" | "shipments" | "analytics" | "ai-insights" | "inbound" | "picking" | "packing" | "returns" | "qc" | "locations";

export function Dashboard({ onLogout }: DashboardProps) {
  const [currentView, setCurrentView] = useState<ViewType>("overview");
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const renderView = () => {
    switch (currentView) {
      case "overview":
        return <DashboardOverview onNavigate={setCurrentView} />;
      case "inventory":
        return <InventoryView />;
      case "inbound":
        return <InboundView />;
      case "picking":
        return <PickingView />;
      case "packing":
        return <PackingView />;
      case "orders":
        return <OrdersView />;
      case "shipments":
        return <ShipmentsView />;
      case "returns":
        return <ReturnsView />;
      case "qc":
        return <QualityControlView />;
      case "locations":
        return <LocationView />;
      case "analytics":
        return <AnalyticsView />;
      case "ai-insights":
        return <AIInsightsView />;
      default:
        return <DashboardOverview onNavigate={setCurrentView} />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <DashboardSidebar
        currentView={currentView}
        onViewChange={setCurrentView}
        isOpen={sidebarOpen}
        onToggle={() => setSidebarOpen(!sidebarOpen)}
      />
      
      <div className={`transition-all duration-300 ${sidebarOpen ? "lg:ml-64" : "lg:ml-0"}`}>
        <DashboardHeader onLogout={onLogout} />
        
        <main className="p-4 sm:p-6 lg:p-8">
          {renderView()}
        </main>
      </div>
    </div>
  );
}
