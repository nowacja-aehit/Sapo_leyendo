import { useEffect, useState } from 'react'
import axios from 'axios'

interface Product {
  id: number
  sku: string
  name: string
}

interface OutboundOrderItem {
  id: number
  product: Product
  quantityOrdered: number
  quantityPicked: number
}

interface OutboundOrder {
  id: number
  referenceNumber: string
  status: string
  shipDate: string
  destination: string
  items: OutboundOrderItem[]
}

export default function OutboundList() {
  const [orders, setOrders] = useState<OutboundOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/outbound')
      .then(response => {
        setOrders(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch outbound orders')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading outbound orders...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <h2>Outbound Orders (Shipments)</h2>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Reference</th>
            <th>Status</th>
            <th>Destination</th>
            <th>Ship Date</th>
            <th>Items Count</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.referenceNumber}</td>
              <td>
                <span className={`badge ${order.status === 'SHIPPED' ? 'bg-success' : order.status === 'PICKED' ? 'bg-info' : 'bg-warning'}`}>
                  {order.status}
                </span>
              </td>
              <td>{order.destination}</td>
              <td>{order.shipDate}</td>
              <td>{order.items ? order.items.length : 0}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
