import { useEffect, useState } from 'react'
import axios from 'axios'

interface MoveTask {
  id: number
  type: string
  inventory: {
    product: { sku: string; name: string }
    lpn: string
    quantity: number
  }
  sourceLocation: { name: string }
  targetLocation: { name: string }
  status: string
}

export default function TaskList() {
  const [tasks, setTasks] = useState<MoveTask[]>([])
  const [loading, setLoading] = useState(true)

  const fetchTasks = () => {
    axios.get('/api/inventory/tasks/pending')
      .then(res => {
        setTasks(res.data)
        setLoading(false)
      })
      .catch(console.error)
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const handleComplete = async (taskId: number) => {
    try {
      await axios.post(`/api/inventory/tasks/${taskId}/complete`)
      fetchTasks()
    } catch (err) {
      console.error(err)
      alert('Failed to complete task')
    }
  }

  if (loading) return <div>Loading tasks...</div>

  return (
    <div>
      <h2>Pending Tasks</h2>
      <table className="table table-bordered">
        <thead>
          <tr>
            <th>ID</th>
            <th>Type</th>
            <th>Item</th>
            <th>From</th>
            <th>To</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(task => (
            <tr key={task.id}>
              <td>{task.id}</td>
              <td>{task.type}</td>
              <td>{task.inventory.product.sku} ({task.inventory.quantity})</td>
              <td>{task.sourceLocation.name}</td>
              <td>{task.targetLocation.name}</td>
              <td>
                <button 
                  className="btn btn-success btn-sm"
                  onClick={() => handleComplete(task.id)}
                >
                  Complete
                </button>
              </td>
            </tr>
          ))}
          {tasks.length === 0 && (
            <tr>
              <td colSpan={6} className="text-center">No pending tasks</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
