const baseURL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

function readCsrfCookie() {
  const cookie = document.cookie.split('; ')
    .find((item) => item.startsWith('XSRF-TOKEN='))
  return cookie ? decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)) : null
}

async function getCsrfToken() {
  const cookieToken = readCsrfCookie()
  if (cookieToken) return cookieToken

  const response = await fetch(`${baseURL}/api/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) {
    throw new Error('Unable to initialize secure API requests.')
  }
  const data = await response.json()
  return readCsrfCookie() || data.token
}

function userMessage(status, message) {
  if (status === 400) return message || 'The backend rejected the request.'
  if (status === 401) return message || 'Authentication is required. Sign in again to continue.'
  if (status === 403) return 'You do not have permission to perform this action.'
  if (status === 404) return `The backend API route or requested record was not found (404). ${message || ''}`.trim()
  if (status === 409) return message || 'This operation conflicts with an existing record.'
  if ([502, 503, 504].includes(status)) return message || 'The Spring Boot backend is unavailable. Confirm that it started successfully and is listening on the configured port.'
  if (status >= 500) return `The Spring Boot backend returned an internal error${message ? `: ${message}` : '.'}`
  return message || 'Request failed.'
}

async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const mutating = !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)
  const csrf = mutating ? await getCsrfToken() : null
  let response

  try {
    response = await fetch(`${baseURL}${path}`, {
      ...options,
      credentials: 'include',
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
        ...(options.headers || {}),
      },
    })
  } catch (cause) {
    const error = new Error('The API request could not be completed.', { cause })
    const apiURL = new URL(`${baseURL}${path}`, window.location.origin)
    if (apiURL.origin === window.location.origin) {
      error.userMessage = 'Unable to reach the API at the frontend origin. Set VITE_API_BASE_URL to the backend API URL if the backend is hosted separately.'
      error.kind = 'api-unavailable'
    } else {
      error.userMessage = `The backend at ${apiURL.origin} is unavailable or unreachable: ${cause.message || 'network error'}.`
      error.kind = 'network'
    }
    throw error
  }

  const text = await response.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { data = text }
  if (!response.ok) {
    const error = new Error(data?.message || `Request failed with status ${response.status}.`)
    error.status = response.status
    error.response = { status: response.status, data }
    error.userMessage = userMessage(
      response.status,
      data?.message || data?.error
        || data?.errors?.map?.((item) => item.defaultMessage || item.message).join(', '),
    )
    if (response.status === 503) error.kind = 'backend-unavailable'
    else if (response.status === 401) error.kind = 'unauthorized'
    else if (response.status === 403) error.kind = 'forbidden'
    else if (response.status === 404) error.kind = 'not-found'
    else if (response.status >= 500) error.kind = 'backend-error'
    if (response.status === 401 && path !== '/api/auth/login') {
      window.dispatchEvent(new Event('ccms:unauthorized'))
    }
    throw error
  }
  return { data, status: response.status }
}

const body = (payload) => ({ body: JSON.stringify(payload) })

const api = {
  get: (path, options = {}) => {
    const query = options.params ? `?${new URLSearchParams(options.params).toString()}` : ''
    return request(`${path}${query}`)
  },
  post: (path, payload) => request(path, { method: 'POST', ...body(payload) }),
  put: (path, payload) => request(path, { method: 'PUT', ...body(payload) }),
  patch: (path, payload) => request(path, { method: 'PATCH', ...(payload === undefined ? {} : body(payload)) }),
  delete: (path) => request(path, { method: 'DELETE' }),
}

export default api
