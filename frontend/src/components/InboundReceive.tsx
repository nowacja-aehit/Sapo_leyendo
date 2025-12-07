import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
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
  items: InboundOrderItem[]
}

export default function InboundReceive() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState<InboundOrder | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  
  // Form state
  const [selectedItemId, setSelectedItemId] = useState<number | ''>('')
  const [lpn, setLpn] = useState('')
  const [quantity, setQuantity] = useState<number>(0)
  const [damageCode, setDamageCode] = useState('')

  useEffect(() => {
    axios.get(`/api/inbound/${id}`)
      .then(response => {
        setOrder(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch order')
        setLoading(false)
      })
  }, [id])

  const handleReceive = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedItemId || !lpn || quantity <= 0) return

    try {
      await axios.post('/api/inbound/receive', {
        inboundOrderItemId: Number(selectedItemId),
        lpn,
        quantity,
        operatorId: 1, // TODO: Get from auth context
        damageCode
      }).then(res => {
         // Auto-generate putaway task
         axios.post(`/api/inventory/putaway/${res.data.id}`)
      })
      
      // Refresh order
      const response = await axios.get(`/api/inbound/${id}`)
      setOrder(response.data)
      
      // Trigger Putaway (Optional auto-trigger)
      // await axios.post(`/api/inventory/putaway/${response.data.lastReceiptId}`)

      // Reset form
      setLpn('')
      setQuantity(0)
      setDamageCode('')
      alert('Item received successfully')
    } catch (err) {
      console.error(err)
      alert('Failed to receive item')
    }
  }

  if (loading) return <div>Loading...</div>
  if (error) return <div className="alert alert-danger">{error}</div>
  if (!order) return <div>Order not found</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Receive Order: {order.referenceNumber}</h2>
        <button className="btn btn-secondary" onClick={() => navigate('/inbound')}>Back</button>
      </div>

      <div className="row">
        <div className="col-md-8">
          <h4>Items</h4>
          <table className="table table-bordered">
            <thead>
              <tr>
                <th>Product</th>
                <th>Expected</th>
                <th>Received</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map(item => (
                <tr key={item.id} className={item.quantityReceived >= item.quantityExpected ? 'table-success' : ''}>
                  <td>{item.product.sku} - {item.product.name}</td>
                  <td>{item.quantityExpected}</td>
                  <td>{item.quantityReceived}</td>
                  <td>
                    {item.quantityReceived < item.quantityExpected && (
                      <button 
                        className="btn btn-sm btn-primary"
                        onClick={() => {
                          setSelectedItemId(item.id)
                          setQuantity(item.quantityExpected - item.quantityReceived)
                        }}
                      >
                        Select
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="col-md-4">
          <div className="card">
            <div className="card-header">Receive Item</div>
            <div className="card-body">
              <form onSubmit={handleReceive}>
                <div className="mb-3">
                  <label className="form-label">Item ID</label>
                  <input type="text" className="form-control" value={selectedItemId} readOnly />
                </div>
                <div className="mb-3">
                  <label className="form-label">LPN (Pallet ID)</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    value={lpn} 
                    onChange={e => setLpn(e.target.value)} 
                    required 
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Quantity</label>
                  <input 
                    type="number" 
                    className="form-control" 
                    value={quantity} 
                    onChange={e => setQuantity(Number(e.target.value))} 
                    required 
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Damage Code (Optional)</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    value={damageCode} 
                    onChange={e => setDamageCode(e.target.value)} 
                  />
                </div>
                <button type="submit" className="btn btn-success w-100" disabled={!selectedItemId}>
                  Confirm Receipt
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
