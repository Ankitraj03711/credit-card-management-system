import api from './api'
export const getAddresses = (customerId) => api.get(`/api/addresses/customer/${customerId}`)
export const createAddress = (payload) => api.post('/api/addresses', payload)
export const updateAddress = (id, payload) => api.put(`/api/addresses/${id}`, payload)
export const deleteAddress = (id) => api.delete(`/api/addresses/${id}`)
