import { useState } from 'react'
import * as applicationsApi from '../services/cardApplicationService'
import ErrorMessage from '../components/ErrorMessage'
import LoadingSpinner from '../components/LoadingSpinner'
import StatusBadge from '../components/StatusBadge'
import { Empty, PageTitle, Table, dateTime, money, useRequest } from '../components/shared'

export function AdminCardApplicationsPage() {
  const [refreshKey, setRefreshKey] = useState(0)
  const applications = useRequest(applicationsApi.getAllApplications, [refreshKey])
  const [expandedId, setExpandedId] = useState('')
  const [busyId, setBusyId] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [reviewing, setReviewing] = useState(null)
  const [rejectionReason, setRejectionReason] = useState('')

  const openReview = (application, decision) => {
    if (application.status !== 'PENDING' || busyId) return
    setError('')
    setRejectionReason('')
    setReviewing({ application, decision })
  }

  const review = async () => {
    if (!reviewing || reviewing.application.status !== 'PENDING' || busyId) return
    const { application, decision } = reviewing
    const action = decision === 'approve' ? 'approve' : 'reject'
    setBusyId(application.applicationId)
    setError('')
    setNotice('')
    try {
      const { data } = decision === 'approve'
        ? await applicationsApi.approveApplication(application.applicationId)
        : await applicationsApi.rejectApplication(application.applicationId, rejectionReason.trim())
      const expectedStatus = decision === 'approve' ? 'APPROVED' : 'REJECTED'
      if (data.status !== expectedStatus) {
        throw new Error(`The server did not confirm ${expectedStatus.toLowerCase()} status.`)
      }
      setReviewing(null)
      setNotice(decision === 'approve'
        ? `Application approved. Card issued${data.issuedCardLastFour ? ` (•••• ${data.issuedCardLastFour})` : ''}.`
        : 'Application rejected. No card was issued.')
      setRefreshKey((key) => key + 1)
    } catch (requestError) {
      setError(requestError.userMessage || `Unable to ${action} this application.`)
    } finally {
      setBusyId('')
    }
  }

  if (applications.loading) return <LoadingSpinner />
  if (applications.error) return <ErrorMessage message={applications.error} onRetry={applications.retry} />
  const pendingCount = applications.data?.filter((application) => application.status === 'PENDING').length || 0

  return <>
    <PageTitle eyebrow="APPLICATION REVIEW" title="Card applications" description="Review customer requests before issuing a credit card." />
    {error && !reviewing && <ErrorMessage message={error} />}
    {notice && <div className="application-review-notice" role="status">{notice}</div>}
    {pendingCount > 0 && <div className="application-queue-notice" role="status">
      <span className="application-queue-indicator" aria-hidden="true" />
      <strong>{pendingCount} pending {pendingCount === 1 ? 'application' : 'applications'} require review</strong>
      <span>Use View, Approve, or Reject in the application row.</span>
    </div>}
    <section className="portal-panel">
      {applications.data?.length ? <Table headers={['Application ID', 'Customer', 'Card product', 'Network', 'Requested limit', 'Application date', 'Status', 'Actions']}>
        {applications.data.map((application) => <tr key={application.applicationId} className={application.status === 'PENDING' ? 'application-row-pending' : undefined}>
          <td>{application.applicationId}</td>
          <td><div className="primary-cell">{application.customerName}</div><div className="secondary-cell">{application.customerEmail}</div></td>
          <td>{application.cardTypeName}</td><td>{application.network}</td>
          <td>{money(application.requestedCreditLimit)}</td><td>{dateTime(application.applicationDate)}</td>
          <td>{application.status === 'PENDING'
            ? <span className="application-pending-status"><StatusBadge value={application.status} /><span>Action needed</span></span>
            : <StatusBadge value={application.status} />}</td>
          <td><div className="application-actions"><button className="btn-secondary" onClick={() => setExpandedId(expandedId === application.applicationId ? '' : application.applicationId)}>View</button>
            {application.status === 'PENDING' && <>
              <button className="btn-primary" disabled={Boolean(busyId)} onClick={() => openReview(application, 'approve')}>Approve</button>
              <button className="btn-secondary application-reject" disabled={Boolean(busyId)} onClick={() => openReview(application, 'reject')}>Reject</button>
            </>}
          </div></td>
        </tr>).flatMap((row, index) => {
          const application = applications.data[index]
          if (expandedId !== application.applicationId) return [row]
          return [row, <tr key={`${application.applicationId}-detail`}><td colSpan="8"><div className="application-detail">
            <strong>Product details</strong><p>{application.productDescription || 'No product description is available.'}</p>
            <span>Annual fee: {money(application.annualFee)}</span>
            {application.reviewedDate && <span> · Reviewed {dateTime(application.reviewedDate)} by {application.reviewedBy}</span>}
            {application.rejectionReason && <p>Reason: {application.rejectionReason}</p>}
            {application.issuedCardLastFour && <p>Issued card: •••• {application.issuedCardLastFour}</p>}
          </div></td></tr>]
        })}
      </Table> : <Empty text="No card applications have been submitted." />}
    </section>
    {reviewing && <div className="application-dialog-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !busyId) setReviewing(null)
    }}>
      <section className="application-review-dialog" role="dialog" aria-modal="true" aria-labelledby="application-review-title">
        <p className="eyebrow">PENDING APPLICATION</p>
        <h2 id="application-review-title">{reviewing.decision === 'approve' ? 'Confirm approval' : 'Reject application'}</h2>
        <p className="application-dialog-copy">
          {reviewing.decision === 'approve'
            ? 'Approving will issue a new active card using the existing card issuance workflow.'
            : 'Rejecting closes this application. No card will be issued.'}
        </p>
        <dl className="application-review-details">
          <div><dt>Customer</dt><dd>{reviewing.application.customerName}<br />{reviewing.application.customerEmail}</dd></div>
          <div><dt>Card product</dt><dd>{reviewing.application.cardTypeName}</dd></div>
          <div><dt>Network</dt><dd>{reviewing.application.network}</dd></div>
          <div><dt>Requested limit</dt><dd>{money(reviewing.application.requestedCreditLimit)}</dd></div>
        </dl>
        {reviewing.decision === 'reject' && <label className="application-rejection-field">
          <span>Rejection reason <span className="muted">(optional)</span></span>
          <textarea maxLength={500} value={rejectionReason} onChange={(event) => setRejectionReason(event.target.value)} placeholder="Add a reason for the customer (optional)" />
          <small>{rejectionReason.length}/500 characters</small>
        </label>}
        {error && <ErrorMessage message={error} />}
        <div className="application-dialog-actions">
          <button className="btn-secondary" disabled={Boolean(busyId)} onClick={() => setReviewing(null)}>Cancel</button>
          <button className={reviewing.decision === 'approve' ? 'btn-primary' : 'btn-secondary application-reject'} disabled={Boolean(busyId)} onClick={review}>
            {busyId === reviewing.application.applicationId
              ? 'Processing…'
              : reviewing.decision === 'approve' ? 'Confirm approval' : 'Confirm rejection'}
          </button>
        </div>
      </section>
    </div>}
  </>
}
