import { useEffect, useState } from 'react'
import * as customersApi from '../services/customerService'
import * as cardsApi from '../services/cardService'
import * as addressesApi from '../services/addressService'
import * as authApi from '../services/authService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import StatusBadge from '../components/StatusBadge'
import { useRequest, useRouteParams, navigateTo, notify, PageTitle, Table, Empty, Field, Select, money, date } from '../components/shared'

function CustomersPage() {
  const customers = useRequest(customersApi.getCustomers)
  const navigate = navigateTo
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const pageSize = 10

  if (customers.loading) return <LoadingSpinner />
  if (customers.error) return <ErrorMessage message={customers.error} onRetry={customers.retry} />

  const filtered = (customers.data || []).filter((customer) =>
    `${customer.name} ${customer.email} ${customer.phone}`.toLowerCase().includes(search.toLowerCase())
      && (!status || customer.customerStatus === status))
  const rows = filtered.slice((page - 1) * pageSize, page * pageSize)
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))

  return (
    <>
      <PageTitle eyebrow="CUSTOMER MANAGEMENT" title="Customers" description="Search, review and maintain customer profiles." action={<button className="btn-primary" onClick={() => navigateTo('/customers/new')}><span aria-hidden="true">＋</span> New customer</button>} />
      <div className="ui">
        <div className="table-toolbar">
          <div className="ui">
            <label className="search-field"><span aria-hidden="true">⌕</span><input aria-label="Search customers" value={search} onChange={(e) => { setSearch(e.target.value); setPage(1) }} placeholder="Search name, email or phone" /></label>
          </div>
          <label className="filter-field"><span>Customer status</span><select value={status} onChange={(e) => { setStatus(e.target.value); setPage(1) }}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
        </div>
        <Table headers={['Customer', 'Contact', 'Annual income', 'Status', '']}>
          {rows.map((customer) => (
            <tr key={customer.customerId} className="ui">
              <td className="ui">
                <p className="ui">{customer.name}</p>
                <p className="ui">ID #{customer.customerId}</p>
              </td>
              <td className="ui">
                <p>{customer.email}</p>
                <p className="ui">{customer.phone}</p>
              </td>
              <td className="ui">{money(customer.annualIncome)}</td>
              <td className="ui"><StatusBadge value={customer.customerStatus} /></td>
              <td className="ui">
                <button onClick={() => navigateTo(`/customers/${customer.customerId}`)} className="ui">View</button>
              </td>
            </tr>
          ))}
        </Table>
        {!rows.length && <Empty text="No customers match your search." />}
        {filtered.length > pageSize && <div className="pagination"><span>Showing {(page - 1) * pageSize + 1}–{Math.min(page * pageSize, filtered.length)} of {filtered.length} customers</span><div><button className="btn-secondary" disabled={page === 1} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page} of {totalPages}</span><button className="btn-secondary" disabled={page === totalPages} onClick={() => setPage(page + 1)}>Next</button></div></div>}
      </div>
    </>
  )
}

function CustomerFormPage({ edit = false }) {
  const { id } = useRouteParams()
  const navigate = navigateTo
  const existing = useRequest(() => edit ? customersApi.getCustomer(id) : Promise.resolve({ data: null }), [id, edit])
  const [form, setForm] = useState({ name: '', phone: '', email: '', annualIncome: '', customerStatus: 'ACTIVE', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (existing.data && edit) {
      setForm({
        name: existing.data.name || '',
        phone: existing.data.phone || '',
        email: existing.data.email || '',
        annualIncome: existing.data.annualIncome || '',
        customerStatus: existing.data.customerStatus || 'ACTIVE',
      })
    }
  }, [existing.data, edit])

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    let loginCreated = false
    try {
      if (edit) {
        await customersApi.updateCustomer(id, {
          name: form.name,
          phone: form.phone,
          annualIncome: Number(form.annualIncome),
        })
      } else {
        await authApi.register({ email: form.email, password: form.password, role: 'CUSTOMER' })
        loginCreated = true
        setForm((prev) => ({ ...prev, password: '' }))
        await customersApi.createCustomer({
          name: form.name,
          phone: form.phone,
          annualIncome: Number(form.annualIncome),
          email: form.email,
          customerStatus: form.customerStatus,
        })
      }
      notify(edit ? 'Customer profile updated.' : 'Customer created successfully.')
      navigateTo(edit ? `/customers/${id}` : '/customers')
    } catch (err) {
      setError(err.userMessage || 'Unable to save customer.')
      if (loginCreated) {
        setError(`The customer login was created, but the profile could not be saved: ${err.userMessage || 'backend error'}. Do not retry with the same email; contact an administrator to resolve the incomplete setup.`)
      } else if (!edit && err.status === 409) {
        setError('A login already exists for this email. Use a new email or have an administrator resolve the account before creating this customer.')
      }
    } finally {
      setBusy(false)
    }
  }

  if (edit && existing.loading) return <LoadingSpinner />
  if (edit && existing.error) return <ErrorMessage message={existing.error} onRetry={existing.retry} />

  return (
    <>
      <PageTitle eyebrow="People" title={edit ? 'Edit customer' : 'New customer'} description={edit ? `Update profile #${id}.` : 'Create a customer record.'} />
      <form onSubmit={submit} className="ui">
        {error && <div className="ui"><ErrorMessage message={error} /></div>}
        <div className="ui">
          <Field label="Full name" required value={form.name} onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
          <Field label="Email" type="email" required disabled={edit} value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
          {!edit && <Field label="Temporary sign-in password" type="password" autoComplete="new-password" minLength="8" required value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} />}
          <Field label="Phone" required value={form.phone} onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))} />
          <Field label="Annual income" type="number" min="0" step="0.01" required value={form.annualIncome} onChange={(e) => setForm((prev) => ({ ...prev, annualIncome: e.target.value }))} />
          {!edit && (
            <Select label="Status" value={form.customerStatus} onChange={(e) => setForm((prev) => ({ ...prev, customerStatus: e.target.value }))}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </Select>
          )}
        </div>
        <div className="ui">
          <button disabled={busy} className="ui">{busy ? 'Saving…' : edit ? 'Update customer' : 'Create customer'}</button>
          <button type="button" className="ui" onClick={() => navigateTo(-1)}>Cancel</button>
        </div>
      </form>
    </>
  )
}

