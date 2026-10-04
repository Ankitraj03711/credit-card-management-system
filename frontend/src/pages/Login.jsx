import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import ErrorMessage from '../components/ErrorMessage'
import { Redirect, navigateTo } from '../components/shared'

const getHome = (role) => role === 'CUSTOMER' ? '/customer/dashboard' : '/admin/dashboard'

export default function Login() {
  const { user, login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  if (user) return <Redirect path={getHome(user.role)} />

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const account = await login(form.email.trim(), form.password)
      navigateTo(getHome(account.role))
    } catch (err) {
      setError(err.userMessage || err.message || 'Authentication failed.')
    } finally {
      setBusy(false)
    }
  }

  return <main className="login-page">
    <section className="login-brand-panel">
      <div className="login-brand"><span className="brand-mark">C</span> Cardwise</div>
      <div className="login-pitch">
        <p className="login-kicker">CREDIT CARD MANAGEMENT</p>
        <h1>Make every<br />card count.</h1>
        <p className="login-copy">A smarter, simpler way to manage your credit card portfolio and stay on top of what matters.</p>
        <div className="login-card-art" aria-hidden="true"><div className="art-chip" /><div className="art-wave">〰</div><div className="art-dots">•••• &nbsp;•••• &nbsp;•••• &nbsp;2486</div><div className="art-bottom"><span>Cardwise Platinum</span><span>VISA</span></div></div>
      </div>
      <p className="login-footnote">A clearer view of every customer and card.</p>
    </section>
    <section className="login-form-panel">
      <form className="login-form" onSubmit={submit}>
        <div className="mobile-login-brand"><span className="brand-mark">C</span> Cardwise</div>
        <p className="eyebrow">WELCOME BACK</p>
        <h2>Sign in to your account</h2>
        <p className="login-subtitle">Enter your credentials to continue.</p>
        {error && <ErrorMessage message={error} />}
        <label className="login-field"><span>Email address</span><input type="email" autoComplete="username" placeholder="you@example.com" required value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} /></label>
        <label className="login-field"><span>Password</span><input type="password" autoComplete="current-password" placeholder="Enter your password" required value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} /></label>
        <button disabled={busy} className="btn-primary login-submit">{busy ? 'Signing in…' : 'Sign in'} <span aria-hidden="true">→</span></button>
        <p className="auth-switch">Don't have an account? <button type="button" className="auth-switch-link" onClick={() => navigateTo('/register')}>Create an account</button></p>
        <p className="login-security"><span aria-hidden="true">▣</span> Protected access for authorized account holders</p>
      </form>
      <p className="login-copyright">© {new Date().getFullYear()} Cardwise Financial. All rights reserved.</p>
    </section>
  </main>
}
