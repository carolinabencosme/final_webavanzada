const STATUS_STYLES: Record<string, string> = {
  PAID: 'bg-green-100 text-green-800',
  PENDING: 'bg-yellow-100 text-yellow-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
}

export default function StatusBadge({ status }: { status: string }) {
  return (
    <span className={`badge ${STATUS_STYLES[status] || 'bg-gray-100 text-gray-800'}`}>{status}</span>
  )
}
