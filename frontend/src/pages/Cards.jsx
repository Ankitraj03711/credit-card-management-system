import { useState } from 'react'
import * as customersApi from '../services/customerService'
import * as cardsApi from '../services/cardService'
import * as cardTypesApi from '../services/cardTypeService'
import * as transactionsApi from '../services/transactionService'
import * as paymentsApi from '../services/paymentService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import StatusBadge from '../components/StatusBadge'
import { useRequest, useRouteParams, navigateTo, notify, PageTitle, Stat, Table, Empty, Field, Select, money, date, dateTime } from '../components/shared'

function CardsPage() {
  const cards = useRequest(cardsApi.getCards)
  const navigate = navigateTo
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const pageSize = 10

  if (cards.loading) return <LoadingSpinner />
  if (cards.error) return <ErrorMessage message={cards.error} onRetry={cards.retry} />

  const filtered = (cards.data || []).filter((card) =>
    (card.cardNumber?.endsWith(search) || !search)
      && (!status || card.cardStatus === status))
  const rows = filtered.slice((page - 1) * pageSize, page * pageSize)
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))

  return (
    <>
      <PageTitle eyebrow="CARD PORTFOLIO" title="Credit cards" description="Review card status, balances and lifecycle actions." action={<button className="btn-primary" onClick={() => navigateTo('/cards/new')}><span aria-hidden="true">＋</span> Issue card</button>} />
      <div className="ui">
        <div className="table-toolbar">
          <div className="ui">
            <label className="search-field"><span aria-hidden="true">⌕</span><input aria-label="Search cards" inputMode="numeric" maxLength={4} value={search} onChange={(e) => { setSearch(e.target.value.replace(/\D/g, '').slice(0, 4)); setPage(1) }} placeholder="Search last 4 digits" /></label>
          </div>
          <label className="filter-field"><span>Card status</span><select value={status} onChange={(e) => { setStatus(e.target.value); setPage(1) }}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="BLOCKED">Blocked</option><option value="CLOSED">Closed</option></select></label>
        </div>
        <Table headers={['Card', 'Customer', 'Limit', 'Available', 'Expiry', 'Status', '']}>
          {rows.map((card) => (
            <tr key={card.cardNumber}>
              <td className="ui">•••• {card.cardNumber?.slice(-4)}</td>
              <td className="ui">#{card.customerId}</td>
              <td className="ui">{money(card.creditLimit)}</td>
              <td className="ui">{money(card.availableLimit)}</td>
              <td className="ui">{date(card.expiryDate)}</td>
              <td className="ui"><StatusBadge value={card.cardStatus} /></td>
              <td className="ui"><button className="ui" onClick={() => navigateTo(`/cards/${encodeURIComponent(card.cardNumber)}`)}>Details</button></td>
            </tr>
          ))}
        </Table>
        {!rows.length && <Empty text="No cards match your search." />}
        {filtered.length > pageSize && <div className="pagination"><span>Showing {(page - 1) * pageSize + 1}–{Math.min(page * pageSize, filtered.length)} of {filtered.length} cards</span><div><button className="btn-secondary" disabled={page === 1} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page} of {totalPages}</span><button className="btn-secondary" disabled={page === totalPages} onClick={() => setPage(page + 1)}>Next</button></div></div>}
      </div>
    </>
  )
}

