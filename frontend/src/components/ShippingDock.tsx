import React, { useState } from 'react';
import { shippingService, TransportLoad, Manifest } from '../services/shippingService';

const ShippingDock: React.FC = () => {
    const [loads, setLoads] = useState<TransportLoad[]>([]);
    const [newLoad, setNewLoad] = useState({
        carrierId: '',
        vehiclePlateNumber: '',
        driverName: '',
        driverPhone: '',
        dockId: ''
    });
    const [selectedLoadId, setSelectedLoadId] = useState<number | null>(null);
    const [shipmentIdToAssign, setShipmentIdToAssign] = useState<string>('');
    const [manifest, setManifest] = useState<Manifest | null>(null);

    const handleCreateLoad = async () => {
        try {
            const load = await shippingService.createLoad(
                parseInt(newLoad.carrierId),
                newLoad.vehiclePlateNumber,
                newLoad.driverName,
                newLoad.driverPhone,
                parseInt(newLoad.dockId)
            );
            setLoads([...loads, load]);
            alert(`Load #${load.id} created`);
        } catch (error) {
            console.error('Error creating load:', error);
            alert('Failed to create load');
        }
    };

    const handleAssignShipment = async () => {
        if (!selectedLoadId) return;
        try {
            await shippingService.assignShipmentToLoad(selectedLoadId, parseInt(shipmentIdToAssign));
            alert('Shipment assigned to load');
            setShipmentIdToAssign('');
        } catch (error) {
            console.error('Error assigning shipment:', error);
            alert('Failed to assign shipment');
        }
    };

    const handleDispatch = async (loadId: number) => {
        try {
            const generatedManifest = await shippingService.dispatchLoad(loadId);
            setManifest(generatedManifest);
            alert(`Load #${loadId} dispatched!`);
        } catch (error) {
            console.error('Error dispatching load:', error);
            alert('Failed to dispatch load');
        }
    };

    return (
        <div className="container mt-4">
            <h2>Shipping Dock</h2>

            <div className="card mb-4">
                <div className="card-body">
                    <h5 className="card-title">Create Transport Load</h5>
                    <div className="row g-3">
                        <div className="col-md-2">
                            <input type="text" className="form-control" placeholder="Carrier ID" value={newLoad.carrierId} onChange={e => setNewLoad({...newLoad, carrierId: e.target.value})} />
                        </div>
                        <div className="col-md-2">
                            <input type="text" className="form-control" placeholder="Plate Number" value={newLoad.vehiclePlateNumber} onChange={e => setNewLoad({...newLoad, vehiclePlateNumber: e.target.value})} />
                        </div>
                        <div className="col-md-3">
                            <input type="text" className="form-control" placeholder="Driver Name" value={newLoad.driverName} onChange={e => setNewLoad({...newLoad, driverName: e.target.value})} />
                        </div>
                        <div className="col-md-3">
                            <input type="text" className="form-control" placeholder="Driver Phone" value={newLoad.driverPhone} onChange={e => setNewLoad({...newLoad, driverPhone: e.target.value})} />
                        </div>
                        <div className="col-md-2">
                            <input type="text" className="form-control" placeholder="Dock ID" value={newLoad.dockId} onChange={e => setNewLoad({...newLoad, dockId: e.target.value})} />
                        </div>
                    </div>
                    <button className="btn btn-primary mt-3" onClick={handleCreateLoad}>Create Load</button>
                </div>
            </div>

            <div className="row">
                <div className="col-md-6">
                    <h4>Active Loads</h4>
                    <ul className="list-group">
                        {loads.map(load => (
                            <li key={load.id} className={`list-group-item ${selectedLoadId === load.id ? 'active' : ''}`} onClick={() => setSelectedLoadId(load.id)}>
                                Load #{load.id} - {load.carrierId} ({load.vehiclePlateNumber})
                                <button className="btn btn-sm btn-warning float-end" onClick={(e) => { e.stopPropagation(); handleDispatch(load.id); }}>Dispatch</button>
                            </li>
                        ))}
                    </ul>
                </div>
                <div className="col-md-6">
                    {selectedLoadId && (
                        <div className="card">
                            <div className="card-body">
                                <h5>Manage Load #{selectedLoadId}</h5>
                                <div className="input-group mb-3">
                                    <input 
                                        type="text" 
                                        className="form-control" 
                                        placeholder="Shipment ID" 
                                        value={shipmentIdToAssign}
                                        onChange={(e) => setShipmentIdToAssign(e.target.value)}
                                    />
                                    <button className="btn btn-secondary" onClick={handleAssignShipment}>Assign Shipment</button>
                                </div>
                            </div>
                        </div>
                    )}
                    {manifest && (
                        <div className="alert alert-success mt-3">
                            <h5>Manifest Generated</h5>
                            <pre>{manifest.content}</pre>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ShippingDock;
