import { useState } from 'react'
import * as customersApi from '../services/customerService'
import * as addressesApi from '../services/addressService'
import * as cardTypesApi from '../services/cardTypeService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { useRequest, PageTitle, Table, Empty, Field, Select, money } from '../components/shared'

function AddressManagementPage() {
  const customers = useRequest(customersApi.getCustomers)
  const [customerId, setCustomerId] = useState('')
  const [addresses, setAddresses] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ addressId: null, homeNumber: '', town: '', pinCode: '', district: '', state: '', country: '' })

  const loadAddresses = async (id) => {
    if (!id) return
    setBusy(true)
    setError('')
    try {
      const { data } = await addressesApi.getAddresses(id)
      setAddresses(data || [])
    } catch (err) {
      setError(err.userMessage || 'Unable to load addresses.')
    } finally {
      setBusy(false)
    }
  }

  const editAddress = (address) => setForm({ ...address })
  const reset = () => setForm({ addressId: null, homeNumber: '', town: '', pinCode: '', district: '', state: '', country: '' })

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      if (form.addressId) {
        await addressesApi.updateAddress(form.addressId, {
          homeNumber: form.homeNumber,
          town: form.town,
          pinCode: form.pinCode,
          district: form.district,
          state: form.state,
          country: form.country,
        })
      } else {
        await addressesApi.createAddress({
          homeNumber: form.homeNumber,
          town: form.town,
          pinCode: form.pinCode,
          district: form.district,
          state: form.state,
          country: form.country,
          customerId: Number(customerId),
        })
      }
      reset()
      await loadAddresses(customerId)
    } catch (err) {
      setError(err.userMessage || 'Unable to save address.')
    } finally {
      setBusy(false)
    }
  }

  const removeAddress = async (addressId) => {
    if (!window.confirm('Delete this address?')) return
    setBusy(true)
    setError('')
    try {
      await addressesApi.deleteAddress(addressId)
      await loadAddresses(customerId)
    } catch (err) {
      setError(err.userMessage || 'Unable to delete address.')
    } finally {
      setBusy(false)
    }
  }

  if (customers.loading) return <LoadingSpinner />
  if (customers.error) return <ErrorMessage message={customers.error} onRetry={customers.retry} />

  return (
    <>
      <PageTitle eyebrow="Configuration" title="Address management" description="Create, update and delete customer addresses." />
      <div className="ui">
        <div className="ui">
          <Select label="Customer" value={customerId} onChange={(e) => { setCustomerId(e.target.value); reset(); setAddresses([]); loadAddresses(e.target.value) }}>
            <option value="">Select customer</option>
            {(customers.data || []).map((customer) => <option key={customer.customerId} value={customer.customerId}>{customer.name} · #{customer.customerId}</option>)}
          </Select>
        </div>
      </div>

      {error && <div className="ui"><ErrorMessage message={error} /></div>}
      {busy && <div className="ui"><LoadingSpinner /></div>}

      {!!customerId && (
        <div className="ui">
          <form onSubmit={submit} className="ui">
            <h2 className="ui">{form.addressId ? 'Update address' : 'New address'}</h2>
            <div className="ui">
              <Field label="Home number" required value={form.homeNumber} onChange={(e) => setForm((prev) => ({ ...prev, homeNumber: e.target.value }))} />
              <Field label="Town" required value={form.town} onChange={(e) => setForm((prev) => ({ ...prev, town: e.target.value }))} />
              <Field label="Pin code" required value={form.pinCode} onChange={(e) => setForm((prev) => ({ ...prev, pinCode: e.target.value }))} />
              <Field label="District" required value={form.district} onChange={(e) => setForm((prev) => ({ ...prev, district: e.target.value }))} />
              <Field label="State" required value={form.state} onChange={(e) => setForm((prev) => ({ ...prev, state: e.target.value }))} />
              <Field label="Country" required value={form.country} onChange={(e) => setForm((prev) => ({ ...prev, country: e.target.value }))} />
            </div>
            <div className="ui">
              <button disabled={busy} className="ui">{form.addressId ? 'Update address' : 'Add address'}</button>
              {form.addressId && <button type="button" className="ui" onClick={reset}>Cancel edit</button>}
            </div>
          </form>

          <div className="ui">
            <Table headers={['Address', 'Town', 'District', 'Actions']}>
              {addresses.map((address) => (
                <tr key={address.addressId}>
                  <td className="ui">{address.homeNumber}, {address.state} - {address.pinCode}</td>
                  <td className="ui">{address.town}</td>
                  <td className="ui">{address.district}</td>
                  <td className="ui">
                    <button className="ui" onClick={() => editAddress(address)}>Edit</button>
                    <button className="ui" onClick={() => removeAddress(address.addressId)}>Delete</button>
                  </td>
                </tr>
              ))}
            </Table>
            {!addresses.length && <Empty text="No addresses found for selected customer." />}
          </div>
        </div>
      )}
    </>
  )
}

function CardTypeManagementPage() {
  const cardTypes = useRequest(cardTypesApi.getCardTypes)
  const [form, setForm] = useState({ cardTypeId: null, name: '', description: '', annualFee: '', network: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const reset = () => setForm({ cardTypeId: null, name: '', description: '', annualFee: '', network: '' })
  const edit = (type) => setForm({ ...type, annualFee: type.annualFee ?? '', network: type.network ?? '' })

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const payload = { name: form.name, description: form.description, annualFee: Number(form.annualFee), network: form.network || null }
      if (form.cardTypeId) await cardTypesApi.updateCardType(form.cardTypeId, payload)
      else await cardTypesApi.createCardType(payload)
      reset()
      cardTypes.retry()
    } catch (err) {
      setError(err.userMessage || 'Unable to save card type.')
    } finally {
      setBusy(false)
    }
  }

  const remove = async (id) => {
    if (!window.confirm('Delete this card type?')) return
    setBusy(true)
    try {
      await cardTypesApi.deleteCardType(id)
      cardTypes.retry()
    } catch (err) {
      setError(err.userMessage || 'Unable to delete card type.')
    } finally {
      setBusy(false)
    }
  }

  if (cardTypes.loading) return <LoadingSpinner />
  if (cardTypes.error) return <ErrorMessage message={cardTypes.error} onRetry={cardTypes.retry} />

  return (
    <>
      <PageTitle eyebrow="Configuration" title="Card Products" description="Manage the real card products available for customer applications." />
      {error && <div className="ui"><ErrorMessage message={error} /></div>}
      <div className="ui">
        <form onSubmit={submit} className="ui">
          <h2 className="ui">{form.cardTypeId ? 'Edit card product' : 'Add card product'}</h2>
          <div className="ui">
            <Field label="Name" required value={form.name} onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
            <label className="ui">
              <span className="ui">Description</span>
              <textarea className="ui" value={form.description} onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))} />
            </label>
            <Field label="Annual fee" type="number" min="0" step="0.01" required value={form.annualFee} onChange={(e) => setForm((prev) => ({ ...prev, annualFee: e.target.value }))} />
            <Select label="Card network" value={form.network} onChange={(e) => setForm((prev) => ({ ...prev, network: e.target.value }))}>
              <option value="">Not configured for applications</option>
              <option value="VISA">VISA</option>
              <option value="MASTERCARD">MASTERCARD</option>
              <option value="RUPAY">RUPAY</option>
            </Select>
          </div>
          <div className="ui">
            <button disabled={busy} className="ui">{form.cardTypeId ? 'Update card type' : 'Create card type'}</button>
            {form.cardTypeId && <button type="button" className="ui" onClick={reset}>Cancel edit</button>}
          </div>
        </form>

        <p className="muted">
          Credit limits are requested per application; this product catalog has no fixed
          credit-limit or active-status field. Application availability reflects whether
          a supported card network is configured.
        </p>

        <div className="ui">
          <Table headers={['Product / card type', 'Network', 'Credit limit', 'Status / availability', 'Annual fee', 'Actions']}>
            {(cardTypes.data || []).map((type) => (
              <tr key={type.cardTypeId}>
                <td className="ui">
                  <strong>{type.name}</strong>
                  <div className="muted">{type.description || 'No product description'}</div>
                </td>
                <td className="ui">{type.network || 'Not configured'}</td>
                <td className="ui">Set per application</td>
                <td className="ui">{type.network ? 'Available for applications' : 'Unavailable — configure network'}</td>
                <td className="ui">{money(type.annualFee)}</td>
                <td className="ui">
                  <button className="ui" onClick={() => edit(type)}>Edit</button>
                  <button className="ui" onClick={() => remove(type.cardTypeId)}>Delete</button>
                </td>
              </tr>
            ))}
          </Table>
        </div>
      </div>
    </>
  )
}

export { AddressManagementPage }
export { CardTypeManagementPage }