function CustomerDetailsPage() {
  const { id } = useRouteParams()
  const navigate = navigateTo
  const customer = useRequest(() => customersApi.getCustomer(id), [id])
  const cards = useRequest(() => cardsApi.getCustomerCards(id), [id])
  const addresses = useRequest(() => addressesApi.getAddresses(id), [id])
  const [statusBusy, setStatusBusy] = useState(false)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [mutationError, setMutationError] = useState('')

  const updateStatus = async (status) => {
    setStatusBusy(true)
    setMutationError('')
    try {
      await customersApi.updateCustomerStatus(id, { status })
      notify(`Customer status changed to ${status}.`)
      customer.retry()
    } catch (err) {
      setMutationError(err.userMessage || 'Unable to update customer status.')
    } finally {
      setStatusBusy(false)
    }
  }

  const removeCustomer = async () => {
    if (!window.confirm('Delete this customer?')) return
    setDeleteBusy(true)
    setMutationError('')
    try {
      await customersApi.deleteCustomer(id)
      notify('Customer deleted.')
      navigateTo('/customers')
    } catch (err) {
      setMutationError(err.userMessage || 'Unable to delete customer.')
      setDeleteBusy(false)
    }
  }

  if (customer.loading || cards.loading || addresses.loading) return <LoadingSpinner />
  if (customer.error || cards.error || addresses.error) return <ErrorMessage message={customer.error || cards.error || addresses.error} onRetry={() => { customer.retry(); cards.retry(); addresses.retry() }} />

  return (
    <>
      <button className="ui" onClick={() => navigateTo('/customers')}>
        <span aria-hidden="true">•</span> Back
      </button>
      <PageTitle eyebrow={`Customer #${id}`} title={customer.data.name} description={customer.data.email} action={<button className="ui" onClick={() => navigateTo(`/customers/${id}/edit`)}>Edit profile</button>} />
      {mutationError && <div className="ui"><ErrorMessage message={mutationError} /></div>}
      <div className="ui">
        <div className="ui">
          <h2 className="ui">Profile</h2>
          <p className="ui">Phone: <span className="ui">{customer.data.phone}</span></p>
          <p className="ui">Annual income: <span className="ui">{money(customer.data.annualIncome)}</span></p>
          <p className="ui">Status:</p>
          <div className="ui"><StatusBadge value={customer.data.customerStatus} /></div>
          <div className="ui">
            <button disabled={statusBusy} className="ui" onClick={() => updateStatus('ACTIVE')}>Set ACTIVE</button>
            <button disabled={statusBusy} className="ui" onClick={() => updateStatus('INACTIVE')}>Set INACTIVE</button>
            <button disabled={deleteBusy} className="ui" onClick={removeCustomer}>
              {deleteBusy ? 'Deleting…' : 'Delete customer'}
            </button>
          </div>
        </div>

        <div className="ui">
          <div className="ui">
            <h2 className="ui">Associated cards</h2>
          </div>
          <Table headers={['Card', 'Type', 'Limit', 'Outstanding', 'Status', '']}>
            {(cards.data || []).map((card) => (
              <tr key={card.cardNumber}>
                <td className="ui">•••• {card.cardNumber?.slice(-4)}</td>
                <td className="ui">#{card.cardTypeId}</td>
                <td className="ui">{money(card.creditLimit)}</td>
                <td className="ui">{money(card.outstandingBalance)}</td>
                <td className="ui"><StatusBadge value={card.cardStatus} /></td>
                <td className="ui"><button className="ui" onClick={() => navigateTo(`/cards/${encodeURIComponent(card.cardNumber)}`)}>Details</button></td>
              </tr>
            ))}
          </Table>
          {!cards.data?.length && <Empty text="No cards associated." />}
        </div>
      </div>

      <div className="ui">
        <div className="ui">
          <h2 className="ui">Addresses</h2>
        </div>
        <Table headers={['Address', 'Town', 'District', 'State', 'Pin']}>
          {(addresses.data || []).map((address) => (
            <tr key={address.addressId}>
              <td className="ui">{address.homeNumber}</td>
              <td className="ui">{address.town}</td>
              <td className="ui">{address.district}</td>
              <td className="ui">{address.state}</td>
              <td className="ui">{address.pinCode}</td>
            </tr>
          ))}
        </Table>
        {!addresses.data?.length && <Empty text="No addresses found." />}
      </div>
    </>
  )
}

export { CustomersPage }
export { CustomerFormPage }
export { CustomerDetailsPage }
