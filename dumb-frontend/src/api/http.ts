import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import type { ApiResult } from '../types/models'

const API_SUCCESS_CODE = 0

const http = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const payload = res.data as ApiResult<unknown>
    if (typeof payload?.code === 'number' && payload.code !== API_SUCCESS_CODE) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload as any
  },
  (error) => {
    const message = error?.response?.data?.message || '请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
