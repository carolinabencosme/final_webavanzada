import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import es from './locales/es.json'

const STORAGE_KEY = 'i18n-lang'

function getInitialLng(): string {
  if (typeof window === 'undefined') return 'es'
  return localStorage.getItem(STORAGE_KEY) || 'es'
}

void i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    es: { translation: es },
  },
  lng: getInitialLng(),
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
})

i18n.on('languageChanged', (lng) => {
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, lng)
  }
  if (typeof document !== 'undefined') {
    document.documentElement.lang = lng.startsWith('es') ? 'es' : 'en'
  }
})

if (typeof document !== 'undefined') {
  document.documentElement.lang = i18n.language.startsWith('es') ? 'es' : 'en'
}

export default i18n
