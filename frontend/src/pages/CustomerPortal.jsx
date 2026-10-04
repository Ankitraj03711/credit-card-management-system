import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import * as cardsApi from '../services/cardService'
import * as cardTypesApi from '../services/cardTypeService'
import * as customersApi from '../services/customerService'
import * as paymentsApi from '../services/paymentService'
import * as transactionsApi from '../services/transactionService'
import ErrorMessage from '../components/ErrorMessage'
import LoadingSpinner from '../components/LoadingSpinner'
import StatusBadge from '../components/StatusBadge'
import { Empty, PageTitle, Stat, Table, money, date, dateTime, navigateTo } from '../components/shared'

function useCustomerData() {
  const { user } = useAuth()
  const [attempt, setAttempt] = useState(0)
  const [state, setState] = useState({ loading: true, error: '', customer: null, cards: [], transactions: [], payments: [], cardTypes: [] })

  useEffect(() => {
    let active = true
    setState((prev) => ({ ...prev, loading: true, error: '' }))

    async function load() {
      try {
        const { data: customer } = await customersApi.getCurrentCustomer()
        const [cards, transactions, payments, cardTypes] = await Promise.all([
          cardsApi.getCustomerCards(customer.customerId),
          transactionsApi.getCustomerTransactions(customer.customerId),
          paymentsApi.getCustomerPayments(customer.customerId),
          cardTypesApi.getCardTypes(),
        ])
        if (active) {
          setState({
            loading: false,
            error: '',
            customer,
            cards: cards.data || [],
            transactions: transactions.data || [],
            payments: payments.data || [],
            cardTypes: cardTypes.data || [],
          })
        }
      } catch (error) {
        if (active) setState((prev) => ({ ...prev, loading: false, error: error.userMessage || error.message || 'Unable to load your account.' }))
      }
    }

    load()
    return () => { active = false }
  }, [user?.email, attempt])

  return { ...state, retry: () => setAttempt((count) => count + 1) }
}

function PortalState({ data, children }) {
  if (data.loading) return <LoadingSpinner />
  if (data.error) return <ErrorMessage message={data.error} onRetry={data.retry} />
  return children
}

function orderedNewest(items, key) {
  return [...items].sort((a, b) => new Date(b[key] || 0) - new Date(a[key] || 0))
}

function CustomerDashboardContent({ data }) {
  const { customer, cards, transactions, payments } = data
  const activeCards = cards.filter((card) => card.cardStatus === 'ACTIVE')
  const recentTransactions = orderedNewest(transactions, 'transactionDate').slice(0, 5)
  const recentPayments = orderedNewest(payments, 'paymentDate').slice(0, 5)
  const totalLimit = cards.reduce((sum, card) => sum + Number(card.creditLimit || 0), 0)
  const availableLimit = cards.reduce((sum, card) => sum + Number(card.availableLimit || 0), 0)
  const outstanding = cards.reduce((sum, card) => sum + Number(card.outstandingBalance || 0), 0)

  return <>
    <PageTitle eyebrow="YOUR ACCOUNT" title={`Welcome, ${customer.name.split(' ')[0]}`} description="Your credit at a glance. Here’s what’s happening with your account." />
    <div className="portal-stat-grid">
      <Stat label="Total credit limit" value={money(totalLimit)} note="Across all your cards" />
      <Stat label="Available credit" value={money(availableLimit)} note="Ready to use" />
      <Stat label="Outstanding balance" value={money(outstanding)} note="Current balance" />
      <Stat label="Active cards" value={activeCards.length} note={`${cards.length} card${cards.length === 1 ? '' : 's'} in your account`} />
    </div>
    <div className="portal-grid">
      <section className="portal-panel">
        <div className="section-heading"><div><h2>Recent transactions</h2><p>Your latest card activity</p></div><span className="section-icon">↗</span></div>
        {recentTransactions.length ? <Table headers={['Transaction', 'Card', 'Date', 'Amount']}>
          {recentTransactions.map((item) => <tr key={item.transactionId}>
            <td><div className="primary-cell">{item.merchant || item.description || item.transactionType?.replaceAll('_', ' ')}</div><div className="secondary-cell">Recorded</div></td>
            <td>•••• {item.cardNumber?.slice(-4)}</td>
            <td>{dateTime(item.transactionDate)}</td>
            <td className="amount-cell">{money(item.amount)}</td>
          </tr>)}
        </Table> : <Empty text="No transactions yet. Your card activity will appear here." />}
      </section>
      <section className="portal-panel">
        <div className="section-heading"><div><h2>Recent payments</h2><p>Your latest payment activity</p></div><span className="section-icon">↙</span></div>
        {recentPayments.length ? <Table headers={['Payment', 'Card', 'Date', 'Amount']}>
          {recentPayments.map((item) => <tr key={item.paymentReference}>
            <td><div className="primary-cell">{item.paymentMode?.replaceAll('_', ' ')}</div><div className="secondary-cell">{item.paymentReference}</div></td>
            <td>•••• {item.cardNumber?.slice(-4)}</td>
            <td>{dateTime(item.paymentDate)}</td>
            <td className="amount-cell">{money(item.amount)}</td>
          </tr>)}
        </Table> : <Empty text="No payments yet. Payments you make will appear here." />}
      </section>
    </div>
    <section className="portal-panel account-summary">
      <div className="section-heading"><div><h2>Your cards</h2><p>Credit available across your cards</p></div></div>
      {cards.length ? <div className="mini-card-list">{cards.slice(0, 3).map((card) => {
        const product = data.cardTypes.find((type) => type.cardTypeId === card.cardTypeId)
        return <div className="mini-card-row" key={card.cardNumber}>
          <div className="card-chip">▤</div><div className="mini-card-details"><strong>•••• {card.cardNumber?.slice(-4)}</strong><span>{product?.network || 'Network not configured'} · {product?.name || `Card type ${card.cardTypeId}`}</span><span>Expires {date(card.expiryDate)}</span></div>
          <StatusBadge value={card.cardStatus} /><strong className="mini-card-balance">{money(card.availableLimit)} <small>available</small></strong>
          <div className="mini-card-metrics"><span>Limit {money(card.creditLimit)}</span><span>Outstanding {money(card.outstandingBalance)}</span></div>
        </div>
      })}</div> : <><Empty text="You don't have a credit card yet." /><button className="btn-primary" onClick={() => navigateTo('/customer/apply-card')}>Apply for a Credit Card</button></>}
    </section>
    <section className="portal-panel">
      <div className="section-heading"><div><h2>Quick actions</h2><p>Manage your Cardwise account</p></div></div>
      <div className="customer-quick-actions">
        <button className="btn-primary" onClick={() => navigateTo('/customer/apply-card')}>＋ Apply for a Credit Card</button>
        <button className="btn-secondary" onClick={() => navigateTo('/customer/cards')}>View My Cards</button>
        <button className="btn-secondary" onClick={() => navigateTo('/customer/transactions')}>View Transactions</button>
        <button className="btn-secondary" onClick={() => navigateTo('/customer/payments')}>Make/View Payments</button>
      </div>
    </section>
  </>
}

