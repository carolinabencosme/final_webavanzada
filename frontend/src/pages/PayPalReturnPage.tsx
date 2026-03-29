import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import toast from 'react-hot-toast'
import { capturePayPalOrder } from '../api/orders'
import { getUser } from '../store/authStore'

export default function PayPalReturnPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [err, setErr] = useState<string | null>(null)
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
    onError: () => {
      setErr(t('cart.paypalCaptureErr'))
    },
  })

  useEffect(() => {
    if (!token) {
      setErr(t('cart.paypalMissingToken'))
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
          <p className="text-ink font-medium">{err}</p>
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
