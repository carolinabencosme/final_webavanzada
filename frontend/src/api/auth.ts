import api from '../lib/axios'

export const login = (email: string, password: string) =>
  api.post('/auth/login', { email, password }).then(r => r.data.data)

export const register = (username: string, email: string, password: string) =>
  api.post('/auth/register', { username, email, password }).then(r => r.data.data)

export const getMe = () => api.get('/auth/me').then(r => r.data.data)

export interface UpdateProfileBody {
  username?: string
  email?: string
  currentPassword?: string
  newPassword?: string
}

export const updateProfile = (data: UpdateProfileBody) =>
  api.put('/users/me', data).then(r => r.data.data)

export const getUsers = () => api.get('/users').then(r => r.data.data)

export interface AdminCreateUserBody {
  username: string
  email: string
  password: string
  role?: 'CLIENT' | 'ADMIN'
  active?: boolean
  sendWelcomeEmail?: boolean
}

export const createUser = (data: AdminCreateUserBody) =>
  api.post('/users', data).then(r => r.data.data)

export const updateUser = (id: string, data: unknown) =>
  api.put(`/users/${id}`, data).then(r => r.data.data)

export const deleteUser = (id: string) => api.delete(`/users/${id}`)
