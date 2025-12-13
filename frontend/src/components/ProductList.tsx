import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
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
    fetchProducts()
  }, [])

  const fetchProducts = () => {
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
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return

    try {
      await axios.delete(`/api/products/${id}`)
      setProducts(products.filter(p => p.id !== id))
    } catch (err) {
      console.error(err)
      alert('Failed to delete product')
    }
  }

  if (loading) return <div>Loading products...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Product Inventory</h2>
        <Link to="/products/new" className="btn btn-primary">Add Product</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>SKU</th>
            <th>Name</th>
            <th>Description</th>
            <th>Weight (kg)</th>
            <th>Actions</th>
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
              <td>
                <div className="btn-group btn-group-sm">
                  <Link to={`/products/${product.id}`} className="btn btn-outline-primary">Edit</Link>
                  <button 
                    onClick={() => handleDelete(product.id)} 
                    className="btn btn-outline-danger"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
