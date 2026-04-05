import { useState, useEffect, useMemo } from 'react'
import { Search, ChevronLeft, ChevronRight } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { getProperties, searchProperties } from '../api/catalog'
import PropertyCard from '../components/PropertyCard'
import SkeletonCard from '../components/SkeletonCard'

const PROPERTY_TYPES = ['HOTEL', 'APARTMENT', 'VILLA']

export default function CatalogPage() {
  const { t } = useTranslation()
  const [searchParams, setSearchParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [city, setCity] = useState('')
  const [propertyType, setPropertyType] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const today = useMemo(() => new Date(), [])
  const [checkIn, setCheckIn] = useState('')
  const [checkOut, setCheckOut] = useState('')

  useEffect(() => {
    const c = searchParams.get('city')
    if (c) {
      setCity(decodeURIComponent(c))
      setPage(0)
    }
  }, [searchParams])

  const isSearching = search.trim() || city || propertyType
  const dateFilter =
    checkIn && checkOut && checkOut > checkIn ? { checkIn, checkOut } : undefined
  const { data, isLoading } = useQuery({
    queryKey: ['properties', page, search, city, propertyType, checkIn, checkOut],
    queryFn: () =>
      isSearching
        ? searchProperties({
            q: search || undefined,
            city: city || undefined,
            propertyType: propertyType || undefined,
            page,
            size: 12,
            ...dateFilter,
          })
        : getProperties(page, 12, dateFilter),
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearch(searchInput)
    setPage(0)
  }

  const clearFilters = () => {
    setSearch('')
    setSearchInput('')
    setCity('')
    setPropertyType('')
    setCheckIn('')
    setCheckOut('')
    setPage(0)
    setSearchParams({})
  }

  const presetDates = () => {
    const t = new Date(today)
    t.setDate(t.getDate() + 1)
    const o = new Date(today)
    o.setDate(o.getDate() + 3)
    setCheckIn(t.toISOString().slice(0, 10))
    setCheckOut(o.toISOString().slice(0, 10))
    setPage(0)
  }

  const onTypeChange = (value: string) => {
    setPropertyType(value)
    setPage(0)
  }

  return (
    <div className="bg-paper min-h-full">
      <div className="border-b border-stone-200/80 bg-gradient-to-br from-white via-primary-50/40 to-amber-50/30">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12 md:py-16">
          <p className="section-label mb-2">{t('catalog.label')}</p>
          <h1 className="font-serif text-4xl md:text-5xl text-ink font-semibold tracking-tight">{t('catalog.title')}</h1>
          <p className="mt-3 prose-editorial max-w-2xl">{t('catalog.intro')}</p>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="mb-8 p-5 md:p-6 bg-surface border border-stone-200 rounded-2xl shadow-book">
          <p className="section-label mb-2">{t('catalog.dateFilter')}</p>
          <p className="text-xs text-ink-muted mb-3">{t('catalog.dateFilterHint')}</p>
          <div className="flex flex-col sm:flex-row gap-3 items-end">
            <label className="block flex-1">
              <span className="text-xs text-ink-muted">{t('propertyDetail.checkIn')}</span>
              <input type="date" className="input mt-1 w-full" value={checkIn} onChange={(e) => { setCheckIn(e.target.value); setPage(0) }} />
            </label>
            <label className="block flex-1">
              <span className="text-xs text-ink-muted">{t('propertyDetail.checkOut')}</span>
              <input type="date" className="input mt-1 w-full" value={checkOut} onChange={(e) => { setCheckOut(e.target.value); setPage(0) }} />
            </label>
            <button type="button" onClick={presetDates} className="btn-secondary !text-[11px] whitespace-nowrap">
              {t('catalog.suggestDates')}
            </button>
          </div>
        </div>

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
              value={propertyType}
              onChange={(e) => onTypeChange(e.target.value)}
              className="input min-w-[200px] appearance-none bg-white cursor-pointer"
            >
              <option value="">{t('catalog.allTypes')}</option>
              {PROPERTY_TYPES.map((g) => (
                <option key={g} value={g}>
                  {g}
                </option>
              ))}
            </select>
            {(search || city || propertyType) && (
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
          <div className="text-center py-24 border-2 border-dashed border-primary-200/60 bg-primary-50/30 rounded-3xl">
            <Search className="w-10 h-10 mx-auto mb-4 text-primary-400" strokeWidth={1} />
            <p className="font-serif text-xl text-ink">{t('catalog.emptyTitle')}</p>
            <p className="prose-editorial mt-2 max-w-md mx-auto">{t('catalog.emptyHint')}</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6 md:gap-8">
            {data?.content?.map((prop: { id: string }) => (
              <PropertyCard key={prop.id} property={prop as Parameters<typeof PropertyCard>[0]['property']} />
            ))}
          </div>
        )}

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-6 mt-14 pt-10 border-t border-stone-200">
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
