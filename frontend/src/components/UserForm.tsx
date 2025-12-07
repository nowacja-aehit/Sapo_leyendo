import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'

interface Role {
  id: number
  roleName: string
}

interface UserFormData {
  login: string
  firstName: string
  lastName: string
  password: string
  active: boolean
  roleIds: number[]
}

export default function UserForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditMode = !!id

  const [formData, setFormData] = useState<UserFormData>({
    login: '',
    firstName: '',
    lastName: '',
    password: '',
    active: true,
    roleIds: []
  })
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    // Fetch available roles
    axios.get('/api/roles')
      .then(res => setRoles(res.data))
      .catch(err => console.error('Failed to fetch roles', err))

    if (isEditMode) {
      setLoading(true)
      axios.get(`/api/users/${id}`)
        .then(res => {
          const user = res.data
          setFormData({
            login: user.login,
            firstName: user.firstName,
            lastName: user.lastName,
            password: '', // Don't populate password
            active: user.active,
            roleIds: user.roles ? user.roles.map((r: any) => r.id) : []
          })
          setLoading(false)
        })
        .catch(_err => {
          setError('Failed to fetch user details')
          setLoading(false)
        })
    }
  }, [id, isEditMode])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (isEditMode) {
        await axios.put(`/api/users/${id}`, formData)
      } else {
        await axios.post('/api/users', formData)
      }
      navigate('/users')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save user')
    } finally {
      setLoading(false)
    }
  }

  const handleRoleChange = (roleId: number) => {
    setFormData(prev => {
      const currentRoles = prev.roleIds
      if (currentRoles.includes(roleId)) {
        return { ...prev, roleIds: currentRoles.filter(id => id !== roleId) }
      } else {
        return { ...prev, roleIds: [...currentRoles, roleId] }
      }
    })
  }

  if (loading && isEditMode) return <div>Loading...</div>

  return (
    <div className="card">
      <div className="card-header">
        <h3>{isEditMode ? 'Edit User' : 'Create New User'}</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Login (Username)</label>
            <input
              type="text"
              className="form-control"
              value={formData.login}
              onChange={e => setFormData({...formData, login: e.target.value})}
              required
              disabled={isEditMode} // Usually login shouldn't change
            />
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">First Name</label>
              <input
                type="text"
                className="form-control"
                value={formData.firstName}
                onChange={e => setFormData({...formData, firstName: e.target.value})}
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                className="form-control"
                value={formData.lastName}
                onChange={e => setFormData({...formData, lastName: e.target.value})}
                required
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">
              {isEditMode ? 'New Password (leave blank to keep current)' : 'Password'}
            </label>
            <input
              type="password"
              className="form-control"
              value={formData.password}
              onChange={e => setFormData({...formData, password: e.target.value})}
              required={!isEditMode}
            />
          </div>

          <div className="mb-3 form-check">
            <input
              type="checkbox"
              className="form-check-input"
              id="activeCheck"
              checked={formData.active}
              onChange={e => setFormData({...formData, active: e.target.checked})}
            />
            <label className="form-check-label" htmlFor="activeCheck">Active User</label>
          </div>

          <div className="mb-3">
            <label className="form-label">Roles</label>
            <div className="card p-3">
              {roles.map(role => (
                <div key={role.id} className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`role-${role.id}`}
                    checked={formData.roleIds.includes(role.id)}
                    onChange={() => handleRoleChange(role.id)}
                  />
                  <label className="form-check-label" htmlFor={`role-${role.id}`}>
                    {role.roleName}
                  </label>
                </div>
              ))}
            </div>
          </div>

          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save User'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/users')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
