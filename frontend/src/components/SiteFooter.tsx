import { Link } from 'react-router-dom'
import { BookMarked } from 'lucide-react'
import { useTranslation } from 'react-i18next'

export default function SiteFooter() {
  const { t } = useTranslation()

  return (
    <footer className="mt-auto border-t border-ink/10 bg-ink text-paper/90">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        <div className="grid gap-10 md:grid-cols-12">
          <div className="md:col-span-5">
            <div className="flex items-center gap-2 font-serif text-xl text-paper mb-2">
              <BookMarked className="w-6 h-6 text-gold-light shrink-0" strokeWidth={1.25} />
              {t('brand.name')}
            </div>
            <p className="text-[11px] uppercase tracking-[0.15em] text-gold-light/80 mb-4">{t('brand.university')}</p>
            <p className="text-sm text-paper/65 leading-relaxed max-w-md">{t('footer.blurb')}</p>
          </div>
          <div className="md:col-span-3 md:col-start-7">
            <h3 className="section-label text-gold-light/90 mb-4">{t('footer.explore')}</h3>
            <ul className="space-y-2 text-sm">
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
            <h3 className="section-label text-gold-light/90 mb-4">{t('footer.help')}</h3>
            <ul className="space-y-2 text-sm text-paper/70">
              <li>{t('footer.help1')}</li>
              <li>{t('footer.help2')}</li>
              <li>{t('footer.help3')}</li>
            </ul>
          </div>
        </div>
        <div className="mt-12 pt-8 border-t border-white/10 flex flex-col gap-3 text-xs text-paper/45">
          <span>
            © {new Date().getFullYear()} {t('brand.university')} · {t('brand.name')}
          </span>
          <span className="text-paper/35 max-w-3xl leading-relaxed">{t('footer.legal')}</span>
        </div>
      </div>
    </footer>
  )
}
