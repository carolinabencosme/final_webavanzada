import { Link } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { useTranslation } from 'react-i18next'

export default function SiteFooter() {
  const { t } = useTranslation()

  return (
    <footer className="mt-auto border-t border-stone-800/20 bg-gradient-to-b from-stone-900 to-stone-950 text-stone-300">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        <div className="grid gap-10 md:grid-cols-12">
          <div className="md:col-span-5">
            <div className="flex items-center gap-2 font-serif text-2xl text-white mb-2">
              <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 text-white shadow-lg shadow-primary-900/40">
                <MapPin className="w-4 h-4" strokeWidth={2} />
              </span>
              {t('brand.name')}
            </div>
            <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-primary-300/90 mb-4">{t('brand.tagline')}</p>
            <p className="text-sm text-stone-400 leading-relaxed max-w-md">{t('footer.blurb')}</p>
          </div>
          <div className="md:col-span-3 md:col-start-7">
            <h3 className="text-[11px] font-bold uppercase tracking-[0.2em] text-primary-400/90 mb-4">{t('footer.explore')}</h3>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link to="/catalog" className="hover:text-white transition-colors">
                  {t('footer.fullCatalog')}
                </Link>
              </li>
              <li>
                <Link to="/" className="hover:text-white transition-colors">
                  {t('footer.home')}
                </Link>
              </li>
              <li>
                <Link to="/login" className="hover:text-white transition-colors">
                  {t('footer.memberLogin')}
                </Link>
              </li>
            </ul>
          </div>
          <div className="md:col-span-3">
            <h3 className="text-[11px] font-bold uppercase tracking-[0.2em] text-primary-400/90 mb-4">{t('footer.help')}</h3>
            <ul className="space-y-2.5 text-sm text-stone-500">
              <li>{t('footer.help1')}</li>
              <li>{t('footer.help2')}</li>
              <li>{t('footer.help3')}</li>
            </ul>
          </div>
        </div>
        <div className="mt-12 pt-8 border-t border-white/10 flex flex-col gap-3 text-xs text-stone-500">
          <span>
            © {new Date().getFullYear()} {t('brand.name')} · {t('brand.tagline')}
          </span>
          <span className="text-stone-600 max-w-3xl leading-relaxed">{t('footer.legal')}</span>
        </div>
      </div>
    </footer>
  )
}
