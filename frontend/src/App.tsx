import { useState } from 'react'
import ProductList from './components/ProductList'
import UserList from './components/UserList'

function App() {
  const [view, setView] = useState<'products' | 'users'>('products')

  return (
    <div className="container mt-4">
      <header className="mb-4 border-bottom pb-2">
        <h1 className="display-5">Sapo Leyendo WMS</h1>
        <nav className="nav nav-pills">
          <button 
            className={`nav-link ${view === 'products' ? 'active' : ''}`} 
            onClick={() => setView('products')}
          >
            Products
          </button>
          <button 
            className={`nav-link ${view === 'users' ? 'active' : ''}`} 
            onClick={() => setView('users')}
          >
            Users
          </button>
        </nav>
      </header>

      <main>
        {view === 'products' && <ProductList />}
        {view === 'users' && <UserList />}
      </main>
    </div>
  )
}

export default App
