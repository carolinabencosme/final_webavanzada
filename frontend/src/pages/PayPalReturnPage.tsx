import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import toast from 'react-hot-toast'
import { capturePayPalOrder, getOrderApiErrorCode } from '../api/orders'
import { getUser } from '../store/authStore'

type PayPalReturnReadableError = {
  message: string
  detail?: string
}

const mapPayPalReturnError = (error: unknown, t: (key: string) => string): PayPalReturnReadableError => {
  const backendCode = getOrderApiErrorCode(error)
  const byCode: Record<string, PayPalReturnReadableError> = {
    PAYPAL_CONFIG_INVALID: { message: t('cart.paypalConfigInvalid'), detail: t('cart.paypalHint') },
    PAYPAL_TOKEN_FAILED: { message: t('cart.paypalTokenErr'), detail: t('cart.paypalMissingToken') },
    PAYPAL_PROVIDER_MISMATCH: { message: t('cart.paypalProviderMismatch'), detail: t('cart.backToCart') },
    PAYPAL_CREATE_ORDER_FAILED: { message: t('cart.paypalCreateOrderErr'), detail: t('cart.paypalCreateErr') },
    PAYPAL_CAPTURE_FAILED: { message: t('cart.paypalCaptureErrDetailed'), detail: t('cart.paypalCaptureErr') },
  }

  if (backendCode && byCode[backendCode]) return byCode[backendCode]
  return { message: t('cart.unknown') }
}

export default function PayPalReturnPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [err, setErr] = useState<PayPalReturnReadableError | null>(null)
  const token = searchParams.get('token')
  const ran = useRef(false)

  const mut = useMutation({
    mutationFn: () => {
      const user = getUser()
      if (!user?.email || !token) throw new Error('missing')
      return capturePayPalOrder({ userEmail: user.email, paypalOrderId: token })
    },
    onSuccess: () => {
      toast.success(t('cart.orderOk'))
      qc.invalidateQueries({ queryKey: ['cart'] })
      qc.invalidateQueries({ queryKey: ['orders'] })
      qc.invalidateQueries({ queryKey: ['admin-stats'] })
      navigate('/my-orders', { replace: true })
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
      ) : (
        <p className="text-ink-muted text-sm">{t('cart.paypalProcessing')}</p>
      )}
    </div>
  )
}
