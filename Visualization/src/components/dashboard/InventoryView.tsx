import { useState, useEffect } from "react";
import { Search, Filter, Download, Plus, Package, AlertTriangle, X } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { InventoryItem } from "../../data/mockData";
import { fetchInventory, createInventoryItem, updateInventoryItem } from "../../services/api";
import { getAllLocations, Location } from "../../services/locationService";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "../ui/dialog";
import { Label } from "../ui/label";

export function InventoryView() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("all");
  const [editingItem, setEditingItem] = useState<InventoryItem | null>(null);
  const [editFormData, setEditFormData] = useState<InventoryItem | null>(null);
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [isAddingNew, setIsAddingNew] = useState(false);
  const [locations, setLocations] = useState<Location[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [newCategoryName, setNewCategoryName] = useState("");

  useEffect(() => {
    loadData();
    loadLocations();
  }, []);

  const loadData = async () => {
    const data = await fetchInventory();
    setItems(data);
    const uniqueCategories = Array.from(new Set(data.map(i => i.category).filter(Boolean)));
    setCategories(uniqueCategories.length ? uniqueCategories : ["Electronics", "Accessories", "Bags"]);
  };

  const loadLocations = async () => {
    try {
      const data = await getAllLocations();
      setLocations(data);
    } catch (error) {
      console.error("Failed to load locations", error);
      setLocations([]);
    }
  };

  const filteredItems = items.filter(item => {
    const normalizedTerm = searchTerm.trim().toLowerCase();
    const name = (item.name ?? "").toLowerCase();
    const sku = (item.sku ?? "").toLowerCase();
    const status = item.status ?? "";

    const matchesSearch =
      normalizedTerm.length === 0 ||
      name.includes(normalizedTerm) ||
      sku.includes(normalizedTerm);
    const matchesFilter = filterStatus === "all" || status === filterStatus;
    return matchesSearch && matchesFilter;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case "In Stock": return "default";
      case "Low Stock": return "secondary";
      case "Out of Stock": return "destructive";
      default: return "default";
    }
  };

  const handleEditClick = (item: InventoryItem) => {
    setIsAddingNew(false);
    setEditingItem(item);
    setEditFormData({ ...item });
  };

  const handleAddClick = () => {
    setIsAddingNew(true);
    const newItem: InventoryItem = {
      id: "",
      name: "",
      sku: "",
      category: categories[0] || "",
      quantity: 0,
      reorderLevel: 10,
      location: "",
      status: "In Stock",
      price: 0,
      lastUpdated: new Date().toISOString().split('T')[0]
    };
    setEditingItem(newItem);
    setEditFormData(newItem);
  };

  const handleSaveEdit = async () => {
    if (!editFormData) return;

    try {
      if (isAddingNew) {
        await createInventoryItem(editFormData);
      } else {
        await updateInventoryItem(editFormData.id, editFormData);
      }
      await loadData();
      setEditingItem(null);
      setEditFormData(null);
      setIsAddingNew(false);
    } catch (error) {
      console.error("Failed to save item", error);
      // Here you could add error handling UI
    }
  };

  const handleCloseDialog = () => {
    setEditingItem(null);
    setEditFormData(null);
    setIsAddingNew(false);
  };

  const handleAddCategory = () => {
    if (!newCategoryName.trim()) return;
    const value = newCategoryName.trim();
    setCategories((prev) => Array.from(new Set([...prev, value])));
    if (editFormData) {
      setEditFormData({ ...editFormData, category: value });
    }
    setNewCategoryName("");
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-gray-900 mb-2">Zarządzanie zapasami</h1>
          <p className="text-gray-600">Śledź i zarządzaj zapasami magazynowymi z wglądami zasilanymi AI</p>
        </div>
        <Button className="gap-2" onClick={handleAddClick}>
          <Plus size={20} />
          Dodaj produkt
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <Input
              placeholder="Szukaj po nazwie lub SKU..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={filterStatus} onValueChange={setFilterStatus}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="Filtruj według statusu" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Wszystkie statusy</SelectItem>
              <SelectItem value="In Stock">Na stanie</SelectItem>
              <SelectItem value="Low Stock">Niski stan</SelectItem>
              <SelectItem value="Out of Stock">Brak na stanie</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" className="gap-2">
            <Filter size={20} />
            Więcej filtrów
          </Button>
          <Button variant="outline" className="gap-2">
            <Download size={20} />
            Eksportuj
          </Button>
        </div>
      </Card>

      {/* Stats Summary */}
      <div className="grid sm:grid-cols-3 gap-4">
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <Package className="text-blue-600" size={24} />
            </div>
            <div>
              <div className="text-gray-900">{items.length}</div>
              <div className="text-sm text-gray-600">Wszystkie produkty</div>
            </div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <Package className="text-green-600" size={24} />
            </div>
            <div>
              <div className="text-gray-900">
                {items.filter(i => i.status === "In Stock").length}
              </div>
              <div className="text-sm text-gray-600">Na stanie</div>
            </div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
              <AlertTriangle className="text-orange-600" size={24} />
            </div>
            <div>
              <div className="text-gray-900">
                {items.filter(i => i.status === "Low Stock" || i.status === "Out of Stock").length}
              </div>
              <div className="text-sm text-gray-600">Wymaga uwagi</div>
            </div>
          </div>
        </Card>
      </div>

      {/* Inventory Table */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Produkt</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">SKU</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Kategoria</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Lokalizacja</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Ilość</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Cena</th>
                <th className="px-6 py-3 text-left text-xs text-gray-600 uppercase tracking-wider">Akcje</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredItems.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900">{item.name}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-600">{item.sku}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-600">{item.category}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-600 font-mono">{item.location}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900">{item.quantity}</div>
                    <div className="text-xs text-gray-500">Min: {item.reorderLevel}</div>
                  </td>
                  <td className="px-6 py-4">
                    <Badge variant={getStatusColor(item.status)}>
                      {item.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-900">${item.price}</div>
                  </td>
                  <td className="px-6 py-4">
                    <Button variant="ghost" size="sm" onClick={() => handleEditClick(item)}>Edytuj</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Edit Dialog */}
      <Dialog open={editingItem !== null} onOpenChange={handleCloseDialog}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>{isAddingNew ? "Dodaj nowy produkt" : "Edytuj produkt"}</DialogTitle>
            <DialogDescription>
              {isAddingNew 
                ? "Wprowadź szczegóły nowego produktu. Kliknij zapisz, aby dodać." 
                : "Wprowadź zmiany w szczegółach produktu. Kliknij zapisz, gdy skończysz."}
            </DialogDescription>
          </DialogHeader>
          {editFormData && (
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                  Nazwa
                </Label>
                <Input
                  id="name"
                  value={editFormData.name}
                  onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="sku" className="text-right">
                  SKU
                </Label>
                <Input
                  id="sku"
                  value={editFormData.sku}
                  onChange={(e) => setEditFormData({ ...editFormData, sku: e.target.value })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="category" className="text-right">
                  Kategoria
                </Label>
                <Select
                  value={editFormData.category}
                  onValueChange={(value) => setEditFormData({ ...editFormData, category: value })}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="Wybierz kategorię" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((cat) => (
                      <SelectItem key={cat} value={cat}>{cat}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <span />
                <div className="col-span-3 flex gap-2">
                  <Input
                    placeholder="Dodaj nową kategorię"
                    value={newCategoryName}
                    onChange={(e) => setNewCategoryName(e.target.value)}
                  />
                  <Button variant="outline" onClick={handleAddCategory}>Dodaj</Button>
                </div>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="quantity" className="text-right">
                  Ilość
                </Label>
                <Input
                  id="quantity"
                  type="number"
                  value={editFormData.quantity}
                  onChange={(e) => setEditFormData({ ...editFormData, quantity: parseInt(e.target.value) || 0 })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="price" className="text-right">
                  Cena
                </Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  value={editFormData.price}
                  onChange={(e) => setEditFormData({ ...editFormData, price: parseFloat(e.target.value) || 0 })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="location" className="text-right">
                  Lokalizacja
                </Label>
                <Select
                  value={editFormData.location}
                  onValueChange={(value) => setEditFormData({ ...editFormData, location: value })}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="Wybierz lokalizację" />
                  </SelectTrigger>
                  <SelectContent>
                    {locations.map((loc) => (
                      <SelectItem key={loc.id} value={loc.name}>{loc.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="reorderLevel" className="text-right">
                  Poziom zamówienia
                </Label>
                <Input
                  id="reorderLevel"
                  type="number"
                  value={editFormData.reorderLevel}
                  onChange={(e) => setEditFormData({ ...editFormData, reorderLevel: parseInt(e.target.value) || 0 })}
                  className="col-span-3"
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              Anuluj
            </Button>
            <Button onClick={handleSaveEdit}>
              Zapisz zmiany
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}