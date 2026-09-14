import { reactive } from 'vue'

const TOKEN_KEY = 'bom_token'
const USER_KEY = 'bom_user'

/** 登录态：令牌与当前用户，持久化到 localStorage */
export const auth = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
})

export function setAuth(token, user) {
  auth.token = token
  auth.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth() {
  auth.token = ''
  auth.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const isAdmin = () => auth.user?.role === 'ADMIN'
export const isBuyer = () => ['ENGINEER', 'PLANNER'].includes(auth.user?.role)
export const isSupplier = () => false
export const canWrite = () => !!auth.user && auth.user.role !== 'VIEWER'
export const canWritePortal = canWrite
export const canEditMaster = canWrite

export const ROLE_LABEL = { ADMIN: '管理员', ENGINEER: '产品工程师', PLANNER: '制造工程师', VIEWER: '只读' }
export const fmt = (v) => (v ? String(v).replace('T', ' ').substring(0, 19) : '')
export const today = () => new Date().toISOString().substring(0, 10)