export function CustomerDashboard() {
  const data = useCustomerData()
  return <PortalState data={data}><CustomerDashboardContent data={data} /></PortalState>
}

function CustomerProfileContent({ customer, onCustomerUpdated }) {
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState(() => ({
    name: customer.name || '',
    email: customer.email || '',
    phone: customer.phone || '',
    address: customer.address || '',
    annualIncome: customer.annualIncome == null ? '' : String(customer.annualIncome),
  }))
  const [saveError, setSaveError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    setForm({
      name: customer.name || '',
      email: customer.email || '',
      phone: customer.phone || '',
      address: customer.address || '',
      annualIncome: customer.annualIncome == null ? '' : String(customer.annualIncome),
    })
  }, [customer])

  const cancelEdit = () => {
    setForm({
      name: customer.name || '',
      email: customer.email || '',
      phone: customer.phone || '',
      address: customer.address || '',
      annualIncome: customer.annualIncome == null ? '' : String(customer.annualIncome),
    })
    setSaveError('')
    setEditing(false)
  }

  const saveProfile = async (event) => {
    event.preventDefault()
    setSaving(true)
    setSaveError('')
    setNotice('')
    try {
      const { data: updatedCustomer } = await customersApi.updateMyProfile({
        name: form.name,
        phone: form.phone,
        address: form.address,
        annualIncome: Number(form.annualIncome),
      })
      onCustomerUpdated(updatedCustomer)
      setEditing(false)
      setNotice('Profile updated successfully.')
    } catch (error) {
      setSaveError(error.userMessage || error.response?.data?.message || error.message || 'Unable to update your profile.')
    } finally {
      setSaving(false)
    }
  }

  return <>
    <PageTitle eyebrow="PERSONAL DETAILS" title="My profile" description="The personal information associated with your account." />
    <section className="portal-panel profile-panel">
      <div className="profile-monogram">{customer.name.split(/\s+/).map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</div>
      <div className="profile-name"><h2>{customer.name}</h2><StatusBadge value={customer.customerStatus} /><p>Customer since your account was created</p></div>
      {editing ? <form className="profile-edit-form" onSubmit={saveProfile}>
        <label><span className="label">Full Name</span><input className="field" required maxLength={100} value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} /></label>
        <label><span className="label">Email</span><input className="field" type="email" readOnly value={form.email} /></label>
        <label><span className="label">Phone Number</span><input className="field" type="tel" required maxLength={15} value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} /></label>
        <label className="profile-address-field"><span className="label">Address</span><textarea className="field" rows="3" maxLength={500} value={form.address} onChange={(event) => setForm((current) => ({ ...current, address: event.target.value }))} /></label>
        <label><span className="label">Annual Income</span><input className="field" type="number" min="0" step="0.01" inputMode="decimal" required value={form.annualIncome} onChange={(event) => setForm((current) => ({ ...current, annualIncome: event.target.value }))} /></label>
        {saveError && <p className="profile-form-error" role="alert">{saveError}</p>}
        <div className="profile-form-actions"><button className="btn-secondary" type="button" onClick={cancelEdit} disabled={saving}>Cancel</button><button className="btn-primary" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Changes'}</button></div>
      </form> : <>
        <dl className="profile-fields">
          <div><dt>Email address</dt><dd>{customer.email}</dd></div>
          <div><dt>Phone number</dt><dd>{customer.phone}</dd></div>
          <div><dt>Customer ID</dt><dd>#{customer.customerId}</dd></div>
          <div><dt>Address</dt><dd>{customer.address || '—'}</dd></div>
          <div><dt>Annual Income</dt><dd>{money(customer.annualIncome)}</dd></div>
        </dl>
        <div className="profile-form-actions">
          {notice && <p className="profile-form-success" role="status">{notice}</p>}
          <button className="btn-secondary" type="button" onClick={() => { setNotice(''); setEditing(true) }}>Edit Profile</button>
        </div>
      </>}
    </section>
  </>
}

