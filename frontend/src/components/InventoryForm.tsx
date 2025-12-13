import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'

interface Product {
  id: number
  sku: string
  name: string
}

interface Location {
  id: number
  name: string
}

interface InventoryFormData {
  product: { id: number } | null
  location: { id: number } | null
  quantity: number
  lpn: string
  status: string
}

export default function InventoryForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditMode = !!id

  const [formData, setFormData] = useState<InventoryFormData>({
    product: null,
    location: null,
    quantity: 0,
    lpn: '',
    status: 'AVAILABLE'
  })
  
  const [products, setProducts] = useState<Product[]>([])
  const [locations, setLocations] = useState<Location[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    // Fetch dependencies
    const fetchData = async () => {
      try {
        const [productsRes, locationsRes] = await Promise.all([
          axios.get('/api/products'),
          axios.get('/api/locations')
        ])
        setProducts(productsRes.data)
        setLocations(locationsRes.data)
      } catch (err) {
        console.error('Failed to fetch dependencies', err)
        setError('Failed to load products or locations')
      }
    }
    fetchData()

    if (isEditMode) {
      // Note: We don't have a specific GET /api/inventory/{id} endpoint in the controller yet!
      // The controller has:
      // getAllInventory, getInventoryByProduct, getInventoryByLocation
      // It does NOT have getInventoryById.
      // So "Edit" might be tricky without adding that endpoint.
      // However, for "Adding", we are fine.
      // I will skip "Edit" logic implementation for now or I should add getInventoryById to backend.
      // Given the user asked for "Adding and Removing", I will focus on Add.
      // If I want to support Edit, I must add the endpoint.
      // Let's add the endpoint to backend first to be complete.
    }
  }, [id, isEditMode])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    if (!formData.product || !formData.location) {
      setError('Product and Location are required')
      setLoading(false)
      return
    }

    try {
      const payload = {
        ...formData,
        id: id ? parseInt(id) : null
      }
      
      await axios.post('/api/inventory', payload)
      navigate('/inventory')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save inventory')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    
    if (name === 'productId') {
      const product = products.find(p => p.id === parseInt(value))
      setFormData(prev => ({ ...prev, product: product || null }))
    } else if (name === 'locationId') {
      const location = locations.find(l => l.id === parseInt(value))
      setFormData(prev => ({ ...prev, location: location || null }))
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: name === 'quantity' ? parseInt(value) : value
      }))
    }
  }

  if (loading && isEditMode) return <div>Loading...</div>

  return (
    <div className="card">
      <div className="card-header">
        <h3>{isEditMode ? 'Edit Inventory' : 'Add Inventory'}</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Product</label>
            <select
              className="form-select"
              name="productId"
              value={formData.product?.id || ''}
              onChange={handleChange}
              required
            >
              <option value="">Select Product</option>
              {products.map(p => (
                <option key={p.id} value={p.id}>{p.sku} - {p.name}</option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Location</label>
            <select
              className="form-select"
              name="locationId"
              value={formData.location?.id || ''}
              onChange={handleChange}
              required
            >
              <option value="">Select Location</option>
              {locations.map(l => (
                <option key={l.id} value={l.id}>{l.name}</option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Quantity</label>
            <input
              type="number"
              className="form-control"
              name="quantity"
              value={formData.quantity}
              onChange={handleChange}
              required
              min="1"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">LPN (License Plate Number)</label>
            <input
              type="text"
              className="form-control"
              name="lpn"
              value={formData.lpn}
              onChange={handleChange}
              placeholder="Optional"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Status</label>
            <select
              className="form-select"
              name="status"
              value={formData.status}
              onChange={handleChange}
            >
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="RESERVED">RESERVED</option>
              <option value="QC_HOLD">QC_HOLD</option>
              <option value="DAMAGED">DAMAGED</option>
            </select>
          </div>

          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/inventory')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
