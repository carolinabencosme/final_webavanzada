import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { BookOpen, Shield, Zap, Star, ArrowRight } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { getBooks } from '../api/catalog'
import BookCard from '../components/BookCard'
import SkeletonCard from '../components/SkeletonCard'

const features = [
  { icon: BookOpen, title: 'Vast Catalog', desc: '50+ curated books across all genres' },
  { icon: Zap, title: 'Instant Purchase', desc: 'Secure checkout with instant confirmation' },
  { icon: Shield, title: 'Safe & Secure', desc: 'Your data is protected with JWT authentication' },
  { icon: Star, title: 'Honest Reviews', desc: 'Verified purchase reviews from real readers' },
]

export default function HomePage() {
  const { data, isLoading } = useQuery({ queryKey: ['books-home'], queryFn: () => getBooks(0, 8) })

  return (
    <div>
      <section className="bg-gradient-to-br from-primary-600 to-primary-800 text-white py-24 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            <h1 className="text-5xl font-bold mb-6 leading-tight">Discover Your Next<br />Great Read 📚</h1>
            <p className="text-xl text-primary-200 mb-10 max-w-2xl mx-auto">
              Explore our curated collection of books across every genre. From timeless classics to modern bestsellers.
            </p>
            <div className="flex gap-4 justify-center flex-wrap">
              <Link to="/catalog" className="bg-white text-primary-700 hover:bg-primary-50 font-semibold px-8 py-3 rounded-xl transition-all flex items-center gap-2 shadow-lg hover:shadow-xl">
                Browse Catalog <ArrowRight className="w-4 h-4" />
              </Link>
              <Link to="/register" className="border-2 border-white/40 hover:bg-white/10 text-white font-semibold px-8 py-3 rounded-xl transition-all">
                Get Started Free
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      <section className="py-20 px-4 bg-white">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">Why BookStore?</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map(({ icon: Icon, title, desc }, i) => (
              <motion.div key={title} initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.1 }} className="text-center p-6">
                <div className="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                  <Icon className="w-6 h-6 text-primary-600" />
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{title}</h3>
                <p className="text-gray-500 text-sm">{desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-20 px-4 bg-gray-50">
        <div className="max-w-6xl mx-auto">
          <div className="flex items-center justify-between mb-12">
            <h2 className="text-3xl font-bold text-gray-900">Latest Books</h2>
            <Link to="/catalog" className="text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
            {isLoading ? Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)
              : data?.content?.map((book: unknown) => <BookCard key={(book as {id: string}).id} book={book as Parameters<typeof BookCard>[0]['book']} />)}
          </div>
        </div>
      </section>
    </div>
  )
}
