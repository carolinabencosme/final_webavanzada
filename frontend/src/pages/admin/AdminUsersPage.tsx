import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { getUsers } from '../../api/auth'

export default function AdminUsersPage() {
  const { t } = useTranslation()
  const { data: users, isLoading, error } = useQuery({ queryKey: ['admin-users'], queryFn: getUsers })

  if (isLoading) {
    return <p className="text-ink-muted text-sm">…</p>
  }
  if (error) {
    return <p className="text-red-800 text-sm">{t('admin.usersLoadErr')}</p>
  }

  const list = Array.isArray(users) ? users : []

  return (
    <div>
      <h1 className="font-serif text-3xl text-ink font-semibold mb-8">{t('admin.usersTitle')}</h1>
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
    </div>
  )
}
