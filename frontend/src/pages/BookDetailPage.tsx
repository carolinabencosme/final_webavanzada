import { useParams, Link } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { ArrowLeft, Star, ShoppingCart } from 'lucide-react'
import toast from 'react-hot-toast'
import { getBookById } from '../api/catalog'
import { addToCart } from '../api/cart'
import { getUser } from '../store/authStore'
import { getBookCover, PLACEHOLDER_COVER_URL } from '../lib/bookImages'

export default function BookDetailPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const qc = useQueryClient()
  const user = getUser()
  const { data: book, isLoading, error } = useQuery({
    queryKey: ['book', id],
    queryFn: () => getBookById(id!),
    enabled: !!id,
  })

  const handleAdd = async () => {
    if (!user) {
      toast.error(t('bookDetail.loginRequired'))
      return
    }
    if (!id) return
    try {
      await addToCart(id, 1)
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success(t('bookDetail.toastOk'))
    } catch {
      toast.error(t('bookDetail.toastErr'))
    }
  }

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center text-ink-muted font-sans text-sm">{t('bookDetail.loading')}</div>
    )
  }
  if (error || !book) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center">
        <p className="font-serif text-xl text-ink mb-4">{t('bookDetail.notFound')}</p>
        <Link to="/catalog" className="btn-ghost">
          ← {t('bookDetail.back')}
        </Link>
      </div>
    )
  }

  const b = book as {
    id: string
    title: string
    author: string
    genre: string
    price: number
    coverUrl?: string
    averageRating?: number
    description?: string
    imageUrl?: string
    thumbnail?: string
    images?: string[]
  }

  const cover = getBookCover(b)
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    e.currentTarget.src = PLACEHOLDER_COVER_URL
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 md:py-14">
        <Link to="/catalog" className="inline-flex items-center gap-2 text-ink-muted hover:text-ink text-sm font-medium mb-10 transition-colors">
          <ArrowLeft className="w-4 h-4" strokeWidth={1.5} /> {t('bookDetail.breadcrumb')}
        </Link>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-16 items-start">
          <div className="card overflow-hidden shadow-book">
            <div className="aspect-[2/3] bg-paper-deep">
              <img src={cover} alt="" className="w-full h-full object-cover" onError={handleImageError} />
            </div>
          </div>
          <div>
            <span className="badge mb-4">{b.genre}</span>
            <h1 className="font-serif text-4xl md:text-[2.75rem] leading-tight text-ink font-semibold">{b.title}</h1>
            <p className="mt-3 text-lg text-ink-muted">
              {t('bookDetail.by')} {b.author}
            </p>
            <div className="flex items-center gap-2 mt-6">
              <Star className="w-5 h-5 fill-gold-light text-gold-light" strokeWidth={0} />
              <span className="text-sm tabular-nums text-ink-muted">
                {b.averageRating?.toFixed(1) ?? '—'} {t('bookDetail.ratingSuffix')}
              </span>
            </div>
            <p className="mt-8 font-serif text-3xl text-primary-700 font-semibold">${Number(b.price).toFixed(2)}</p>
            {b.description && (
              <div className="mt-8 pt-8 border-t border-ink/10">
                <p className="section-label mb-3">{t('bookDetail.aboutEdition')}</p>
                <p className="prose-editorial whitespace-pre-line">{b.description}</p>
              </div>
            )}
            <button type="button" onClick={handleAdd} className="mt-10 btn-primary !normal-case !text-sm !py-3 !px-8">
              <ShoppingCart className="w-5 h-5" strokeWidth={1.75} />
              {t('bookDetail.addToCart')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
