import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { ArrowRight } from 'lucide-react'
import { getBooks, getGenres } from '../api/catalog'
import BookCard from '../components/BookCard'
import SkeletonCard from '../components/SkeletonCard'

export default function HomePage() {
  const { t } = useTranslation()
  const {
    data,
    isLoading,
    isError,
    refetch,
    isFetching,
  } = useQuery({ queryKey: ['books-home'], queryFn: () => getBooks(0, 8) })
  const { data: genres } = useQuery({ queryKey: ['genres'], queryFn: getGenres })
  const topicList =
    Array.isArray(genres) && genres.length > 0 ? genres.slice(0, 8) : ['Fiction', 'Science', 'History', 'Philosophy', 'Arts', 'Education']

  return (
    <div>
      <section className="relative overflow-hidden border-b border-ink/8">
        <div className="absolute inset-0 bg-gradient-to-br from-paper via-paper to-paper-deep" />
        <div
          className="absolute top-0 right-0 w-[55%] h-full opacity-[0.07] pointer-events-none"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23000000' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
          }}
        />
        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
          <p className="section-label mb-4">{t('home.heroLabel')}</p>
          <h1 className="font-serif font-semibold text-display text-ink max-w-3xl">{t('home.heroTitle')}</h1>
          <p className="mt-6 prose-editorial max-w-xl">{t('home.heroBody')}</p>
          <div className="mt-10 flex flex-wrap gap-4">
            <Link to="/catalog" className="btn-primary inline-flex gap-2">
              {t('home.catalogCta')} <ArrowRight className="w-3.5 h-3.5" strokeWidth={2.5} />
            </Link>
            <Link to="/register" className="btn-secondary">
              {t('home.registerCta')}
            </Link>
          </div>
        </div>
      </section>

      <section className="bg-ink text-paper py-16 md:py-20">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-3xl">
            <p className="section-label text-gold-light mb-3">{t('home.missionLabel')}</p>
            <blockquote className="font-serif text-2xl md:text-3xl leading-snug text-paper/95 font-medium">
              “{t('home.missionQuote')}”
            </blockquote>
            <p className="mt-6 text-sm text-paper/55 max-w-lg leading-relaxed">{t('home.missionFootnote')}</p>
          </div>
        </div>
      </section>

      <section className="py-16 md:py-24 border-b border-ink/8 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10">
            <div>
              <p className="section-label mb-2">{t('home.exploreLabel')}</p>
              <h2 className="font-serif text-3xl md:text-4xl text-ink">{t('home.topicsTitle')}</h2>
              <p className="mt-2 prose-editorial max-w-lg">{t('home.topicsDesc')}</p>
            </div>
            <Link to="/catalog" className="btn-ghost self-start md:self-auto shrink-0">
              {t('home.topicsLink')}
            </Link>
          </div>
          <div className="flex flex-wrap gap-2">
            {topicList.map((g) => (
              <Link
                key={g}
                to={`/catalog?genre=${encodeURIComponent(g)}`}
                className="px-4 py-2.5 text-sm font-medium border border-ink/12 bg-paper hover:bg-paper-deep hover:border-ink/20 transition-colors text-ink"
              >
                {g}
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="py-16 md:py-24 bg-paper">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-12">
            <div>
              <p className="section-label mb-2">{t('home.recentLabel')}</p>
              <h2 className="font-serif text-3xl md:text-4xl text-ink">{t('home.recentTitle')}</h2>
            </div>
            <Link to="/catalog" className="btn-ghost text-sm">
              {t('home.recentLink')}
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6 md:gap-8">
            {isLoading || (isFetching && !data)
              ? Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)
              : isError
                ? (
                    <div className="col-span-full rounded border border-ink/12 bg-paper-deep px-6 py-8 text-center">
                      <p className="text-ink font-medium">{t('home.recentError')}</p>
                      <button type="button" onClick={() => refetch()} className="btn-primary mt-4">
                        {t('home.recentRetry')}
                      </button>
                    </div>
                  )
                : (data?.content?.length ? data.content : []).map((book: unknown) => (
                    <BookCard key={(book as { id: string }).id} book={book as Parameters<typeof BookCard>[0]['book']} />
                  ))}
          </div>
        </div>
      </section>
    </div>
  )
}