function CardCreatePage() {
  const customers = useRequest(customersApi.getCustomers)
  const cardTypes = useRequest(cardTypesApi.getCardTypes)
  const navigate = navigateTo
  const [form, setForm] = useState({ customerId: '', cardTypeId: '', creditLimit: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      await cardsApi.issueCard({
        customerId: Number(form.customerId),
        cardTypeId: Number(form.cardTypeId),
        creditLimit: Number(form.creditLimit),
      })
      notify('Credit card issued successfully.')
      navigateTo('/cards')
    } catch (err) {
      setError(err.userMessage || 'Unable to issue card.')
    } finally {
      setBusy(false)
    }
  }

  if (customers.loading || cardTypes.loading) return <LoadingSpinner />

  return (
    <>
      <PageTitle eyebrow="Portfolio" title="Issue card" description="Backend will generate card number and card metadata." />
      <form onSubmit={submit} className="ui">
        {error && <div className="ui"><ErrorMessage message={error} /></div>}
        <div className="ui">
          <Select label="Customer" value={form.customerId} onChange={(e) => setForm((prev) => ({ ...prev, customerId: e.target.value }))} required>
            <option value="">Select customer</option>
            {(customers.data || []).map((customer) => <option key={customer.customerId} value={customer.customerId}>{customer.name} · #{customer.customerId}</option>)}
          </Select>
          <Select label="Card type" value={form.cardTypeId} onChange={(e) => setForm((prev) => ({ ...prev, cardTypeId: e.target.value }))} required>
            <option value="">Select card type</option>
            {(cardTypes.data || []).map((cardType) => <option key={cardType.cardTypeId} value={cardType.cardTypeId}>{cardType.name} · {money(cardType.annualFee)}</option>)}
          </Select>
          <Field label="Credit limit" type="number" min="0.01" step="0.01" required value={form.creditLimit} onChange={(e) => setForm((prev) => ({ ...prev, creditLimit: e.target.value }))} />
        </div>
        <div className="ui">
          <button disabled={busy} className="ui">{busy ? 'Issuing…' : 'Issue card'}</button>
          <button type="button" className="ui" onClick={() => navigateTo(-1)}>Cancel</button>
        </div>
      </form>
    </>
  )
}

function CardDetailsPage() {
  const { cardNumber } = useRouteParams()
  const decodedCardNumber = decodeURIComponent(cardNumber)
  const card = useRequest(() => cardsApi.getCard(decodedCardNumber), [decodedCardNumber])
  const transactions = useRequest(() => transactionsApi.getCardTransactions(decodedCardNumber), [decodedCardNumber])
  const payments = useRequest(() => paymentsApi.getCardPayments(decodedCardNumber), [decodedCardNumber])
  const txSummary = useRequest(() => transactionsApi.getTransactionSummary(decodedCardNumber), [decodedCardNumber])
  const paySummary = useRequest(() => paymentsApi.getPaymentSummary(decodedCardNumber), [decodedCardNumber])
  const [actionBusy, setActionBusy] = useState(false)
  const [actionError, setActionError] = useState('')

  const changeStatus = async (action) => {
    if (['block', 'close'].includes(action) && !window.confirm(`Are you sure you want to ${action} this card?`)) return
    setActionBusy(true)
    setActionError('')
    try {
      await cardsApi.changeCardStatus(decodedCardNumber, action)
      const actionLabel = { activate: 'activated', block: 'blocked', unblock: 'unblocked', close: 'closed' }[action]
      notify(`Card ${actionLabel} successfully.`)
      card.retry()
    } catch (err) {
      setActionError(err.userMessage || 'Unable to change card status.')
    } finally {
      setActionBusy(false)
    }
  }

  if (card.loading || transactions.loading || payments.loading || txSummary.loading || paySummary.loading) return <LoadingSpinner />
  if (card.error || transactions.error || payments.error || txSummary.error || paySummary.error) return <ErrorMessage message={card.error || transactions.error || payments.error || txSummary.error || paySummary.error} />

  return (
    <>
      <PageTitle eyebrow={`Card •••• ${decodedCardNumber.slice(-4)}`} title="Card details" description={`Customer #${card.data.customerId}`} />
      <div className="ui">
        <div className="stat-grid">
          <Stat label="Credit limit" value={money(card.data.creditLimit)} />
          <Stat label="Available limit" value={money(card.data.availableLimit)} tone="mint" />
          <Stat label="Outstanding" value={money(card.data.outstandingBalance)} tone="amber" />
          <Stat label="Card status" value={card.data.cardStatus} />
        </div>
      </div>

      <div className="ui">
        <h2 className="ui">Card lifecycle actions</h2>
        {actionError && <div className="ui"><ErrorMessage message={actionError} /></div>}
        <div className="ui">
          {card.data.cardStatus === 'BLOCKED' && <button disabled={actionBusy} className="ui" onClick={() => changeStatus('unblock')}>Unblock card</button>}
          {card.data.cardStatus === 'ACTIVE' && <button disabled={actionBusy} className="ui" onClick={() => changeStatus('block')}>Block card</button>}
          {card.data.cardStatus !== 'CLOSED' && <button disabled={actionBusy} className="ui" onClick={() => changeStatus('close')}>Close card</button>}
        </div>
      </div>

      <div className="ui">
        <div className="ui">
          <div className="ui"><h3 className="ui">Transaction summary</h3></div>
          <div className="ui">
            <div><p className="ui">Total transactions</p><p className="ui">{txSummary.data.totalTransactions}</p></div>
            <div><p className="ui">Total amount</p><p className="ui">{money(txSummary.data.totalTransactionAmount)}</p></div>
            <div><p className="ui">Purchases</p><p className="ui">{txSummary.data.purchaseCount} · {money(txSummary.data.purchaseAmount)}</p></div>
            <div><p className="ui">Cash withdrawals</p><p className="ui">{txSummary.data.cashWithdrawalCount} · {money(txSummary.data.cashWithdrawalAmount)}</p></div>
            <div><p className="ui">Fees</p><p className="ui">{txSummary.data.feeCount} · {money(txSummary.data.feeAmount)}</p></div>
            <div><p className="ui">Refunds</p><p className="ui">{txSummary.data.refundCount} · {money(txSummary.data.refundAmount)}</p></div>
          </div>
        </div>

        <div className="ui">
          <div className="ui"><h3 className="ui">Payment summary</h3></div>
          <div className="ui">
            <div><p className="ui">Total payments</p><p className="ui">{paySummary.data.totalPayments}</p></div>
            <div><p className="ui">Total paid</p><p className="ui">{money(paySummary.data.totalPaidAmount)}</p></div>
            <div><p className="ui">Last payment</p><p className="ui">{money(paySummary.data.lastPaymentAmount)}</p></div>
            <div><p className="ui">Last payment date</p><p className="ui">{dateTime(paySummary.data.lastPaymentDate)}</p></div>
          </div>
        </div>
      </div>

      <div className="ui">
        <div className="ui">
          <div className="ui"><h3 className="ui">Recent transactions</h3></div>
          <Table headers={['Type', 'Amount', 'Date', 'Detail']}>
            {(transactions.data || []).slice(0, 8).map((item) => (
              <tr key={item.transactionId}>
                <td className="ui"><StatusBadge value={item.transactionType} /></td>
                <td className="ui">{money(item.amount)}</td>
                <td className="ui">{dateTime(item.transactionDate)}</td>
                <td className="ui"><button type="button" className="text-button" onClick={() => navigateTo(`/transactions/${item.transactionId}`)}>View</button></td>
              </tr>
            ))}
          </Table>
          {!transactions.data?.length && <Empty text="No transactions have been recorded for this card." />}
        </div>
        <div className="ui">
          <div className="ui"><h3 className="ui">Recent payments</h3></div>
          <Table headers={['Status', 'Amount', 'Date', 'Detail']}>
            {(payments.data || []).slice(0, 8).map((item) => (
              <tr key={item.paymentReference}>
                <td className="ui"><StatusBadge value={item.paymentStatus} /></td>
                <td className="ui">{money(item.amount)}</td>
                <td className="ui">{dateTime(item.paymentDate)}</td>
                <td className="ui"><button type="button" className="text-button" onClick={() => navigateTo(`/payments/${encodeURIComponent(item.paymentReference)}`)}>View</button></td>
              </tr>
            ))}
          </Table>
          {!payments.data?.length && <Empty text="No payments have been recorded for this card." />}
        </div>
      </div>
    </>
  )
}

export { CardsPage }
export { CardCreatePage }
export { CardDetailsPage }
