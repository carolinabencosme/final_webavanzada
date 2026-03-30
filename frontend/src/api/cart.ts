import api from '../lib/axios'

/** Identidad del usuario viene del JWT vía API Gateway (cabeceras X-User-Id). */
export const getCart = () =>
  api.get('/cart/items').then((r) => {
    const raw = r.data.data
    const items = Array.isArray(raw) ? raw : []
    return { items }
  })

export const addToCart = (bookId: string, quantity = 1) =>
  api.post('/cart/items', { bookId, quantity }).then((r) => r.data.data)

export const updateCartItem = (itemId: number, quantity: number) =>
  api.put(`/cart/items/${itemId}`, null, { params: { quantity } }).then((r) => r.data.data)

export const removeCartItem = (itemId: number) =>
  api.delete(`/cart/items/${itemId}`).then((r) => r.data.data)

export const clearCart = () => api.delete('/cart/items').then((r) => r.data.data)
