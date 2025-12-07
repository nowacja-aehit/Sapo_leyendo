import { useEffect, useState } from 'react'
import axios from 'axios'

interface User {
  id: number
  login: string
  firstName: string
  lastName: string
  active: boolean
}

export default function UserList() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
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
  }, [])

  if (loading) return <div>Loading users...</div>
  if (error) return <div className="alert alert-danger">{error}</div>

  return (
    <div>
      <h2>System Users</h2>
      <div className="row">
        {users.map(user => (
          <div key={user.id} className="col-md-4 mb-3">
            <div className="card">
              <div className="card-body">
                <h5 className="card-title">{user.firstName} {user.lastName}</h5>
                <h6 className="card-subtitle mb-2 text-muted">@{user.login}</h6>
                <p className="card-text">
                  Status: <span className={`badge ${user.active ? 'bg-success' : 'bg-secondary'}`}>
                    {user.active ? 'Active' : 'Inactive'}
                  </span>
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
