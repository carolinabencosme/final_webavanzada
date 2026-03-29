import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'

/** Keeps document title and html lang in sync with i18n. */
export default function DocumentTitle() {
  const { t, i18n } = useTranslation()

  useEffect(() => {
    document.title = t('meta.title')
  }, [t, i18n.language])

  return null
}
