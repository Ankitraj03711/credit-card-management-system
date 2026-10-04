import { useState } from 'react'
import * as cardsApi from '../services/cardService'
import * as transactionsApi from '../services/transactionService'
import * as paymentsApi from '../services/paymentService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import StatusBadge from '../components/StatusBadge'
import { useRequest, useRouteParams, navigateTo, notify, PageTitle, Table, Empty, Field, Select, money, dateTime } from '../components/shared'
import { validatePositiveAmount } from '../utils/validation'

function RecordEntryPage({ payments = false }) {
  const cards = useRequest(cardsApi.getCards)
  const navigate = navigateTo
  const [form, setForm] = useState({ cardNumber: '', amount: '', transactionType: 'PURCHASE', paymentMode: 'UPI', description: '', merchant: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const selectedCard = (cards.data || []).find((card) => card.cardNumber === form.cardNumber)

  const submit = async (event) => {
    event.preventDefault()
    const amountError = validatePositiveAmount(form.amount)
    if (amountError) {
      setError(amountError)
      return
    }
    if (payments && Number(form.amount) > Number(selectedCard?.outstandingBalance || 0)) {
      setError("Payment amount cannot exceed this card's outstanding balance.")
      return
    }
    if (!payments && selectedCard?.cardStatus !== 'ACTIVE') {
      setError('Only active cards can be used for a transaction.')
      return
    }
    if (!payments && ['PURCHASE', 'CASH_WITHDRAWAL', 'FEE'].includes(form.transactionType) && Number(form.amount) > Number(selectedCard?.availableLimit || 0)) {
      setError("Transaction amount exceeds the card's available credit.")
      return
    }
    if (!payments && form.transactionType === 'REFUND' && Number(form.amount) > Number(selectedCard?.outstandingBalance || 0)) {
      setError("Refund amount cannot exceed the card's outstanding balance.")
      return
    }
    setBusy(true)
    setError('')
    try {
      if (payments) {
        await paymentsApi.createPayment({
          cardNumber: form.cardNumber,
          amount: Number(form.amount),
          paymentMode: form.paymentMode,
          description: form.description || null,
        })
      } else {
        await transactionsApi.createTransaction({
          cardNumber: form.cardNumber,
          transactionType: form.transactionType,
          amount: Number(form.amount),
          description: form.description || null,
          merchant: form.merchant || null,
        })
      }
      notify(payments ? 'Payment recorded successfully.' : 'Transaction recorded successfully.')
      navigateTo(payments ? '/payments' : '/transactions')
    } catch (err) {
      setError(err.userMessage || `Unable to create ${payments ? 'payment' : 'transaction'}.`)
    } finally {
      setBusy(false)
    }
  }

  if (cards.loading) return <LoadingSpinner />
  if (cards.error) return <ErrorMessage message={cards.error} onRetry={cards.retry} />

  return (
    <>
      <PageTitle eyebrow={payments ? 'Payments' : 'Transactions'} title={payments ? 'Record payment' : 'Record transaction'} description="Submit using backend DTO fields." />
      <form onSubmit={submit} className="ui">
        {error && <div className="ui"><ErrorMessage message={error} /></div>}
        <div className="ui">
          <Select label="Card" required value={form.cardNumber} onChange={(e) => setForm((prev) => ({ ...prev, cardNumber: e.target.value }))}>
            <option value="">Select card</option>
            {(cards.data || []).filter((card) => payments || card.cardStatus === 'ACTIVE').map((card) => <option key={card.cardNumber} value={card.cardNumber}>•••• {card.cardNumber.slice(-4)} · #{card.customerId}</option>)}
          </Select>
          {selectedCard && <p className="muted">Outstanding {money(selectedCard.outstandingBalance)} · Available {money(selectedCard.availableLimit)}</p>}
          <Field label="Amount" type="number" min="0.01" step="0.01" required value={form.amount} onChange={(e) => setForm((prev) => ({ ...prev, amount: e.target.value }))} />
          {payments ? (
            <Select label="Payment mode" value={form.paymentMode} onChange={(e) => setForm((prev) => ({ ...prev, paymentMode: e.target.value }))}>
              <option value="UPI">UPI</option>
              <option value="BANK_TRANSFER">BANK_TRANSFER</option>
              <option value="DEBIT_CARD">DEBIT_CARD</option>
              <option value="NET_BANKING">NET_BANKING</option>
            </Select>
          ) : (
            <>
              <Select label="Transaction type" value={form.transactionType} onChange={(e) => setForm((prev) => ({ ...prev, transactionType: e.target.value }))}>
                <option value="PURCHASE">PURCHASE</option>
                <option value="CASH_WITHDRAWAL">CASH_WITHDRAWAL</option>
                <option value="FEE">FEE</option>
                <option value="REFUND">REFUND</option>
              </Select>
              <Field label="Merchant" maxLength={100} value={form.merchant} onChange={(e) => setForm((prev) => ({ ...prev, merchant: e.target.value }))} />
            </>
          )}
          <label className="ui">
            <span className="ui">Description</span>
            <textarea className="ui" maxLength={255} value={form.description} onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))} />
          </label>
        </div>
        <div className="ui">
          <button disabled={busy} className="ui">{busy ? 'Saving…' : payments ? 'Record payment' : 'Record transaction'}</button>
          <button type="button" className="ui" onClick={() => navigateTo(-1)}>Cancel</button>
        </div>
      </form>
    </>
  )
}

function HistoryPage({ payments = false }) {
  const cards = useRequest(cardsApi.getCards)
  const [cardNumber, setCardNumber] = useState('')
  const [rows, setRows] = useState([])
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(1)
  const [hasLoaded, setHasLoaded] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const pageSize = 10

  const load = async () => {
    if (!cardNumber) return
    setBusy(true)
    setError('')
    setRows([])
    try {
      const { data } = payments ? await paymentsApi.getCardPayments(cardNumber) : await transactionsApi.getCardTransactions(cardNumber)
      setRows(data || [])
      setPage(1)
      setHasLoaded(true)
    } catch (err) {
      setError(err.userMessage || 'Unable to load history.')
    } finally {
      setBusy(false)
    }
  }

  const filtered = rows.filter((item) => {
    const kind = payments ? item.paymentMode : item.transactionType
    const reference = payments ? item.paymentReference : item.transactionId
    return (!filter || kind === filter)
      && `${kind || ''} ${reference || ''} ${item.cardNumber || ''} ${item.merchant || ''} ${item.description || ''}`.toLowerCase().includes(search.toLowerCase())
  })
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const visible = filtered.slice((page - 1) * pageSize, page * pageSize)
  const kinds = [...new Set(rows.map((item) => payments ? item.paymentMode : item.transactionType).filter(Boolean))]

  return (
    <>
      <PageTitle eyebrow={payments ? 'PAYMENT OPERATIONS' : 'TRANSACTION OPERATIONS'} title={payments ? 'Payment history' : 'Transactions'} description={payments ? 'Review and record customer card payments.' : 'Review and record card activity across the portfolio.'} action={<button className="btn-primary" onClick={() => navigateTo(payments ? '/payments/new' : '/transactions/new')}><span aria-hidden="true">＋</span> {payments ? 'Record payment' : 'Record transaction'}</button>} />
      <div className="ui">
        <div className="table-toolbar">
          <Select label="Card" value={cardNumber} onChange={(e) => { setCardNumber(e.target.value); setRows([]); setHasLoaded(false); setError('') }}>
            <option value="">Select card</option>
            {(cards.data || []).map((card) => <option key={card.cardNumber} value={card.cardNumber}>•••• {card.cardNumber.slice(-4)} · #{card.customerId}</option>)}
          </Select>
          <button className="ui" disabled={!cardNumber || busy} onClick={load}>Load history</button>
        </div>
        {rows.length > 0 && <div className="table-toolbar">
          <label className="search-field"><span aria-hidden="true">⌕</span><input aria-label="Search history" placeholder="Search reference, card or merchant" value={search} onChange={(event) => { setSearch(event.target.value); setPage(1) }} /></label>
          <label className="filter-field"><span>{payments ? 'Payment mode' : 'Transaction type'}</span><select value={filter} onChange={(event) => { setFilter(event.target.value); setPage(1) }}><option value="">All types</option>{kinds.map((kind) => <option key={kind} value={kind}>{kind.replaceAll('_', ' ')}</option>)}</select></label>
        </div>}
      </div>
      {error && <div className="ui"><ErrorMessage message={error} /></div>}
      {busy ? <LoadingSpinner /> : (
        <div className="ui">
          {visible.length > 0 && <Table headers={payments ? ['Reference', 'Card', 'Amount', 'Mode', 'Date', 'Status', ''] : ['Type', 'Card', 'Amount', 'Merchant', 'Date', '']}>
            {visible.map((item) => (
              <tr key={payments ? item.paymentReference : item.transactionId}>
                <td className="ui">{payments ? item.paymentReference : <StatusBadge value={item.transactionType} />}</td>
                <td className="ui">•••• {item.cardNumber?.slice(-4)}</td>
                <td className="ui">{money(item.amount)}</td>
                <td className="ui">{payments ? <StatusBadge value={item.paymentMode} /> : item.merchant || '—'}</td>
                <td className="ui">{payments ? dateTime(item.paymentDate) : dateTime(item.transactionDate)}</td>
                {payments && <td className="ui"><StatusBadge value={item.paymentStatus} /></td>}
                <td className="ui">
                  <button type="button" className="text-button" onClick={() => navigateTo(payments ? `/payments/${encodeURIComponent(item.paymentReference)}` : `/transactions/${item.transactionId}`)}>Details</button>
                </td>
              </tr>
            ))}
          </Table>}
          {!visible.length && <Empty text={rows.length
            ? 'No records match your search.'
            : hasLoaded
              ? `No ${payments ? 'payments' : 'transactions'} have been recorded for this card.`
              : 'Select a card and load history.'} />}
          {filtered.length > pageSize && <div className="pagination"><span>Showing {(page - 1) * pageSize + 1}–{Math.min(page * pageSize, filtered.length)} of {filtered.length}</span><div><button className="btn-secondary" disabled={page === 1} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page} of {totalPages}</span><button className="btn-secondary" disabled={page === totalPages} onClick={() => setPage(page + 1)}>Next</button></div></div>}
        </div>
      )}
    </>
  )
}

function TransactionDetailsPage() {
  const { transactionId } = useRouteParams()
  const transaction = useRequest(() => transactionsApi.getTransaction(transactionId), [transactionId])
  if (transaction.loading) return <LoadingSpinner />
  if (transaction.error) return <ErrorMessage message={transaction.error} onRetry={transaction.retry} />

  return (
    <>
      <PageTitle eyebrow="Transactions" title={`Transaction ${transactionId}`} description="Record details from /api/transactions/{transactionId}" />
      <div className="ui">
        <dl className="ui">
          <div><dt className="ui">Card</dt><dd className="ui">•••• {transaction.data.cardNumber?.slice(-4)}</dd></div>
          <div><dt className="ui">Type</dt><dd className="ui"><StatusBadge value={transaction.data.transactionType} /></dd></div>
          <div><dt className="ui">Amount</dt><dd className="ui">{money(transaction.data.amount)}</dd></div>
          <div><dt className="ui">Date</dt><dd className="ui">{dateTime(transaction.data.transactionDate)}</dd></div>
          <div><dt className="ui">Merchant</dt><dd className="ui">{transaction.data.merchant || '—'}</dd></div>
          <div><dt className="ui">Description</dt><dd className="ui">{transaction.data.description || '—'}</dd></div>
        </dl>
      </div>
    </>
  )
}

export { RecordEntryPage }
export { HistoryPage }
export { TransactionDetailsPage }
