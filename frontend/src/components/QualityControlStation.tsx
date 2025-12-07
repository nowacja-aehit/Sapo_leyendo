import React, { useState } from 'react';
import { qcService, QcInspection } from '../services/qcService';
import { useAuth } from '../context/AuthContext';

const QualityControlStation: React.FC = () => {
    const { user } = useAuth();
    const [inspectionData, setInspectionData] = useState({
        productId: '',
        sourceType: 'INBOUND',
        referenceId: '',
        sampleSize: ''
    });
    const [currentInspection, setCurrentInspection] = useState<QcInspection | null>(null);
    const [result, setResult] = useState<string>('PASS');
    const [ncrData, setNcrData] = useState({
        defectType: 'DAMAGED',
        description: ''
    });

    const handleCreateInspection = async () => {
        try {
            const inspection = await qcService.createInspection(
                parseInt(inspectionData.productId),
                inspectionData.sourceType,
                parseInt(inspectionData.referenceId),
                parseInt(inspectionData.sampleSize)
            );
            setCurrentInspection(inspection);
            alert(`Inspection #${inspection.id} created`);
        } catch (error) {
            console.error('Error creating inspection:', error);
            alert('Failed to create inspection');
        }
    };

    const handleExecuteInspection = async () => {
        if (!currentInspection || !user) return;
        try {
            await qcService.executeInspection(currentInspection.id, result, user.id);
            alert(`Inspection #${currentInspection.id} completed: ${result}`);
            if (result === 'PASS') {
                setCurrentInspection(null);
            }
        } catch (error) {
            console.error('Error executing inspection:', error);
            alert('Failed to execute inspection');
        }
    };

    const handleCreateNcr = async () => {
        if (!currentInspection) return;
        try {
            await qcService.createNcr(
                currentInspection.id,
                ncrData.defectType,
                ncrData.description,
                []
            );
            alert('NCR created');
            setNcrData({ defectType: 'DAMAGED', description: '' });
        } catch (error) {
            console.error('Error creating NCR:', error);
            alert('Failed to create NCR');
        }
    };

    return (
        <div className="container mt-4">
            <h2>Quality Control Station</h2>

            {!currentInspection ? (
                <div className="card">
                    <div className="card-body">
                        <h5 className="card-title">New Inspection</h5>
                        <div className="row g-3">
                            <div className="col-md-3">
                                <input type="text" className="form-control" placeholder="Product ID" value={inspectionData.productId} onChange={e => setInspectionData({...inspectionData, productId: e.target.value})} />
                            </div>
                            <div className="col-md-3">
                                <select className="form-select" value={inspectionData.sourceType} onChange={e => setInspectionData({...inspectionData, sourceType: e.target.value})}>
                                    <option value="INBOUND">Inbound</option>
                                    <option value="RETURN">Return</option>
                                </select>
                            </div>
                            <div className="col-md-3">
                                <input type="text" className="form-control" placeholder="Ref ID (Order/RMA)" value={inspectionData.referenceId} onChange={e => setInspectionData({...inspectionData, referenceId: e.target.value})} />
                            </div>
                            <div className="col-md-3">
                                <input type="text" className="form-control" placeholder="Sample Size" value={inspectionData.sampleSize} onChange={e => setInspectionData({...inspectionData, sampleSize: e.target.value})} />
                            </div>
                        </div>
                        <button className="btn btn-primary mt-3" onClick={handleCreateInspection}>Start Inspection</button>
                    </div>
                </div>
            ) : (
                <div className="card">
                    <div className="card-body">
                        <h5 className="card-title">Inspection #{currentInspection.id}</h5>
                        <div className="mb-3">
                            <label className="form-label">Result</label>
                            <select className="form-select" value={result} onChange={(e) => setResult(e.target.value)}>
                                <option value="PASS">Pass</option>
                                <option value="FAIL">Fail</option>
                                <option value="CONDITIONAL_PASS">Conditional Pass</option>
                            </select>
                        </div>
                        
                        {result === 'FAIL' && (
                            <div className="border p-3 mb-3 rounded bg-light">
                                <h6>Non-Conformance Report</h6>
                                <div className="mb-2">
                                    <select className="form-select" value={ncrData.defectType} onChange={e => setNcrData({...ncrData, defectType: e.target.value})}>
                                        <option value="DAMAGED">Damaged</option>
                                        <option value="WRONG_ITEM">Wrong Item</option>
                                        <option value="EXPIRED">Expired</option>
                                    </select>
                                </div>
                                <div className="mb-2">
                                    <textarea className="form-control" placeholder="Description" value={ncrData.description} onChange={e => setNcrData({...ncrData, description: e.target.value})} />
                                </div>
                                <button className="btn btn-danger btn-sm" onClick={handleCreateNcr}>Create NCR</button>
                            </div>
                        )}

                        <button className="btn btn-success w-100" onClick={handleExecuteInspection}>Complete Inspection</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QualityControlStation;
