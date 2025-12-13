import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Login from './components/Login'
import ProductList from './components/ProductList'
import ProductForm from './components/ProductForm'
import UserList from './components/UserList'
import UserForm from './components/UserForm'
import RoleList from './components/RoleList'
import RoleForm from './components/RoleForm'
import LocationList from './components/LocationList'
import LocationForm from './components/LocationForm'
import ZoneList from './components/ZoneList'
import ZoneForm from './components/ZoneForm'
import LocationTypeList from './components/LocationTypeList'
import LocationTypeForm from './components/LocationTypeForm'
import InventoryList from './components/InventoryList'
import InventoryForm from './components/InventoryForm'
import TaskList from './components/TaskList'
import TaskForm from './components/TaskForm'
import InboundList from './components/InboundList'
import InboundForm from './components/InboundForm'
import InboundReceive from './components/InboundReceive'
import OutboundList from './components/OutboundList'
import OutboundForm from './components/OutboundForm'
import ReportDashboard from './components/ReportDashboard'
import PickingDashboard from './components/PickingDashboard'
import PackingStation from './components/PackingStation'
import ShippingDock from './components/ShippingDock'
import QualityControlStation from './components/QualityControlStation'

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
  const { user, logout, hasPermission } = useAuth()
  const location = useLocation()
  
  if (!user) return null

  const isActive = (path: string) => location.pathname === path ? 'active' : ''

  return (
    <header className="mb-4 border-bottom pb-2">
      <div className="d-flex justify-content-between align-items-center">
        <h1 className="display-5">Sapo Leyendo WMS</h1>
        <div className="d-flex align-items-center gap-3">
          <span>Welcome, {user.firstName} ({user.roles?.[0]})</span>
          <button onClick={logout} className="btn btn-outline-danger btn-sm">Logout</button>
        </div>
      </div>
      <nav className="nav nav-pills mt-2">
        {hasPermission('REPORT_VIEW') && (
          <Link className={`nav-link ${isActive('/')}`} to="/">Dashboard</Link>
        )}
        <Link className={`nav-link ${isActive('/products')}`} to="/products">Products</Link>
        {hasPermission('USER_MANAGEMENT') && (
          <>
            <Link className={`nav-link ${isActive('/users')}`} to="/users">Users</Link>
            <Link className={`nav-link ${isActive('/roles')}`} to="/roles">Roles</Link>
          </>
        )}
        <Link className={`nav-link ${isActive('/locations')}`} to="/locations">Locations</Link>
        {hasPermission('INVENTORY_READ') && (
          <>
            <Link className={`nav-link ${isActive('/inventory')}`} to="/inventory">Inventory</Link>
            <Link className={`nav-link ${isActive('/tasks')}`} to="/tasks">Tasks</Link>
          </>
        )}
        {hasPermission('INBOUND_READ') && (
          <Link className={`nav-link ${isActive('/inbound')}`} to="/inbound">Inbound</Link>
        )}
        {hasPermission('OUTBOUND_READ') && (
          <Link className={`nav-link ${isActive('/outbound')}`} to="/outbound">Outbound</Link>
        )}
        <Link className={`nav-link ${isActive('/picking')}`} to="/picking">Picking</Link>
        <Link className={`nav-link ${isActive('/packing')}`} to="/packing">Packing</Link>
        <Link className={`nav-link ${isActive('/shipping')}`} to="/shipping">Shipping</Link>
        <Link className={`nav-link ${isActive('/qc')}`} to="/qc">QC</Link>
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
          <Route path="/products/new" element={
            <PrivateRoute>
              <ProductForm />
            </PrivateRoute>
          } />
          <Route path="/products/:id" element={
            <PrivateRoute>
              <ProductForm />
            </PrivateRoute>
          } />
          <Route path="/users" element={
            <PrivateRoute>
              <UserList />
            </PrivateRoute>
          } />
          <Route path="/users/new" element={
            <PrivateRoute>
              <UserForm />
            </PrivateRoute>
          } />
          <Route path="/users/:id" element={
            <PrivateRoute>
              <UserForm />
            </PrivateRoute>
          } />
          <Route path="/roles" element={
            <PrivateRoute>
              <RoleList />
            </PrivateRoute>
          } />
          <Route path="/roles/new" element={
            <PrivateRoute>
              <RoleForm />
            </PrivateRoute>
          } />
          <Route path="/roles/:id" element={
            <PrivateRoute>
              <RoleForm />
            </PrivateRoute>
          } />
          <Route path="/locations" element={
            <PrivateRoute>
              <LocationList />
            </PrivateRoute>
          } />
          <Route path="/locations/new" element={
            <PrivateRoute>
              <LocationForm />
            </PrivateRoute>
          } />
          <Route path="/zones" element={
            <PrivateRoute>
              <ZoneList />
            </PrivateRoute>
          } />
          <Route path="/zones/new" element={
            <PrivateRoute>
              <ZoneForm />
            </PrivateRoute>
          } />
          <Route path="/location-types" element={
            <PrivateRoute>
              <LocationTypeList />
            </PrivateRoute>
          } />
          <Route path="/location-types/new" element={
            <PrivateRoute>
              <LocationTypeForm />
            </PrivateRoute>
          } />
          <Route path="/inventory" element={
            <PrivateRoute>
              <InventoryList />
            </PrivateRoute>
          } />
          <Route path="/inventory/new" element={
            <PrivateRoute>
              <InventoryForm />
            </PrivateRoute>
          } />
          <Route path="/tasks" element={
            <PrivateRoute>
              <TaskList />
            </PrivateRoute>
          } />
          <Route path="/tasks/new" element={
            <PrivateRoute>
              <TaskForm />
            </PrivateRoute>
          } />
          <Route path="/inbound" element={
            <PrivateRoute>
              <InboundList />
            </PrivateRoute>
          } />
          <Route path="/inbound/new" element={
            <PrivateRoute>
              <InboundForm />
            </PrivateRoute>
          } />
          <Route path="/inbound/:id/receive" element={
            <PrivateRoute>
              <InboundReceive />
            </PrivateRoute>
          } />
          <Route path="/outbound" element={
            <PrivateRoute>
              <OutboundList />
            </PrivateRoute>
          } />
          <Route path="/outbound/new" element={
            <PrivateRoute>
              <OutboundForm />
            </PrivateRoute>
          } />
          <Route path="/picking" element={
            <PrivateRoute>
              <PickingDashboard />
            </PrivateRoute>
          } />
          <Route path="/packing" element={
            <PrivateRoute>
              <PackingStation />
            </PrivateRoute>
          } />
          <Route path="/shipping" element={
            <PrivateRoute>
              <ShippingDock />
            </PrivateRoute>
          } />
          <Route path="/qc" element={
            <PrivateRoute>
              <QualityControlStation />
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
