import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import Login from './Login.jsx'
import Register from './Register.jsx'
import Verify from './Verify.jsx'
import Dashboard from './Dashboard.jsx'
import Footer from './Footer.jsx'
import { api } from './api'

function RequireAuth({ children }) {
  if (!api.isLoggedIn()) {
    return <Navigate to="/login" replace />
  }
  return children
}

function TopBar() {
  const navigate = useNavigate()
  if (!api.isLoggedIn()) return null

  function handleLogout() {
    api.logout()
    navigate('/login')
  }

  return (
    <div className="topbar">
      <div className="brand">
        <span className="brand-mark"></span>
        CareerPilot
        <span className="brand-tag">// resume flight deck</span>
      </div>
      <button className="logout-btn" onClick={handleLogout}>Sign out</button>
    </div>
  )
}

export default function App() {
  return (
    <div className="app-shell">
      <TopBar />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify" element={<Verify />} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <Dashboard />
            </RequireAuth>
          }
        />
      </Routes>
      <Footer />
    </div>
  )
}
