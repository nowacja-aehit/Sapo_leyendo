import { useState } from "react";
import { Bell, Search, Menu, LogOut, Package, TrendingUp, AlertTriangle, Truck, X } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Badge } from "../ui/badge";

interface Notification {
  id: number;
  type: "info" | "warning" | "success" | "alert";
  title: string;
  message: string;
  time: string;
  read: boolean;
  icon: any;
}

interface DashboardHeaderProps {
  onLogout: () => void;
}

export function DashboardHeader({ onLogout }: DashboardHeaderProps) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([
    {
      id: 1,
      type: "alert",
      title: "Niski poziom zapasów",
      message: "Kable ładujące USB-C osiągnęły minimalny poziom zapasów (5 jednostek)",
      time: "5 min temu",
      read: false,
      icon: AlertTriangle
    },
    {
      id: 2,
      type: "success",
      title: "Zamówienie dostarczone",
      message: "Zamówienie #ORD-2024-0245 zostało pomyślnie dostarczone",
      time: "1 godz. temu",
      read: false,
      icon: Package
    },
    {
      id: 3,
      type: "info",
      title: "Nowa przesyłka w tranzycie",
      message: "Przesyłka SHP-7890 jest teraz w tranzycie do New York, NY",
      time: "2 godz. temu",
      read: false,
      icon: Truck
    },
    {
      id: 4,
      type: "warning",
      title: "Wzrost popytu",
      message: "AI wykryło 15% wzrost popytu na kategorię Elektronika",
      time: "3 godz. temu",
      read: true,
      icon: TrendingUp
    },
    {
      id: 5,
      type: "success",
      title: "Optymalizacja zakończona",
      message: "Optymalizacja tras magazynowych została pomyślnie ukończona",
      time: "5 godz. temu",
      read: true,
      icon: Package
    }
  ]);

  const unreadCount = notifications.filter(n => !n.read).length;

  const markAsRead = (id: number) => {
    setNotifications(notifications.map(n => 
      n.id === id ? { ...n, read: true } : n
    ));
  };

  const markAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })));
  };

  const deleteNotification = (id: number) => {
    setNotifications(notifications.filter(n => n.id !== id));
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case "alert": return "bg-red-100 text-red-600";
      case "warning": return "bg-yellow-100 text-yellow-600";
      case "success": return "bg-green-100 text-green-600";
      case "info": return "bg-blue-100 text-blue-600";
      default: return "bg-gray-100 text-gray-600";
    }
  };

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div className="flex items-center justify-between px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex items-center gap-4 flex-1">
          <button className="lg:hidden text-gray-600">
            <Menu size={24} />
          </button>
          
          <div className="relative max-w-md w-full hidden sm:block">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <Input
              placeholder="Szukaj zapasów, zamówień, produktów..."
              className="pl-10"
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Notifications Button */}
          <div className="relative">
            <button 
              className="relative p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
              onClick={() => setShowNotifications(!showNotifications)}
            >
              <Bell size={20} />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 w-5 h-5 bg-red-500 rounded-full flex items-center justify-center text-white text-xs">
                  {unreadCount}
                </span>
              )}
            </button>

            {/* Notifications Panel */}
            {showNotifications && (
              <>
                {/* Backdrop */}
                <div 
                  className="fixed inset-0 z-40"
                  onClick={() => setShowNotifications(false)}
                />
                
                {/* Panel */}
                <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-[600px] overflow-hidden flex flex-col">
                  {/* Header */}
                  <div className="p-4 border-b border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="text-gray-900">Powiadomienia</h3>
                      {unreadCount > 0 && (
                        <Badge variant="secondary">{unreadCount} nowe</Badge>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button
                        onClick={markAllAsRead}
                        className="text-sm text-blue-600 hover:text-blue-700"
                      >
                        Oznacz wszystkie jako przeczytane
                      </button>
                    )}
                  </div>

                  {/* Notifications List */}
                  <div className="overflow-y-auto flex-1">
                    {notifications.length === 0 ? (
                      <div className="p-8 text-center text-gray-500">
                        <Bell size={48} className="mx-auto mb-3 text-gray-300" />
                        <p>Brak powiadomień</p>
                      </div>
                    ) : (
                      <div className="divide-y divide-gray-200">
                        {notifications.map((notification) => {
                          const Icon = notification.icon;
                          return (
                            <div
                              key={notification.id}
                              className={`p-4 hover:bg-gray-50 transition-colors cursor-pointer ${
                                !notification.read ? 'bg-blue-50' : ''
                              }`}
                              onClick={() => markAsRead(notification.id)}
                            >
                              <div className="flex gap-3">
                                <div className={`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${getTypeColor(notification.type)}`}>
                                  <Icon size={20} />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="flex items-start justify-between gap-2 mb-1">
                                    <div className="text-sm text-gray-900 flex items-center gap-2">
                                      {notification.title}
                                      {!notification.read && (
                                        <span className="w-2 h-2 bg-blue-600 rounded-full"></span>
                                      )}
                                    </div>
                                    <button
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        deleteNotification(notification.id);
                                      }}
                                      className="text-gray-400 hover:text-gray-600 flex-shrink-0"
                                    >
                                      <X size={14} />
                                    </button>
                                  </div>
                                  <p className="text-xs text-gray-600 mb-1">
                                    {notification.message}
                                  </p>
                                  <p className="text-xs text-gray-400">
                                    {notification.time}
                                  </p>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>

                  {/* Footer */}
                  {notifications.length > 0 && (
                    <div className="p-3 border-t border-gray-200 bg-gray-50">
                      <button className="text-sm text-blue-600 hover:text-blue-700 w-full text-center">
                        Zobacz wszystkie powiadomienia
                      </button>
                    </div>
                  )}
                </div>
              </>
            )}
          </div>

          <div className="hidden sm:flex items-center gap-3 pl-3 border-l border-gray-200">
            <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white">
              AD
            </div>
            <div className="hidden md:block">
              <div className="text-sm text-gray-900">Użytkownik Administrator</div>
              <div className="text-xs text-gray-500">admin@sapleyendo.com</div>
            </div>
          </div>

          <Button variant="ghost" size="sm" onClick={onLogout} className="gap-2">
            <LogOut size={16} />
            <span className="hidden sm:inline">Wyloguj</span>
          </Button>
        </div>
      </div>
    </header>
  );
}