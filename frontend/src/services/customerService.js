import api from './api'
export const getCustomers = () => api.get('/api/customers')
export const getCurrentCustomer = () => api.get('/api/customers/me')
export const updateMyProfile = (payload) => api.put('/api/customers/me', payload)
export const getCustomer = (id) => api.get(`/api/customers/${id}`)
export const createCustomer = (payload) => api.post('/api/customers', payload)
export const updateCustomer = (id, payload) => api.put(`/api/customers/${id}`, payload)
export const updateCustomerStatus = (id, payload) => api.patch(`/api/customers/${id}/status`, payload)
export const deleteCustomer = (id) => api.delete(`/api/customers/${id}`)
