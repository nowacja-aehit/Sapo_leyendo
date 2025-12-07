import { useEffect, useState } from 'react'
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
}

export default function InventoryList() {
  const [inventory, setInventory] = useState<Inventory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
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
  }, [])

  if (loading) return <div>Loading inventory...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <h2>Current Inventory</h2>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Product SKU</th>
            <th>Product Name</th>
            <th>Location</th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          {inventory.map(item => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.product.sku}</td>
              <td>{item.product.name}</td>
              <td>{item.location.name}</td>
              <td>{item.quantity}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
