import api from '../lib/axios'
import { getUser } from '../store/authStore'

function uid(): string {
  const u = getUser()
  if (!u?.userId) throw new Error('Not authenticated')
  return u.userId
}

/** Backend returns a list; we normalize to `{ items }` for the UI. */
export const getCart = () =>
  api.get(`/cart/${uid()}`).then((r) => {
    const raw = r.data.data
    const items = Array.isArray(raw) ? raw : []
    return { items }
  })

export const addToCart = (bookId: string, quantity = 1) =>
  api.post(`/cart/${uid()}`, { bookId, quantity }).then((r) => r.data.data)

export const updateCartItem = (itemId: number, quantity: number) =>
  api
    .put(`/cart/${uid()}/items/${itemId}`, null, { params: { quantity } })
    .then((r) => r.data.data)

export const removeCartItem = (itemId: number) =>
  api.delete(`/cart/${uid()}/items/${itemId}`).then((r) => r.data.data)

export const clearCart = () => api.delete(`/cart/${uid()}`).then((r) => r.data.data)
