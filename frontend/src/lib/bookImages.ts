const PLACEHOLDER_COVER_URL = 'https://via.placeholder.com/520x780/1a1f26/e8e0d5?text=Book'

type NullableString = string | null | undefined

type BookLike = {
  coverUrl?: NullableString
  imageUrl?: NullableString
  thumbnail?: NullableString
  images?: Array<NullableString>
}

const normalizeUrl = (url: NullableString): string | null => {
  if (typeof url !== 'string') return null
  const trimmed = url.trim()
  return trimmed.length > 0 ? trimmed : null
}

const dedupeUrls = (urls: Array<NullableString>): string[] => {
  const seen = new Set<string>()
  const unique: string[] = []

  for (const url of urls) {
    const normalized = normalizeUrl(url)
    if (!normalized || seen.has(normalized)) continue
    seen.add(normalized)
    unique.push(normalized)
  }

  return unique
}

export function getBookCover(book: BookLike | null | undefined): string {
  if (!book) return PLACEHOLDER_COVER_URL

  const candidates = dedupeUrls([
    book.coverUrl,
    book.imageUrl,
    book.thumbnail,
    ...(book.images ?? []),
  ])

  return candidates[0] ?? PLACEHOLDER_COVER_URL
}

export { PLACEHOLDER_COVER_URL }
