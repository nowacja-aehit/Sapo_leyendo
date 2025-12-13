import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

interface Inventory {
  id: number
  lpn: string
  product: { sku: string; name: string }
  quantity: number
  location: { name: string }
}

interface Location {
  id: number
  name: string
}

export default function TaskForm() {
  const navigate = useNavigate()
  const [inventoryList, setInventoryList] = useState<Inventory[]>([])
  const [locations, setLocations] = useState<Location[]>([])
  
  const [formData, setFormData] = useState({
    inventoryId: '',
    targetLocationId: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [invRes, locRes] = await Promise.all([
          axios.get('/api/inventory'),
          axios.get('/api/locations')
        ])
        setInventoryList(invRes.data)
        setLocations(locRes.data)
      } catch (err) {
        console.error(err)
        setError('Failed to load data')
      }
    }
    fetchData()
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await axios.post('/api/inventory/tasks', {
        inventoryId: parseInt(formData.inventoryId),
        targetLocationId: parseInt(formData.targetLocationId)
      })
      navigate('/tasks')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create task')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Create Move Task</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Inventory Item (What to move)</label>
            <select
              className="form-select"
              value={formData.inventoryId}
              onChange={e => setFormData({...formData, inventoryId: e.target.value})}
              required
            >
              <option value="">Select Inventory</option>
              {inventoryList.map(inv => (
                <option key={inv.id} value={inv.id}>
                  {inv.product.sku} ({inv.quantity}) - {inv.location.name} {inv.lpn ? `[${inv.lpn}]` : ''}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Target Location (Where to move)</label>
            <select
              className="form-select"
              value={formData.targetLocationId}
              onChange={e => setFormData({...formData, targetLocationId: e.target.value})}
              required
            >
              <option value="">Select Location</option>
              {locations.map(loc => (
                <option key={loc.id} value={loc.id}>{loc.name}</option>
              ))}
            </select>
          </div>

          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Task'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/tasks')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
