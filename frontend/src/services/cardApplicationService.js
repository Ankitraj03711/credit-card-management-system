import api from './api'

export const getMyApplications = () => api.get('/api/card-applications/customer')
export const getAllApplications = () => api.get('/api/card-applications')
export const submitApplication = (payload) => api.post('/api/card-applications', payload)
export const approveApplication = (id) => api.put(`/api/card-applications/${encodeURIComponent(id)}/approve`, {})
export const rejectApplication = (id, reason) => api.put(`/api/card-applications/${encodeURIComponent(id)}/reject`, { reason })
