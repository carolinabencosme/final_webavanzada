import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { Package } from 'lucide-react'
import { getMyOrders } from '../api/orders'

export default function OrdersPage() {
  const { t } = useTranslation()
  const { data: orders, isLoading } = useQuery({ queryKey: ['orders'], queryFn: getMyOrders })

  if (isLoading) {
    return <div className="max-w-4xl mx-auto px-4 py-20 text-ink-muted text-sm">{t('orders.loading')}</div>
  }

  const list = Array.isArray(orders) ? orders : []

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-ink/8 bg-white">
        <div className="max-w-4xl mx-auto px-4 py-12">
          <p className="section-label mb-2">{t('orders.label')}</p>
          <h1 className="font-serif text-4xl text-ink font-semibold">{t('orders.title')}</h1>
        </div>
      </div>
      <div className="max-w-4xl mx-auto px-4 py-10">
        {list.length === 0 ? (
          <p className="prose-editorial">{t('orders.empty')}</p>
        ) : (
          <ul className="space-y-3">
            {list.map((o: { id: number; status?: string; total?: unknown; createdAt?: string }) => (
              <li key={o.id} className="card p-6 flex items-start gap-4">
                <div className="w-11 h-11 rounded-lg bg-paper-deep flex items-center justify-center shrink-0">
                  <Package className="w-5 h-5 text-primary-700" strokeWidth={1.5} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-serif text-lg font-semibold text-ink">#{o.id}</p>
                  <p className="text-sm text-ink-muted">
                    {o.status ?? '—'}
                    {o.createdAt ? ` · ${new Date(o.createdAt).toLocaleString()}` : ''}
                  </p>
                  <p className="text-primary-700 font-semibold font-serif text-xl mt-2">${Number(o.total ?? 0).toFixed(2)}</p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
