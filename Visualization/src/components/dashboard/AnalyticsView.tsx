import { TrendingUp, TrendingDown, DollarSign, Package, ShoppingCart } from "lucide-react";
import { Card } from "../ui/card";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

const salesData = [
  { month: "Sty", sales: 45000, orders: 234 },
  { month: "Lut", sales: 52000, orders: 267 },
  { month: "Mar", sales: 48000, orders: 245 },
  { month: "Kwi", sales: 61000, orders: 312 },
  { month: "Maj", sales: 55000, orders: 289 },
  { month: "Cze", sales: 67000, orders: 345 },
];

const categoryData = [
  { name: "Elektronika", value: 45 },
  { name: "Akcesoria", value: 30 },
  { name: "Torby", value: 15 },
  { name: "Inne", value: 10 },
];

const COLORS = ["#3B82F6", "#10B981", "#F59E0B", "#6B7280"];

export function AnalyticsView() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-gray-900 mb-2">Analityka i raporty</h1>
        <p className="text-gray-600">Kompleksowe wglądy zasilane analityką AI</p>
      </div>

      {/* KPI Cards */}
      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <DollarSign className="text-blue-600" size={24} />
            </div>
            <div className="flex items-center gap-1 text-green-600 text-sm">
              <TrendingUp size={16} />
              <span>+12.5%</span>
            </div>
          </div>
          <div className="text-gray-900 mb-1">$328,000</div>
          <div className="text-sm text-gray-600">Całkowity przychód</div>
        </Card>

        <Card className="p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <ShoppingCart className="text-green-600" size={24} />
            </div>
            <div className="flex items-center gap-1 text-green-600 text-sm">
              <TrendingUp size={16} />
              <span>+8.2%</span>
            </div>
          </div>
          <div className="text-gray-900 mb-1">1,692</div>
          <div className="text-sm text-gray-600">Wszystkie zamówienia</div>
        </Card>

        <Card className="p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <Package className="text-purple-600" size={24} />
            </div>
            <div className="flex items-center gap-1 text-red-600 text-sm">
              <TrendingDown size={16} />
              <span>-3.1%</span>
            </div>
          </div>
          <div className="text-gray-900 mb-1">89.5%</div>
          <div className="text-sm text-gray-600">Wskaźnik realizacji</div>
        </Card>

        <Card className="p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
              <TrendingUp className="text-orange-600" size={24} />
            </div>
            <div className="flex items-center gap-1 text-green-600 text-sm">
              <TrendingUp size={16} />
              <span>+15.3%</span>
            </div>
          </div>
          <div className="text-gray-900 mb-1">2.3 dni</div>
          <div className="text-sm text-gray-600">Śr. czas przetwarzania</div>
        </Card>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Sales Trend */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Trend sprzedaży</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={salesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="sales" stroke="#3B82F6" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        {/* Orders by Month */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Zamówienia według miesiąca</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={salesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="orders" fill="#10B981" />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        {/* Category Distribution */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Zapasy według kategorii</h2>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </Card>

        {/* Top Products */}
        <Card className="p-6">
          <h2 className="text-gray-900 mb-4">Najlepiej sprzedające się produkty</h2>
          <div className="space-y-4">
            {[
              { name: "Bezprzewodowe słuchawki Bluetooth", sales: 245, trend: "+12%" },
              { name: "Smartwatch Series 5", sales: 189, trend: "+8%" },
              { name: "Bezprzewodowa mysz", sales: 156, trend: "+15%" },
              { name: "Etui na telefon Premium", sales: 134, trend: "+5%" },
              { name: "Przenośny powerbank", sales: 98, trend: "+22%" },
            ].map((product, index) => (
              <div key={index} className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="text-sm text-gray-900">{product.name}</div>
                  <div className="text-xs text-gray-500">{product.sales} sprzedanych jednostek</div>
                </div>
                <div className="text-sm text-green-600">{product.trend}</div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}