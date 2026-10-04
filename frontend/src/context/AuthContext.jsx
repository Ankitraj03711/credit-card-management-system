import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest, logout as logoutRequest } from '../services/authService'

const AuthContext = createContext(null)
const AUTH_STORAGE_KEY = 'ccms_user'

function getTokenClaims(token) {
  try {
    const encoded = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(encoded.padEnd(Math.ceil(encoded.length / 4) * 4, '=')))
  } catch {
    return {}
  }
}

function loadUser() {
  try {
    const stored = JSON.parse(sessionStorage.getItem(AUTH_STORAGE_KEY))
    if (!stored?.email || !stored?.role || (stored.expiresAt && stored.expiresAt <= Date.now())) {
      sessionStorage.removeItem(AUTH_STORAGE_KEY)
      return null
    }
    return stored
  } catch {
    sessionStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadUser)
  const clearLocalSession = () => {
    sessionStorage.removeItem(AUTH_STORAGE_KEY)
    setUser(null)
  }
  const logout = async () => {
    try {
      await logoutRequest()
    } finally {
      clearLocalSession()
    }
  }

  useEffect(() => {
    const unauthorized = () => clearLocalSession()
    window.addEventListener('ccms:unauthorized', unauthorized)
    return () => window.removeEventListener('ccms:unauthorized', unauthorized)
  }, [])

  useEffect(() => {
    if (!user?.expiresAt) return undefined
    const timeout = window.setTimeout(clearLocalSession, Math.max(0, user.expiresAt - Date.now()))
    return () => window.clearTimeout(timeout)
  }, [user])

  const login = async (email, password) => {
    const { data } = await loginRequest({ email, password })
    const token = data.token || data.accessToken || null
    const claims = token ? getTokenClaims(token) : {}
    const authorities = claims.roles || claims.authorities
    const tokenRole = claims.role || (Array.isArray(authorities) ? authorities[0]?.authority || authorities[0] : authorities)
    const role = String(data.role || tokenRole || '').replace(/^ROLE_/, '').toUpperCase()
    if (!['ADMIN', 'CUSTOMER_SERVICE', 'CUSTOMER'].includes(role)) {
      throw new Error('The backend did not return a supported account role.')
    }
    const tokenExpiresAt = claims.exp ? Number(claims.exp) * 1000 : null
    const nextUser = {
      email: data.email || email,
      role,
      ...(token ? { token } : {}),
      ...(data.expiresAt ? { expiresAt: typeof data.expiresAt === 'number' ? data.expiresAt : new Date(data.expiresAt).getTime() } : {}),
      ...(data.expiresIn ? { expiresAt: Date.now() + Number(data.expiresIn) * 1000 } : {}),
      ...(!data.expiresAt && !data.expiresIn && tokenExpiresAt ? { expiresAt: tokenExpiresAt } : {}),
    }
    sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextUser))
    setUser(nextUser)
    return nextUser
  }

  const value = useMemo(() => ({ user, login, logout: () => logout() }), [user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export const useAuth = () => useContext(AuthContext)
