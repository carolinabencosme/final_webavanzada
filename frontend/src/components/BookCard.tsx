import { ShoppingCart, Star } from 'lucide-react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useTranslation } from 'react-i18next'
import { addToCart } from '../api/cart'
import { getUser } from '../store/authStore'
import { useQueryClient } from '@tanstack/react-query'

interface Book {
  id: string
  title: string
  author: string
  genre: string
  price: number
  coverUrl: string
  averageRating: number
}

interface Props {
  book: Book
}

export default function BookCard({ book }: Props) {
  const { t } = useTranslation()
  const user = getUser()
  const qc = useQueryClient()

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (!user) {
      toast.error(t('bookCard.loginRequired'))
      return
    }
    try {
      await addToCart(book.id, 1)
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success(t('bookCard.added', { title: book.title }))
    } catch {
      toast.error(t('bookCard.addError'))
    }
  }

  const cover =
    book.coverUrl ||
    `https://via.placeholder.com/280x420/1a1f26/e8e0d5?text=${encodeURIComponent(book.title.slice(0, 18))}`

  return (
    <article className="group flex flex-col h-full">
      <Link to={`/books/${book.id}`} className="card flex flex-col flex-1 overflow-hidden transition-shadow duration-300 hover:shadow-lift">
        <div className="aspect-[2/3] bg-paper-deep overflow-hidden relative">
          <img
            src={cover}
            alt=""
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-[1.03]"
            onError={(e) => {
              ;(e.target as HTMLImageElement).src =
                'https://via.placeholder.com/280x420/1a1f26/e8e0d5?text=Book'
            }}
          />
          <div className="absolute inset-x-0 bottom-0 h-1/3 bg-gradient-to-t from-ink/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
        </div>
        <div className="p-4 flex flex-col flex-1 border-t border-ink/6">
          <span className="badge mb-2 w-fit">{book.genre}</span>
          <h3 className="font-serif text-lg leading-snug text-ink font-semibold line-clamp-2 group-hover:text-primary-700 transition-colors">
            {book.title}
          </h3>
          <p className="text-ink-muted text-xs mt-1 line-clamp-1">{book.author}</p>
          <div className="flex items-center gap-1 mt-2">
            <Star className="w-3.5 h-3.5 fill-gold-light/90 text-gold-light shrink-0" strokeWidth={0} />
            <span className="text-xs tabular-nums text-ink-muted">{book.averageRating?.toFixed(1) ?? '—'}</span>
          </div>
          <p className="mt-auto pt-3 font-serif text-xl text-primary-700 font-semibold">${Number(book.price).toFixed(2)}</p>
        </div>
      </Link>
      <button
        type="button"
        onClick={handleAddToCart}
        className="mt-3 w-full btn-primary !normal-case !tracking-normal text-[13px] py-2.5"
      >
        <ShoppingCart className="w-4 h-4" strokeWidth={1.75} />
        {t('bookCard.addToCart')}
      </button>
    </article>
  )
}