export function CustomerProfilePage() {
  const data = useCustomerData()
  const [customer, setCustomer] = useState(null)

  useEffect(() => {
    setCustomer(data.customer)
  }, [data.customer])

  return <PortalState data={data}>{customer && <CustomerProfileContent customer={customer} onCustomerUpdated={setCustomer} />}</PortalState>
}

export function CustomerCardsPage() {
  const data = useCustomerData()
  const typeFor = (card) => data.cardTypes.find((type) => type.cardTypeId === card.cardTypeId)
  const [cvvs, setCvvs] = useState({})
  const [loadingCvv, setLoadingCvv] = useState({})
  const [cvvErrors, setCvvErrors] = useState({})

  const toggleCvv = async (cardNumber) => {
    if (cvvs[cardNumber]) {
      setCvvs((current) => ({ ...current, [cardNumber]: null }))
      setCvvErrors((current) => ({ ...current, [cardNumber]: '' }))
      return
    }
    setLoadingCvv((current) => ({ ...current, [cardNumber]: true }))
    setCvvErrors((current) => ({ ...current, [cardNumber]: '' }))
    try {
      const { data: response } = await cardsApi.getCardCvv(cardNumber)
      setCvvs((current) => ({ ...current, [cardNumber]: response.cvv }))
    } catch (error) {
      setCvvErrors((current) => ({ ...current, [cardNumber]: error.userMessage || error.message || 'Unable to retrieve this CVV.' }))
    } finally {
      setLoadingCvv((current) => ({ ...current, [cardNumber]: false }))
    }
  }

  return <PortalState data={data}>
    <PageTitle eyebrow="YOUR CARDS" title="My credit cards" description="Card details and credit availability for your account." />
    <section className="portal-panel">
      {data.cards.length ? <div className="table-wrap"><Table headers={['Card', 'Network', 'Card type', 'Expiry date', 'Credit limit', 'Available', 'Outstanding', 'CVV', 'Status']}>
        {data.cards.map((card) => <tr key={card.cardNumber}>
          <td><div className="card-number-cell">•••• •••• •••• {card.cardNumber?.slice(-4)}</div><div className="secondary-cell">Issued {date(card.issueDate)}</div></td>
          <td>{typeFor(card)?.network || 'Not configured'}</td>
          <td>{typeFor(card)?.name || `Card type ${card.cardTypeId}`}</td>
          <td>{date(card.expiryDate)}</td>
          <td>{money(card.creditLimit)}</td>
          <td className="amount-cell">{money(card.availableLimit)}</td>
          <td>{money(card.outstandingBalance)}</td>
          <td><span className="cvv-value">{cvvs[card.cardNumber] || '•••'}</span><button className="cvv-toggle" type="button" onClick={() => toggleCvv(card.cardNumber)} disabled={loadingCvv[card.cardNumber]} aria-label={`${cvvs[card.cardNumber] ? 'Hide' : 'Show'} CVV for card ending ${card.cardNumber?.slice(-4)}`}>{loadingCvv[card.cardNumber] ? 'Loading…' : cvvs[card.cardNumber] ? 'Hide CVV' : 'Show CVV'}</button>{cvvErrors[card.cardNumber] && <span className="cvv-error" role="alert">{cvvErrors[card.cardNumber]}</span>}</td>
          <td><StatusBadge value={card.cardStatus} /></td>
        </tr>)}
      </Table></div> : <><Empty text="You don't have a credit card yet." /><button className="btn-primary" onClick={() => navigateTo('/customer/apply-card')}>Apply for a Credit Card</button></>}
    </section>
    <p className="privacy-note"><span aria-hidden="true">▣</span> Full card numbers stay hidden. CVVs are retrieved securely only when you choose to show one.</p>
  </PortalState>
}

