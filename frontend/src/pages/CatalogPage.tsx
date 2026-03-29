import { useState, useEffect, type ComponentProps } from 'react'
import { Search, ChevronLeft, ChevronRight } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { getBooks, searchBooks, getGenres } from '../api/catalog'
import BookCard from '../components/BookCard'
import SkeletonCard from '../components/SkeletonCard'

export default function CatalogPage() {
  const { t } = useTranslation()
  const [searchParams, setSearchParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [genre, setGenre] = useState('')
  const [searchInput, setSearchInput] = useState('')

  useEffect(() => {
    const g = searchParams.get('genre')
    if (g) {
      setGenre(decodeURIComponent(g))
      setPage(0)
    }
  }, [searchParams])

  const { data: genres } = useQuery({ queryKey: ['genres'], queryFn: getGenres })
  const isSearching = search.trim() || genre
  const { data, isLoading } = useQuery({
    queryKey: ['books', page, search, genre],
    queryFn: () => (isSearching ? searchBooks({ title: search, genre, page, size: 12 }) : getBooks(page, 12, genre)),
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearch(searchInput)
    setPage(0)
  }

  const clearFilters = () => {
    setSearch('')
    setSearchInput('')
    setGenre('')
    setPage(0)
    setSearchParams({})
  }

  const onGenreChange = (value: string) => {
    setGenre(value)
    setPage(0)
    if (value) setSearchParams({ genre: value })
    else setSearchParams({})
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-ink/8 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12 md:py-16">
          <p className="section-label mb-2">{t('catalog.label')}</p>
          <h1 className="font-serif text-4xl md:text-5xl text-ink font-semibold tracking-tight">{t('catalog.title')}</h1>
          <p className="mt-3 prose-editorial max-w-2xl">{t('catalog.intro')}</p>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex flex-col lg:flex-row gap-4 mb-10">
          <form onSubmit={handleSearch} className="flex-1 relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-subtle" strokeWidth={1.5} />
            <input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="input pl-11 pr-28"
              placeholder={t('catalog.searchPlaceholder')}
            />
            <button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 btn-primary !py-2 !text-[11px]">
              {t('catalog.search')}
            </button>
          </form>
          <div className="flex flex-col sm:flex-row gap-3">
            <select
              value={genre}
              onChange={(e) => onGenreChange(e.target.value)}
              className="input min-w-[200px] appearance-none bg-white cursor-pointer"
            >
              <option value="">{t('catalog.allSubjects')}</option>
              {(genres as string[] | undefined)?.map((g) => (
                <option key={g} value={g}>
                  {g}
                </option>
              ))}
            </select>
            {(search || genre) && (
              <button type="button" onClick={clearFilters} className="btn-secondary whitespace-nowrap">
                {t('catalog.clearFilters')}
              </button>
            )}
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6 md:gap-8">
            {Array.from({ length: 12 }).map((_, i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        ) : data?.content?.length === 0 ? (
          <div className="text-center py-24 border border-dashed border-ink/15 bg-white">
            <Search className="w-10 h-10 mx-auto mb-4 text-ink/25" strokeWidth={1} />
            <p className="font-serif text-xl text-ink">{t('catalog.emptyTitle')}</p>
            <p className="prose-editorial mt-2 max-w-md mx-auto">{t('catalog.emptyHint')}</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6 md:gap-8">
            {data?.content?.map((book: ComponentProps<typeof BookCard>['book']) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        )}

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-6 mt-14 pt-10 border-t border-ink/8">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
              className="btn-secondary disabled:opacity-35"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs font-semibold uppercase tracking-widest text-ink-muted">
              {t('catalog.page', { current: page + 1, total: data.totalPages })}
            </span>
            <button
              type="button"
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="btn-secondary disabled:opacity-35"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
