import { Outlet } from 'react-router-dom'
import AdminSidebar from '../../components/AdminSidebar'

export default function AdminLayout() {
  return (
    <div className="flex min-h-[calc(100vh-8rem)] bg-gradient-to-br from-stone-50 via-paper to-primary-50/30">
      <AdminSidebar />
      <div className="flex-1 p-6 md:p-10 overflow-x-auto">
        <Outlet />
      </div>
    </div>
  )
}
