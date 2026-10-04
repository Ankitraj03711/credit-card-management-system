import { useEffect } from 'react'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import { usePath, Redirect, navigateTo } from './components/shared'
import Login from './pages/Login'
import Register from './pages/Register'
import { Dashboard } from './pages/Dashboard'
import { CustomersPage, CustomerFormPage, CustomerDetailsPage } from './pages/Customers'
import { CardsPage, CardCreatePage, CardDetailsPage } from './pages/Cards'
import { RecordEntryPage, HistoryPage, TransactionDetailsPage } from './pages/Transactions'
import { PaymentDetailsPage } from './pages/Payments'
import { ReportsPage } from './pages/Reports'
import { AddressManagementPage, CardTypeManagementPage } from './pages/CardTypes'
import { CustomerApplyCardPage, CustomerApplicationsPage } from './pages/CustomerApplications'
import { AdminCardApplicationsPage } from './pages/AdminCardApplications'
import { CustomerCardsPage, CustomerDashboard, CustomerPaymentsPage, CustomerProfilePage, CustomerTransactionsPage } from './pages/CustomerPortal'

const staffRoles = ['ADMIN', 'CUSTOMER_SERVICE']

function homeFor(user) {
  return user?.role === 'CUSTOMER' ? '/customer/dashboard' : '/admin/dashboard'
}

function legacyDestination(path, user) {
  if (path === '/' || path === '/dashboard') return homeFor(user)
  const section = path.split('/').filter(Boolean)[0]
  if (['customers', 'cards', 'transactions', 'payments', 'reports', 'addresses', 'card-types'].includes(section)) {
    return `/admin${path}`
  }
  return null
}

export default function App() {
  const path = usePath()
  const { user } = useAuth()
  const legacyPath = user && legacyDestination(path, user)
  const isCustomerPath = path === '/customer' || path.startsWith('/customer/')
  const isAdminPath = path === '/admin' || path.startsWith('/admin/')
  const adminPath = isAdminPath ? path.replace(/^\/admin/, '') || '/dashboard' : ''
  const customerAllowed = ['/customer/dashboard', '/customer/profile', '/customer/cards', '/customer/apply-card', '/customer/applications', '/customer/transactions', '/customer/payments'].includes(path)
  const adminAllowed = [
    '/dashboard', '/customers', '/customers/new', '/addresses', '/cards', '/cards/new',
    '/transactions', '/transactions/new', '/payments', '/payments/new', '/reports', '/card-types', '/card-applications',
  ].includes(adminPath) && (adminPath !== '/card-types' || user?.role === 'ADMIN')
    || /^\/(customers|cards|transactions|payments)\/[^/]+(\/edit)?$/.test(adminPath)
  const forbidden = user && ((isCustomerPath && user.role !== 'CUSTOMER') || (isAdminPath && !staffRoles.includes(user.role)))
  const unknownPath = user && ((isCustomerPath && !customerAllowed) || (isAdminPath && !adminAllowed) || (!isCustomerPath && !isAdminPath && !legacyPath))

  useEffect(() => {
    if (!user && !['/login', '/register'].includes(path)) navigateTo('/login')
    else if (user && path === '/login') navigateTo(homeFor(user))
    else if (legacyPath) navigateTo(legacyPath)
    else if (forbidden || unknownPath) navigateTo(homeFor(user))
  }, [user, path, legacyPath, forbidden, unknownPath])

  if (path === '/login') return <Login />
  if (path === '/register') return user ? <Redirect path={homeFor(user)} /> : <Register />
  if (!user) return <Redirect path="/login" />
  if (legacyPath || forbidden || unknownPath) return null

  if (isCustomerPath) {
    let page = <CustomerDashboard />
    if (path === '/customer/profile') page = <CustomerProfilePage />
    else if (path === '/customer/cards') page = <CustomerCardsPage />
    else if (path === '/customer/apply-card') page = <CustomerApplyCardPage />
    else if (path === '/customer/applications') page = <CustomerApplicationsPage />
    else if (path === '/customer/transactions') page = <CustomerTransactionsPage />
    else if (path === '/customer/payments') page = <CustomerPaymentsPage />
    return <Layout>{page}</Layout>
  }

  let page = <Dashboard />
  if (adminPath === '/customers') page = <CustomersPage />
  else if (adminPath === '/customers/new') page = <CustomerFormPage />
  else if (/^\/customers\/[^/]+\/edit$/.test(adminPath)) page = <CustomerFormPage edit />
  else if (/^\/customers\/[^/]+$/.test(adminPath)) page = <CustomerDetailsPage />
  else if (adminPath === '/addresses') page = <AddressManagementPage />
  else if (adminPath === '/cards') page = <CardsPage />
  else if (adminPath === '/cards/new') page = <CardCreatePage />
  else if (/^\/cards\/[^/]+$/.test(adminPath)) page = <CardDetailsPage />
  else if (adminPath === '/transactions') page = <HistoryPage />
  else if (adminPath === '/transactions/new') page = <RecordEntryPage />
  else if (/^\/transactions\/[^/]+$/.test(adminPath)) page = <TransactionDetailsPage />
  else if (adminPath === '/payments') page = <HistoryPage payments />
  else if (adminPath === '/payments/new') page = <RecordEntryPage payments />
  else if (/^\/payments\/[^/]+$/.test(adminPath)) page = <PaymentDetailsPage />
  else if (adminPath === '/card-types') page = <CardTypeManagementPage />
  else if (adminPath === '/reports') page = <ReportsPage />
  else if (adminPath === '/card-applications') page = <AdminCardApplicationsPage />
  return <Layout>{page}</Layout>
}
