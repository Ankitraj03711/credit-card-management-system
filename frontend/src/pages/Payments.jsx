import * as paymentsApi from '../services/paymentService'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import StatusBadge from '../components/StatusBadge'
import { useRequest, useRouteParams, PageTitle, Empty, money, dateTime } from '../components/shared'

function PaymentDetailsPage() {
  const { paymentReference } = useRouteParams()
  const decodedReference = decodeURIComponent(paymentReference)
  const payment = useRequest(() => paymentsApi.getPayment(decodedReference), [decodedReference])
  if (payment.loading) return <LoadingSpinner />
  if (payment.error) return <ErrorMessage message={payment.error} onRetry={payment.retry} />

  return (
    <>
      <PageTitle eyebrow="Payments" title={`Payment ${decodedReference}`} description="Record details from /api/payments/{paymentReference}" />
      <div className="ui">
        <dl className="ui">
          <div><dt className="ui">Card</dt><dd className="ui">•••• {payment.data.cardNumber?.slice(-4)}</dd></div>
          <div><dt className="ui">Mode</dt><dd className="ui"><StatusBadge value={payment.data.paymentMode} /></dd></div>
          <div><dt className="ui">Amount</dt><dd className="ui">{money(payment.data.amount)}</dd></div>
          <div><dt className="ui">Status</dt><dd className="ui"><StatusBadge value={payment.data.paymentStatus} /></dd></div>
          <div><dt className="ui">Date</dt><dd className="ui">{dateTime(payment.data.paymentDate)}</dd></div>
          <div><dt className="ui">Description</dt><dd className="ui">{payment.data.description || '—'}</dd></div>
        </dl>
      </div>
    </>
  )
}

export { PaymentDetailsPage }
