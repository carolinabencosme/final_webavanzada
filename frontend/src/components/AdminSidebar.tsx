import { Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { LayoutDashboard, Users, ShoppingBag, BookOpen } from 'lucide-react'

const paths = [
  { to: '/admin', icon: LayoutDashboard, labelKey: 'admin.linkOverview' as const },
  { to: '/admin/users', icon: Users, labelKey: 'admin.linkUsers' as const },
  { to: '/admin/orders', icon: ShoppingBag, labelKey: 'admin.linkOrders' as const },
]

export default function AdminSidebar() {
  const { t } = useTranslation()
  const { pathname } = useLocation()

  return (
    <aside className="w-56 shrink-0 bg-ink text-paper/90 border-r border-white/10 min-h-[calc(100vh-8rem)] p-6">
      <div className="flex flex-col gap-1 font-serif text-lg text-paper mb-10">
        <div className="flex items-center gap-2">
          <BookOpen className="w-5 h-5 text-gold-light" strokeWidth={1.25} />
          {t('admin.sidebarTitle')}
        </div>
        <span className="text-[10px] uppercase tracking-widest text-paper/45 font-sans">{t('admin.sidebarSubtitle')}</span>
      </div>
      <nav className="space-y-0.5">
        {paths.map(({ to, icon: Icon, labelKey }) => (
          <Link
            key={to}
            to={to}
            className={`flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors ${
              pathname === to ? 'bg-white/10 text-paper' : 'text-paper/65 hover:bg-white/5 hover:text-paper'
            }`}
          >
            <Icon className="w-4 h-4 opacity-80" strokeWidth={1.5} />
            {t(labelKey)}
          </Link>
        ))}
      </nav>
    </aside>
  )
}
