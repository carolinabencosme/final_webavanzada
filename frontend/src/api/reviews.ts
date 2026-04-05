import api from '../lib/axios'

export const getPropertyReviews = (propertyId: string) =>
  api.get(`/reviews/property/${propertyId}`).then((r) => r.data.data)

export const getPropertyRating = (propertyId: string) =>
  api.get(`/reviews/property/${propertyId}/rating`).then((r) => r.data.data)

export const createReview = (userId: string, propertyId: string, rating: number, comment: string, userEmail: string) =>
  api.post(`/reviews/${userId}`, { propertyId, rating, comment, userEmail }).then((r) => r.data.data)
