import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'

interface Permission {
  id: number
  name: string
  description: string
}

interface RoleFormData {
  roleName: string
  description: string
  permissionIds: number[]
}

export default function RoleForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditMode = !!id

  const [formData, setFormData] = useState<RoleFormData>({
    roleName: '',
    description: '',
    permissionIds: []
  })
  const [permissions, setPermissions] = useState<Permission[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    // Fetch available permissions
    axios.get('/api/roles/permissions')
      .then(res => setPermissions(res.data))
      .catch(err => console.error('Failed to fetch permissions', err))

    if (isEditMode) {
      setLoading(true)
      axios.get(`/api/roles/${id}`)
        .then(res => {
          const role = res.data
          setFormData({
            roleName: role.roleName,
            description: role.description,
            permissionIds: role.permissions ? role.permissions.map((p: any) => p.id) : []
          })
          setLoading(false)
        })
        .catch(_err => {
          setError('Failed to fetch role details')
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
        await axios.put(`/api/roles/${id}`, formData)
      } else {
        await axios.post('/api/roles', formData)
      }
      navigate('/roles')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save role')
    } finally {
      setLoading(false)
    }
  }

  const handlePermissionChange = (permId: number) => {
    setFormData(prev => {
      const currentPerms = prev.permissionIds
      if (currentPerms.includes(permId)) {
        return { ...prev, permissionIds: currentPerms.filter(id => id !== permId) }
      } else {
        return { ...prev, permissionIds: [...currentPerms, permId] }
      }
    })
  }

  if (loading && isEditMode) return <div>Loading...</div>

  return (
    <div className="card">
      <div className="card-header">
        <h3>{isEditMode ? 'Edit Role' : 'Create New Role'}</h3>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Role Name</label>
            <input
              type="text"
              className="form-control"
              value={formData.roleName}
              onChange={e => setFormData({...formData, roleName: e.target.value})}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Description</label>
            <textarea
              className="form-control"
              value={formData.description}
              onChange={e => setFormData({...formData, description: e.target.value})}
              rows={3}
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Permissions</label>
            <div className="card p-3" style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {permissions.map(perm => (
                <div key={perm.id} className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`perm-${perm.id}`}
                    checked={formData.permissionIds.includes(perm.id)}
                    onChange={() => handlePermissionChange(perm.id)}
                  />
                  <label className="form-check-label" htmlFor={`perm-${perm.id}`}>
                    <strong>{perm.name}</strong> - <small className="text-muted">{perm.description}</small>
                  </label>
                </div>
              ))}
            </div>
          </div>

          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save Role'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/roles')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
