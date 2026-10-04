import api from './api'
export const login = (payload) => api.post('/api/auth/login', payload)
export const register = (payload) => api.post('/api/auth/register', payload)
export const logout = () => api.post('/api/auth/logout')
