import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

interface Product {
  id: number
  sku: string
  name: string
}

interface InboundOrderItem {
  id: number
  product: Product
  quantityExpected: number
  quantityReceived: number
}

interface InboundOrder {
  id: number
  referenceNumber: string
  status: string
  expectedDate: string
  items: InboundOrderItem[]
}

export default function InboundList() {
  const [orders, setOrders] = useState<InboundOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/inbound')
      .then(response => {
        setOrders(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch inbound orders')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading inbound orders...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Inbound Orders (Receipts)</h2>
        <Link to="/inbound/new" className="btn btn-primary">Create Inbound Order</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Reference</th>
            <th>Status</th>
            <th>Expected Date</th>
            <th>Items Count</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.referenceNumber}</td>
              <td>
                <span className={`badge ${order.status === 'RECEIVED' ? 'bg-success' : 'bg-primary'}`}>
                  {order.status}
                </span>
              </td>
              <td>{order.expectedDate}</td>
              <td>{order.items ? order.items.length : 0}</td>
              <td>
                <Link to={`/inbound/${order.id}/receive`} className="btn btn-sm btn-primary">
                  Receive
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
