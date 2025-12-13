import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

interface LocationTypeFormData {
  name: string
  maxWeight: number
  maxVolume: number
  length: number
  width: number
  height: number
}

export default function LocationTypeForm() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<LocationTypeFormData>({
    name: '',
    maxWeight: 0,
    maxVolume: 0,
    length: 0,
    width: 0,
    height: 0
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await axios.post('/api/locations/types', formData)
      navigate('/location-types')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save location type')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'name' ? value : parseFloat(value)
    }))
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Create New Location Type</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Name</label>
            <input
              type="text"
              className="form-control"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Max Weight (kg)</label>
              <input
                type="number"
                step="0.01"
                className="form-control"
                name="maxWeight"
                value={formData.maxWeight}
                onChange={handleChange}
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Max Volume (m3)</label>
              <input
                type="number"
                step="0.01"
                className="form-control"
                name="maxVolume"
                value={formData.maxVolume}
                onChange={handleChange}
              />
            </div>
          </div>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Length (m)</label>
              <input
                type="number"
                step="0.01"
                className="form-control"
                name="length"
                value={formData.length}
                onChange={handleChange}
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Width (m)</label>
              <input
                type="number"
                step="0.01"
                className="form-control"
                name="width"
                value={formData.width}
                onChange={handleChange}
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Height (m)</label>
              <input
                type="number"
                step="0.01"
                className="form-control"
                name="height"
                value={formData.height}
                onChange={handleChange}
              />
            </div>
          </div>
          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/location-types')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
