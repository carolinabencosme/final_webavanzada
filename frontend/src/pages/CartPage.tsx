import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Trash2, ShoppingBag } from 'lucide-react'
import toast from 'react-hot-toast'
import { getCart, removeCartItem, updateCartItem, clearCart } from '../api/cart'
import {
  checkout,
  createPayPalOrder,
  getOrderApiErrorCode,
  getOrderApiErrorMessage,
  getPayPalPublicConfig,
} from '../api/orders'
import { getUser } from '../store/authStore'

type PayPalReadableError = {
  message: string
  detail?: string
}

const mapPayPalError = (error: unknown, t: (key: string) => string): PayPalReadableError => {
  const backendErrorMap: Record<string, PayPalReadableError> = {
    PAYPAL_CONFIG_INVALID: { message: t('cart.paypalConfigInvalid') },
    PAYPAL_TOKEN_FAILED: {
      message: t('cart.paypalTokenErr'),
      detail: t('cart.paypalMissingToken'),
    },
    PAYPAL_PROVIDER_MISMATCH: { message: t('cart.paypalProviderMismatch') },
    PAYPAL_CREATE_ORDER_FAILED: { message: t('cart.paypalCreateOrderErr') },
    PAYPAL_CAPTURE_FAILED: { message: t('cart.paypalCaptureErrDetailed') },
  }

  const backendCode = getOrderApiErrorCode(error)
  if (backendCode && backendErrorMap[backendCode]) return backendErrorMap[backendCode]

  const msg = error instanceof Error ? error.message : String(error)

  if (/INSTRUMENT_DECLINED|payer/i.test(msg)) {
    return { message: t('cart.paypalCaptureErrDetailed') }
  }
  if (/create/i.test(msg)) return { message: t('cart.paypalCreateOrderErr') }
  if (/capture|approve|order/i.test(msg)) {
    return { message: t('cart.paypalCaptureErrDetailed') }
  }

  return { message: t('cart.unknown') }
}

