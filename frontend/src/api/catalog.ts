import api from '../lib/axios'

export interface PropertyListParams {
  page?: number
  size?: number
  city?: string
  propertyType?: string
  roomType?: string
  minPrice?: number
  maxPrice?: number
  q?: string
  /** Si ambas están definidas, el backend excluye propiedades sin disponibilidad en el rango. */
  checkIn?: string
  checkOut?: string
}

export const getProperties = (page = 0, size = 12, params?: Omit<PropertyListParams, 'page' | 'size'>) =>
  api.get('/properties', { params: { page, size, ...params } }).then((r) => r.data.data)

export const searchProperties = (params: PropertyListParams) =>
  api.get('/properties', { params: { ...params, size: params.size || 12 } }).then((r) => r.data.data)

export const getPropertyById = (id: string) => api.get(`/properties/${id}`).then((r) => r.data.data)
