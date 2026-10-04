import api from './api'
export const createTransaction = (payload) => api.post('/api/transactions', payload)
export const getCardTransactions = (number) => api.get(`/api/transactions/card/${encodeURIComponent(number)}`)
export const getTransaction = (id) => api.get(`/api/transactions/${id}`)
export const getCustomerTransactions = (id) => api.get(`/api/transactions/customer/${id}`)
export const filterTransactions = (number, params) => api.get(`/api/transactions/card/${encodeURIComponent(number)}/filter`, { params })
export const getTransactionSummary = (number) => api.get(`/api/transactions/card/${encodeURIComponent(number)}/summary`)