export default function CartPage() {
  const { t } = useTranslation()
  const qc = useQueryClient()
  const user = getUser()
  const { data, isLoading } = useQuery({ queryKey: ['cart'], queryFn: getCart })
  const [paypalErr, setPaypalErr] = useState<PayPalReadableError | null>(null)

  const paypalConfigQuery = useQuery({
    queryKey: ['paypal-public-config'],
    queryFn: getPayPalPublicConfig,
  })

  const checkoutMut = useMutation({
    mutationFn: () =>
      checkout({
        userEmail: user!.email,
        cardNumber: '4242424242424242',
        cardExpiry: '12/30',
        cardCvc: '123',
      }),
    onSuccess: () => {
      toast.success(t('cart.orderOk'))
      qc.invalidateQueries({ queryKey: ['cart'] })
      qc.invalidateQueries({ queryKey: ['orders'] })
      qc.invalidateQueries({ queryKey: ['admin-stats'] })
    },
    onError: (error) =>
      toast.error(
        getOrderApiErrorMessage(error, t('cart.orderErr'), {
          PAYPAL_TOKEN_FAILED: t('cart.paypalTokenErr'),
          PAYPAL_CREATE_ORDER_FAILED: t('cart.paypalCreateOrderErr'),
          PAYPAL_CAPTURE_FAILED: t('cart.paypalCaptureErrDetailed'),
        })
      ),
  })

  const paypalCheckoutMut = useMutation({
    mutationFn: async () => {
      const origin = window.location.origin
      return createPayPalOrder({
        userEmail: user!.email,
        returnUrl: `${origin}/checkout/paypal-return`,
        cancelUrl: `${origin}/cart?paypal=cancelled`,
      })
    },
    onSuccess: (created) => {
      if (!created.approvalUrl) {
        throw new Error('Missing PayPal approval URL')
      }
      window.location.assign(created.approvalUrl)
    },
    onError: (error) => {
      const readable = mapPayPalError(error, t)
      setPaypalErr(readable)
      toast.error(readable.detail ? `${readable.message} ${readable.detail}` : readable.message)
    },
  })

  const items = data?.items ?? []

  if (isLoading) {
    return <div className="max-w-3xl mx-auto px-4 py-20 text-ink-muted text-sm">{t('cart.loading')}</div>
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-ink/8 bg-white">
        <div className="max-w-3xl mx-auto px-4 py-12">
          <p className="section-label mb-2">{t('cart.label')}</p>
          <h1 className="font-serif text-4xl text-ink font-semibold">{t('cart.title')}</h1>
        </div>
      </div>
      <div className="max-w-3xl mx-auto px-4 py-10">
        {items.length === 0 ? (
          <div className="card p-12 text-center border-ink/8">
            <p className="font-serif text-xl text-ink mb-4">{t('cart.emptyTitle')}</p>
            <p className="prose-editorial mb-8 max-w-md mx-auto">{t('cart.emptyHint')}</p>
            <Link to="/catalog" className="btn-primary inline-flex">
              {t('cart.goCatalog')}
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {items.map(
              (item: { id: number; bookTitle: string; bookAuthor: string; quantity: number; price: unknown }) => (
                <div key={item.id} className="card p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <h3 className="font-serif text-lg font-semibold text-ink">{item.bookTitle}</h3>
                    <p className="text-sm text-ink-muted">{item.bookAuthor}</p>
                    <p className="text-primary-700 font-semibold mt-2 font-serif text-lg">
                      ${Number(item.price).toFixed(2)} × {item.quantity}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <input
                      type="number"
                      min={1}
                      className="input w-20 py-2 text-center"
                      value={item.quantity}
                      onChange={(e) => {
                        const q = parseInt(e.target.value, 10)
                        if (q >= 1) {
                          updateCartItem(item.id, q).then(() => qc.invalidateQueries({ queryKey: ['cart'] }))
                        }
                      }}
                    />
                    <button
                      type="button"
                      className="p-2 text-ink-muted hover:text-red-700 transition-colors"
                      aria-label="Remove"
                      onClick={() =>
                        removeCartItem(item.id).then(() => {
                          qc.invalidateQueries({ queryKey: ['cart'] })
                          toast.success(t('cart.removed'))
                        })
                      }
                    >
                      <Trash2 className="w-5 h-5" strokeWidth={1.5} />
                    </button>
                  </div>
                </div>
              )
            )}

            <div className="flex flex-col gap-4 justify-between items-start sm:items-center pt-8 border-t border-ink/10">
              <button
                type="button"
                className="text-sm text-ink-muted hover:text-red-800 underline underline-offset-4"
                onClick={() =>
                  clearCart().then(() => {
                    qc.invalidateQueries({ queryKey: ['cart'] })
                    toast.success(t('cart.cleared'))
                  })
                }
              >
                {t('cart.clearAll')}
              </button>

              <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
                <button
                  type="button"
                  className="btn-secondary !normal-case !tracking-normal gap-2 w-full sm:w-auto justify-center"
                  disabled={!paypalConfigQuery.data?.enabled || paypalCheckoutMut.isPending}
                  onClick={() => {
                    setPaypalErr(null)
                    paypalCheckoutMut.mutate()
                  }}
                >
                  {paypalCheckoutMut.isPending ? t('cart.processing') : t('cart.paypalCheckout')}
                </button>
                <button
                  type="button"
                  className="btn-primary !normal-case !tracking-normal gap-2 w-full sm:w-auto justify-center"
                  disabled={checkoutMut.isPending || paypalCheckoutMut.isPending}
                  onClick={() => checkoutMut.mutate()}
                >
                  <ShoppingBag className="w-5 h-5" strokeWidth={1.75} />
                  {checkoutMut.isPending ? t('cart.processing') : t('cart.checkout')}
                </button>
              </div>
            </div>
            <p className="text-xs text-ink-muted max-w-xl">{t('cart.paypalHint')}</p>
            {paypalConfigQuery.data?.availabilityMessage ? (
              <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-900">
                {paypalConfigQuery.data.availabilityMessage}
              </div>
            ) : null}
            {paypalErr ? (
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-800">
                <p className="font-medium">{paypalErr.message}</p>
                {paypalErr.detail ? <p className="mt-1">{paypalErr.detail}</p> : null}
              </div>
            ) : null}
          </div>
        )}
      </div>
    </div>
  )
}
