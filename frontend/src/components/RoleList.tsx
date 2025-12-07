import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useNavigate } from 'react-router-dom'

interface Role {
  id: number
  roleName: string
  description: string
  permissions: { id: number; name: string }[]
}

export default function RoleList() {
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    fetchRoles()
  }, [])

  const fetchRoles = () => {
    axios.get('/api/roles')
      .then(response => {
        setRoles(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch roles')
        setLoading(false)
      })
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this role?')) return

    try {
      await axios.delete(`/api/roles/${id}`)
      fetchRoles()
    } catch (err) {
      console.error(err)
      alert('Failed to delete role')
    }
  }

  if (loading) return <div>Loading roles...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>System Roles</h2>
        <Link to="/roles/new" className="btn btn-primary">
          <i className="bi bi-plus-lg"></i> Create New Role
        </Link>
      </div>

      <div className="card">
        <div className="card-body p-0">
          <table className="table table-striped table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Description</th>
                <th>Permissions</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {roles.map(role => (
                <tr key={role.id}>
                  <td>{role.id}</td>
                  <td>{role.roleName}</td>
                  <td>{role.description}</td>
                  <td>
                    {role.permissions?.map(p => (
                      <span key={p.id} className="badge bg-secondary me-1">{p.name}</span>
                    ))}
                  </td>
                  <td className="text-end">
                    <button 
                      className="btn btn-sm btn-outline-primary me-2"
                      onClick={() => navigate(`/roles/${role.id}`)}
                    >
                      Edit
                    </button>
                    <button 
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDelete(role.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {roles.length === 0 && (
                <tr>
                  <td colSpan={5} className="text-center py-4">No roles found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
