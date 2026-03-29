import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { LayoutDashboard } from 'lucide-react'

export default function AdminDashboard() {
  const { t } = useTranslation()

  return (
    <div>
      <div className="flex items-center gap-3 mb-10">
        <div className="w-12 h-12 rounded-lg bg-ink flex items-center justify-center">
          <LayoutDashboard className="w-6 h-6 text-gold-light" strokeWidth={1.25} />
        </div>
        <div>
          <h1 className="font-serif text-3xl text-ink font-semibold">{t('admin.dashboardTitle')}</h1>
          <p className="text-ink-muted text-sm mt-0.5">{t('admin.dashboardSubtitle', { name: t('brand.name') })}</p>
        </div>
      </div>
      <div className="grid sm:grid-cols-2 gap-4 max-w-2xl">
        <Link to="/admin/users" className="card p-8 hover:border-primary-200/40 transition-colors group">
          <h2 className="font-serif text-xl text-ink font-semibold group-hover:text-primary-700">{t('admin.users')}</h2>
          <p className="prose-editorial text-sm mt-2">{t('admin.usersDesc')}</p>
        </Link>
        <Link to="/admin/orders" className="card p-8 hover:border-primary-200/40 transition-colors group">
          <h2 className="font-serif text-xl text-ink font-semibold group-hover:text-primary-700">{t('admin.orders')}</h2>
          <p className="prose-editorial text-sm mt-2">{t('admin.ordersDesc')}</p>
        </Link>
      </div>
    </div>
  )
}
