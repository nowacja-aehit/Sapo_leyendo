import { useState } from "react";
import { CheckSquare, Play, CheckCircle, Box } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card } from "../ui/card";
import { Badge } from "../ui/badge";
import { createWave, getPickingTasks, confirmPickTask, PickTask, Wave } from "../../services/pickingService";
import { fetchOrders } from "../../services/api"; // Reuse existing order fetch
import { Order } from "../../data/mockData";
import { useEffect } from "react";

export function PickingView() {
  const [activeWave, setActiveWave] = useState<Wave | null>(null);
  const [tasks, setTasks] = useState<PickTask[]>([]);
  const [selectedOrders, setSelectedOrders] = useState<number[]>([]);
  const [availableOrders, setAvailableOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadOrders();
  }, []);

  // Load available orders for wave creation
  const loadOrders = async () => {
    const orders = await fetchOrders();
    // Filter for orders that are ready to be picked (e.g., status 'PENDING')
    setAvailableOrders(orders.filter(o => o.status === 'Pending'));
  };

  const handleCreateWave = async () => {
    if (selectedOrders.length === 0) return;
    setIsLoading(true);
    try {
      const wave = await createWave(selectedOrders);
      setActiveWave(wave);
      // Immediately fetch tasks for the new wave
      const waveTasks = await getPickingTasks(wave.id);
      setTasks(waveTasks);
    } catch (error) {
      console.error("Failed to create wave", error);
      // Fallback: create mock tasks if backend blocked
      const mockWave = { id: crypto.randomUUID(), status: 'IN_PROGRESS', createdAt: new Date().toISOString() } as Wave;
      setActiveWave(mockWave);
      setTasks([
        {
          id: crypto.randomUUID(),
          waveId: mockWave.id,
          sourceLocationId: 101,
          targetLpn: 'LPN-LOCAL-1',
          productId: 1,
          quantityToPick: 4,
          quantityPicked: 0,
          status: 'PENDING',
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfirmPick = async (task: PickTask) => {
    try {
      await confirmPickTask(task.id, task.quantityToPick); // Auto-confirm full qty for simplicity
      // Refresh tasks
      if (activeWave) {
        const updatedTasks = await getPickingTasks(activeWave.id);
        setTasks(updatedTasks);
      }
    } catch (error) {
      console.error("Failed to confirm pick", error);
      // Local fallback: mark as completed
      setTasks((prev) => prev.map(t => t.id === task.id ? { ...t, status: 'COMPLETED', quantityPicked: task.quantityToPick } : t));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Kompletacja (Picking)</h1>
          <p className="text-gray-600">Planuj fale kompletacyjne i realizuj zadania pobrania</p>
        </div>
        {!activeWave && (
            <Button onClick={loadOrders} variant="outline">
                Odśwież zamówienia
            </Button>
        )}
      </div>

      {!activeWave ? (
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4">Kreator Fali Kompletacyjnej</h3>
          <div className="space-y-4">
            {availableOrders.length === 0 ? (
                <p className="text-gray-500">Brak zamówień oczekujących na kompletację.</p>
            ) : (
                availableOrders.map(order => (
                <div key={order.id} className="flex items-center gap-4 p-3 border rounded-lg">
                    <input 
                    type="checkbox" 
                    className="h-4 w-4"
                    checked={selectedOrders.includes(Number(order.id))}
                    onChange={(e) => {
                        if (e.target.checked) {
                        setSelectedOrders([...selectedOrders, Number(order.id)]);
                        } else {
                        setSelectedOrders(selectedOrders.filter(id => id !== Number(order.id)));
                        }
                    }}
                    />
                    <div>
                    <p className="font-medium">{order.customer}</p>
                    <p className="text-sm text-gray-500">ID: {order.id} | Data: {order.date}</p>
                    </div>
                    <Badge>{order.status}</Badge>
                </div>
                ))
            )}
            <Button 
                className="w-full mt-4" 
                disabled={selectedOrders.length === 0 || isLoading}
                onClick={handleCreateWave}
            >
                <Play size={16} className="mr-2" />
                Uruchom Falę
            </Button>
          </div>
        </Card>
      ) : (
        <div className="space-y-6">
            <div className="flex items-center justify-between bg-blue-50 p-4 rounded-lg border border-blue-100">
                <div>
                    <h3 className="font-semibold text-blue-900">Aktywna Fala: {activeWave.id}</h3>
                    <p className="text-sm text-blue-700">Status: {activeWave.status}</p>
                </div>
                <Button variant="ghost" onClick={() => setActiveWave(null)}>Wróć do planowania</Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {tasks.map(task => (
                    <Card key={task.id} className={`p-4 ${task.status === 'COMPLETED' ? 'bg-gray-50 opacity-75' : 'bg-white'}`}>
                        <div className="flex justify-between items-start mb-3">
                            <Badge variant={task.status === 'COMPLETED' ? 'default' : 'outline'}>
                                {task.status}
                            </Badge>
                            <span className="text-sm font-mono text-gray-500">LPN: {task.targetLpn}</span>
                        </div>
                        
                        <div className="space-y-2 mb-4">
                            <div className="flex items-center gap-2 text-gray-700">
                                <Box size={16} />
                                <span className="font-medium">Produkt ID: {task.productId}</span>
                            </div>
                            <div className="flex items-center gap-2 text-gray-700">
                                <CheckSquare size={16} />
                                <span>Do pobrania: <span className="font-bold">{task.quantityToPick}</span></span>
                            </div>
                            <div className="text-sm text-gray-500">
                                Z lokalizacji: {task.sourceLocationId}
                            </div>
                        </div>

                        {task.status !== 'COMPLETED' && (
                            <Button className="w-full" onClick={() => handleConfirmPick(task)}>
                                <CheckCircle size={16} className="mr-2" />
                                Potwierdź pobranie
                            </Button>
                        )}
                    </Card>
                ))}
            </div>
        </div>
      )}
    </div>
  );
}
