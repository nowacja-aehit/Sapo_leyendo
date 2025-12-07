import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

interface Zone {
  id: number
  name: string
}

interface LocationType {
  id: number
  name: string
}

interface Location {
  id: number
  name: string
  zone: Zone
  locationType: LocationType
  status: string
  active: boolean
}

export default function LocationList() {
  const [locations, setLocations] = useState<Location[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/locations')
      .then(response => {
        setLocations(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch locations')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading locations...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Warehouse Locations</h2>
        <Link to="/locations/new" className="btn btn-primary">New Location</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Zone</th>
            <th>Type</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {locations.map(location => (
            <tr key={location.id}>
              <td>{location.id}</td>
              <td>{location.name}</td>
              <td>{location.zone?.name || '-'}</td>
              <td>{location.locationType?.name || '-'}</td>
              <td>
                <span className={`badge ${location.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}`}>
                  {location.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
