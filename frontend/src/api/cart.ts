import api from '../lib/axios'

export const getCart = () => api.get('/cart').then(r => r.data.data)
export const addToCart = (bookId: string, quantity = 1) =>
  api.post('/cart/items', { bookId, quantity }).then(r => r.data.data)
export const updateCartItem = (itemId: number, quantity: number) =>
  api.put(`/cart/items/${itemId}`, { quantity }).then(r => r.data.data)
export const removeCartItem = (itemId: number) =>
  api.delete(`/cart/items/${itemId}`).then(r => r.data.data)
export const clearCart = () => api.delete('/cart')
