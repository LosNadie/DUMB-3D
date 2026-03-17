export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface AuthResponse {
  token: string
  username: string
  role: string
}

export interface ReviewItem {
  id: number
  albumId: number
  title: string
  content?: string
  coverImage: string
  score: number
  authorName: string
  albumTitle: string
  artist: string
  genre: string
  releaseDate: string
  publishTime: string
  updateTime?: string
}

export interface NewsItem {
  id: number
  title: string
  content: string
  coverImage: string
  category: string
  publishTime: string
  views: number
}

export interface AnimeItem {
  id: number
  title: string
  content: string
  coverImage: string
  backgroundImage?: string
  score: number
  studio: string
  genre?: string
  releaseDate?: string
  publishTime: string
  updateTime?: string
  views: number
}

export interface InterviewItem {
  id: number
  artistName: string
  title: string
  content: string
  coverImage: string
  publishTime: string
}

export interface MovieItem {
  id: number
  title: string
  content: string
  coverImage: string
  backgroundImage?: string
  score: number
  director: string
  actors: string
  genre?: string
  region?: string
  releaseDate?: string
  publishTime: string
  updateTime?: string
  views: number
}

export interface CommentItem {
  id: number
  contentType: string
  contentId: number
  userId: number
  username?: string
  content: string
  score?: number
  parentId?: number
  createTime: string
}
