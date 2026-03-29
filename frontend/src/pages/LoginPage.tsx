import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Mail, Lock } from 'lucide-react'
import toast from 'react-hot-toast'
import { login } from '../api/auth'
import { setUser } from '../store/authStore'

export default function LoginPage() {
  const { t } = useTranslation()
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
      toast.success(t('auth.welcomeBack', { name: data.username }))
      navigate(data.role === 'ADMIN' ? '/admin' : '/catalog')
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } }).response?.data?.message || t('auth.invalidCredentials'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-12rem)] flex">
      <div className="hidden lg:flex lg:w-2/5 bg-ink text-paper p-12 flex-col justify-between">
        <div>
          <p className="section-label text-gold-light mb-4">{t('auth.loginAsideLabel')}</p>
          <h2 className="font-serif text-3xl leading-snug font-medium">
            {t('auth.loginAsideBody', { university: t('brand.university') })}
          </h2>
        </div>
        <p className="text-sm text-paper/45 leading-relaxed max-w-sm">{t('auth.loginAsideFoot')}</p>
      </div>
      <div className="flex-1 flex items-center justify-center p-6 md:p-12 bg-paper">
        <div className="w-full max-w-md">
          <h1 className="font-serif text-3xl text-ink font-semibold">{t('auth.loginTitle')}</h1>
          <p className="text-ink-muted text-sm mt-2">{t('auth.loginSubtitle')}</p>

          <div className="card p-8 mt-8 border-ink/10">
            {error && <div className="mb-4 text-sm text-red-800 bg-red-50 border border-red-100 px-3 py-2">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="text-xs font-semibold uppercase tracking-wider text-ink-muted block mb-2">{t('auth.email')}</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-subtle" strokeWidth={1.5} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="input pl-10"
                    placeholder={t('auth.emailPlaceholder')}
                    required
                  />
                </div>
              </div>
              <div>
                <label className="text-xs font-semibold uppercase tracking-wider text-ink-muted block mb-2">{t('auth.password')}</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-subtle" strokeWidth={1.5} />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="input pl-10"
                    placeholder="••••••••"
                    required
                  />
                </div>
              </div>
              <button type="submit" disabled={loading} className="btn-primary w-full !py-3 !normal-case !tracking-normal text-sm">
                {loading ? t('auth.signingIn') : t('auth.signIn')}
              </button>
            </form>
          </div>

          <div className="mt-6 p-4 bg-paper-deep border border-ink/8 rounded-lg text-sm text-ink-muted">
            <p className="font-semibold text-ink mb-2">{t('login.demoTitle')}</p>
            <p>{t('login.demoAdmin')}</p>
            <p>{t('login.demoClient')}</p>
          </div>

          <p className="text-center text-sm text-ink-muted mt-8">
            {t('auth.noAccount')}{' '}
            <Link to="/register" className="text-primary-700 font-semibold hover:underline">
              {t('auth.registerLink')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
