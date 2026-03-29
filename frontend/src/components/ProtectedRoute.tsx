import { Navigate } from 'react-router-dom'
import { getUser } from '../store/authStore'

interface Props { children: React.ReactNode; adminOnly?: boolean }

export default function ProtectedRoute({ children, adminOnly }: Props) {
  const user = getUser()
  if (!user) return <Navigate to="/login" replace />
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/unauthorized" replace />
  return <>{children}</>
}
