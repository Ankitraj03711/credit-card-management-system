import { useEffect, useState } from 'react'

export const navigateTo = (path) => {
  if (typeof path === 'number') {
    window.history.go(path)
    return
  }
  window.history.pushState({}, '', path)
  window.dispatchEvent(new PopStateEvent('popstate'))
}

export const notify = (message, type = 'success') => {
  window.dispatchEvent(new CustomEvent('ccms:toast', { detail: { message, type } }))
}

export function usePath() {
  const [path, setPath] = useState(window.location.pathname)
  useEffect(() => {
    const update = () => setPath(window.location.pathname)
    window.addEventListener('popstate', update)
    return () => window.removeEventListener('popstate', update)
  }, [])
  return path
}

export function useRouteParams() {
  const path = usePath()
  const parts = path.split('/').filter(Boolean)
  if (parts[0] === 'admin' || parts[0] === 'customer') parts.shift()
  const decoded = parts.map((part) => decodeURIComponent(part))
  return { id: decoded[1] || '', cardNumber: decoded[1] || '', transactionId: decoded[1] || '', paymentReference: decoded[1] || '' }
}

export function Redirect({ path }) {
  useEffect(() => navigateTo(path), [path])
  return null
}

export const money = (value) => value == null ? '—' : new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value)
export const date = (value) => value ? new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'
export const dateTime = (value) => value ? new Date(value).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—'

export function useRequest(request, deps = []) {
  const [state, setState] = useState({ loading: true, data: null, error: '' })
  const run = () => {
    setState({ loading: true, data: null, error: '' })
    request().then(({ data }) => setState({ loading: false, data, error: '' })).catch((error) => setState({ loading: false, data: null, error: error.userMessage || 'Request failed.' }))
  }
  useEffect(run, deps)
  return { ...state, retry: run }
}

export function PageTitle({ eyebrow, title, description, action }) {
  return <div className="page-title"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</div>
}

export function Stat({ label, value, note, tone }) {
  return <div className={`panel stat-card ${tone ? `stat-${tone}` : ''}`}><span className="stat-mark" aria-hidden="true">↗</span><p className="muted">{label}</p><p className="stat-value">{value}</p>{note && <p className="muted">{note}</p>}</div>
}

export function Table({ headers, children }) {
  return <div className="table-wrap"><table><thead><tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr></thead><tbody>{children}</tbody></table></div>
}

export const Empty = ({ text }) => <div className="empty muted">{text}</div>

export function Field({ label, ...props }) {
  return <label><span className="label">{label}</span><input className="field" {...props} /></label>
}

export function Select({ label, children, ...props }) {
  return <label><span className="label">{label}</span><select className="field" {...props}>{children}</select></label>
}
