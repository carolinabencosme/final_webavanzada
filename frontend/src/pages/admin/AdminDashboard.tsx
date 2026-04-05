import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { LayoutDashboard } from 'lucide-react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { getAdminStats } from '../../api/reservations'

export default function AdminDashboard() {
  const { t } = useTranslation()
  const { data: stats, isLoading, isError } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: getAdminStats,
  })

  const chartData =
    stats?.last7DaysConfirmed?.map((row) => ({
      day: row.date.slice(5),
      total: Number(row.total ?? 0),
    })) ?? []

  return (
    <div>
      <div className="flex items-center gap-3 mb-10">
        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary-600 to-primary-800 flex items-center justify-center shadow-lg shadow-primary-600/25">
          <LayoutDashboard className="w-6 h-6 text-white" strokeWidth={1.25} />
        </div>
        <div>
          <h1 className="font-serif text-3xl text-ink font-semibold">{t('admin.dashboardTitle')}</h1>
          <p className="text-ink-muted text-sm mt-0.5">{t('admin.dashboardSubtitle', { name: t('brand.name') })}</p>
        </div>
      </div>

      {isLoading && <p className="text-ink-muted text-sm">{t('admin.statsLoading')}</p>}
      {isError && <p className="text-red-800 text-sm">{t('admin.statsErr')}</p>}

      {stats && !isLoading && (
        <>
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
            <div className="card p-6 border-ink/10">
              <p className="section-label mb-1">{t('admin.statsToday')}</p>
              <p className="font-serif text-3xl text-ink font-semibold">{stats.reservationsTodayCount}</p>
              <p className="text-xs text-ink-muted mt-2">{t('admin.statsTodayHint')}</p>
            </div>
            <div className="card p-6 border-ink/10">
              <p className="section-label mb-1">{t('admin.statsPending')}</p>
              <p className="font-serif text-3xl text-ink font-semibold">{stats.pendingPaymentCount}</p>
              <p className="text-xs text-ink-muted mt-2">{t('admin.statsPendingHint')}</p>
            </div>
            <div className="card p-6 border-ink/10">
              <p className="section-label mb-1">{t('admin.statsConfirmedToday')}</p>
              <p className="font-serif text-3xl text-ink font-semibold">{stats.confirmedTodayCount}</p>
              <p className="text-xs text-ink-muted mt-2">{t('admin.statsConfirmedTodayHint')}</p>
            </div>
            <div className="card p-6 border-ink/10">
              <p className="section-label mb-1">{t('admin.statsCancelledToday')}</p>
              <p className="font-serif text-3xl text-ink font-semibold">{stats.cancelledTodayCount}</p>
              <p className="text-xs text-ink-muted mt-2">{t('admin.statsCancelledTodayHint')}</p>
            </div>
          </div>

          <div className="grid sm:grid-cols-1 gap-4 mb-10">
            <div className="card p-6 border-ink/10">
              <p className="section-label mb-1">{t('admin.statsRevenueToday')}</p>
              <p className="font-serif text-3xl text-primary-700 font-semibold">
                ${Number(stats.confirmedTodayTotal ?? 0).toFixed(2)}
              </p>
              <p className="text-xs text-ink-muted mt-2">{t('admin.statsRevenueTodayHint')}</p>
            </div>
          </div>

          <div className="card p-6 mb-10 border-ink/10">
            <p className="section-label mb-4">{t('admin.chart7dLabel')}</p>
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                  <XAxis dataKey="day" tick={{ fontSize: 11 }} stroke="#6b7280" />
                  <YAxis tick={{ fontSize: 11 }} stroke="#6b7280" tickFormatter={(v) => `$${v}`} />
                  <Tooltip
                    formatter={(value) => [`$${Number(value ?? 0).toFixed(2)}`, t('admin.chartTooltip')]}
                    contentStyle={{ borderRadius: 4, border: '1px solid rgba(0,0,0,0.08)' }}
                  />
                  <Bar dataKey="total" fill="rgb(79, 70, 229)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </>
      )}

      <div className="grid sm:grid-cols-2 gap-4 max-w-2xl">
        <Link to="/admin/users" className="card p-8 hover:border-primary-200/40 transition-colors group">
          <h2 className="font-serif text-xl text-ink font-semibold group-hover:text-primary-700">{t('admin.users')}</h2>
          <p className="prose-editorial text-sm mt-2">{t('admin.usersDesc')}</p>
        </Link>
        <Link to="/admin/reservations" className="card p-8 hover:border-primary-200/40 transition-colors group">
          <h2 className="font-serif text-xl text-ink font-semibold group-hover:text-primary-700">{t('admin.reservations')}</h2>
          <p className="prose-editorial text-sm mt-2">{t('admin.reservationsDesc')}</p>
        </Link>
      </div>
    </div>
  )
}
