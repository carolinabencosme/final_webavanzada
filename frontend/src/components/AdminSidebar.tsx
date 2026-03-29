import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, Users, ShoppingBag, BookOpen } from 'lucide-react'

const links = [
  { to: '/admin', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/admin/users', icon: Users, label: 'Users' },
  { to: '/admin/orders', icon: ShoppingBag, label: 'All Orders' },
]

export default function AdminSidebar() {
  const { pathname } = useLocation()
  return (
    <aside className="w-56 bg-white border-r border-gray-100 min-h-screen p-4">
      <div className="flex items-center gap-2 font-bold text-primary-600 mb-8 px-2">
        <BookOpen className="w-5 h-5" /> Admin Panel
      </div>
      <nav className="space-y-1">
        {links.map(({ to, icon: Icon, label }) => (
          <Link key={to} to={to}
            className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
              ${pathname === to ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}`}>
            <Icon className="w-4 h-4" /> {label}
          </Link>
        ))}
      </nav>
    </aside>
  )
}
