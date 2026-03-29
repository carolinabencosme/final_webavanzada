import { Star } from 'lucide-react'

interface Props { rating: number; max?: number; interactive?: boolean; onRate?: (r: number) => void; size?: 'sm' | 'md' | 'lg' }

export default function StarRating({ rating, max = 5, interactive, onRate, size = 'md' }: Props) {
  const sizes = { sm: 'w-3.5 h-3.5', md: 'w-5 h-5', lg: 'w-6 h-6' }
  return (
    <div className="flex items-center gap-0.5">
      {Array.from({ length: max }).map((_, i) => (
        <button key={i} type="button" disabled={!interactive}
          onClick={() => interactive && onRate?.(i + 1)}
          className={interactive ? 'cursor-pointer hover:scale-110 transition-transform' : 'cursor-default'}>
          <Star className={`${sizes[size]} ${i < Math.round(rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}`} />
        </button>
      ))}
    </div>
  )
}
