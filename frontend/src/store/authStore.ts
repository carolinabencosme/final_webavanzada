export interface AuthUser {
  userId: string
  username: string
  email: string
  role: 'ADMIN' | 'CLIENT'
  token: string
}

export const getUser = (): AuthUser | null => {
  try {
    const u = localStorage.getItem('bookstore_user')
    return u ? JSON.parse(u) : null
  } catch { return null }
}

export const setUser = (user: AuthUser) => {
  localStorage.setItem('bookstore_token', user.token)
  localStorage.setItem('bookstore_user', JSON.stringify(user))
}

export const clearUser = () => {
  localStorage.removeItem('bookstore_token')
  localStorage.removeItem('bookstore_user')
}

export const isAdmin = () => getUser()?.role === 'ADMIN'
export const isLoggedIn = () => !!getUser()
