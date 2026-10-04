import api from './api'
export const getCards = () => api.get('/api/cards')
export const getCard = (number) => api.get(`/api/cards/${encodeURIComponent(number)}`)
export const getCardCvv = (number) => api.get(`/api/cards/${encodeURIComponent(number)}/cvv`)
export const getCustomerCards = (customerId) => api.get(`/api/cards/customer/${customerId}`)
export const issueCard = (payload) => api.post('/api/cards', payload)
export const changeCardStatus = (number, action) => api.patch(`/api/cards/${encodeURIComponent(number)}/${action}`)
