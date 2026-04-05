import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { ArrowRight, Sparkles } from 'lucide-react'
import { getProperties } from '../api/catalog'
import PropertyCard from '../components/PropertyCard'
import SkeletonCard from '../components/SkeletonCard'
import { HERO_IMAGES } from '../lib/placeholderImages'

const CITY_LINKS = ['Santo Domingo', 'Punta Cana', 'Santiago', 'Puerto Plata', 'La Romana', 'Samaná']

export default function HomePage() {
  const { t } = useTranslation()
  const { data, isLoading, isError, refetch, isFetching } = useQuery({
    queryKey: ['properties-home'],
    queryFn: () => getProperties(0, 8),
  })

  return (
    <div>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-mesh-light" />
        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-20 lg:py-24">
          <div className="grid lg:grid-cols-2 gap-12 lg:gap-16 items-center">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-primary-100/90 border border-primary-200/60 text-primary-800 text-[11px] font-bold uppercase tracking-[0.18em] mb-6">
                <Sparkles className="w-3.5 h-3.5" strokeWidth={2} />
                {t('home.heroLabel')}
              </div>
              <h1 className="font-serif font-semibold text-display max-w-xl text-ink">{t('home.heroTitle')}</h1>
              <p className="mt-6 text-ink-muted max-w-lg text-[16px] leading-relaxed">{t('home.heroBody')}</p>
              <div className="mt-10 flex flex-wrap gap-3">
                <Link to="/catalog" className="btn-primary gap-2">
                  {t('home.catalogCta')} <ArrowRight className="w-4 h-4" strokeWidth={2.5} />
                </Link>
                <Link
                  to="/register"
                  className="btn-secondary border-primary-200 text-primary-800 hover:bg-primary-50 bg-white/80 backdrop-blur-sm"
                >
                  {t('home.registerCta')}
                </Link>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="col-span-2 rounded-3xl overflow-hidden shadow-glow aspect-[16/10]">
                <img
                  src={HERO_IMAGES.main}
                  alt=""
                  className="w-full h-full object-cover"
                  loading="eager"
                />
              </div>
              <div className="rounded-2xl overflow-hidden shadow-book aspect-[4/3]">
                <img src={HERO_IMAGES.tileA} alt="" className="w-full h-full object-cover" loading="lazy" />
              </div>
              <div className="rounded-2xl overflow-hidden shadow-book aspect-[4/3]">
                <img src={HERO_IMAGES.tileB} alt="" className="w-full h-full object-cover" loading="lazy" />
              </div>
              <div className="col-span-2 rounded-2xl overflow-hidden shadow-book aspect-[21/9] max-h-36">
                <img src={HERO_IMAGES.tileC} alt="" className="w-full h-full object-cover" loading="lazy" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="relative py-16 md:py-20 overflow-hidden">
        <div
          className="absolute inset-0 bg-cover bg-center opacity-[0.18]"
          style={{ backgroundImage: `url(${HERO_IMAGES.mosaic})` }}
        />
        <div className="absolute inset-0 bg-gradient-to-br from-primary-900/95 via-primary-800/92 to-stone-900/95" />
        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-2xl">
            <p className="text-primary-200 text-[11px] font-bold uppercase tracking-[0.22em] mb-4">{t('home.missionLabel')}</p>
            <blockquote className="font-serif text-2xl md:text-[1.85rem] leading-snug text-white font-medium">
              “{t('home.missionQuote')}”
            </blockquote>
            <p className="mt-8 text-sm text-white/55 max-w-lg leading-relaxed">{t('home.missionFootnote')}</p>
          </div>
        </div>
      </section>

      <section className="py-16 md:py-20 border-b border-stone-200/80 bg-surface">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10">
            <div>
              <p className="section-label mb-2">{t('home.exploreLabel')}</p>
              <h2 className="font-serif text-3xl md:text-4xl text-ink">{t('home.topicsTitle')}</h2>
              <p className="mt-2 prose-editorial max-w-lg">{t('home.topicsDesc')}</p>
            </div>
            <Link to="/catalog" className="btn-ghost self-start md:self-auto shrink-0 font-semibold">
              {t('home.topicsLink')}
            </Link>
          </div>
          <div className="flex flex-wrap gap-2">
            {CITY_LINKS.map((c) => (
              <Link
                key={c}
                to={`/catalog?city=${encodeURIComponent(c)}`}
                className="px-5 py-2.5 text-sm font-semibold rounded-full border border-stone-200 bg-paper hover:bg-primary-50 hover:border-primary-200 hover:text-primary-800 transition-all text-ink"
              >
                {c}
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="py-16 md:py-20 bg-paper">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-12">
            <div>
              <p className="section-label mb-2">{t('home.recentLabel')}</p>
              <h2 className="font-serif text-3xl md:text-4xl text-ink">{t('home.recentTitle')}</h2>
            </div>
            <Link to="/catalog" className="btn-ghost text-sm font-semibold">
              {t('home.recentLink')}
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6 md:gap-8">
            {isLoading || (isFetching && !data)
              ? Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)
              : isError
                ? (
                    <div className="col-span-full rounded-2xl border border-stone-200 bg-surface px-6 py-10 text-center shadow-book">
                      <p className="text-ink font-semibold">{t('home.recentError')}</p>
                      <button type="button" onClick={() => refetch()} className="btn-primary mt-5">
                        {t('home.recentRetry')}
                      </button>
                    </div>
                  )
                : (data?.content?.length ? data.content : []).map((prop: { id: string }) => (
                    <PropertyCard key={prop.id} property={prop as Parameters<typeof PropertyCard>[0]['property']} />
                  ))}
          </div>
        </div>
      </section>
    </div>
  )
}
