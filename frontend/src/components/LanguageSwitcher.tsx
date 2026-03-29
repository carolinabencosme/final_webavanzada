import { useTranslation } from 'react-i18next'

export default function LanguageSwitcher({ className = '' }: { className?: string }) {
  const { i18n, t } = useTranslation()
  const isEs = i18n.language.startsWith('es')
  const isEn = i18n.language.startsWith('en')

  return (
    <div className={`flex items-center gap-1.5 ${className}`} role="group" aria-label={t('nav.lang')}>
      <button
        type="button"
        onClick={() => void i18n.changeLanguage('en')}
        className={`text-[11px] uppercase tracking-wider px-1.5 py-0.5 rounded transition-colors ${
          isEn ? 'text-ink font-bold' : 'text-ink-muted hover:text-ink'
        }`}
      >
        EN
      </button>
      <span className="text-ink/25 text-[10px]">|</span>
      <button
        type="button"
        onClick={() => void i18n.changeLanguage('es')}
        className={`text-[11px] uppercase tracking-wider px-1.5 py-0.5 rounded transition-colors ${
          isEs ? 'text-ink font-bold' : 'text-ink-muted hover:text-ink'
        }`}
      >
        ES
      </button>
    </div>
  )
}
