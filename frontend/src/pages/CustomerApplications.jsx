import { useMemo, useState } from 'react'
import * as cardTypesApi from '../services/cardTypeService'
import * as applicationsApi from '../services/cardApplicationService'
import ErrorMessage from '../components/ErrorMessage'
import LoadingSpinner from '../components/LoadingSpinner'
import StatusBadge from '../components/StatusBadge'
import { Empty, PageTitle, money, dateTime, useRequest, navigateTo } from '../components/shared'

const supportedNetworks = ['VISA', 'MASTERCARD', 'RUPAY']

function ProductCard({ product, onApply, disabled }) {
  return <article className="application-product">
    <div className={`product-network product-network-${product.network.toLowerCase()}`}>{product.network}</div>
    <h2>{product.name}</h2>
    <p className="application-fact-label">Product details</p>
    <p className="application-product-description">{product.description || 'No product details are available.'}</p>
    <dl className="application-product-facts">
      <div><dt>Credit limit</dt><dd>Requested by you</dd></div>
      <div><dt>Annual fee</dt><dd>{money(product.annualFee)}</dd></div>
      <div><dt>Eligibility</dt><dd>Active account required</dd></div>
    </dl>
    <p className="application-limit-note">Your requested credit limit will be reviewed by Cardwise before a card is issued.</p>
    <button className="btn-primary" disabled={disabled} onClick={() => onApply(product)}>Apply Now <span aria-hidden="true">→</span></button>
  </article>
}

export function CustomerApplyCardPage() {
  const products = useRequest(cardTypesApi.getCardTypes)
  const applications = useRequest(applicationsApi.getMyApplications)
  const [selected, setSelected] = useState(null)
  const [limit, setLimit] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(null)

  const availableProducts = useMemo(
    () => (products.data || []).filter((product) => supportedNetworks.includes(product.network)),
    [products.data],
  )

  const submit = async () => {
    if (!selected || !Number.isFinite(Number(limit)) || Number(limit) <= 0) {
      setError('Enter a requested credit limit greater than zero.')
      return
    }
    if (!window.confirm(`Submit an application for ${selected.name} on ${selected.network}? No card will be issued until an administrator approves it.`)) return
    setBusy(true)
    setError('')
    try {
      const { data } = await applicationsApi.submitApplication({
        cardTypeId: selected.cardTypeId,
        requestedCreditLimit: Number(limit),
      })
      setSubmitted(data)
      setSelected(null)
      setLimit('')
      applications.retry()
    } catch (requestError) {
      setError(requestError.userMessage || 'Unable to submit your card application.')
    } finally {
      setBusy(false)
    }
  }

  if (products.loading) return <LoadingSpinner />
  if (products.error) return <ErrorMessage message={products.error} onRetry={products.retry} />

  return <>
    <PageTitle eyebrow="CARDWISE PRODUCTS" title="Find your right credit card" description="Choose a card that fits your needs." />
    {error && <ErrorMessage message={error} />}
    {submitted && <section className="application-success" role="status">
      <div className="application-success-icon">✓</div>
      <div><h2>Application submitted successfully.</h2><p>Application ID: <strong>{submitted.applicationId}</strong> · {submitted.cardTypeName} · {submitted.network} · <StatusBadge value={submitted.status} /></p><p>Submitted {dateTime(submitted.applicationDate)}. Your card is not issued until the application is approved.</p></div>
      <button className="btn-secondary" onClick={() => navigateTo('/customer/applications')}>View my applications</button>
    </section>}
    <section className="application-products">
      {availableProducts.length ? availableProducts.map((product) => <ProductCard key={product.cardTypeId} product={product} onApply={setSelected} disabled={busy} />)
        : <div className="portal-panel"><Empty text="No card products are currently configured for applications. Please check back later." /></div>}
    </section>

    {selected && <section className="portal-panel application-review">
      <div className="section-heading"><div><h2>Review your application</h2><p>Confirm the product and requested limit before submitting.</p></div><button className="btn-secondary" onClick={() => setSelected(null)}>Cancel</button></div>
      <dl className="application-review-details">
        <div><dt>Card</dt><dd>{selected.name}</dd></div>
        <div><dt>Network</dt><dd>{selected.network}</dd></div>
        <div><dt>Annual fee</dt><dd>{money(selected.annualFee)}</dd></div>
        <div><dt>Product details</dt><dd>{selected.description || 'No product details are available.'}</dd></div>
      </dl>
      <label className="application-limit-field"><span>Requested credit limit (₹)</span><input className="field" type="number" min="1" step="1000" value={limit} onChange={(event) => setLimit(event.target.value)} /></label>
      <p className="inline-notice">Submitting an application does not issue a card. Your requested limit and eligibility will be reviewed.</p>
      <button className="btn-primary" disabled={busy} onClick={submit}>{busy ? 'Submitting…' : 'Submit Application'}</button>
    </section>}

    <section className="portal-panel">
      <div className="section-heading"><div><h2>My applications</h2><p>Track the status of your card applications.</p></div><button className="btn-secondary" onClick={() => navigateTo('/customer/applications')}>View all</button></div>
      {applications.loading ? <LoadingSpinner /> : applications.error ? <ErrorMessage message={applications.error} onRetry={applications.retry} />
        : applications.data?.length ? <div className="table-wrap"><table><thead><tr><th>Application</th><th>Card product</th><th>Network</th><th>Date</th><th>Status</th></tr></thead><tbody>{applications.data.slice(0, 5).map((application) => <tr key={application.applicationId}>
          <td>{application.applicationId}</td><td>{application.cardTypeName}</td><td>{application.network}</td><td>{dateTime(application.applicationDate)}</td><td><StatusBadge value={application.status} /></td>
        </tr>)}</tbody></table></div> : <Empty text="No applications yet." />}
    </section>
  </>
}

export function CustomerApplicationsPage() {
  const applications = useRequest(applicationsApi.getMyApplications)
  if (applications.loading) return <LoadingSpinner />
  if (applications.error) return <ErrorMessage message={applications.error} onRetry={applications.retry} />
  return <>
    <PageTitle eyebrow="YOUR REQUESTS" title="My applications" description="Track your credit card application status." />
    <section className="portal-panel">
      {applications.data?.length ? <div className="table-wrap"><table><thead><tr><th>Application ID</th><th>Card product</th><th>Network</th><th>Application date</th><th>Status</th><th>Card</th></tr></thead><tbody>{applications.data.map((application) => <tr key={application.applicationId}>
        <td>{application.applicationId}</td><td>{application.cardTypeName}</td><td>{application.network}</td><td>{dateTime(application.applicationDate)}</td><td><StatusBadge value={application.status} /></td><td>{application.issuedCardLastFour ? `•••• ${application.issuedCardLastFour}` : '—'}</td>
      </tr>)}</tbody></table></div>
        : <><Empty text="No applications yet." /><button className="btn-primary" onClick={() => navigateTo('/customer/apply-card')}>Explore Credit Cards</button></>}
    </section>
  </>
}
