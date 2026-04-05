import { useParams, Link } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { ArrowLeft, Star, Calendar, Users, MessageSquare } from 'lucide-react'
import toast from 'react-hot-toast'
import { getPropertyById } from '../api/catalog'
import { addToCart } from '../api/cart'
import { createReview, getPropertyReviews } from '../api/reviews'
import { getUser } from '../store/authStore'
import { pickStayPlaceholder } from '../lib/placeholderImages'
import { useMemo, useState } from 'react'

function addDays(d: Date, n: number) {
  const x = new Date(d)
  x.setDate(x.getDate() + n)
  return x
}

function fmt(d: Date) {
  return d.toISOString().slice(0, 10)
}

export default function PropertyDetailPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const qc = useQueryClient()
  const user = getUser()
  const today = useMemo(() => new Date(), [])
  const [checkIn, setCheckIn] = useState(() => fmt(addDays(today, 1)))
  const [checkOut, setCheckOut] = useState(() => fmt(addDays(today, 3)))
  const [guests, setGuests] = useState(2)
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewComment, setReviewComment] = useState('')

  const { data: property, isLoading, error } = useQuery({
    queryKey: ['property', id],
    queryFn: () => getPropertyById(id!),
    enabled: !!id,
  })

  const { data: reviews } = useQuery({
    queryKey: ['reviews', id],
    queryFn: () => getPropertyReviews(id!),
    enabled: !!id,
  })

  const handleAdd = async () => {
    if (!user) {
      toast.error(t('propertyDetail.loginRequired'))
      return
    }
    if (!id) return
    try {
      await addToCart(id, checkIn, checkOut, guests)
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success(t('propertyDetail.toastOk'))
    } catch {
      toast.error(t('propertyDetail.toastErr'))
    }
  }

  const handleReview = async () => {
    if (!user || !id) {
      toast.error(t('propertyDetail.loginRequired'))
      return
    }
    try {
      await createReview(user.userId, id, reviewRating, reviewComment.trim(), user.email)
      setReviewComment('')
      qc.invalidateQueries({ queryKey: ['reviews', id] })
      qc.invalidateQueries({ queryKey: ['property', id] })
      toast.success(t('reviews.thanks'))
    } catch {
      toast.error(t('reviews.error'))
    }
  }

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center text-ink-muted font-sans text-sm">{t('propertyDetail.loading')}</div>
    )
  }
  if (error || !property) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center">
        <p className="font-serif text-xl text-ink mb-4">{t('propertyDetail.notFound')}</p>
        <Link to="/catalog" className="btn-ghost">
          ← {t('propertyDetail.back')}
        </Link>
      </div>
    )
  }

  const p = property as {
    id: string
    name: string
    city?: string
    country?: string
    propertyType?: string
    roomType?: string
    description?: string
    pricePerNight?: number
    imageUrl?: string
    amenities?: string[]
    maxGuests?: number
    averageRating?: number
  }

  const coverFallback = pickStayPlaceholder(p.id)
  const cover = p.imageUrl || coverFallback

  return (
    <div className="bg-paper min-h-full">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 md:py-14">
        <Link to="/catalog" className="inline-flex items-center gap-2 text-ink-muted hover:text-ink text-sm font-medium mb-10 transition-colors">
          <ArrowLeft className="w-4 h-4" strokeWidth={1.5} /> {t('propertyDetail.breadcrumb')}
        </Link>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-16 items-start">
          <div className="card overflow-hidden shadow-glow border-stone-100">
            <div className="aspect-[4/3] bg-paper-deep rounded-t-2xl overflow-hidden">
              <img
                src={cover}
                alt=""
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.currentTarget.src = coverFallback
                }}
              />
            </div>
          </div>
          <div>
            {p.propertyType && <span className="badge mb-4">{p.propertyType}</span>}
            <h1 className="font-serif text-4xl md:text-[2.75rem] leading-tight text-ink font-semibold">{p.name}</h1>
            <p className="mt-3 text-lg text-ink-muted">
              {p.city}
              {p.country ? `, ${p.country}` : ''}
            </p>
            <div className="flex items-center gap-2 mt-6">
              <Star className="w-5 h-5 fill-gold-light text-gold-light" strokeWidth={0} />
              <span className="text-sm tabular-nums text-ink-muted">
                {p.averageRating?.toFixed(1) ?? '—'} {t('propertyDetail.ratingSuffix')}
              </span>
            </div>
            <p className="mt-8 font-serif text-3xl text-primary-700 font-semibold">
              ${Number(p.pricePerNight ?? 0).toFixed(2)}
              <span className="text-lg font-sans font-normal text-ink-muted"> / {t('propertyDetail.perNight')}</span>
            </p>

            <div className="mt-8 space-y-4 p-6 bg-surface border border-stone-200 rounded-2xl shadow-book">
              <p className="section-label">{t('propertyDetail.stayDates')}</p>
              <div className="grid sm:grid-cols-2 gap-4">
                <label className="block">
                  <span className="text-xs text-ink-muted flex items-center gap-1 mb-1">
                    <Calendar className="w-3.5 h-3.5" /> {t('propertyDetail.checkIn')}
                  </span>
                  <input type="date" className="input" value={checkIn} onChange={(e) => setCheckIn(e.target.value)} />
                </label>
                <label className="block">
                  <span className="text-xs text-ink-muted flex items-center gap-1 mb-1">
                    <Calendar className="w-3.5 h-3.5" /> {t('propertyDetail.checkOut')}
                  </span>
                  <input type="date" className="input" value={checkOut} onChange={(e) => setCheckOut(e.target.value)} />
                </label>
              </div>
              <label className="block">
                <span className="text-xs text-ink-muted flex items-center gap-1 mb-1">
                  <Users className="w-3.5 h-3.5" /> {t('propertyDetail.guests')}
                </span>
                <input
                  type="number"
                  min={1}
                  max={p.maxGuests ?? 20}
                  className="input"
                  value={guests}
                  onChange={(e) => setGuests(Number(e.target.value))}
                />
              </label>
            </div>

            {p.description && (
              <div className="mt-8 pt-8 border-t border-ink/10">
                <p className="section-label mb-3">{t('propertyDetail.about')}</p>
                <p className="prose-editorial whitespace-pre-line">{p.description}</p>
              </div>
            )}
            {p.amenities && p.amenities.length > 0 && (
              <div className="mt-6 flex flex-wrap gap-2">
                {p.amenities.map((a) => (
                  <span key={a} className="text-xs px-3 py-1.5 bg-primary-50 border border-primary-100 rounded-full text-primary-900">
                    {a}
                  </span>
                ))}
              </div>
            )}
            <button type="button" onClick={handleAdd} className="mt-10 btn-primary !normal-case !text-sm !py-3 !px-8 w-full sm:w-auto">
              {t('propertyDetail.reserve')}
            </button>

            <div className="mt-16 pt-12 border-t border-ink/10">
              <h2 className="font-serif text-2xl text-ink font-semibold flex items-center gap-2">
                <MessageSquare className="w-6 h-6 text-primary-700" strokeWidth={1.5} />
                {t('reviews.title')}
              </h2>
              {!Array.isArray(reviews) || reviews.length === 0 ? (
                <p className="mt-4 text-sm text-ink-muted">{t('reviews.empty')}</p>
              ) : (
                <ul className="mt-6 space-y-4">
                  {(reviews as { id: number; rating: number; comment?: string; userEmail?: string; createdAt?: string }[]).map(
                    (rv) => (
                      <li key={rv.id} className="p-5 bg-surface border border-stone-200 rounded-2xl shadow-sm">
                        <div className="flex items-center gap-2 text-sm">
                          <Star className="w-4 h-4 fill-gold-light text-gold-light" strokeWidth={0} />
                          <span className="font-semibold tabular-nums">{rv.rating}</span>
                          <span className="text-ink-muted text-xs">{rv.userEmail}</span>
                        </div>
                        {rv.comment && <p className="mt-2 text-sm text-ink whitespace-pre-line">{rv.comment}</p>}
                      </li>
                    )
                  )}
                </ul>
              )}
              {user ? (
                <div className="mt-8 space-y-3 p-6 bg-gradient-to-br from-primary-50/80 to-paper-deep border border-primary-100 rounded-2xl">
                  <label className="block">
                    <span className="text-xs text-ink-muted">{t('reviews.rating')}</span>
                    <select
                      className="input mt-1"
                      value={reviewRating}
                      onChange={(e) => setReviewRating(Number(e.target.value))}
                    >
                      {[5, 4, 3, 2, 1].map((n) => (
                        <option key={n} value={n}>
                          {n}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="block">
                    <span className="text-xs text-ink-muted">{t('reviews.comment')}</span>
                    <textarea
                      className="input mt-1 min-h-[100px]"
                      value={reviewComment}
                      onChange={(e) => setReviewComment(e.target.value)}
                    />
                  </label>
                  <button type="button" onClick={handleReview} className="btn-secondary !text-sm">
                    {t('reviews.submit')}
                  </button>
                </div>
              ) : (
                <p className="mt-4 text-sm text-ink-muted">{t('reviews.loginHint')}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