function CustomerHistory({ payments = false }) {
  const data = useCustomerData()
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(1)
  const pageSize = 8
  const source = payments ? data.payments : data.transactions
  const dateField = payments ? 'paymentDate' : 'transactionDate'
  const sorted = useMemo(() => orderedNewest(source, dateField), [source, dateField])
  const filtered = sorted.filter((item) => {
    const type = payments ? item.paymentMode : item.transactionType
    const reference = payments ? item.paymentReference : item.transactionId
    return (!filter || type === filter)
      && `${type || ''} ${item.cardNumber || ''} ${reference || ''} ${item.merchant || ''} ${item.description || ''}`.toLowerCase().includes(search.toLowerCase())
  })
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const visible = filtered.slice((page - 1) * pageSize, page * pageSize)

  useEffect(() => setPage(1), [search, filter])

  const modesOrTypes = [...new Set(source.map((item) => payments ? item.paymentMode : item.transactionType).filter(Boolean))]

  return <PortalState data={data}>
    <PageTitle eyebrow={payments ? 'YOUR PAYMENTS' : 'YOUR ACTIVITY'} title={payments ? 'Payment history' : 'Transaction history'} description={payments ? 'Payments made on your credit cards.' : 'Purchases, withdrawals, fees and refunds across your cards.'} />
    <section className="portal-panel">
      <div className="table-toolbar">
        <label className="search-field"><span aria-hidden="true">⌕</span><input aria-label={`Search ${payments ? 'payments' : 'transactions'}`} placeholder="Search by card or reference" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
        <label className="filter-field"><span>{payments ? 'Payment mode' : 'Transaction type'}</span><select value={filter} onChange={(event) => setFilter(event.target.value)}><option value="">All</option>{modesOrTypes.map((type) => <option value={type} key={type}>{type.replaceAll('_', ' ')}</option>)}</select></label>
      </div>
      {visible.length ? <div className="table-wrap"><Table headers={payments ? ['Payment', 'Card', 'Date', 'Amount', 'Mode', 'Status'] : ['Transaction', 'Card', 'Date', 'Amount', 'Type', 'Status']}>
        {visible.map((item) => payments ? <tr key={item.paymentReference}>
          <td><div className="primary-cell">{item.paymentReference}</div><div className="secondary-cell">{item.description || 'Card payment'}</div></td>
          <td>•••• {item.cardNumber?.slice(-4)}</td>
          <td>{dateTime(item.paymentDate)}</td>
          <td className="amount-cell">{money(item.amount)}</td>
          <td>{item.paymentMode?.replaceAll('_', ' ') || '—'}</td>
          <td><StatusBadge value={item.paymentStatus || 'RECORDED'} /></td>
        </tr> : <tr key={item.transactionId}>
          <td><div className="primary-cell">{item.merchant || item.description || item.transactionType?.replaceAll('_', ' ')}</div><div className="secondary-cell">{item.transactionId}</div></td>
          <td>•••• {item.cardNumber?.slice(-4)}</td>
          <td>{dateTime(item.transactionDate)}</td>
          <td className="amount-cell">{money(item.amount)}</td>
          <td><StatusBadge value={item.transactionType} /></td>
          <td><StatusBadge value="RECORDED" /></td>
        </tr>)}
      </Table></div> : <Empty text={source.length ? 'No records match your search.' : payments ? 'No payment history is available yet.' : 'No transaction history is available yet.'} />}
      {filtered.length > pageSize && <div className="pagination"><span>Showing {(page - 1) * pageSize + 1}–{Math.min(page * pageSize, filtered.length)} of {filtered.length}</span><div><button className="btn-secondary" disabled={page === 1} onClick={() => setPage((value) => value - 1)}>Previous</button><span>Page {page} of {totalPages}</span><button className="btn-secondary" disabled={page === totalPages} onClick={() => setPage((value) => value + 1)}>Next</button></div></div>}
    </section>
  </PortalState>
}

export function CustomerTransactionsPage() {
  return <CustomerHistory />
}

export function CustomerPaymentsPage() {
  return <CustomerHistory payments />
}
