import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

interface ZoneFormData {
  name: string
  temperatureControlled: boolean
  secure: boolean
  allowMixedSku: boolean
}

export default function ZoneForm() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<ZoneFormData>({
    name: '',
    temperatureControlled: false,
    secure: false,
    allowMixedSku: true
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await axios.post('/api/locations/zones', formData)
      navigate('/zones')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save zone')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Create New Zone</h3>
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
          <div className="mb-3 form-check">
            <input
              type="checkbox"
              className="form-check-input"
              id="temperatureControlled"
              name="temperatureControlled"
              checked={formData.temperatureControlled}
              onChange={handleChange}
            />
            <label className="form-check-label" htmlFor="temperatureControlled">Temperature Controlled</label>
          </div>
          <div className="mb-3 form-check">
            <input
              type="checkbox"
              className="form-check-input"
              id="secure"
              name="secure"
              checked={formData.secure}
              onChange={handleChange}
            />
            <label className="form-check-label" htmlFor="secure">Secure Area</label>
          </div>
          <div className="mb-3 form-check">
            <input
              type="checkbox"
              className="form-check-input"
              id="allowMixedSku"
              name="allowMixedSku"
              checked={formData.allowMixedSku}
              onChange={handleChange}
            />
            <label className="form-check-label" htmlFor="allowMixedSku">Allow Mixed SKUs</label>
          </div>
          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/zones')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
