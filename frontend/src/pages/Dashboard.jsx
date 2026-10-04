import * as customersApi from '../services/customerService'
import * as cardsApi from '../services/cardService'
import * as transactionsApi from '../services/transactionService'
import { useEffect, useState } from 'react'
import StatusBadge from '../components/StatusBadge'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { useRequest, PageTitle, Stat, Table, Empty, money, dateTime } from '../components/shared'

function Dashboard() {
  const customers = useRequest(customersApi.getCustomers)
  const cards = useRequest(cardsApi.getCards)
  const [recent, setRecent] = useState([])
  const [transactionsLoading, setTransactionsLoading] = useState(true)
  const [transactionsError, setTransactionsError] = useState('')

  useEffect(() => {
    if (cards.loading || cards.error) return
    let active = true
    setTransactionsLoading(true)
    setTransactionsError('')
    Promise.allSettled((cards.data || []).map((card) => transactionsApi.getCardTransactions(card.cardNumber)))
      .then((results) => {
        if (!active) return
        const failed = results.find((result) => result.status === 'rejected' && result.reason?.status !== 404)
        if (failed) {
          setTransactionsError(failed.reason.userMessage || 'Unable to load recent transactions.')
        }
        setRecent(results.flatMap((result) => result.status === 'fulfilled' ? result.value.data || [] : [])
          .sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate))
          .slice(0, 6))
        setTransactionsLoading(false)
      })
    return () => { active = false }
  }, [cards.data, cards.error, cards.loading])

  if (customers.loading || cards.loading) return <LoadingSpinner />
  if (customers.error || cards.error) return <ErrorMessage message={customers.error || cards.error} onRetry={() => { customers.retry(); cards.retry() }} />

  const customerList = customers.data || []
  const cardList = cards.data || []
  const activeCards = cardList.filter((card) => card.cardStatus === 'ACTIVE').length
  const blockedCards = cardList.filter((card) => card.cardStatus === 'BLOCKED').length
  const outstanding = cardList.reduce((sum, card) => sum + Number(card.outstandingBalance || 0), 0)
  const available = cardList.reduce((sum, card) => sum + Number(card.availableLimit || 0), 0)

  return (
    <>
      <PageTitle eyebrow="ADMIN OVERVIEW" title="Portfolio dashboard" description="A live view of your customers, cards and credit exposure." action={<span className="connection-status"><span className="online-dot" /> Backend connected</span>} />

      <div className="stat-grid">
        <Stat label="Total customers" value={customerList.length} note="From /api/customers" />
        <Stat label="Total credit cards" value={cardList.length} note="Across the portfolio" />
        <Stat label="Active cards" value={activeCards} note="Available for use" tone="mint" />
        <Stat label="Blocked cards" value={blockedCards} note="Require review" tone="amber" />
        <Stat label="Outstanding amount" value={money(outstanding)} note="Current card balances" tone="amber" />
        <Stat label="Available credit" value={money(available)} note="Across all active and blocked cards" tone="mint" />
      </div>
      <section className="admin-recent panel">
        <div className="section-heading"><div><h2>Recent transactions</h2><p>Latest activity across the card portfolio</p></div></div>
        {transactionsLoading ? <div className="loading"><span className="spinner" /> Loading activity…</div>
          : transactionsError ? <ErrorMessage message={transactionsError} />
            : recent.length ? <Table headers={['Transaction', 'Card', 'Type', 'Date', 'Amount']}>
              {recent.map((item) => <tr key={item.transactionId}>
                <td><div className="primary-cell">{item.merchant || item.description || 'Card transaction'}</div><div className="secondary-cell">{item.transactionId}</div></td>
                <td>•••• {item.cardNumber?.slice(-4)}</td>
                <td><StatusBadge value={item.transactionType} /></td>
                <td>{dateTime(item.transactionDate)}</td>
                <td className="amount-cell">{money(item.amount)}</td>
              </tr>)}
            </Table> : <Empty text="No transactions have been recorded yet." />}
      </section>
    </>
  )
}

export { Dashboard }
