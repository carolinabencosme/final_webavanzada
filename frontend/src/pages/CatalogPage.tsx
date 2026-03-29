import { useState } from 'react'
import { motion } from 'framer-motion'
import { Search, Filter, ChevronLeft, ChevronRight } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { getBooks, searchBooks, getGenres } from '../api/catalog'
import BookCard from '../components/BookCard'
import SkeletonCard from '../components/SkeletonCard'

export default function CatalogPage() {
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [genre, setGenre] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const { data: genres } = useQuery({ queryKey: ['genres'], queryFn: getGenres })
  const isSearching = search.trim() || genre
  const { data, isLoading } = useQuery({
    queryKey: ['books', page, search, genre],
    queryFn: () => isSearching ? searchBooks({ title: search, genre, page, size: 12 }) : getBooks(page, 12, genre),
  })
  const handleSearch = (e: React.FormEvent) => { e.preventDefault(); setSearch(searchInput); setPage(0) }
  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Book Catalog</h1>
        <p className="text-gray-500">Discover your next great read</p>
      </motion.div>
      <div className="flex flex-col sm:flex-row gap-4 mb-8">
        <form onSubmit={handleSearch} className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input value={searchInput} onChange={e => setSearchInput(e.target.value)} className="input pl-10 pr-24" placeholder="Search by title or author..." />
          <button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 btn-primary text-sm py-1.5 px-3">Search</button>
        </form>
        <div className="relative">
          <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <select value={genre} onChange={e => { setGenre(e.target.value); setPage(0) }} className="input pl-10 pr-8 appearance-none min-w-[160px]">
            <option value="">All Genres</option>
            {(genres as string[] | undefined)?.map((g) => <option key={g} value={g}>{g}</option>)}
          </select>
        </div>
        {(search || genre) && <button onClick={() => { setSearch(''); setSearchInput(''); setGenre(''); setPage(0) }} className="btn-secondary text-sm">Clear</button>}
      </div>
      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">{Array.from({ length: 12 }).map((_, i) => <SkeletonCard key={i} />)}</div>
      ) : data?.content?.length === 0 ? (
        <div className="text-center py-20 text-gray-500"><Search className="w-12 h-12 mx-auto mb-4 opacity-30" /><p className="text-lg font-medium">No books found</p></div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
          {data?.content?.map((book: any, i: number) => (
            <motion.div key={book.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.03 }}><BookCard book={book} /></motion.div>
          ))}
        </div>
      )}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-10">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="btn-secondary disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
          <span className="text-sm text-gray-600 font-medium">Page {page + 1} of {data.totalPages}</span>
          <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)} className="btn-secondary disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
        </div>
      )}
    </div>
  )
}
