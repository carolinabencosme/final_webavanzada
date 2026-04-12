import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import type { AxiosError } from 'axios'
import { MapPin, Package, Pencil, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { deleteEndedReservation, getMyReservations, updateReservation } from '../api/reservations'

export default function OrdersPage() {
  const { t } = useTranslation()
  const qc = useQueryClient()
  const { data: reservations, isLoading } = useQuery({ queryKey: ['reservations'], queryFn: getMyReservations })
  const [editingId, setEditingId] = useState<number | null>(null)
  const [formIn, setFormIn] = useState('')
  const [formOut, setFormOut] = useState('')
  const [formGuests, setFormGuests] = useState(2)
  const [saving, setSaving] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const checkoutDatePassed = (checkOut?: string) => {
    if (!checkOut) return false
    const co = checkOut.slice(0, 10)
    const today = new Date().toISOString().slice(0, 10)
    return co < today
  }

  const removeFromHistory = async (id: number) => {
    if (!window.confirm(t('orders.deleteConfirm'))) return
    setDeletingId(id)
    try {
      await deleteEndedReservation(id)
      await qc.invalidateQueries({ queryKey: ['reservations'] })
      toast.success(t('orders.deleted'))
    } catch (e) {
      const ax = e as AxiosError<{ message?: string }>
      toast.error(ax.response?.data?.message ?? t('orders.deleteErr'))
    } finally {
      setDeletingId(null)
    }
  }

  if (isLoading) {
    return <div className="max-w-4xl mx-auto px-4 py-20 text-ink-muted text-sm">{t('orders.loading')}</div>
  }

  const list = Array.isArray(reservations) ? reservations : []

  const openEdit = (o: {
    id: number
    checkIn?: string
    checkOut?: string
    guests?: number
    status?: string
  }) => {
    if (o.status !== 'PENDING_PAYMENT' && o.status !== 'CONFIRMED') return
    setEditingId(o.id)
    setFormIn(o.checkIn?.slice(0, 10) ?? '')
    setFormOut(o.checkOut?.slice(0, 10) ?? '')
    setFormGuests(o.guests ?? 2)
  }

  const saveEdit = async () => {
    if (editingId == null) return
    setSaving(true)
    try {
      await updateReservation(editingId, {
        checkIn: formIn,
        checkOut: formOut,
        guests: formGuests,
      })
      await qc.invalidateQueries({ queryKey: ['reservations'] })
      setEditingId(null)
      toast.success(t('orders.updated'))
    } catch {
      toast.error(t('orders.updateErr'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-ink/8 bg-white">
        <div className="max-w-4xl mx-auto px-4 py-12">
          <p className="section-label mb-2">{t('orders.label')}</p>
          <h1 className="font-serif text-4xl text-ink font-semibold">{t('orders.title')}</h1>
        </div>
      </div>
      <div className="max-w-4xl mx-auto px-4 py-10">
        {list.length === 0 ? (
          <p className="prose-editorial">{t('orders.empty')}</p>
        ) : (
          <ul className="space-y-3">
            {list.map(
              (o: {
                id: number
                status?: string
                total?: unknown
                createdAt?: string
                propertyName?: string
                city?: string
                checkIn?: string
                checkOut?: string
                guests?: number
              }) => (
                <li key={o.id} className="card p-6 flex items-start gap-4">
                  <div className="w-11 h-11 rounded-lg bg-paper-deep flex items-center justify-center shrink-0">
                    <Package className="w-5 h-5 text-primary-700" strokeWidth={1.5} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-serif text-lg font-semibold text-ink">{o.propertyName ?? `#${o.id}`}</p>
                    {o.city && (
                      <p className="text-sm text-ink-muted flex items-center gap-1 mt-1">
                        <MapPin className="w-3.5 h-3.5" strokeWidth={1.5} />
                        {o.city}
                      </p>
                    )}
                    {(o.checkIn || o.checkOut) && (
                      <p className="text-sm text-ink-muted mt-1">
                        {o.checkIn?.slice(0, 10)} → {o.checkOut?.slice(0, 10)}
                      </p>
                    )}
                    <p className="text-sm text-ink-muted mt-2">
                      {o.status ?? '—'}
                      {o.createdAt ? ` · ${new Date(o.createdAt).toLocaleString()}` : ''}
                    </p>
                    <p className="text-primary-700 font-semibold font-serif text-xl mt-2">${Number(o.total ?? 0).toFixed(2)}</p>
                    <div className="mt-3 flex flex-wrap items-center gap-3">
                      {(o.status === 'PENDING_PAYMENT' || o.status === 'CONFIRMED') && (
                        <button
                          type="button"
                          onClick={() => openEdit(o)}
                          className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-widest text-primary-700 hover:text-primary-800"
                        >
                          <Pencil className="w-3.5 h-3.5" strokeWidth={1.5} />
                          {t('orders.edit')}
                        </button>
                      )}
                      {o.status !== 'PENDING_PAYMENT' && checkoutDatePassed(o.checkOut) && (
                        <button
                          type="button"
                          disabled={deletingId === o.id}
                          onClick={() => removeFromHistory(o.id)}
                          className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-widest text-ink-muted hover:text-red-700"
                        >
                          <Trash2 className="w-3.5 h-3.5" strokeWidth={1.5} />
                          {deletingId === o.id ? '…' : t('orders.deleteFromHistory')}
                        </button>
                      )}
                    </div>
                  </div>
                </li>
              )
            )}
          </ul>
        )}

        {editingId != null && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-ink/40 backdrop-blur-sm">
            <div className="bg-white max-w-md w-full p-8 shadow-book border border-ink/10 rounded-sm">
              <p className="section-label mb-4">{t('orders.edit')}</p>
              <div className="space-y-4">
                <label className="block">
                  <span className="text-xs text-ink-muted">{t('propertyDetail.checkIn')}</span>
                  <input type="date" className="input mt-1 w-full" value={formIn} onChange={(e) => setFormIn(e.target.value)} />
                </label>
                <label className="block">
                  <span className="text-xs text-ink-muted">{t('propertyDetail.checkOut')}</span>
                  <input type="date" className="input mt-1 w-full" value={formOut} onChange={(e) => setFormOut(e.target.value)} />
                </label>
                <label className="block">
                  <span className="text-xs text-ink-muted">{t('propertyDetail.guests')}</span>
                  <input
                    type="number"
                    min={1}
                    className="input mt-1 w-full"
                    value={formGuests}
                    onChange={(e) => setFormGuests(Number(e.target.value))}
                  />
                </label>
              </div>
              <div className="flex gap-3 mt-8">
                <button type="button" className="btn-secondary flex-1" onClick={() => setEditingId(null)}>
                  {t('orders.cancelEdit')}
                </button>
                <button type="button" className="btn-primary flex-1" disabled={saving} onClick={saveEdit}>
                  {saving ? '…' : t('orders.save')}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
