import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

interface Product {
  id: number
  sku: string
  name: string
}

interface OrderItem {
  product: Product | null
  quantityExpected: number
}

export default function InboundForm() {
  const navigate = useNavigate()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [formData, setFormData] = useState({
    referenceNumber: '',
    expectedDate: '',
    items: [] as OrderItem[]
  })

  useEffect(() => {
    axios.get('/api/products')
      .then(res => setProducts(res.data))
      .catch(err => console.error('Failed to fetch products', err))
  }, [])

  const handleAddItem = () => {
    setFormData(prev => ({
      ...prev,
      items: [...prev.items, { product: null, quantityExpected: 1 }]
    }))
  }

  const handleRemoveItem = (index: number) => {
    setFormData(prev => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index)
    }))
  }

  const handleItemChange = (index: number, field: keyof OrderItem, value: any) => {
    setFormData(prev => {
      const newItems = [...prev.items]
      if (field === 'product') {
        const product = products.find(p => p.id === parseInt(value))
        newItems[index] = { ...newItems[index], product: product || null }
      } else {
        newItems[index] = { ...newItems[index], [field]: value }
      }
      return { ...prev, items: newItems }
    })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const payload = {
        referenceNumber: formData.referenceNumber,
        expectedDate: formData.expectedDate,
        status: 'EXPECTED',
        items: formData.items.map(item => ({
          product: item.product,
          quantityExpected: item.quantityExpected,
          quantityReceived: 0
        }))
      }

      await axios.post('/api/inbound', payload)
      navigate('/inbound')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create inbound order')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Create Inbound Order (ASN)</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Reference Number</label>
              <input
                type="text"
                className="form-control"
                value={formData.referenceNumber}
                onChange={e => setFormData({...formData, referenceNumber: e.target.value})}
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Expected Date</label>
              <input
                type="datetime-local"
                className="form-control"
                value={formData.expectedDate}
                onChange={e => setFormData({...formData, expectedDate: e.target.value})}
                required
              />
            </div>
          </div>

          <h4 className="mt-4">Items</h4>
          {formData.items.map((item, index) => (
            <div key={index} className="row mb-2 align-items-end">
              <div className="col-md-6">
                <label className="form-label">Product</label>
                <select
                  className="form-select"
                  value={item.product?.id || ''}
                  onChange={e => handleItemChange(index, 'product', e.target.value)}
                  required
                >
                  <option value="">Select Product</option>
                  {products.map(p => (
                    <option key={p.id} value={p.id}>{p.sku} - {p.name}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Quantity Expected</label>
                <input
                  type="number"
                  className="form-control"
                  value={item.quantityExpected}
                  onChange={e => handleItemChange(index, 'quantityExpected', parseInt(e.target.value))}
                  min="1"
                  required
                />
              </div>
              <div className="col-md-2">
                <button type="button" className="btn btn-danger" onClick={() => handleRemoveItem(index)}>
                  Remove
                </button>
              </div>
            </div>
          ))}
          
          <button type="button" className="btn btn-secondary mb-3" onClick={handleAddItem}>
            Add Item
          </button>

          <div className="d-flex gap-2 border-top pt-3">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Order'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/inbound')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
