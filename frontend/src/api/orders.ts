import api from '../lib/axios'
import { getUser } from '../store/authStore'

function uid(): string {
  const u = getUser()
  if (!u?.userId) throw new Error('Not authenticated')
  return u.userId
}

export interface CheckoutBody {
  userEmail: string
  cardNumber?: string
  cardExpiry?: string
  cardCvc?: string
}

export const checkout = (body: CheckoutBody) =>
  api.post(`/orders/${uid()}/checkout`, body).then((r) => r.data.data)

export const getMyOrders = () => api.get(`/orders/${uid()}`).then((r) => r.data.data)

export const getOrderById = (id: string) =>
  api.get(`/orders/${uid()}/${id}`).then((r) => r.data.data)

/** Si el backend añade un endpoint global de pedidos para admin, actualiza esta ruta. */
export const getAllOrders = () => api.get(`/orders/${uid()}`).then((r) => r.data.data)

export const downloadInvoice = (orderId: string) =>
  api.get(`/reports/invoice/${orderId}`, { responseType: 'blob' })
