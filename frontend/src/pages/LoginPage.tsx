import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { BookOpen, Mail, Lock } from 'lucide-react'
import toast from 'react-hot-toast'
import { login } from '../api/auth'
import { setUser } from '../store/authStore'

export default function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(email, password)
      setUser({ userId: data.userId, username: data.username, email: data.email, role: data.role, token: data.token })
      toast.success(`Welcome back, ${data.username}!`)
      navigate(data.role === 'ADMIN' ? '/admin' : '/catalog')
    } catch (err: unknown) {
      setError((err as {response?: {data?: {message?: string}}}).response?.data?.message || 'Invalid credentials')
    } finally { setLoading(false) }
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-gray-50">
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-2xl shadow-lg border border-gray-100 w-full max-w-md p-8">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
            <BookOpen className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Welcome back</h1>
            <p className="text-gray-500 text-sm">Sign in to your account</p>
          </div>
        </div>
        {error && <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1 block">Email</label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                className="input pl-10" placeholder="you@example.com" required />
            </div>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1 block">Password</label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input type="password" value={password} onChange={e => setPassword(e.target.value)}
                className="input pl-10" placeholder="••••••••" required />
            </div>
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3 text-base">
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <div className="mt-6 p-4 bg-gray-50 rounded-lg text-sm text-gray-600">
          <p className="font-medium mb-1">Demo accounts:</p>
          <p>Admin: admin@bookstore.com / admin123</p>
          <p>Client: alice@test.com / password123</p>
        </div>
        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account? <Link to="/register" className="text-primary-600 hover:underline font-medium">Sign up</Link>
        </p>
      </motion.div>
    </div>
  )
}
