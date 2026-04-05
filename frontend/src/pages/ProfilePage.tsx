import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import toast from 'react-hot-toast'
import { getMe, updateProfile } from '../api/auth'
import { getUser, setUser } from '../store/authStore'

export default function ProfilePage() {
  const { t } = useTranslation()
  const session = getUser()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!session) return
    getMe()
      .then((u: { username?: string; email?: string }) => {
        setUsername(u.username ?? '')
        setEmail(u.email ?? '')
      })
      .catch(() => toast.error(t('profile.loadErr')))
      .finally(() => setLoading(false))
  }, [session, t])

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!session) return
    setSaving(true)
    try {
      const body: {
        username?: string
        email?: string
        currentPassword?: string
        newPassword?: string
      } = { username, email }
      if (newPassword.trim()) {
        body.newPassword = newPassword
        body.currentPassword = currentPassword
      }
      const updated = await updateProfile(body)
      setUser({
        ...session,
        username: updated.username ?? session.username,
        email: updated.email ?? session.email,
      })
      setCurrentPassword('')
      setNewPassword('')
      toast.success(t('profile.saved'))
    } catch {
      toast.error(t('profile.saveErr'))
    } finally {
      setSaving(false)
    }
  }

  if (!session) {
    return null
  }

  if (loading) {
    return <div className="max-w-lg mx-auto px-4 py-20 text-ink-muted text-sm">{t('profile.loading')}</div>
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-ink/8 bg-white">
        <div className="max-w-lg mx-auto px-4 py-12">
          <p className="section-label mb-2">{t('profile.label')}</p>
          <h1 className="font-serif text-4xl text-ink font-semibold">{t('profile.title')}</h1>
        </div>
      </div>
      <div className="max-w-lg mx-auto px-4 py-10">
        <form onSubmit={onSubmit} className="card p-8 space-y-6 shadow-book">
          <label className="block">
            <span className="text-xs text-ink-muted uppercase tracking-widest">{t('auth.displayName')}</span>
            <input className="input mt-1" value={username} onChange={(e) => setUsername(e.target.value)} />
          </label>
          <label className="block">
            <span className="text-xs text-ink-muted uppercase tracking-widest">{t('auth.email')}</span>
            <input
              type="email"
              className="input mt-1"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
          <div className="pt-4 border-t border-ink/10">
            <p className="section-label mb-3">{t('profile.changePassword')}</p>
            <label className="block">
              <span className="text-xs text-ink-muted">{t('profile.currentPassword')}</span>
              <input
                type="password"
                className="input mt-1"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            <label className="block mt-4">
              <span className="text-xs text-ink-muted">{t('profile.newPassword')}</span>
              <input
                type="password"
                className="input mt-1"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                autoComplete="new-password"
              />
            </label>
          </div>
          <button type="submit" disabled={saving} className="btn-primary w-full sm:w-auto">
            {saving ? t('profile.saving') : t('profile.save')}
          </button>
        </form>
      </div>
    </div>
  )
}
