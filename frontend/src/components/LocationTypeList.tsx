import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

interface LocationType {
  id: number
  name: string
  maxWeight: number
  maxVolume: number
  length: number
  width: number
  height: number
}

export default function LocationTypeList() {
  const [types, setTypes] = useState<LocationType[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    axios.get('/api/locations/types')
      .then(response => {
        setTypes(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch location types')
        setLoading(false)
      })
  }, [])

  if (loading) return <div>Loading types...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Location Types</h2>
        <Link to="/location-types/new" className="btn btn-primary">Add Type</Link>
      </div>
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Max Weight</th>
            <th>Max Volume</th>
            <th>Dimensions (LxWxH)</th>
          </tr>
        </thead>
        <tbody>
          {types.map(type => (
            <tr key={type.id}>
              <td>{type.id}</td>
              <td>{type.name}</td>
              <td>{type.maxWeight}</td>
              <td>{type.maxVolume}</td>
              <td>{type.length} x {type.width} x {type.height}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
