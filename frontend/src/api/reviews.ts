import api from '../lib/axios'

export const getBookReviews = (bookId: string) =>
  api.get(`/reviews/book/${bookId}`).then(r => r.data.data)
export const getBookRating = (bookId: string) =>
  api.get(`/reviews/book/${bookId}/rating`).then(r => r.data.data)
export const createReview = (bookId: string, rating: number, comment: string, username: string) =>
  api.post('/reviews', { bookId, rating, comment, username }).then(r => r.data.data)
export const updateReview = (id: number, rating: number, comment: string) =>
  api.put(`/reviews/${id}`, { rating, comment }).then(r => r.data.data)
export const deleteReview = (id: number) => api.delete(`/reviews/${id}`)
export const getMyReviews = () => api.get('/reviews/my').then(r => r.data.data)
