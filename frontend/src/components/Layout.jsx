import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'

const navigation = {
  ADMIN: [
    ['/admin/dashboard', 'Dashboard', '◫'],
    ['/admin/customers', 'Customers', '♙'],
    ['/admin/cards', 'Credit cards', '▤'],
    ['/admin/transactions', 'Transactions', '↗'],
    ['/admin/payments', 'Payments', '$'],
    ['/admin/reports', 'Reports', '▥'],
    ['/admin/card-types', 'Card products', '◈'],
    ['/admin/card-applications', 'Card applications', '▣'],
  ],
  CUSTOMER_SERVICE: [
    ['/admin/dashboard', 'Dashboard', '◫'],
    ['/admin/customers', 'Customers', '♙'],
    ['/admin/cards', 'Credit cards', '▤'],
    ['/admin/transactions', 'Transactions', '↗'],
    ['/admin/payments', 'Payments', '$'],
    ['/admin/reports', 'Reports', '▥'],
  ],
  CUSTOMER: [
    ['/customer/dashboard', 'Dashboard', '◫'],
    ['/customer/profile', 'My profile', '♙'],
    ['/customer/cards', 'My credit cards', '▤'],
    ['/customer/apply-card', 'Apply for Credit Card', '＋'],
    ['/customer/applications', 'My applications', '▣'],
    ['/customer/transactions', 'Transactions', '↗'],
    ['/customer/payments', 'Payments', '$'],
  ],
}

function go(path) {
  window.history.pushState({}, '', path)
  window.dispatchEvent(new PopStateEvent('popstate'))
}

export default function Layout({ children }) {
  const [open, setOpen] = useState(false)
  const [path, setPath] = useState(window.location.pathname)
  const { user, logout } = useAuth()
  useEffect(() => {
    const update = () => setPath(window.location.pathname)
    window.addEventListener('popstate', update)
    return () => window.removeEventListener('popstate', update)
  }, [])
  const visibleItems = navigation[user?.role] || []
  const isActive = (to) => path === to || (to !== '/admin/dashboard' && to !== '/customer/dashboard' && path.startsWith(`${to}/`))
  const currentSection = visibleItems.find(([to]) => isActive(to))?.[1] || 'Dashboard'
  const signOut = () => {
    logout()
    go('/login')
  }

  return <div className="layout">
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand-row"><button className="brand" onClick={() => go(user?.role === 'CUSTOMER' ? '/customer/dashboard' : '/admin/dashboard')}><span className="brand-mark">C</span><span>Cardwise</span></button><button className="mobile-only plain-button" onClick={() => setOpen(false)} aria-label="Close navigation">×</button></div>
      <p className="sidebar-eyebrow">{user?.role === 'CUSTOMER' ? 'YOUR ACCOUNT' : 'MANAGEMENT'}</p>
      <nav className="sidebar-nav" aria-label="Main navigation">{visibleItems.map(([to, label, icon]) => <button key={to} onClick={() => { go(to); setOpen(false) }} className={`nav-item ${isActive(to) ? 'nav-item-active' : ''}`}><span className="nav-icon" aria-hidden="true">{icon}</span>{label}</button>)}</nav>
      <div className="sidebar-footer">
        <div className="account-box"><div className="avatar">{user?.email?.[0]?.toUpperCase()}</div><div className="account-info"><p className="account-email">{user?.email}</p><p className="account-role">{user?.role === 'CUSTOMER_SERVICE' ? 'Operations staff' : user?.role}</p></div></div>
        <button onClick={signOut} className="sign-out"><span aria-hidden="true">↪</span> Logout</button>
      </div>
    </aside>
    {open && <button className="mobile-overlay" onClick={() => setOpen(false)} aria-label="Close navigation" />}
    <main className="main-content"><header className="topbar"><button className="mobile-only menu-button" onClick={() => setOpen(true)} aria-label="Open navigation">☰</button><div className="workspace-label"><span className="online-dot" />{user?.role === 'CUSTOMER' ? 'Personal account' : 'Operations workspace'}<span className="crumb-divider">/</span><strong>{currentSection}</strong></div><div className="user-summary"><span className="user-greeting">{user?.email}</span><span className="avatar small">{user?.email?.[0]?.toUpperCase()}</span></div></header><div className="content">{children}</div></main>
  </div>
}
