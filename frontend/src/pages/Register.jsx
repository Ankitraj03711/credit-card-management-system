import { useState } from 'react'
import { login, logout, register } from '../services/authService'
import { createCustomer } from '../services/customerService'
import { navigateTo } from '../components/shared'

const initialForm = {
  name: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
}

function validate(form) {
  if (!form.name.trim()) return 'Full name is required.'
  if (!form.email.trim()) return 'Email address is required.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return 'Enter a valid email address.'
  if (!form.phone.trim()) return 'Phone number is required.'
  const phoneDigits = form.phone.replace(/\D/g, '')
  if (phoneDigits.length < 7 || phoneDigits.length > 15 || !/^[+\d\s()-]+$/.test(form.phone.trim())) {
    return 'Enter a valid phone number with 7 to 15 digits.'
  }
  if (!form.password) return 'Password is required.'
  if (!form.confirmPassword) return 'Confirm password is required.'
  if (form.password !== form.confirmPassword) return 'Passwords do not match.'
  return ''
}

function registrationError(error) {
  if (error.status === 409) return 'An account with this email already exists.'
  if (error.status === 400) return error.userMessage || error.message || 'Please check the information you entered.'
  return error.userMessage || 'We could not reach the registration service. Please try again.'
}

export default function Register() {
  const [form, setForm] = useState(initialForm)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [accountCreated, setAccountCreated] = useState(false)

  const updateField = (event) => {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
    setError('')
  }

  const submit = async (event) => {
    event.preventDefault()
    const validationMessage = validate(form)
    if (validationMessage) {
      setError(validationMessage)
      return
    }

    setBusy(true)
    setError('')

    const email = form.email.trim()
    if (!accountCreated) {
      try {
        await register({ email, password: form.password, role: 'CUSTOMER' })
        setAccountCreated(true)
      } catch (registrationFailure) {
        setError(registrationError(registrationFailure))
        setBusy(false)
        return
      }
    }

    try {
      await login({ email, password: form.password })
      await createCustomer({
        name: form.name.trim(),
        email,
        phone: form.phone.replace(/\D/g, ''),
        annualIncome: 0,
        customerStatus: 'ACTIVE',
      })
    } catch (profileFailure) {
      setError(`Your sign-in account was created, but setting up your customer profile failed. You can retry safely. ${profileFailure.userMessage || 'Please try again later.'}`)
      setBusy(false)
      return
    }

    try {
      await logout()
    } catch (logoutFailure) {
      setError(`Your account and customer profile were created, but sign-out failed. Please sign in to continue. ${logoutFailure.userMessage || ''}`)
      setBusy(false)
      return
    }
    setSuccess('Account created successfully')
    setBusy(false)
    window.setTimeout(() => navigateTo('/login'), 1400)
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
      <form className="login-form register-form" onSubmit={submit} noValidate>
        <div className="mobile-login-brand"><span className="brand-mark">C</span> Cardwise</div>
        <p className="eyebrow">CUSTOMER REGISTRATION</p>
        <h2>Create your account</h2>
        <p className="login-subtitle">Create your Cardwise account to manage your credit cards.</p>
        {error && <div className="auth-alert" role="alert">{error}</div>}
        {success && <div className="auth-alert auth-alert-success" role="status">{success}</div>}
        <label className="login-field"><span>Full Name</span><input name="name" type="text" autoComplete="name" placeholder="Enter your full name" required value={form.name} onChange={updateField} /></label>
        <label className="login-field"><span>Email Address</span><input name="email" type="email" autoComplete="email" placeholder="you@example.com" required value={form.email} onChange={updateField} /></label>
        <label className="login-field"><span>Phone Number</span><input name="phone" type="tel" autoComplete="tel" placeholder="Enter your phone number" required value={form.phone} onChange={updateField} /></label>
        <label className="login-field"><span>Password</span><input name="password" type="password" autoComplete="new-password" placeholder="Create a password" required value={form.password} onChange={updateField} /></label>
        <label className="login-field"><span>Confirm Password</span><input name="confirmPassword" type="password" autoComplete="new-password" placeholder="Confirm your password" required value={form.confirmPassword} onChange={updateField} /></label>
        <button disabled={busy || Boolean(success)} className="btn-primary login-submit">{busy ? accountCreated ? 'Setting up profile…' : 'Creating account…' : accountCreated ? 'Retry Profile Setup' : 'Create Account'} <span aria-hidden="true">→</span></button>
        <p className="auth-switch">Already have an account? <button type="button" className="auth-switch-link" onClick={() => navigateTo('/login')}>Sign in</button></p>
        <p className="login-security"><span aria-hidden="true">▣</span> Secure customer account registration</p>
      </form>
      <p className="login-copyright">© {new Date().getFullYear()} Cardwise Financial. All rights reserved.</p>
    </section>
  </main>
}
