import React, { useState } from 'react';
import { packingService, Shipment, Parcel } from '../services/packingService';

const PackingStation: React.FC = () => {
    const [outboundOrderId, setOutboundOrderId] = useState<string>('');
    const [currentShipment, setCurrentShipment] = useState<Shipment | null>(null);
    const [currentParcel, setCurrentParcel] = useState<Parcel | null>(null);
    const [packingMaterialId, setPackingMaterialId] = useState<string>('1'); // Default box
    const [productId, setProductId] = useState<string>('');
    const [quantity, setQuantity] = useState<string>('1');

    const handleStartPacking = async () => {
        try {
            const shipment = await packingService.startPacking(parseInt(outboundOrderId));
            setCurrentShipment(shipment);
            alert(`Packing started for Shipment #${shipment.id}`);
        } catch (error) {
            console.error('Error starting packing:', error);
            alert('Failed to start packing');
        }
    };

    const handleCreateParcel = async () => {
        if (!currentShipment) return;
        try {
            const parcel = await packingService.createParcel(currentShipment.id, parseInt(packingMaterialId));
            setCurrentParcel(parcel);
            alert(`Parcel #${parcel.id} created`);
        } catch (error) {
            console.error('Error creating parcel:', error);
            alert('Failed to create parcel');
        }
    };

    const handleAddItem = async () => {
        if (!currentParcel) return;
        try {
            await packingService.addItemToParcel(currentParcel.id, parseInt(productId), parseInt(quantity));
            alert('Item added to parcel');
            setProductId('');
            setQuantity('1');
        } catch (error) {
            console.error('Error adding item:', error);
            alert('Failed to add item');
        }
    };

    const handleCloseShipment = async () => {
        if (!currentShipment) return;
        try {
            await packingService.closeShipment(currentShipment.id);
            alert('Shipment closed successfully');
            setCurrentShipment(null);
            setCurrentParcel(null);
            setOutboundOrderId('');
        } catch (error) {
            console.error('Error closing shipment:', error);
            alert('Failed to close shipment');
        }
    };

    return (
        <div className="container mt-4">
            <h2>Packing Station</h2>

            {!currentShipment ? (
                <div className="card">
                    <div className="card-body">
                        <h5 className="card-title">Start Packing</h5>
                        <div className="input-group">
                            <input 
                                type="text" 
                                className="form-control" 
                                placeholder="Outbound Order ID" 
                                value={outboundOrderId}
                                onChange={(e) => setOutboundOrderId(e.target.value)}
                            />
                            <button className="btn btn-primary" onClick={handleStartPacking}>Start</button>
                        </div>
                    </div>
                </div>
            ) : (
                <div>
                    <div className="alert alert-info">
                        Packing Shipment #{currentShipment.id} (Order #{currentShipment.outboundOrderId})
                        <button className="btn btn-sm btn-warning float-end" onClick={handleCloseShipment}>Close Shipment</button>
                    </div>

                    {!currentParcel ? (
                        <div className="card mb-3">
                            <div className="card-body">
                                <h5>New Parcel</h5>
                                <div className="input-group">
                                    <select className="form-select" value={packingMaterialId} onChange={(e) => setPackingMaterialId(e.target.value)}>
                                        <option value="1">Small Box</option>
                                        <option value="2">Medium Box</option>
                                        <option value="3">Large Box</option>
                                    </select>
                                    <button className="btn btn-success" onClick={handleCreateParcel}>Create Parcel</button>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="card">
                            <div className="card-body">
                                <h5>Packing Parcel #{currentParcel.id}</h5>
                                <div className="row g-3">
                                    <div className="col-md-6">
                                        <input 
                                            type="text" 
                                            className="form-control" 
                                            placeholder="Product ID" 
                                            value={productId}
                                            onChange={(e) => setProductId(e.target.value)}
                                        />
                                    </div>
                                    <div className="col-md-4">
                                        <input 
                                            type="number" 
                                            className="form-control" 
                                            placeholder="Qty" 
                                            value={quantity}
                                            onChange={(e) => setQuantity(e.target.value)}
                                        />
                                    </div>
                                    <div className="col-md-2">
                                        <button className="btn btn-primary w-100" onClick={handleAddItem}>Add</button>
                                    </div>
                                </div>
                                <div className="mt-3">
                                    <button className="btn btn-secondary" onClick={() => setCurrentParcel(null)}>Finish Parcel</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default PackingStation;
