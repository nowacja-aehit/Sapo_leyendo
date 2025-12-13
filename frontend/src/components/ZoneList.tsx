import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

interface Zone {
  id: number
  name: string
  temperatureControlled: boolean
  secure: boolean
  allowMixedSku: boolean
}

export default function ZoneList() {
  const [zones, setZones] = useState<Zone[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/locations/zones')
      .then(response => {
        setZones(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch zones')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading zones...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Warehouse Zones</h2>
        <Link to="/zones/new" className="btn btn-primary">Add Zone</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Temp. Controlled</th>
            <th>Secure</th>
            <th>Mixed SKU</th>
          </tr>
        </thead>
        <tbody>
          {zones.map(zone => (
            <tr key={zone.id}>
              <td>{zone.id}</td>
              <td>{zone.name}</td>
              <td>{zone.temperatureControlled ? 'Yes' : 'No'}</td>
              <td>{zone.secure ? 'Yes' : 'No'}</td>
              <td>{zone.allowMixedSku ? 'Yes' : 'No'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
