import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useNavigate } from 'react-router-dom'

interface User {
  id: number
  login: string
  firstName: string
  lastName: string
  active: boolean
  roles: { id: number; roleName: string }[]
}

export default function UserList() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = () => {
    axios.get('/api/users')
      .then(response => {
        setUsers(response.data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError('Failed to fetch users')
        setLoading(false)
      })
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return

    try {
      await axios.delete(`/api/users/${id}`)
      fetchUsers()
    } catch (err) {
      console.error(err)
      alert('Failed to delete user')
    }
  }

  if (loading) return <div>Loading users...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>System Users</h2>
        <Link to="/users/new" className="btn btn-primary">
          <i className="bi bi-plus-lg"></i> Create New User
        </Link>
      </div>

      <div className="card">
        <div className="card-body p-0">
          <table className="table table-striped table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>ID</th>
                <th>Login</th>
                <th>Name</th>
                <th>Roles</th>
                <th>Status</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.login}</td>
                  <td>{user.firstName} {user.lastName}</td>
                  <td>
                    {user.roles?.map(r => (
                      <span key={r.id} className="badge bg-info me-1">{r.roleName}</span>
                    ))}
                  </td>
                  <td>
                    <span className={`badge ${user.active ? 'bg-success' : 'bg-secondary'}`}>
                      {user.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="text-end">
                    <button 
                      className="btn btn-sm btn-outline-primary me-2"
                      onClick={() => navigate(`/users/${user.id}`)}
                    >
                      Edit
                    </button>
                    <button 
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDelete(user.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center py-4">No users found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
