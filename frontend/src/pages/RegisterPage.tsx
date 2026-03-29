import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { BookOpen, User, Mail, Lock } from 'lucide-react'
import toast from 'react-hot-toast'
import { register } from '../api/auth'
import { setUser } from '../store/authStore'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) => setForm(f => ({ ...f, [k]: e.target.value }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (form.password !== form.confirm) { setError('Passwords do not match'); return }
    setError(''); setLoading(true)
    try {
      const data = await register(form.username, form.email, form.password)
      setUser({ userId: data.userId, username: data.username, email: data.email, role: data.role, token: data.token })
      toast.success('Account created!')
      navigate('/catalog')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed')
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
          <div><h1 className="text-2xl font-bold text-gray-900">Create account</h1><p className="text-gray-500 text-sm">Join BookStore today</p></div>
        </div>
        {error && <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          {[['username','Username','text',User,'johndoe'],['email','Email','email',Mail,'you@example.com'],['password','Password','password',Lock,'••••••••'],['confirm','Confirm','password',Lock,'••••••••']] .map(([k,label,type,Icon,ph]: any) => (
            <div key={k}>
              <label className="text-sm font-medium text-gray-700 mb-1 block">{label}</label>
              <div className="relative"><Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input type={type} value={(form as any)[k]} onChange={set(k)} className="input pl-10" placeholder={ph} required /></div>
            </div>))}
          <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3">{loading ? 'Creating...' : 'Create Account'}</button>
        </form>
        <p className="text-center text-sm text-gray-500 mt-6">Already have an account? <Link to="/login" className="text-primary-600 hover:underline font-medium">Sign in</Link></p>
      </motion.div>
    </div>
  )
}
