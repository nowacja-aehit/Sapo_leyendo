import React, { useState } from 'react';
import { pickingService, PickingTask, Wave } from '../services/pickingService';
import { useAuth } from '../context/AuthContext';

const PickingDashboard: React.FC = () => {
    const { user } = useAuth();
    const [waves, setWaves] = useState<Wave[]>([]); // In a real app, we'd fetch this list
    const [selectedWaveId, setSelectedWaveId] = useState<string | null>(null);
    const [tasks, setTasks] = useState<PickingTask[]>([]);
    const [outboundOrderIds, setOutboundOrderIds] = useState<string>('');

    const handleCreateWave = async () => {
        const ids = outboundOrderIds.split(',').map(id => parseInt(id.trim()));
        try {
            const newWave = await pickingService.createWave(ids);
            setWaves([...waves, newWave]);
            alert(`Wave ${newWave.id} created!`);
        } catch (error) {
            console.error('Error creating wave:', error);
            alert('Failed to create wave');
        }
    };

    const handleRunWave = async (waveId: string) => {
        const ids = outboundOrderIds.split(',').map(id => parseInt(id.trim()));
        try {
            await pickingService.runWave(waveId, ids);
            alert(`Wave ${waveId} started!`);
            fetchTasks(waveId);
        } catch (error) {
            console.error('Error running wave:', error);
            alert('Failed to run wave');
        }
    };

    const fetchTasks = async (waveId: string) => {
        try {
            const fetchedTasks = await pickingService.getPickingTasks(waveId);
            setTasks(fetchedTasks);
            setSelectedWaveId(waveId);
        } catch (error) {
            console.error('Error fetching tasks:', error);
        }
    };

    const handleConfirmTask = async (taskId: string, quantity: number) => {
        if (!user) return;
        try {
            await pickingService.confirmTask(taskId, quantity);
            setTasks(tasks.map(t => t.id === taskId ? { ...t, status: 'PICKED', quantityPicked: quantity } : t));
        } catch (error) {
            console.error('Error confirming task:', error);
            alert('Failed to confirm task');
        }
    };

    return (
        <div className="container mt-4">
            <h2>Picking Dashboard</h2>
            
            <div className="card mb-4">
                <div className="card-body">
                    <h5 className="card-title">Create Wave</h5>
                    <div className="input-group mb-3">
                        <input 
                            type="text" 
                            className="form-control" 
                            placeholder="Outbound Order IDs (comma separated)" 
                            value={outboundOrderIds}
                            onChange={(e) => setOutboundOrderIds(e.target.value)}
                        />
                        <button className="btn btn-primary" onClick={handleCreateWave}>Create Wave</button>
                    </div>
                </div>
            </div>

            <div className="row">
                <div className="col-md-4">
                    <h4>Waves</h4>
                    <ul className="list-group">
                        {waves.map(wave => (
                            <li key={wave.id} className="list-group-item d-flex justify-content-between align-items-center">
                                Wave #{wave.id.substring(0, 8)}... ({wave.status})
                                <div>
                                    <button className="btn btn-sm btn-success me-2" onClick={() => handleRunWave(wave.id)}>Run</button>
                                    <button className="btn btn-sm btn-info" onClick={() => fetchTasks(wave.id)}>Tasks</button>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
                <div className="col-md-8">
                    <h4>Tasks {selectedWaveId && `for Wave #${selectedWaveId.substring(0, 8)}...`}</h4>
                    <table className="table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Product</th>
                                <th>Location</th>
                                <th>Qty</th>
                                <th>Target LPN</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {tasks.map(task => (
                                <tr key={task.id}>
                                    <td>{task.id.substring(0, 8)}...</td>
                                    <td>{task.productId}</td>
                                    <td>{task.sourceLocationId}</td>
                                    <td>{task.quantityToPick}</td>
                                    <td>{task.targetLpn}</td>
                                    <td>{task.status}</td>
                                    <td>
                                        {task.status !== 'PICKED' && (
                                            <button className="btn btn-sm btn-primary" onClick={() => handleConfirmTask(task.id, task.quantityToPick)}>Confirm</button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default PickingDashboard;
