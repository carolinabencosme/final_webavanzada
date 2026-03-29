import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { getAllOrdersAdmin } from '../../api/orders'

export default function AdminOrdersPage() {
  const { t } = useTranslation()
  const { data: orders, isLoading } = useQuery({ queryKey: ['orders', 'admin'], queryFn: getAllOrdersAdmin })
  const list = Array.isArray(orders) ? orders : []

  return (
    <div>
      <h1 className="font-serif text-3xl text-ink font-semibold mb-2">{t('admin.ordersTitle')}</h1>
      <p className="prose-editorial text-sm max-w-xl mb-8">{t('admin.ordersHint')}</p>
      {isLoading ? (
        <p className="text-ink-muted text-sm">…</p>
      ) : list.length === 0 ? (
        <p className="text-ink-muted">{t('admin.noRows')}</p>
      ) : (
        <ul className="space-y-2">
          {list.map(
            (o: {
              id: number
              userId?: string
              userEmail?: string
              total?: unknown
              status?: string
            }) => (
              <li key={o.id} className="card px-5 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div>
                  <span className="font-mono text-sm text-ink">#{o.id}</span>
                  {o.userEmail && (
                    <span className="block text-xs text-ink-muted mt-0.5">{o.userEmail}</span>
                  )}
                </div>
                <div className="flex items-center gap-4">
                  <span className="text-sm text-ink-muted">{o.status}</span>
                  <span className="font-serif font-semibold text-primary-700">
                    ${Number(o.total ?? 0).toFixed(2)}
                  </span>
                </div>
              </li>
            )
          )}
        </ul>
      )}
    </div>
  )
}
