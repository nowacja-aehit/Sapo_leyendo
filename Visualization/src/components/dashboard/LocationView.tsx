import { useState, useEffect } from "react";
import { MapPin, Plus, LayoutGrid, Settings } from "lucide-react";
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
import { getAllLocations, getAllZones, getAllLocationTypes, createLocation, Location, Zone, LocationType } from "../../services/locationService";

export function LocationView() {
  const [locations, setLocations] = useState<Location[]>([]);
  const [zones, setZones] = useState<Zone[]>([]);
  const [types, setTypes] = useState<LocationType[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  
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
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <Card className="p-4 flex items-center gap-4">
          <div className="p-3 bg-blue-100 rounded-lg text-blue-600">
            <MapPin size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500">Wszystkie Lokalizacje</p>
            <p className="text-2xl font-bold">{locations.length}</p>
          </div>
        </Card>
        <Card className="p-4 flex items-center gap-4">
          <div className="p-3 bg-purple-100 rounded-lg text-purple-600">
            <LayoutGrid size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500">Strefy Magazynowe</p>
            <p className="text-2xl font-bold">{zones.length}</p>
          </div>
        </Card>
        <Card className="p-4 flex items-center gap-4">
          <div className="p-3 bg-green-100 rounded-lg text-green-600">
            <Settings size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500">Typy Regałów</p>
            <p className="text-2xl font-bold">{types.length}</p>
          </div>
        </Card>
      </div>

      {/* Locations Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {locations.map((loc) => (
          <Card key={loc.id} className="p-4 hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-bold text-lg text-gray-800">{loc.name}</h3>
              <Badge variant={loc.status === 'ACTIVE' ? 'default' : 'secondary'}>
                {loc.status}
              </Badge>
            </div>
            <div className="space-y-1 text-sm text-gray-600">
              <p>Strefa: <span className="font-medium">{loc.zone?.name || '-'}</span></p>
              <p>Typ: <span className="font-medium">{loc.locationType?.name || '-'}</span></p>
              {loc.barcode && <p className="font-mono text-xs mt-2 bg-gray-100 p-1 rounded text-center">{loc.barcode}</p>}
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
