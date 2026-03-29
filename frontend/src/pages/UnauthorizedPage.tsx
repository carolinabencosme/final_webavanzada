import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

export default function UnauthorizedPage() {
  const { t } = useTranslation()

  return (
    <div className="min-h-[50vh] flex flex-col items-center justify-center px-4 py-20">
      <p className="section-label mb-3">{t('unauthorized.code')}</p>
      <h1 className="font-serif text-3xl text-ink font-semibold text-center">{t('unauthorized.title')}</h1>
      <p className="prose-editorial text-center max-w-md mt-4">{t('unauthorized.body')}</p>
      <Link to="/" className="btn-primary mt-10">
        {t('unauthorized.cta')}
      </Link>
    </div>
  )
}
