import api from '../lib/axios'
import type { AxiosError, AxiosResponse } from 'axios'

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
  clientId: string | null
  currency: string
  baseUrlMode?: 'sandbox' | 'live' | null
  provider?: 'paypal' | 'mock'
  availabilityMessage?: string | null
}

export interface ReservationStats {
  reservationsTodayCount: number
  pendingPaymentCount: number
  confirmedTodayCount: number
  cancelledTodayCount: number
  completedTodayCount: number
  confirmedTodayTotal: number
  last7DaysConfirmed: { date: string; total: number }[]
}

export interface ApiErrorPayload {
  success?: boolean
  message?: string
  code?: string
  details?: Record<string, unknown> | null
}

export const getReservationApiErrorCode = (error: unknown): string | undefined => {
  const axiosError = error as AxiosError<ApiErrorPayload>
  return axiosError.response?.data?.code
}

export const getReservationApiErrorMessage = (
  error: unknown,
  fallback: string,
  mapping: Record<string, string>
): string => {
  const code = getReservationApiErrorCode(error)
  if (code && mapping[code]) return mapping[code]
  return fallback
}

export const checkout = (body: CheckoutBody) =>
  api.post('/reservations/checkout', body).then((r) => r.data.data)

export const getPayPalPublicConfig = () =>
  api.get('/reservations/paypal/public-config').then((r) => r.data.data as PayPalPublicConfig)

export const createPayPalOrder = (body: PayPalCreateBody) =>
  api.post('/reservations/paypal/create', body).then((r) => r.data.data) as Promise<{
    paypalOrderId: string
    approvalUrl: string
    localOrderId?: string
  }>

export const capturePayPalOrder = (body: PayPalCaptureBody) =>
  api.post('/reservations/paypal/capture', body).then((r) => r.data.data)

export const getMyReservations = () => api.get('/reservations').then((r) => r.data.data)

export const getReservationById = (id: string) => api.get(`/reservations/${id}`).then((r) => r.data.data)

export const cancelReservation = (id: number) => api.put(`/reservations/${id}/cancel`).then((r) => r.data.data)

export interface UpdateReservationBody {
  checkIn: string
  checkOut: string
  guests?: number
}

export const updateReservation = (id: number, body: UpdateReservationBody) =>
  api.put(`/reservations/${id}`, body).then((r) => r.data.data)

export const getAllReservationsAdmin = () => api.get('/reservations/admin/all').then((r) => r.data.data)

export const getAdminStats = () =>
  api.get('/reservations/admin/stats').then((r) => r.data.data as ReservationStats)

export const downloadInvoice = (orderId: string) =>
  api.get(`/reports/invoice/${orderId}`, { responseType: 'blob' })

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

export const downloadInvoiceWithRetry = async (
  orderId: string,
  options?: { retries?: number; delayMs?: number }
): Promise<AxiosResponse<Blob>> => {
  const retries = Math.max(0, options?.retries ?? 5)
  const delayMs = Math.max(100, options?.delayMs ?? 1000)

  let attempt = 0
  // retry when invoice is still propagating (404) or report service temporarily unavailable via gateway (503)
  // this prevents breaking UX right after a successful PayPal capture.
  for (;;) {
    try {
      return await downloadInvoice(orderId)
    } catch (error) {
      const status = (error as AxiosError)?.response?.status
      const shouldRetry = (status === 404 || status === 503) && attempt < retries
      if (!shouldRetry) throw error
      attempt += 1
      await sleep(delayMs)
    }
  }
}
