import { Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { LayoutDashboard, Users, ShoppingBag, Sparkles } from 'lucide-react'

const paths = [
  { to: '/admin', icon: LayoutDashboard, labelKey: 'admin.linkOverview' as const },
  { to: '/admin/users', icon: Users, labelKey: 'admin.linkUsers' as const },
  { to: '/admin/reservations', icon: ShoppingBag, labelKey: 'admin.linkOrders' as const },
]

export default function AdminSidebar() {
  const { t } = useTranslation()
  const { pathname } = useLocation()

  return (
    <aside className="w-56 shrink-0 bg-gradient-to-b from-stone-900 to-primary-950 text-stone-200 border-r border-white/10 min-h-[calc(100vh-8rem)] p-6">
      <div className="flex flex-col gap-1 font-serif text-lg text-white mb-10">
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-500/30 text-primary-200">
            <Sparkles className="w-4 h-4" strokeWidth={2} />
          </span>
          {t('admin.sidebarTitle')}
        </div>
        <span className="text-[10px] uppercase tracking-widest text-primary-200/60 font-sans font-semibold">{t('admin.sidebarSubtitle')}</span>
      </div>
      <nav className="space-y-1">
        {paths.map(({ to, icon: Icon, labelKey }) => {
          const active =
            to === '/admin' ? pathname === '/admin' : pathname === to || pathname.startsWith(`${to}/`)
          return (
            <Link
              key={to}
              to={to}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                active ? 'bg-white/15 text-white shadow-md' : 'text-stone-400 hover:bg-white/5 hover:text-white'
              }`}
            >
              <Icon className="w-4 h-4 opacity-80" strokeWidth={1.5} />
              {t(labelKey)}
            </Link>
          )
        })}
      </nav>
    </aside>
  )
}
