import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { createUser, getUsers, type AdminCreateUserBody } from '../../api/auth'

export default function AdminUsersPage() {
  const { t } = useTranslation()
  const queryClient = useQueryClient()
  const { data: users, isLoading, error } = useQuery({ queryKey: ['admin-users'], queryFn: getUsers })

  const [open, setOpen] = useState(false)
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<'CLIENT' | 'ADMIN'>('CLIENT')
  const [active, setActive] = useState(true)
  const [sendWelcome, setSendWelcome] = useState(true)
  const [formMsg, setFormMsg] = useState<{ ok: boolean; text: string } | null>(null)

  const createMut = useMutation({
    mutationFn: (body: AdminCreateUserBody) => createUser(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] })
      setFormMsg({ ok: true, text: t('admin.createUserSuccess') })
      setUsername('')
      setEmail('')
      setPassword('')
      setRole('CLIENT')
      setActive(true)
      setSendWelcome(true)
      setOpen(false)
    },
    onError: () => {
      setFormMsg({ ok: false, text: t('admin.createUserErr') })
    },
  })

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    setFormMsg(null)
    createMut.mutate({
      username: username.trim(),
      email: email.trim(),
      password,
      role,
      active,
      sendWelcomeEmail: sendWelcome,
    })
  }

  if (isLoading) {
    return <p className="text-ink-muted text-sm">…</p>
  }
  if (error) {
    return <p className="text-red-800 text-sm">{t('admin.usersLoadErr')}</p>
  }

  const list = Array.isArray(users) ? users : []

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <h1 className="font-serif text-3xl text-ink font-semibold">{t('admin.usersTitle')}</h1>
        <button
          type="button"
          onClick={() => {
            setFormMsg(null)
            setOpen(true)
          }}
          className="btn-primary px-5 py-2.5 text-sm font-semibold rounded-xl"
        >
          {t('admin.createUser')}
        </button>
      </div>

      {formMsg && !open && (
        <p className={`text-sm mb-4 ${formMsg.ok ? 'text-primary-800' : 'text-red-800'}`}>{formMsg.text}</p>
      )}

      <div className="card overflow-hidden p-0 border-ink/10">
        <table className="w-full text-sm">
          <thead className="bg-paper-deep border-b border-ink/8">
            <tr>
              <th className="text-left p-4 font-semibold text-ink text-xs uppercase tracking-wider">Email</th>
              <th className="text-left p-4 font-semibold text-ink text-xs uppercase tracking-wider">Name</th>
              <th className="text-left p-4 font-semibold text-ink text-xs uppercase tracking-wider">Role</th>
            </tr>
          </thead>
          <tbody>
            {list.map((u: { id?: string; email?: string; username?: string; role?: string }) => (
              <tr key={u.id ?? u.email} className="border-b border-ink/5 last:border-0">
                <td className="p-4 text-ink">{u.email}</td>
                <td className="p-4 text-ink-muted">{u.username}</td>
                <td className="p-4">
                  <span className="badge">{u.role}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {open && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-ink/40 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="admin-create-user-title"
        >
          <div className="card max-w-md w-full p-6 border-ink/10 shadow-xl">
            <h2 id="admin-create-user-title" className="font-serif text-xl text-ink font-semibold mb-4">
              {t('admin.createUser')}
            </h2>
            <form onSubmit={submit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-ink-muted uppercase tracking-wider mb-1">
                  {t('admin.fieldUsername')}
                </label>
                <input
                  className="w-full rounded-lg border border-ink/15 bg-white px-3 py-2 text-sm text-ink"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  minLength={3}
                  autoComplete="username"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-ink-muted uppercase tracking-wider mb-1">
                  {t('admin.fieldEmail')}
                </label>
                <input
                  type="email"
                  className="w-full rounded-lg border border-ink/15 bg-white px-3 py-2 text-sm text-ink"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="email"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-ink-muted uppercase tracking-wider mb-1">
                  {t('admin.fieldPassword')}
                </label>
                <input
                  type="password"
                  className="w-full rounded-lg border border-ink/15 bg-white px-3 py-2 text-sm text-ink"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  minLength={6}
                  autoComplete="new-password"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-ink-muted uppercase tracking-wider mb-1">
                  {t('admin.fieldRole')}
                </label>
                <select
                  className="w-full rounded-lg border border-ink/15 bg-white px-3 py-2 text-sm text-ink"
                  value={role}
                  onChange={(e) => setRole(e.target.value as 'CLIENT' | 'ADMIN')}
                >
                  <option value="CLIENT">CLIENT</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <label className="flex items-center gap-2 text-sm text-ink cursor-pointer">
                <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} />
                {t('admin.fieldActive')}
              </label>
              <label className="flex items-center gap-2 text-sm text-ink cursor-pointer">
                <input type="checkbox" checked={sendWelcome} onChange={(e) => setSendWelcome(e.target.checked)} />
                {t('admin.fieldSendWelcome')}
              </label>
              {formMsg && open && !formMsg.ok && <p className="text-red-800 text-sm">{formMsg.text}</p>}
              <div className="flex gap-3 pt-2">
                <button
                  type="submit"
                  disabled={createMut.isPending}
                  className="btn-primary flex-1 py-2.5 text-sm font-semibold rounded-xl disabled:opacity-50"
                >
                  {createMut.isPending ? '…' : t('admin.createUserSubmit')}
                </button>
                <button
                  type="button"
                  onClick={() => setOpen(false)}
                  className="flex-1 py-2.5 text-sm font-semibold rounded-xl border border-ink/15 bg-white text-ink hover:bg-paper-deep transition-colors"
                >
                  {t('admin.createUserCancel')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
