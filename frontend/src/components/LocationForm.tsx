import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'

interface Zone {
  id: number
  name: string
}

interface LocationType {
  id: number
  name: string
}

interface Location {
  id?: number
  name: string
  zone: Zone | null
  locationType: LocationType | null
  barcode: string
  aisle: string
  rack: string
  level: string
  bin: string
  pickSequence: number
  status: string
  active: boolean
}

export default function LocationForm() {
  const navigate = useNavigate()
  const { id } = useParams()
  
  const [zones, setZones] = useState<Zone[]>([])
  const [types, setTypes] = useState<LocationType[]>([])
  
  const [formData, setFormData] = useState<Location>({
    name: '',
    zone: null,
    locationType: null,
    barcode: '',
    aisle: '',
    rack: '',
    level: '',
    bin: '',
    pickSequence: 0,
    status: 'ACTIVE',
    active: true
  })

  useEffect(() => {
    // Fetch Zones and Types
    axios.get('/api/locations/zones').then(res => setZones(res.data))
    axios.get('/api/locations/types').then(res => setTypes(res.data))

    if (id) {
      axios.get(`/api/locations/${id}`).then(res => setFormData(res.data))
    }
  }, [id])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await axios.post('/api/locations', formData)
      navigate('/locations')
    } catch (err) {
      console.error(err)
      alert('Failed to save location')
    }
  }

  return (
    <div>
      <h2>{id ? 'Edit' : 'New'} Location</h2>
      <form onSubmit={handleSubmit}>
        <div className="row">
          <div className="col-md-6 mb-3">
            <label className="form-label">Name</label>
            <input 
              type="text" 
              className="form-control" 
              value={formData.name} 
              onChange={e => setFormData({...formData, name: e.target.value})} 
              required 
            />
          </div>
          <div className="col-md-6 mb-3">
            <label className="form-label">Barcode</label>
            <input 
              type="text" 
              className="form-control" 
              value={formData.barcode} 
              onChange={e => setFormData({...formData, barcode: e.target.value})} 
            />
          </div>
        </div>

        <div className="row">
          <div className="col-md-6 mb-3">
            <label className="form-label">Zone</label>
            <select 
              className="form-select" 
              value={formData.zone?.id || ''} 
              onChange={e => {
                const zone = zones.find(z => z.id === Number(e.target.value))
                setFormData({...formData, zone: zone || null})
              }}
              required
            >
              <option value="">Select Zone</option>
              {zones.map(z => <option key={z.id} value={z.id}>{z.name}</option>)}
            </select>
          </div>
          <div className="col-md-6 mb-3">
            <label className="form-label">Type</label>
            <select 
              className="form-select" 
              value={formData.locationType?.id || ''} 
              onChange={e => {
                const type = types.find(t => t.id === Number(e.target.value))
                setFormData({...formData, locationType: type || null})
              }}
              required
            >
              <option value="">Select Type</option>
              {types.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </div>
        </div>

        <div className="row">
          <div className="col-md-3 mb-3">
            <label className="form-label">Aisle</label>
            <input type="text" className="form-control" value={formData.aisle} onChange={e => setFormData({...formData, aisle: e.target.value})} />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Rack</label>
            <input type="text" className="form-control" value={formData.rack} onChange={e => setFormData({...formData, rack: e.target.value})} />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Level</label>
            <input type="text" className="form-control" value={formData.level} onChange={e => setFormData({...formData, level: e.target.value})} />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Bin</label>
            <input type="text" className="form-control" value={formData.bin} onChange={e => setFormData({...formData, bin: e.target.value})} />
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Pick Sequence</label>
          <input 
            type="number" 
            className="form-control" 
            value={formData.pickSequence} 
            onChange={e => setFormData({...formData, pickSequence: Number(e.target.value)})} 
          />
        </div>

        <button type="submit" className="btn btn-primary">Save</button>
        <button type="button" className="btn btn-secondary ms-2" onClick={() => navigate('/locations')}>Cancel</button>
      </form>
    </div>
  )
}
