import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { User, Mail, Lock, type LucideIcon } from 'lucide-react'
import toast from 'react-hot-toast'
import { register } from '../api/auth'
import { setUser } from '../store/authStore'

export default function RegisterPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (form.password !== form.confirm) {
      setError(t('auth.passwordMismatch'))
      return
    }
    setError('')
    setLoading(true)
    try {
      const data = await register(form.username, form.email, form.password)
      setUser({ userId: data.userId, username: data.username, email: data.email, role: data.role, token: data.token })
      toast.success(t('register.welcome', { name: t('brand.name') }))
      navigate('/catalog')
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } }).response?.data?.message || t('auth.registerError'))
    } finally {
      setLoading(false)
    }
  }

  const fields: [string, string, string, LucideIcon][] = [
    ['username', 'auth.displayName', 'text', User],
    ['email', 'auth.email', 'email', Mail],
    ['password', 'auth.password', 'password', Lock],
    ['confirm', 'auth.confirmPassword', 'password', Lock],
  ]

  return (
    <div className="min-h-[calc(100vh-12rem)] flex">
      <div className="hidden lg:flex lg:w-2/5 bg-ink text-paper p-12 flex-col justify-between">
        <div>
          <p className="section-label text-gold-light mb-4">{t('auth.registerAsideLabel')}</p>
          <h2 className="font-serif text-3xl leading-snug font-medium">{t('auth.registerAsideBody', { name: t('brand.name') })}</h2>
        </div>
        <p className="text-sm text-paper/45">{t('auth.registerAsideFoot')}</p>
      </div>
      <div className="flex-1 flex items-center justify-center p-6 md:p-12 bg-paper">
        <div className="w-full max-w-md">
          <h1 className="font-serif text-3xl text-ink font-semibold">{t('auth.registerTitle')}</h1>
          <p className="text-ink-muted text-sm mt-2">{t('auth.registerSubtitle')}</p>

          <div className="card p-8 mt-8 border-ink/10">
            {error && <div className="mb-4 text-sm text-red-800 bg-red-50 border border-red-100 px-3 py-2">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-4">
              {fields.map(([k, labelKey, type, Icon]) => (
                <div key={k}>
                  <label className="text-xs font-semibold uppercase tracking-wider text-ink-muted block mb-2">{t(labelKey)}</label>
                  <div className="relative">
                    <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-subtle" />
                    <input
                      type={type}
                      value={(form as Record<string, string>)[k]}
                      onChange={set(k)}
                      className="input pl-10"
                      placeholder={
                        k === 'email' ? t('auth.emailPlaceholder') : k === 'username' ? t('auth.namePlaceholder') : '••••••••'
                      }
                      required
                    />
                  </div>
                </div>
              ))}
              <button type="submit" disabled={loading} className="btn-primary w-full !py-3 !normal-case !tracking-normal text-sm mt-2">
                {loading ? t('auth.creating') : t('auth.createAccount')}
              </button>
            </form>
          </div>

          <p className="text-center text-sm text-ink-muted mt-8">
            {t('auth.hasAccount')}{' '}
            <Link to="/login" className="text-primary-700 font-semibold hover:underline">
              {t('auth.loginLink')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
