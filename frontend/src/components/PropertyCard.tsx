import { Link } from 'react-router-dom'
import { MapPin, Star } from 'lucide-react'
import { pickStayPlaceholder } from '../lib/placeholderImages'

export interface PropertyCardModel {
  id: string
  name: string
  city?: string
  propertyType?: string
  roomType?: string
  pricePerNight?: number | string
  imageUrl?: string
  averageRating?: number
}

type Props = { property: PropertyCardModel }

export default function PropertyCard({ property: p }: Props) {
  const img = p.imageUrl || pickStayPlaceholder(p.id)
  const price = typeof p.pricePerNight === 'number' ? p.pricePerNight : Number(p.pricePerNight ?? 0)
  const fallback = pickStayPlaceholder(p.id)

  return (
    <Link
      to={`/properties/${p.id}`}
      className="card flex flex-col flex-1 overflow-hidden transition-all duration-300 hover:shadow-lift hover:-translate-y-0.5 group"
    >
      <div className="aspect-[4/3] bg-paper-deep overflow-hidden relative">
        <img
          src={img}
          alt=""
          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-[1.04]"
          onError={(e) => {
            e.currentTarget.src = fallback
          }}
        />
        {p.propertyType && (
          <span className="absolute top-3 left-3 badge bg-white/95 backdrop-blur-md shadow-sm">{p.propertyType}</span>
        )}
      </div>
      <div className="p-5 flex flex-col flex-1">
        <div className="flex items-start justify-between gap-3">
          <h3 className="font-serif text-xl text-ink font-semibold leading-snug line-clamp-2">{p.name}</h3>
        </div>
        {p.city && (
          <p className="mt-2 text-sm text-ink-muted flex items-center gap-1.5">
            <MapPin className="w-3.5 h-3.5 shrink-0 text-primary-600" strokeWidth={1.5} />
            {p.city}
            {p.roomType ? ` · ${p.roomType}` : ''}
          </p>
        )}
        <div className="mt-4 flex items-center justify-between gap-3 mt-auto pt-4 border-t border-stone-100">
          <div className="flex items-center gap-1.5 text-sm text-ink-muted tabular-nums">
            <Star className="w-4 h-4 fill-gold-light text-gold-light" strokeWidth={0} />
            <span>{p.averageRating != null ? p.averageRating.toFixed(1) : '—'}</span>
          </div>
          <p className="font-serif text-xl font-semibold text-primary-700">
            ${price.toFixed(2)}
            <span className="text-xs font-sans font-normal text-ink-muted"> / noche</span>
          </p>
        </div>
      </div>
    </Link>
  )
}
