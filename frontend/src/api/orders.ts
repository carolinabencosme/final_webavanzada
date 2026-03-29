import api from '../lib/axios'

export const checkout = () => api.post('/checkout').then(r => r.data.data)
export const getMyOrders = () => api.get('/orders/me').then(r => r.data.data)
export const getOrderById = (id: string) => api.get(`/orders/${id}`).then(r => r.data.data)
export const getAllOrders = () => api.get('/orders/admin/all').then(r => r.data.data)
export const downloadInvoice = (orderId: string) =>
  api.get(`/reports/invoice/${orderId}`, { responseType: 'blob' })
