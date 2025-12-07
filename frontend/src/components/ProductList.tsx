import { useEffect, useState } from 'react'
import axios from 'axios'

interface Product {
  id: number
  sku: string
  name: string
  description: string
  weightKg: number
}

export default function ProductList() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/products')
      .then(response => {
        setProducts(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch products')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading products...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <h2>Product Inventory</h2>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>SKU</th>
            <th>Name</th>
            <th>Description</th>
            <th>Weight (kg)</th>
          </tr>
        </thead>
        <tbody>
          {products.map(product => (
            <tr key={product.id}>
              <td>{product.id}</td>
              <td>{product.sku}</td>
              <td>{product.name}</td>
              <td>{product.description}</td>
              <td>{product.weightKg}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
