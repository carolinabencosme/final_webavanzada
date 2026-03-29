import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, BookOpen, User, LogOut, LayoutDashboard, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { clearUser, getUser } from '../store/authStore'
import { useQuery } from '@tanstack/react-query'
import { getCart } from '../api/cart'

export default function Navbar() {
  const navigate = useNavigate()
  const user = getUser()
  const [open, setOpen] = useState(false)
  
  const { data: cart } = useQuery({ queryKey: ['cart'], queryFn: getCart, enabled: !!user })
  const cartCount = cart?.items?.length || 0

  const logout = () => { clearUser(); navigate('/') }

  return (
    <nav className="bg-white border-b border-gray-100 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2 font-bold text-xl text-primary-600">
            <BookOpen className="w-6 h-6" /> BookStore
          </Link>

          <div className="hidden md:flex items-center gap-6">
            <Link to="/" className="text-gray-600 hover:text-primary-600 transition-colors font-medium">Home</Link>
            <Link to="/catalog" className="text-gray-600 hover:text-primary-600 transition-colors font-medium">Catalog</Link>
            {user && <Link to="/my-orders" className="text-gray-600 hover:text-primary-600 transition-colors font-medium">My Orders</Link>}
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className="text-gray-600 hover:text-primary-600 transition-colors font-medium flex items-center gap-1">
                <LayoutDashboard className="w-4 h-4" /> Admin
              </Link>
            )}
          </div>

          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <>
                <Link to="/cart" className="relative p-2 text-gray-600 hover:text-primary-600 transition-colors">
                  <ShoppingCart className="w-5 h-5" />
                  {cartCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                      {cartCount}
                    </span>
                  )}
                </Link>
                <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
                  <User className="w-4 h-4 text-primary-600" />
                  {user.username}
                </div>
                <button onClick={logout} className="btn-secondary text-sm py-1.5">
                  <LogOut className="w-4 h-4" /> Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-secondary text-sm py-1.5">Login</Link>
                <Link to="/register" className="btn-primary text-sm py-1.5">Sign Up</Link>
              </>
            )}
          </div>

          <button className="md:hidden p-2" onClick={() => setOpen(!open)}>
            {open ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      <AnimatePresence>
        {open && (
          <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }} className="md:hidden bg-white border-t border-gray-100 px-4 py-4 space-y-3">
            <Link to="/" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>Home</Link>
            <Link to="/catalog" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>Catalog</Link>
            {user && <Link to="/cart" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>Cart ({cartCount})</Link>}
            {user && <Link to="/my-orders" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>My Orders</Link>}
            {user?.role === 'ADMIN' && <Link to="/admin" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>Admin Dashboard</Link>}
            {user ? <button onClick={() => { logout(); setOpen(false) }} className="block text-red-600 font-medium">Logout</button>
              : <><Link to="/login" className="block text-gray-700 font-medium" onClick={() => setOpen(false)}>Login</Link>
                <Link to="/register" className="block text-primary-600 font-medium" onClick={() => setOpen(false)}>Sign Up</Link></>}
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  )
}
