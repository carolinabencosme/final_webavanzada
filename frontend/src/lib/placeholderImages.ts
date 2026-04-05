/** Fotos de prueba variadas (Unsplash) para cards y fallbacks */
export const STAY_PLACEHOLDER_IMAGES = [
  'https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&q=85&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1602002418082-a4443e081dd1?w=800&q=85&auto=format&fit=crop',
] as const

export function pickStayPlaceholder(id?: string): string {
  if (!id) return STAY_PLACEHOLDER_IMAGES[0]
  let h = 0
  for (let i = 0; i < id.length; i++) h = (h + id.charCodeAt(i) * (i + 1)) % 2147483647
  return STAY_PLACEHOLDER_IMAGES[Math.abs(h) % STAY_PLACEHOLDER_IMAGES.length]
}

export const HERO_IMAGES = {
  main: 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=1400&q=85&auto=format&fit=crop',
  tileA: 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=600&q=85&auto=format&fit=crop',
  tileB: 'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=600&q=85&auto=format&fit=crop',
  tileC: 'https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?w=600&q=85&auto=format&fit=crop',
  mosaic: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=1200&q=85&auto=format&fit=crop',
} as const
