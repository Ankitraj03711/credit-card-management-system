import api from './api'
export const getCardTypes = () => api.get('/api/card-types')
export const createCardType = (payload) => api.post('/api/card-types', payload)
export const updateCardType = (id, payload) => api.put(`/api/card-types/${id}`, payload)
export const deleteCardType = (id) => api.delete(`/api/card-types/${id}`)
