import api from '../lib/axios'

export const getBooks = (page = 0, size = 12, genre?: string) =>
  api.get('/books', { params: { page, size, genre } }).then(r => r.data.data)

export const searchBooks = (params: { title?: string; author?: string; genre?: string; page?: number; size?: number }) =>
  api.get('/books/search', { params: { ...params, size: params.size || 12 } }).then(r => r.data.data)

export const getBookById = (id: string) =>
  api.get(`/books/${id}`).then(r => r.data.data)

export const getGenres = () =>
  api.get('/genres').then(r => r.data.data)
