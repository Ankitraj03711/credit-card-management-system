import { useMemo, useState } from 'react'
import * as cardsApi from '../services/cardService'
import * as customersApi from '../services/customerService'
import * as transactionsApi from '../services/transactionService'
import * as paymentsApi from '../services/paymentService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { useRequest, PageTitle, Stat, Select, Empty, money, dateTime } from '../components/shared'

function ReportsPage() {
  const cards = useRequest(cardsApi.getCards)
  const customers = useRequest(customersApi.getCustomers)
  const [cardNumber, setCardNumber] = useState('')
  const [txSummary, setTxSummary] = useState(null)
  const [paySummary, setPaySummary] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const portfolio = useMemo(() => {
    const rows = cards.data || []
    const totalLimit = rows.reduce((sum, card) => sum + Number(card.creditLimit || 0), 0)
    const totalOutstanding = rows.reduce((sum, card) => sum + Number(card.outstandingBalance || 0), 0)
    const totalAvailable = rows.reduce((sum, card) => sum + Number(card.availableLimit || 0), 0)
    return {
      totalLimit,
      totalOutstanding,
      totalAvailable,
      activeCards: rows.filter((card) => card.cardStatus === 'ACTIVE').length,
      blockedCards: rows.filter((card) => card.cardStatus === 'BLOCKED').length,
      utilization: totalLimit ? (totalOutstanding / totalLimit) * 100 : 0,
    }
  }, [cards.data])

  const loadSummaries = async () => {
    if (!cardNumber) return
    setBusy(true)
    setError('')
    try {
      const [{ data: tx }, { data: pay }] = await Promise.all([
        transactionsApi.getTransactionSummary(cardNumber),
        paymentsApi.getPaymentSummary(cardNumber),
      ])
      setTxSummary(tx)
      setPaySummary(pay)
    } catch (err) {
      setError(err.userMessage || 'Unable to load summaries.')
    } finally {
      setBusy(false)
    }
  }

  if (cards.loading || customers.loading) return <LoadingSpinner />
  if (cards.error || customers.error) return <ErrorMessage message={cards.error || customers.error} onRetry={() => { cards.retry(); customers.retry() }} />

  return (
    <>
      <PageTitle eyebrow="Insights" title="Reports & summary" description="Portfolio and card-specific summaries from existing endpoints." />
      <div className="stat-grid">
        <Stat label="Customers" value={(customers.data || []).length} />
        <Stat label="Total cards" value={(cards.data || []).length} />
        <Stat label="Active cards" value={portfolio.activeCards} tone="mint" />
        <Stat label="Blocked cards" value={portfolio.blockedCards} tone="amber" />
        <Stat label="Total credit limit" value={money(portfolio.totalLimit)} tone="mint" />
        <Stat label="Available credit" value={money(portfolio.totalAvailable)} tone="mint" />
        <Stat label="Outstanding balance" value={money(portfolio.totalOutstanding)} tone="amber" />
        <Stat label="Utilization" value={`${Math.round(portfolio.utilization)}%`} tone="amber" note="Outstanding ÷ total limit" />
      </div>

      <div className="ui">
        <h2 className="ui">Card-specific summaries</h2>
        <div className="ui">
          <Select label="Card" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)}>
            <option value="">Select card</option>
            {(cards.data || []).map((card) => <option key={card.cardNumber} value={card.cardNumber}>•••• {card.cardNumber.slice(-4)} · #{card.customerId}</option>)}
          </Select>
          <button className="ui" disabled={!cardNumber || busy} onClick={loadSummaries}>Load summaries</button>
        </div>
      </div>

      {error && <div className="ui"><ErrorMessage message={error} /></div>}

      {(txSummary || paySummary) && (
        <div className="ui">
          <div className="ui">
            <h3 className="ui">Transaction summary</h3>
            {txSummary ? (
              <ul className="ui">
                <li>Total transactions: <strong>{txSummary.totalTransactions}</strong></li>
                <li>Total amount: <strong>{money(txSummary.totalTransactionAmount)}</strong></li>
                <li>Purchases: <strong>{txSummary.purchaseCount}</strong> ({money(txSummary.purchaseAmount)})</li>
                <li>Cash withdrawals: <strong>{txSummary.cashWithdrawalCount}</strong> ({money(txSummary.cashWithdrawalAmount)})</li>
                <li>Fees: <strong>{txSummary.feeCount}</strong> ({money(txSummary.feeAmount)})</li>
                <li>Refunds: <strong>{txSummary.refundCount}</strong> ({money(txSummary.refundAmount)})</li>
              </ul>
            ) : <Empty text="No transaction summary loaded." />}
          </div>

          <div className="ui">
            <h3 className="ui">Payment summary</h3>
            {paySummary ? (
              <ul className="ui">
                <li>Total payments: <strong>{paySummary.totalPayments}</strong></li>
                <li>Total paid: <strong>{money(paySummary.totalPaidAmount)}</strong></li>
                <li>Last payment amount: <strong>{money(paySummary.lastPaymentAmount)}</strong></li>
                <li>Last payment date: <strong>{dateTime(paySummary.lastPaymentDate)}</strong></li>
              </ul>
            ) : <Empty text="No payment summary loaded." />}
          </div>
        </div>
      )}
    </>
  )
}

export { ReportsPage }
