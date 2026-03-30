import api from '../lib/axios'

export interface CheckoutBody {
  userEmail: string
  cardNumber?: string
  cardExpiry?: string
  cardCvc?: string
}

export interface PayPalCreateBody {
  userEmail: string
  returnUrl: string
  cancelUrl: string
}

export interface PayPalCaptureBody {
  userEmail: string
  paypalOrderId: string
}

export interface PayPalPublicConfig {
  enabled: boolean
  clientId: string
  currency: string
}

export interface OrderStats {
  pendingCount: number
  paidTodayCount: number
  paidTodayTotal: number
  last7DaysPaid: { date: string; total: number }[]
}

export const checkout = (body: CheckoutBody) =>
  api.post('/orders/checkout', body).then((r) => r.data.data)

export const getPayPalPublicConfig = () =>
  api.get('/orders/paypal/public-config').then((r) => r.data.data as PayPalPublicConfig)

export const createPayPalOrder = (body: PayPalCreateBody) =>
  api.post('/orders/paypal/create', body).then((r) => r.data.data) as Promise<{
    paypalOrderId: string
    approvalUrl: string
    localOrderId?: string
  }>

export const capturePayPalOrder = (body: PayPalCaptureBody) =>
  api.post('/orders/paypal/capture', body).then((r) => r.data.data)

export const getMyOrders = () => api.get('/orders').then((r) => r.data.data)

export const getOrderById = (id: string) => api.get(`/orders/${id}`).then((r) => r.data.data)

export const getAllOrdersAdmin = () => api.get('/orders/admin/all').then((r) => r.data.data)

export const getAdminStats = () => api.get('/orders/admin/stats').then((r) => r.data.data as OrderStats)

export const downloadInvoice = (orderId: string) =>
  api.get(`/reports/invoice/${orderId}`, { responseType: 'blob' })
