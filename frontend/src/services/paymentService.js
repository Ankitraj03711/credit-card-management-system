import api from './api'
export const createPayment = (payload) => api.post('/api/payments', payload)
export const getPayment = (reference) => api.get(`/api/payments/${encodeURIComponent(reference)}`)
export const getCardPayments = (number) => api.get(`/api/payments/card/${encodeURIComponent(number)}`)
export const getCustomerPayments = (id) => api.get(`/api/payments/customer/${id}`)
export const filterPayments = (number, params) => api.get(`/api/payments/card/${encodeURIComponent(number)}/filter`, { params })
export const getPaymentSummary = (number) => api.get(`/api/payments/card/${encodeURIComponent(number)}/summary`)
