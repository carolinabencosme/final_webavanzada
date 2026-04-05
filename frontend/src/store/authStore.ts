export interface AuthUser {
  userId: string
  username: string
  email: string
  role: 'ADMIN' | 'CLIENT'
  token: string
}

export const getUser = (): AuthUser | null => {
  try {
    let raw = localStorage.getItem('luma_user')
    if (!raw) {
      raw = localStorage.getItem('bookstore_user')
      const oldTok = localStorage.getItem('bookstore_token')
      if (raw && oldTok) {
        localStorage.setItem('luma_user', raw)
        localStorage.setItem('luma_token', oldTok)
        localStorage.removeItem('bookstore_user')
        localStorage.removeItem('bookstore_token')
      }
    }
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const setUser = (user: AuthUser) => {
  localStorage.setItem('luma_token', user.token)
  localStorage.setItem('luma_user', JSON.stringify(user))
}

export const clearUser = () => {
  localStorage.removeItem('luma_token')
  localStorage.removeItem('luma_user')
}

export const isAdmin = () => getUser()?.role === 'ADMIN'
export const isLoggedIn = () => !!getUser()
