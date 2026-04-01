import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import toast from 'react-hot-toast'
import { capturePayPalOrder, downloadInvoice, getOrderApiErrorCode } from '../api/orders'
import { getUser } from '../store/authStore'

type PayPalReturnReadableError = {
  message: string
  detail?: string
}

const mapPayPalReturnError = (error: unknown, t: (key: string) => string): PayPalReturnReadableError => {
  const backendCode = getOrderApiErrorCode(error)
  const byCode: Record<string, PayPalReturnReadableError> = {
    PAYPAL_CONFIG_INVALID: { message: t('cart.paypalConfigInvalid') },
    PAYPAL_TOKEN_FAILED: { message: t('cart.paypalTokenErr'), detail: t('cart.paypalMissingToken') },
    PAYPAL_PROVIDER_MISMATCH: { message: t('cart.paypalProviderMismatch') },
    PAYPAL_CREATE_ORDER_FAILED: { message: t('cart.paypalCreateOrderErr') },
    PAYPAL_CAPTURE_FAILED: { message: t('cart.paypalCaptureErrDetailed') },
  }

  if (backendCode && byCode[backendCode]) return byCode[backendCode]
  return { message: t('cart.unknown') }
}

export default function PayPalReturnPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const qc = useQueryClient()
  const [err, setErr] = useState<PayPalReturnReadableError | null>(null)
  const [completedOrder, setCompletedOrder] = useState<any>(null)
  const [downloadingInvoice, setDownloadingInvoice] = useState(false)
  const token = searchParams.get('token')
  const ran = useRef(false)

  const handleInvoiceDownload = async () => {
    if (!completedOrder?.id || downloadingInvoice) return
    try {
      setDownloadingInvoice(true)
      const response = await downloadInvoice(String(completedOrder.id))
      const contentType = response.headers['content-type'] ?? 'application/pdf'
      const blob = new Blob([response.data], { type: contentType })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      const disposition = response.headers['content-disposition'] as string | undefined
      const fileNameMatch = disposition?.match(/filename="?([^"]+)"?/)
      link.href = url
      link.download = fileNameMatch?.[1] ?? `invoice-${completedOrder.id}.pdf`
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch {
      toast.error(t('cart.unknown'))
    } finally {
      setDownloadingInvoice(false)
    }
  }

  const mut = useMutation({
    mutationFn: () => {
      const user = getUser()
      if (!user?.email || !token) throw new Error('missing')
      return capturePayPalOrder({ userEmail: user.email, paypalOrderId: token })
    },
    onSuccess: (order) => {
      setCompletedOrder(order)
      toast.success(t('cart.orderOk'))
      qc.invalidateQueries({ queryKey: ['cart'] })
      qc.invalidateQueries({ queryKey: ['orders'] })
      qc.invalidateQueries({ queryKey: ['admin-stats'] })
    },
    onError: (error) => {
      const readable = mapPayPalReturnError(error, t)
      setErr(readable)
      toast.error(readable.detail ? `${readable.message} ${readable.detail}` : readable.message)
    },
  })

  useEffect(() => {
    if (!token) {
      setErr({ message: t('cart.paypalTokenErr'), detail: t('cart.paypalMissingToken') })
      return
    }
    if (ran.current) return
    ran.current = true
    mut.mutate()
    // eslint-disable-next-line react-hooks/exhaustive-deps -- run once per token
  }, [token])

  return (
    <div className="max-w-lg mx-auto px-4 py-24 text-center">
      {err ? (
        <>
          <p className="text-ink font-medium">{err.message}</p>
          {err.detail ? <p className="text-xs text-ink-muted mt-2">{err.detail}</p> : null}
          <Link to="/cart" className="btn-primary inline-block mt-6">
            {t('cart.backToCart')}
          </Link>
        </>
      ) : completedOrder ? (
        <div className="text-left card p-6">
          <h1 className="font-serif text-2xl text-ink mb-4">{t('cart.summaryTitle')}</h1>
          <p className="text-sm text-ink-muted mb-1">
            {t('cart.summaryOrder')} <strong>#{completedOrder.id}</strong>
          </p>
          <p className="text-sm text-ink-muted mb-1">
            {t('cart.summaryStatus')} <strong>{completedOrder.status}</strong>
          </p>
          <p className="text-sm text-ink-muted mb-4">
            {t('cart.summaryDate')} {completedOrder.createdAt ? new Date(completedOrder.createdAt).toLocaleString() : '—'}
          </p>

          <ul className="space-y-2 mb-4">
            {(completedOrder.items ?? []).map((item: any, idx: number) => (
              <li key={`${item.bookId}-${idx}`} className="text-sm text-ink">
                {item.bookTitle} · {item.quantity} × ${Number(item.price ?? 0).toFixed(2)}
              </li>
            ))}
          </ul>

          <p className="font-serif text-xl text-primary-700 mb-6">${Number(completedOrder.total ?? 0).toFixed(2)}</p>
          <div className="flex gap-3">
            <Link to="/my-orders" className="btn-primary inline-block">
              {t('cart.goToOrders')}
            </Link>
            <button type="button" className="btn-secondary inline-block" onClick={handleInvoiceDownload} disabled={downloadingInvoice}>
              {t('cart.downloadInvoice')}
            </button>
          </div>
        </div>
      ) : (
        <p className="text-ink-muted text-sm">{t('cart.paypalProcessing')}</p>
      )}
    </div>
  )
}
