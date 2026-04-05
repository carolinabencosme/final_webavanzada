import api from '../lib/axios'

export const getCart = () => api.get('/cart/items').then((r) => r.data.data)

export const addToCart = (propertyId: string, checkIn: string, checkOut: string, guests = 2) =>
  api.post('/cart/items', { propertyId, checkIn, checkOut, guests }).then((r) => r.data.data)

export const removeCartItem = (itemId: number) => api.delete(`/cart/items/${itemId}`).then((r) => r.data.data)

export const clearCart = () => api.delete('/cart/items').then((r) => r.data.data)
