import { Outlet } from 'react-router-dom'
import AdminSidebar from '../../components/AdminSidebar'

export default function AdminLayout() {
  return (
    <div className="flex min-h-[calc(100vh-8rem)] bg-paper">
      <AdminSidebar />
      <div className="flex-1 p-6 md:p-10 overflow-x-auto">
        <Outlet />
      </div>
    </div>
  )
}
