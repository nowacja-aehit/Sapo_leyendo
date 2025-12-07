import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Login from './components/Login'
import ProductList from './components/ProductList'
import UserList from './components/UserList'
import LocationList from './components/LocationList'
import InventoryList from './components/InventoryList'
import InboundList from './components/InboundList'
import OutboundList from './components/OutboundList'
import ReportDashboard from './components/ReportDashboard'

function PrivateRoute({ children }: { children: JSX.Element }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}

function NavBar() {
  const { user, logout } = useAuth()
  const location = useLocation()
  
  if (!user) return null

  const isActive = (path: string) => location.pathname === path ? 'active' : ''

  return (
    <header className="mb-4 border-bottom pb-2">
      <div className="d-flex justify-content-between align-items-center">
        <h1 className="display-5">Sapo Leyendo WMS</h1>
        <div className="d-flex align-items-center gap-3">
          <span>Welcome, {user.firstName}</span>
          <button onClick={logout} className="btn btn-outline-danger btn-sm">Logout</button>
        </div>
      </div>
      <nav className="nav nav-pills mt-2">
        <Link className={`nav-link ${isActive('/')}`} to="/">Dashboard</Link>
        <Link className={`nav-link ${isActive('/products')}`} to="/products">Products</Link>
        <Link className={`nav-link ${isActive('/users')}`} to="/users">Users</Link>
        <Link className={`nav-link ${isActive('/locations')}`} to="/locations">Locations</Link>
        <Link className={`nav-link ${isActive('/inventory')}`} to="/inventory">Inventory</Link>
        <Link className={`nav-link ${isActive('/inbound')}`} to="/inbound">Inbound</Link>
        <Link className={`nav-link ${isActive('/outbound')}`} to="/outbound">Outbound</Link>
      </nav>
    </header>
  )
}

function AppContent() {
  return (
    <div className="container mt-4">
      <NavBar />
      <main>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={
            <PrivateRoute>
              <ReportDashboard />
            </PrivateRoute>
          } />
          <Route path="/products" element={
            <PrivateRoute>
              <ProductList />
            </PrivateRoute>
          } />
          <Route path="/users" element={
            <PrivateRoute>
              <UserList />
            </PrivateRoute>
          } />
          <Route path="/locations" element={
            <PrivateRoute>
              <LocationList />
            </PrivateRoute>
          } />
          <Route path="/inventory" element={
            <PrivateRoute>
              <InventoryList />
            </PrivateRoute>
          } />
          <Route path="/inbound" element={
            <PrivateRoute>
              <InboundList />
            </PrivateRoute>
          } />
          <Route path="/outbound" element={
            <PrivateRoute>
              <OutboundList />
            </PrivateRoute>
          } />
        </Routes>
      </main>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
