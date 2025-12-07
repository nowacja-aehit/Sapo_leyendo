import { useEffect, useState } from 'react'
import axios from 'axios'

interface Location {
  id: number
  name: string
  zone: string
  type: string
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
      <h2>Warehouse Locations</h2>
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
              <td>{location.zone}</td>
              <td>{location.type}</td>
              <td>
                <span className={`badge ${location.active ? 'bg-success' : 'bg-secondary'}`}>
                  {location.active ? 'Active' : 'Inactive'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
