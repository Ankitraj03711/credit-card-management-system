import { useEffect, useState } from 'react'

export default function ToastHost() {
  const [toast, setToast] = useState(null)

  useEffect(() => {
    let timeout
    const show = (event) => {
      window.clearTimeout(timeout)
      setToast(event.detail)
      timeout = window.setTimeout(() => setToast(null), 3600)
    }
    window.addEventListener('ccms:toast', show)
    return () => {
      window.clearTimeout(timeout)
      window.removeEventListener('ccms:toast', show)
    }
  }, [])

  if (!toast) return null
  return <div className={`toast toast-${toast.type}`} role="status" aria-live="polite">
    <span className="toast-mark" aria-hidden="true">{toast.type === 'error' ? '!' : '✓'}</span>
    <span>{toast.message}</span>
    <button aria-label="Dismiss notification" onClick={() => setToast(null)}>×</button>
  </div>
}
