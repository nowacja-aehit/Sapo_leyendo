import { useState, useEffect } from "react";
import { MapPin, Plus, LayoutGrid, Settings, Trash2 } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "../ui/dialog";
import { Label } from "../ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { getAllLocations, getAllZones, getAllLocationTypes, createLocation, deleteLocation, Location, Zone, LocationType } from "../../services/locationService";

export function LocationView() {
  const [locations, setLocations] = useState<Location[]>([]);
  const [zones, setZones] = useState<Zone[]>([]);
  const [types, setTypes] = useState<LocationType[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newZoneName, setNewZoneName] = useState("");
  const [newTypeName, setNewTypeName] = useState("");
  
  // Form State
  const [newName, setNewName] = useState("");
  const [selectedZone, setSelectedZone] = useState("");
  const [selectedType, setSelectedType] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [locs, zns, tps] = await Promise.all([
        getAllLocations(),
        getAllZones(),
        getAllLocationTypes()
      ]);
      setLocations(locs);
      setZones(zns);
      setTypes(tps);
    } catch (error) {
      console.error("Failed to load location data", error);
    }
  };

  const handleCreateLocation = async () => {
    try {
      await createLocation({
        name: newName,
        zone: zones.find(z => z.id.toString() === selectedZone),
        locationType: types.find(t => t.id.toString() === selectedType),
        status: 'ACTIVE'
      });
      setIsDialogOpen(false);
      setNewName("");
      setSelectedZone("");
      setSelectedType("");
      loadData();
    } catch (error) {
      console.error("Failed to create location", error);
      alert("Błąd tworzenia lokalizacji.");
    }
  };

  const handleDeleteLocation = async (id: number) => {
    try {
      await deleteLocation(id);
    } catch (error) {
      console.error("Failed to delete location", error);
    } finally {
      setLocations((prev) => prev.filter((l) => l.id !== id));
    }
  };

  const handleQuickAddZone = () => {
    if (!newZoneName.trim()) return;
    const newZone: Zone = {
      id: Date.now(),
      name: newZoneName.trim(),
      isSecure: false,
      isTemperatureControlled: false,
    };
    setZones((prev) => [...prev, newZone]);
    setSelectedZone(newZone.id.toString());
    setNewZoneName("");
  };

  const handleQuickAddType = () => {
    if (!newTypeName.trim()) return;
    const newType: LocationType = {
      id: Date.now(),
      name: newTypeName.trim(),
    };
    setTypes((prev) => [...prev, newType]);
    setSelectedType(newType.id.toString());
    setNewTypeName("");
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Zarządzanie Lokalizacjami</h1>
          <p className="text-gray-600">Definiuj strefy, aleje i miejsca składowania</p>
        </div>
        <Button className="gap-2" onClick={() => setIsDialogOpen(true)}>
          <Plus size={20} />
          Nowa Lokalizacja
        </Button>
      </div>

      {/* Stats Overview */}
      <Card className="p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-3 md:gap-4 items-stretch">
          <div className="flex-1 p-3 bg-blue-100 rounded-lg text-blue-600 flex items-center gap-3">
            <MapPin size={24} />
            <div>
              <p className="text-sm text-gray-500">Wszystkie Lokalizacje</p>
              <p className="text-2xl font-bold">{locations.length}</p>
            </div>
          </div>
          <div className="flex-1 p-3 bg-purple-100 rounded-lg text-purple-600 flex items-center gap-3">
            <LayoutGrid size={24} />
            <div>
              <p className="text-sm text-gray-500">Strefy Magazynowe</p>
              <p className="text-2xl font-bold">{zones.length}</p>
            </div>
          </div>
          <div className="flex-1 p-3 bg-green-100 rounded-lg text-green-600 flex items-center gap-3">
            <Settings size={24} />
            <div>
              <p className="text-sm text-gray-500">Typy Regałów</p>
              <p className="text-2xl font-bold">{types.length}</p>
            </div>
          </div>
        </div>
      </Card>

      {/* Locations Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
        {locations.map((loc) => (
          <Card key={loc.id} className="p-3 hover:shadow-md transition-shadow flex items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <h3 className="font-bold text-lg text-gray-800 whitespace-nowrap">{loc.name}</h3>
              <Badge variant={loc.status === 'ACTIVE' ? 'default' : 'secondary'}>
                {loc.status}
              </Badge>
            </div>
            <div className="flex items-center gap-4 text-sm text-gray-700 flex-wrap">
              <span className="font-medium">{loc.zone?.name || 'Brak strefy'}</span>
              <span className="text-gray-400">•</span>
              <span className="font-medium">{loc.locationType?.name || 'Brak typu'}</span>
              {loc.barcode && (
                <span className="font-mono text-xs bg-gray-100 px-2 py-1 rounded">{loc.barcode}</span>
              )}
              <Button
                variant="ghost"
                size="sm"
                aria-label={`Usuń ${loc.name}`}
                onClick={() => handleDeleteLocation(loc.id)}
              >
                <Trash2 size={14} />
              </Button>
            </div>
          </Card>
        ))}
      </div>

      {/* Create Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Dodaj nową lokalizację</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Nazwa Lokalizacji</Label>
              <Input 
                placeholder="np. A-01-01-01" 
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Strefa</Label>
              <Select value={selectedZone} onValueChange={setSelectedZone}>
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz strefę" />
                </SelectTrigger>
                <SelectContent>
                  {zones.map(z => (
                    <SelectItem key={z.id} value={z.id.toString()}>{z.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="flex gap-2 mt-2">
                <Input placeholder="Nowa strefa" value={newZoneName} onChange={(e) => setNewZoneName(e.target.value)} />
                <Button variant="outline" onClick={handleQuickAddZone}>Dodaj</Button>
              </div>
            </div>
            <div className="space-y-2">
              <Label>Typ Lokalizacji</Label>
              <Select value={selectedType} onValueChange={setSelectedType}>
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz typ" />
                </SelectTrigger>
                <SelectContent>
                  {types.map(t => (
                    <SelectItem key={t.id} value={t.id.toString()}>{t.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="flex gap-2 mt-2">
                <Input placeholder="Nowy typ" value={newTypeName} onChange={(e) => setNewTypeName(e.target.value)} />
                <Button variant="outline" onClick={handleQuickAddType}>Dodaj</Button>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDialogOpen(false)}>Anuluj</Button>
            <Button onClick={handleCreateLocation}>Utwórz</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
