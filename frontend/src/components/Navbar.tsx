import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { clearUser, getUser } from '../store/authStore'
import { useQuery } from '@tanstack/react-query'
import { getCart } from '../api/cart'
import LanguageSwitcher from './LanguageSwitcher'

export default function Navbar() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const user = getUser()
  const [open, setOpen] = useState(false)
  const { data: cart } = useQuery({ queryKey: ['cart'], queryFn: getCart, enabled: !!user })
  const cartCount = cart?.items?.length || 0

  const logout = () => {
    clearUser()
    navigate('/')
  }

  const linkClass = 'text-sm font-medium text-ink/80 hover:text-ink transition-colors'

  return (
    <header className="sticky top-0 z-50 bg-paper/95 backdrop-blur-md border-b border-ink/8">
      <div className="border-b border-ink/5 bg-ink/[0.03]">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-2 flex justify-between items-center text-[11px] uppercase tracking-[0.12em] text-ink-muted gap-4">
          <span className="hidden sm:inline truncate">{t('brand.topBar')}</span>
          <div className="flex items-center gap-4 sm:gap-6 ml-auto shrink-0">
            <LanguageSwitcher />
            {!user ? (
              <>
                <Link to="/login" className="hover:text-primary-700 transition-colors whitespace-nowrap">
                  {t('nav.signIn')}
                </Link>
                <Link to="/register" className="hover:text-primary-700 transition-colors whitespace-nowrap hidden xs:inline">
                  {t('nav.createAccount')}
                </Link>
              </>
            ) : (
              <span className="text-ink/70 truncate max-w-[10rem] sm:max-w-xs">{user.email}</span>
            )}
          </div>
        </div>
      </div>

      <nav className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-[4.25rem]">
          <Link to="/" className="group flex flex-col items-start gap-0.5 min-w-0">
            <span className="font-serif text-2xl sm:text-[1.65rem] text-ink tracking-tight leading-none group-hover:text-primary-700 transition-colors truncate max-w-[85vw] sm:max-w-none">
              {t('brand.name')}
            </span>
            <span className="text-[10px] uppercase tracking-[0.18em] text-gold font-medium max-w-[16rem] sm:max-w-none leading-tight">
              {t('brand.subtitle')}
            </span>
          </Link>

          <div className="hidden lg:flex items-center gap-10">
            <Link to="/" className={linkClass}>
              {t('nav.home')}
            </Link>
            <Link to="/catalog" className={linkClass}>
              {t('nav.catalog')}
            </Link>
            {user && (
              <Link to="/my-orders" className={linkClass}>
                {t('nav.orders')}
              </Link>
            )}
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className={linkClass}>
                {t('nav.admin')}
              </Link>
            )}
          </div>

          <div className="hidden lg:flex items-center gap-4">
            {user && (
              <Link
                to="/cart"
                className="relative p-2 text-ink/70 hover:text-ink transition-colors"
                aria-label={t('nav.cartAria')}
              >
                <ShoppingCart className="w-[1.35rem] h-[1.35rem]" strokeWidth={1.5} />
                {cartCount > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 min-w-[1.125rem] h-[1.125rem] bg-primary-600 text-white text-[10px] font-bold flex items-center justify-center rounded-full">
                    {cartCount > 9 ? '9+' : cartCount}
                  </span>
                )}
              </Link>
            )}
            {user ? (
              <button type="button" onClick={logout} className="btn-secondary !py-2 !text-[11px]">
                {t('nav.signOut')}
              </button>
            ) : (
              <Link to="/catalog" className="btn-primary !py-2.5">
                {t('nav.shopCatalog')}
              </Link>
            )}
          </div>

          <button
            type="button"
            className="lg:hidden p-2 text-ink"
            onClick={() => setOpen(!open)}
            aria-expanded={open}
            aria-label={t('nav.menuAria')}
          >
            {open ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>

        {open && (
          <div className="lg:hidden border-t border-ink/8 py-4 space-y-1">
            <div className="pb-3 mb-2 border-b border-ink/8">
              <LanguageSwitcher />
            </div>
            <Link to="/" className="block py-2.5 text-ink font-medium" onClick={() => setOpen(false)}>
              {t('nav.home')}
            </Link>
            <Link to="/catalog" className="block py-2.5 text-ink font-medium" onClick={() => setOpen(false)}>
              {t('nav.catalog')}
            </Link>
            {user && (
              <>
                <Link to="/cart" className="block py-2.5 text-ink font-medium" onClick={() => setOpen(false)}>
                  {t('nav.cart')} {cartCount > 0 ? `(${cartCount})` : ''}
                </Link>
                <Link to="/my-orders" className="block py-2.5 text-ink font-medium" onClick={() => setOpen(false)}>
                  {t('nav.orders')}
                </Link>
              </>
            )}
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className="block py-2.5 text-ink font-medium" onClick={() => setOpen(false)}>
                {t('nav.admin')}
              </Link>
            )}
            {user ? (
              <button
                type="button"
                className="block w-full text-left py-2.5 text-primary-700 font-medium"
                onClick={() => {
                  logout()
                  setOpen(false)
                }}
              >
                {t('nav.signOut')}
              </button>
            ) : (
              <div className="flex gap-3 pt-2">
                <Link to="/login" className="btn-secondary flex-1 justify-center" onClick={() => setOpen(false)}>
                  {t('nav.signIn')}
                </Link>
                <Link to="/register" className="btn-primary flex-1 justify-center" onClick={() => setOpen(false)}>
                  {t('nav.createAccount')}
                </Link>
              </div>
            )}
          </div>
        )}
      </nav>
    </header>
  )
}
