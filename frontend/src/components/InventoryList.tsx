import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
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

interface Inventory {
  id: number
  product: Product
  location: Location
  quantity: number
  lpn: string
  status: string
}

export default function InventoryList() {
  const [inventory, setInventory] = useState<Inventory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchInventory()
  }, [])

  const fetchInventory = () => {
    axios.get('/api/inventory')
      .then(response => {
        setInventory(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch inventory')
        setLoading(false)
      })
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this inventory item?')) return

    try {
      await axios.delete(`/api/inventory/${id}`)
      setInventory(inventory.filter(i => i.id !== id))
    } catch (err) {
      console.error(err)
      alert('Failed to delete inventory item')
    }
  }

  if (loading) return <div>Loading inventory...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Current Inventory</h2>
        <Link to="/inventory/new" className="btn btn-primary">Add Inventory</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>LPN</th>
            <th>Product</th>
            <th>Location</th>
            <th>Quantity</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {inventory.map(item => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.lpn || '-'}</td>
              <td>{item.product.sku} - {item.product.name}</td>
              <td>{item.location.name}</td>
              <td>{item.quantity}</td>
              <td>
                <span className={`badge ${item.status === 'AVAILABLE' ? 'bg-success' : 'bg-warning'}`}>
                  {item.status}
                </span>
              </td>
              <td>
                <button 
                  onClick={() => handleDelete(item.id)} 
                  className="btn btn-outline-danger btn-sm"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
