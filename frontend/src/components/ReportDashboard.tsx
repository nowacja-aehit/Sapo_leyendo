import { useEffect, useState } from 'react'
import axios from 'axios'

interface DashboardStats {
  totalProducts: number
  totalLocations: number
  totalInventoryItems: number
  totalInboundOrders: number
  totalOutboundOrders: number
}

export default function ReportDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/reports/dashboard')
      .then(response => {
        setStats(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch dashboard stats')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading dashboard...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <h2 className="mb-4">Operational Dashboard</h2>
      
      <div className="row g-4">
        <div className="col-md-4">
          <div className="card text-white bg-primary h-100">
            <div className="card-header">Products</div>
            <div className="card-body">
              <h5 className="card-title display-4">{stats?.totalProducts}</h5>
              <p className="card-text">Total SKUs defined in the system.</p>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card text-white bg-success h-100">
            <div className="card-header">Inventory</div>
            <div className="card-body">
              <h5 className="card-title display-4">{stats?.totalInventoryItems}</h5>
              <p className="card-text">Active inventory records (LPNs/Batches).</p>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card text-white bg-info h-100">
            <div className="card-header">Locations</div>
            <div className="card-body">
              <h5 className="card-title display-4">{stats?.totalLocations}</h5>
              <p className="card-text">Total warehouse locations managed.</p>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card bg-light h-100 border-primary">
            <div className="card-header">Inbound Activity</div>
            <div className="card-body">
              <h5 className="card-title display-6 text-primary">{stats?.totalInboundOrders}</h5>
              <p className="card-text">Total inbound orders (Receipts) processed or planned.</p>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card bg-light h-100 border-warning">
            <div className="card-header">Outbound Activity</div>
            <div className="card-body">
              <h5 className="card-title display-6 text-warning">{stats?.totalOutboundOrders}</h5>
              <p className="card-text">Total outbound orders (Shipments) processed or planned.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
