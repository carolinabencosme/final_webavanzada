import { motion } from 'framer-motion'
import { ShoppingCart, Star } from 'lucide-react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { addToCart } from '../api/cart'
import { getUser } from '../store/authStore'
import { useQueryClient } from '@tanstack/react-query'

interface Book { id: string; title: string; author: string; genre: string; price: number; coverUrl: string; averageRating: number; }

interface Props { book: Book }

export default function BookCard({ book }: Props) {
  const user = getUser()
  const qc = useQueryClient()

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault()
    if (!user) { toast.error('Please login to add items to cart'); return }
    try {
      await addToCart(book.id, 1)
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success(`"${book.title}" added to cart!`)
    } catch { toast.error('Failed to add to cart') }
  }

  return (
    <motion.div whileHover={{ y: -4, boxShadow: '0 10px 25px rgba(0,0,0,0.1)' }}
      initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl border border-gray-100 overflow-hidden hover:border-primary-200 transition-all duration-300 flex flex-col">
      <Link to={`/books/${book.id}`} className="flex-1">
        <div className="aspect-[2/3] bg-gray-100 overflow-hidden">
          <img src={book.coverUrl || `https://via.placeholder.com/200x300/4F46E5/FFFFFF?text=${encodeURIComponent(book.title.substring(0,10))}`}
            alt={book.title} className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
            onError={(e) => { (e.target as HTMLImageElement).src = `https://via.placeholder.com/200x300/4F46E5/FFFFFF?text=Book` }} />
        </div>
        <div className="p-4">
          <span className="badge bg-primary-50 text-primary-700 mb-2">{book.genre}</span>
          <h3 className="font-semibold text-gray-900 text-sm leading-tight mb-1 line-clamp-2">{book.title}</h3>
          <p className="text-gray-500 text-xs mb-2">{book.author}</p>
          <div className="flex items-center gap-1 mb-3">
            <Star className="w-3.5 h-3.5 fill-yellow-400 text-yellow-400" />
            <span className="text-xs font-medium text-gray-700">{book.averageRating?.toFixed(1) || '0.0'}</span>
          </div>
          <p className="text-primary-600 font-bold text-lg">${Number(book.price).toFixed(2)}</p>
        </div>
      </Link>
      <div className="px-4 pb-4">
        <button onClick={handleAddToCart}
          className="w-full btn-primary justify-center text-sm py-2">
          <ShoppingCart className="w-4 h-4" /> Add to Cart
        </button>
      </div>
    </motion.div>
  )
}
