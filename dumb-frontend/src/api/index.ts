import http from './http'
import type { ApiResult, AnimeItem, AuthResponse, CommentItem, MovieItem, ReviewItem } from '../types/models'

export const authApi = {
  login(payload: { username: string; password: string }) {
    return http.post<any, ApiResult<AuthResponse>>('/auth/login', payload)
  },
  register(payload: { username: string; password: string; email: string }) {
    return http.post<any, ApiResult<AuthResponse>>('/auth/register', payload)
  },
}

export const reviewApi = {
  list(params?: Record<string, string | number>) {
    return http.get<any, ApiResult<ReviewItem[]>>('/reviews', { params })
  },
  detail(id: number) {
    return http.get<any, ApiResult<any>>(`/reviews/${id}`)
  },
  create(payload: Record<string, unknown>) {
    return http.post<any, ApiResult<any>>('/admin/reviews', payload)
  },
  adminList(params?: Record<string, string | number>) {
    return http.get<any, ApiResult<ReviewItem[]>>('/admin/reviews', { params })
  },
  adminDetail(id: number) {
    return http.get<any, ApiResult<any>>(`/admin/reviews/${id}`)
  },
  update(id: number, payload: Record<string, unknown>) {
    return http.put<any, ApiResult<any>>(`/admin/reviews/${id}`, payload)
  },
  remove(id: number) {
    return http.delete<any, ApiResult<any>>(`/admin/reviews/${id}`)
  },
}

export const animeApi = {
  list() {
    return http.get<any, ApiResult<AnimeItem[]>>('/anime')
  },
  detail(id: number) {
    return http.get<any, ApiResult<AnimeItem>>(`/anime/${id}`)
  },
  create(payload: Record<string, unknown>) {
    return http.post<any, ApiResult<any>>('/admin/anime', payload)
  },
  adminList(params?: Record<string, string | number>) {
    return http.get<any, ApiResult<AnimeItem[]>>('/admin/anime', { params })
  },
  adminDetail(id: number) {
    return http.get<any, ApiResult<any>>(`/admin/anime/${id}`)
  },
  update(id: number, payload: Record<string, unknown>) {
    return http.put<any, ApiResult<any>>(`/admin/anime/${id}`, payload)
  },
  remove(id: number) {
    return http.delete<any, ApiResult<any>>(`/admin/anime/${id}`)
  },
}

export const movieApi = {
  list(params?: Record<string, string | number>) {
    return http.get<any, ApiResult<MovieItem[]>>('/movie', { params })
  },
  detail(id: number) {
    return http.get<any, ApiResult<MovieItem>>(`/movie/${id}`)
  },
  create(payload: Record<string, unknown>) {
    return http.post<any, ApiResult<any>>('/admin/movie', payload)
  },
  adminList(params?: Record<string, string | number>) {
    return http.get<any, ApiResult<MovieItem[]>>('/admin/movie', { params })
  },
  adminDetail(id: number) {
    return http.get<any, ApiResult<any>>(`/admin/movie/${id}`)
  },
  update(id: number, payload: Record<string, unknown>) {
    return http.put<any, ApiResult<any>>(`/admin/movie/${id}`, payload)
  },
  remove(id: number) {
    return http.delete<any, ApiResult<any>>(`/admin/movie/${id}`)
  },
}

export const commentApi = {
  list(contentType: string, contentId: number) {
    return http.get<any, ApiResult<CommentItem[]>>('/comments', { params: { contentType, contentId } })
  },
  create(payload: Record<string, unknown>) {
    return http.post<any, ApiResult<CommentItem>>('/comments', payload)
  },
  remove(id: number) {
    return http.delete<any, ApiResult<any>>(`/comments/${id}`)
  },
}
