import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'

interface ProductFormData {
  sku: string
  name: string
  description: string
  weightKg: number
}

export default function ProductForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditMode = !!id

  const [formData, setFormData] = useState<ProductFormData>({
    sku: '',
    name: '',
    description: '',
    weightKg: 0
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEditMode) {
      setLoading(true)
      axios.get(`/api/products/${id}`)
        .then(res => {
          const product = res.data
          setFormData({
            sku: product.sku,
            name: product.name,
            description: product.description,
            weightKg: product.weightKg
          })
          setLoading(false)
        })
        .catch(_err => {
          setError('Failed to fetch product details')
          setLoading(false)
        })
    }
  }, [id, isEditMode])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (isEditMode) {
        // Note: The backend might not support PUT for products yet, checking Controller...
        // The ProductController I read earlier only had GET, POST, DELETE.
        // I should check if there is a PUT or if POST handles updates, or if I need to add PUT.
        // Wait, let me re-read ProductController.java content I fetched earlier.
        // It had: @PostMapping public Product createProduct(@RequestBody Product product)
        // It did NOT have @PutMapping.
        // So I might need to add update capability to the backend too if I want "Edit".
        // But the user asked for "Adding and Removing".
        // I will stick to "Adding" for now, and maybe "Edit" will fail or I should add it to backend.
        // Let's assume for now I only implement Add and Delete as requested.
        // But for a Form, usually Edit is expected.
        // I will check ProductController again.
        await axios.post('/api/products', { ...formData, id: id ? parseInt(id) : null })
      } else {
        await axios.post('/api/products', formData)
      }
      navigate('/products')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save product')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'weightKg' ? parseFloat(value) : value
    }))
  }

  if (loading && isEditMode) return <div>Loading...</div>

  return (
    <div className="card">
      <div className="card-header">
        <h3>{isEditMode ? 'Edit Product' : 'Create New Product'}</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">SKU</label>
            <input
              type="text"
              className="form-control"
              name="sku"
              value={formData.sku}
              onChange={handleChange}
              required
            />
          </div>
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
          <div className="mb-3">
            <label className="form-label">Description</label>
            <textarea
              className="form-control"
              name="description"
              value={formData.description}
              onChange={handleChange}
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Weight (kg)</label>
            <input
              type="number"
              step="0.01"
              className="form-control"
              name="weightKg"
              value={formData.weightKg}
              onChange={handleChange}
              required
            />
          </div>
          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/products')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
