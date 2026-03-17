<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { animeApi, movieApi, reviewApi } from '../../api'
import { useAuthStore } from '../../stores/auth'
import http from '../../api/http'
import type { AnimeItem, ApiResult, MovieItem, ReviewItem } from '../../types/models'
import RichEditor from '../../components/common/RichEditor.vue'

const REVIEW_DEFAULT_SCORE = 8.5
const PAGE_SIZE = 6
type AdminMenu = 'review' | 'news' | 'interview'

const REVIEW_GENRE_OPTIONS = [
  { label: '流行', value: 'POP' },
  { label: '摇滚', value: 'ROCK' },
  { label: '嘻哈', value: 'HIP_HOP' },
  { label: '电子', value: 'ELECTRONIC' },
  { label: '民谣', value: 'FOLK' },
  { label: 'R&B', value: 'RNB' },
  { label: '爵士', value: 'JAZZ' },
  { label: '古典', value: 'CLASSICAL' },
] as const

const ANIME_GENRE_OPTIONS = [
  '热血', '战斗', '格斗', '冒险', '王道', '少年', '少女', '青年', '后宫', '逆后宫',
  '恋爱', '纯爱', '青春', '校园', '治愈', '催泪', '悬疑', '推理', '侦探', '犯罪',
  '心理', '智斗', '黑暗', '致郁', '科幻', '未来', '赛博朋克', '机甲', '太空', '异世界',
  '穿越', '转生', '奇幻', '魔法', '超能力', '妖怪', '鬼怪', '神话', '武侠', '历史',
  '战争', '军事', '竞技', '运动', '音乐', '偶像', '歌舞', '搞笑', '日常', '美食',
  '职场', '社会', '政治', '家庭', '亲情', '友情', '成长', '生存', '末日', '丧尸',
  '灾难', '时间循环', '平行世界', '轮回', '重生', '系统流', '升级流', '种田流', '爽文', '轻小说改',
  '漫画改', '原创', '剧场版', 'OVA', '泡面番', '百合', 'BL', 'GL', '兄妹', '青梅竹马',
  '傲娇', '病娇', '天然呆', '腹黑', '反派', '反英雄', '暗黑英雄', '悬疑反转', '群像', '单元剧',
  '公路片', '蒸汽波', '赛博', '国风', '古风', '玄幻', '修真', '神作', '冷门', '神展开', '开放结局',
] as const

const MOVIE_GENRE_OPTIONS = [
  '剧情', '喜剧', '爱情', '动作', '冒险', '奇幻', '科幻', '悬疑', '惊悚', '恐怖',
  '犯罪', '黑帮', '警匪', '战争', '历史', '传记', '音乐', '歌舞', '西部', '武侠',
  '古装', '宫廷', '灾难', '末日', '丧尸', '怪兽', '怪谈', '灵异', '超自然', '心理',
  '家庭', '亲情', '青春', '校园', '成长', '体育', '竞技', '赛车', '公路', '美食',
  '职场', '商战', '政治', '间谍', '谍战', '法庭', '律政', '医疗', '军事', '海战',
  '空战', '太空', '机甲', '赛博朋克', '蒸汽朋克', '异世界', '穿越', '时间旅行', '平行世界', '轮回',
  '复仇', '黑色电影', '黑色幽默', '讽刺', '荒诞', '实验电影', '艺术电影', '独立电影', '纪录片', '纪录剧情片',
  '动画', '定格动画', '黏土动画', '3D动画', '短片', '微电影', '系列电影', '史诗片', '大片', 'B级片',
  'Cult片', '公路片', '歌剧电影', '音乐纪录片',
] as const

const MOVIE_REGION_OPTIONS = [
  '中国大陆',
  '中国香港',
  '中国台湾',
  '亚洲',
  '美洲',
  '欧洲',
  '非洲',
] as const

interface ReviewFormState {
  score: number
  albumTitle: string
  artist: string
  genres: string[]
  year: string
  tracks: string
  backgroundImage: string
  description: string
  coverImage: string
}

interface NewsFormState {
  title: string
  content: string
  coverImage: string
  backgroundImage: string
  score: number
  studio: string
  genres: string[]
  releaseDate: string
}

interface InterviewFormState {
  title: string
  content: string
  coverImage: string
  backgroundImage: string
  score: number
  director: string
  actors: string
  genres: string[]
  region: string
  releaseDate: string
}

const authStore = useAuthStore()
const activeMenu = ref<AdminMenu>('review')
const publishDialogVisible = ref(false)
const editingReviewId = ref<number | null>(null)
const editingNewsId = ref<number | null>(null)
const editingInterviewId = ref<number | null>(null)
const reviewRows = ref<ReviewItem[]>([])
const newsRows = ref<AnimeItem[]>([])
const interviewRows = ref<MovieItem[]>([])
const reviewListLoading = ref(false)
const newsListLoading = ref(false)
const interviewListLoading = ref(false)
const reviewSubmitting = ref(false)
const newsSubmitting = ref(false)
const interviewSubmitting = ref(false)
const coverUploading = ref(false)
const currentPage = ref(1)
const newsCurrentPage = ref(1)
const interviewCurrentPage = ref(1)
const BACKEND_ORIGIN = 'http://localhost:8080'

const reviewQuery = reactive({
  keyword: '',
  genre: '',
})

const newsQuery = reactive({
  keyword: '',
})

const interviewQuery = reactive({
  keyword: '',
})

const reviewForm = reactive<ReviewFormState>({
  score: REVIEW_DEFAULT_SCORE,
  albumTitle: '',
  artist: '',
  genres: [],
  year: '',
  tracks: '',
  backgroundImage: '',
  description: '',
  coverImage: '',
})
const newsForm = reactive<NewsFormState>({
  title: '',
  content: '',
  coverImage: '',
  backgroundImage: '',
  score: 8.0,
  studio: '',
  genres: [],
  releaseDate: '',
})
const interviewForm = reactive<InterviewFormState>({
  title: '',
  content: '',
  coverImage: '',
  backgroundImage: '',
  score: 8.0,
  director: '',
  actors: '',
  genres: [],
  region: '',
  releaseDate: '',
})

const pagedReviewRows = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return reviewRows.value.slice(start, start + PAGE_SIZE)
})

const pagedNewsRows = computed(() => {
  const start = (newsCurrentPage.value - 1) * PAGE_SIZE
  return newsRows.value.slice(start, start + PAGE_SIZE)
})

const pagedInterviewRows = computed(() => {
  const start = (interviewCurrentPage.value - 1) * PAGE_SIZE
  return interviewRows.value.slice(start, start + PAGE_SIZE)
})

function resolveCoverUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return `${BACKEND_ORIGIN}${url}`
  return url
}

function resetReviewForm() {
  reviewForm.score = REVIEW_DEFAULT_SCORE
  reviewForm.albumTitle = ''
  reviewForm.artist = ''
  reviewForm.genres = []
  reviewForm.year = ''
  reviewForm.tracks = ''
  reviewForm.backgroundImage = ''
  reviewForm.description = ''
  reviewForm.coverImage = ''
  editingReviewId.value = null
}

function resetNewsForm() {
  newsForm.title = ''
  newsForm.content = ''
  newsForm.coverImage = ''
  newsForm.backgroundImage = ''
  newsForm.score = 8.0
  newsForm.studio = ''
  newsForm.genres = []
  newsForm.releaseDate = ''
  editingNewsId.value = null
}

function resetInterviewForm() {
  interviewForm.title = ''
  interviewForm.content = ''
  interviewForm.coverImage = ''
  interviewForm.backgroundImage = ''
  interviewForm.score = 8.0
  interviewForm.director = ''
  interviewForm.actors = ''
  interviewForm.genres = []
  interviewForm.region = ''
  interviewForm.releaseDate = ''
  editingInterviewId.value = null
}

const DRAFT_KEY_PREFIX = 'dumb_admin_draft_'

function getDraftKey() {
  return `${DRAFT_KEY_PREFIX}${activeMenu.value}`
}

function saveDraft() {
  const key = getDraftKey()
  let data: any
  if (activeMenu.value === 'review') {
    data = { ...reviewForm }
  } else if (activeMenu.value === 'news') {
    data = { ...newsForm }
  } else {
    data = { ...interviewForm }
  }
  localStorage.setItem(key, JSON.stringify(data))
  ElMessage.success('草稿已保存')
}

function loadDraft(): boolean {
  const key = getDraftKey()
  const raw = localStorage.getItem(key)
  if (!raw) return false
  try {
    const data = JSON.parse(raw)
    if (activeMenu.value === 'review') {
      Object.assign(reviewForm, data)
    } else if (activeMenu.value === 'news') {
      Object.assign(newsForm, data)
    } else {
      Object.assign(interviewForm, data)
    }
    return true
  } catch {
    return false
  }
}

function clearDraft() {
  localStorage.removeItem(getDraftKey())
}

function isEditing() {
  if (activeMenu.value === 'review') return !!editingReviewId.value
  if (activeMenu.value === 'news') return !!editingNewsId.value
  return !!editingInterviewId.value
}

function onDialogClose() {
  if (!isEditing()) {
    saveDraft()
  }
}

function openPublishDialog() {
  if (activeMenu.value === 'review' && !editingReviewId.value) {
    resetReviewForm()
    loadDraft()
  } else if (activeMenu.value === 'news' && !editingNewsId.value) {
    resetNewsForm()
    loadDraft()
  } else if (activeMenu.value === 'interview' && !editingInterviewId.value) {
    resetInterviewForm()
    loadDraft()
  }
  publishDialogVisible.value = true
}

function parseReviewTitle(fullTitle: string) {
  const parts = fullTitle.split(' - ')
  if (parts.length >= 2) {
    return {
      artist: parts[0] ?? '',
      albumTitle: parts.slice(1).join(' - '),
    }
  }
  return {
    artist: '',
    albumTitle: fullTitle,
  }
}

function parseReviewContent(rawContent: string) {
  let genres: string[] = []
  let year = ''
  let tracks = ''
  let backgroundImage = ''
  let description = rawContent
  
  const lines = rawContent.split('\n')
  let processedLines: string[] = []
  
  for (const line of lines) {
    if (line.startsWith('风格：')) {
      const raw = line.replace('风格：', '').trim()
      genres = raw.split(',').map((s) => s.trim()).filter(Boolean)
    } else if (line.startsWith('年份：')) {
      year = line.replace('年份：', '').trim()
    } else if (line.startsWith('曲目：')) {
      tracks = line.replace('曲目：', '').trim()
    } else if (line.startsWith('背景：')) {
      backgroundImage = line.replace('背景：', '').trim()
    } else {
      processedLines.push(line)
    }
  }
  
  description = processedLines.join('\n').trim()
  return { genres, year, tracks, backgroundImage, description }
}

function stripHtmlText(value: string) {
  return value
    .replace(/<[^>]*>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

async function loadAdminReviews() {
  reviewListLoading.value = true
  try {
    const params: Record<string, string> = {}
    if (reviewQuery.keyword.trim()) params.keyword = reviewQuery.keyword.trim()
    if (reviewQuery.genre) params.genre = reviewQuery.genre
    const res = await reviewApi.adminList(params)
    reviewRows.value = res.data
    if (currentPage.value > Math.ceil((res.data.length || 1) / PAGE_SIZE)) {
      currentPage.value = 1
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载乐评列表失败')
  } finally {
    reviewListLoading.value = false
  }
}

async function loadAdminNews() {
  newsListLoading.value = true
  try {
    const params: Record<string, string> = {}
    if (newsQuery.keyword.trim()) params.keyword = newsQuery.keyword.trim()
    const res = await animeApi.adminList(params)
    newsRows.value = res.data
    if (newsCurrentPage.value > Math.ceil((res.data.length || 1) / PAGE_SIZE)) {
      newsCurrentPage.value = 1
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载动漫列表失败')
  } finally {
    newsListLoading.value = false
  }
}

async function loadAdminInterviews() {
  interviewListLoading.value = true
  try {
    const params: Record<string, string> = {}
    if (interviewQuery.keyword.trim()) params.keyword = interviewQuery.keyword.trim()
    const res = await movieApi.adminList(params)
    interviewRows.value = res.data
    if (interviewCurrentPage.value > Math.ceil((res.data.length || 1) / PAGE_SIZE)) {
      interviewCurrentPage.value = 1
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载电影列表失败')
  } finally {
    interviewListLoading.value = false
  }
}

function onReviewSearch() {
  currentPage.value = 1
  void loadAdminReviews()
}

function resetReviewSearch() {
  reviewQuery.keyword = ''
  reviewQuery.genre = ''
  currentPage.value = 1
  void loadAdminReviews()
}

function onNewsSearch() {
  newsCurrentPage.value = 1
  void loadAdminNews()
}

function resetNewsSearch() {
  newsQuery.keyword = ''
  newsCurrentPage.value = 1
  void loadAdminNews()
}

function onInterviewSearch() {
  interviewCurrentPage.value = 1
  void loadAdminInterviews()
}

function resetInterviewSearch() {
  interviewQuery.keyword = ''
  interviewCurrentPage.value = 1
  void loadAdminInterviews()
}

const beforeCoverUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isValidType = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  if (!isValidType) {
    ElMessage.error('仅支持 JPG/PNG 格式')
  }
  return isValidType
}

const uploadCover: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  const formData = new FormData()
  formData.append('file', file)
  coverUploading.value = true
  try {
    const res = await http.post<any, ApiResult<string>>('/admin/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (activeMenu.value === 'review') {
      reviewForm.coverImage = res.data
    } else if (activeMenu.value === 'news') {
      newsForm.coverImage = res.data
    } else if (activeMenu.value === 'interview') {
      interviewForm.coverImage = res.data
    }
    ElMessage.success('封面上传成功')
    options.onSuccess?.(res)
  } catch (e: any) {
    ElMessage.error(e?.message || '封面上传失败')
    options.onError?.(e)
  } finally {
    coverUploading.value = false
  }
}

const uploadNewsBackground: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  const formData = new FormData()
  formData.append('file', file)
  coverUploading.value = true
  try {
    const res = await http.post<any, ApiResult<string>>('/admin/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    newsForm.backgroundImage = res.data
    ElMessage.success('背景上传成功')
    options.onSuccess?.(res)
  } catch (e: any) {
    ElMessage.error(e?.message || '背景上传失败')
    options.onError?.(e)
  } finally {
    coverUploading.value = false
  }
}

const uploadReviewBackground: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  const formData = new FormData()
  formData.append('file', file)
  coverUploading.value = true
  try {
    const res = await http.post<any, ApiResult<string>>('/admin/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    reviewForm.backgroundImage = res.data
    ElMessage.success('背景上传成功')
    options.onSuccess?.(res)
  } catch (e: any) {
    ElMessage.error(e?.message || '背景上传失败')
    options.onError?.(e)
  } finally {
    coverUploading.value = false
  }
}

const uploadInterviewBackground: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  const formData = new FormData()
  formData.append('file', file)
  coverUploading.value = true
  try {
    const res = await http.post<any, ApiResult<string>>('/admin/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    interviewForm.backgroundImage = res.data
    ElMessage.success('背景上传成功')
    options.onSuccess?.(res)
  } catch (e: any) {
    ElMessage.error(e?.message || '背景上传失败')
    options.onError?.(e)
  } finally {
    coverUploading.value = false
  }
}

async function submitReview() {
  if (!reviewForm.albumTitle.trim()) {
    ElMessage.warning('请输入专辑名称')
    return
  }
  if (!reviewForm.artist.trim()) {
    ElMessage.warning('请输入艺人')
    return
  }
  if (!stripHtmlText(reviewForm.description)) {
    ElMessage.warning('请输入描述')
    return
  }

  let bgImage = reviewForm.backgroundImage
  if (!bgImage) {
    const targetArtist = reviewForm.artist.trim().toLowerCase()
    const sameArtist = reviewRows.value.find((r) => {
      return (r.artist || '').toLowerCase() === targetArtist && r.content
    })
    if (sameArtist?.content) {
      const match = sameArtist.content.match(/背景：(.+)/)
      const matchedBackground = match?.[1]?.trim()
      if (matchedBackground) bgImage = matchedBackground
    }
  }

  const payload = {
    title: `${reviewForm.artist} - ${reviewForm.albumTitle}`,
    content: `风格：${reviewForm.genres.join(',')}\n年份：${reviewForm.year}\n曲目：${reviewForm.tracks}\n背景：${bgImage}\n\n${reviewForm.description}`,
    score: reviewForm.score,
    coverImage: reviewForm.coverImage,
  }
  reviewSubmitting.value = true
  try {
    if (editingReviewId.value) {
      await reviewApi.update(editingReviewId.value, payload)
      ElMessage.success('乐评更新成功')
    } else {
      await reviewApi.create(payload)
      ElMessage.success('乐评发布成功')
    }
    clearDraft()
    publishDialogVisible.value = false
    resetReviewForm()
    await loadAdminReviews()
  } catch (e: any) {
    ElMessage.error(e?.message || '乐评提交失败')
  } finally {
    reviewSubmitting.value = false
  }
}

async function editReview(id: number) {
  try {
    const res = await reviewApi.adminDetail(id)
    const detail = res.data
    const titleInfo = parseReviewTitle(detail.title || '')
    const contentInfo = parseReviewContent(detail.content || '')
    reviewForm.score = Number(detail.score ?? REVIEW_DEFAULT_SCORE)
    reviewForm.coverImage = detail.coverImage || ''
    reviewForm.artist = titleInfo.artist
    reviewForm.albumTitle = titleInfo.albumTitle
    reviewForm.genres = contentInfo.genres
    reviewForm.year = contentInfo.year
    reviewForm.tracks = contentInfo.tracks
    reviewForm.backgroundImage = contentInfo.backgroundImage
    reviewForm.description = contentInfo.description
    editingReviewId.value = id
    activeMenu.value = 'review'
    publishDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '加载乐评详情失败')
  }
}

async function deleteReview(id: number) {
  try {
    await reviewApi.remove(id)
    ElMessage.success('乐评删除成功')
    if (editingReviewId.value === id) {
      resetReviewForm()
    }
    await loadAdminReviews()
  } catch (e: any) {
    ElMessage.error(e?.message || '乐评删除失败')
  }
}

async function submitNews() {
  if (!newsForm.title.trim()) {
    ElMessage.warning('请输入动漫名称')
    return
  }
  if (!newsForm.studio.trim()) {
    ElMessage.warning('请输入制片商')
    return
  }
  if (!stripHtmlText(newsForm.content)) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const payload = {
    title: newsForm.title,
    content: newsForm.content,
    coverImage: newsForm.coverImage,
    backgroundImage: newsForm.backgroundImage,
    score: newsForm.score,
    studio: newsForm.studio,
    genres: newsForm.genres,
    releaseDate: newsForm.releaseDate || null,
  }
  newsSubmitting.value = true
  try {
    if (editingNewsId.value) {
      await animeApi.update(editingNewsId.value, payload)
      ElMessage.success('动漫更新成功')
    } else {
      await animeApi.create(payload)
      ElMessage.success('动漫发布成功')
    }
    clearDraft()
    publishDialogVisible.value = false
    resetNewsForm()
    await loadAdminNews()
  } catch (e: any) {
    ElMessage.error(e?.message || '动漫提交失败')
  } finally {
    newsSubmitting.value = false
  }
}

async function editNews(id: number) {
  try {
    const res = await animeApi.adminDetail(id)
    const detail = res.data
    newsForm.title = detail.title || ''
    newsForm.content = detail.content || ''
    newsForm.coverImage = detail.coverImage || ''
    newsForm.backgroundImage = detail.backgroundImage || ''
    newsForm.score = Number(detail.score ?? 8.0)
    newsForm.studio = detail.studio || ''
    newsForm.genres = detail.genre ? String(detail.genre).split(',').map((item: string) => item.trim()).filter((item: string) => item) : []
    newsForm.releaseDate = detail.releaseDate || ''
    editingNewsId.value = id
    activeMenu.value = 'news'
    publishDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '加载动漫详情失败')
  }
}

async function deleteNews(id: number) {
  try {
    await animeApi.remove(id)
    ElMessage.success('动漫删除成功')
    if (editingNewsId.value === id) {
      resetNewsForm()
    }
    await loadAdminNews()
  } catch (e: any) {
    ElMessage.error(e?.message || '动漫删除失败')
  }
}

async function submitInterview() {
  if (!interviewForm.title.trim()) {
    ElMessage.warning('请输入电影名称')
    return
  }
  if (!interviewForm.director.trim()) {
    ElMessage.warning('请输入导演')
    return
  }
  if (!interviewForm.actors.trim()) {
    ElMessage.warning('请输入主演')
    return
  }
  if (!interviewForm.region.trim()) {
    ElMessage.warning('请选择电影地区')
    return
  }
  if (!stripHtmlText(interviewForm.content)) {
    ElMessage.warning('请输入简介')
    return
  }

  const payload = {
    title: interviewForm.title,
    content: interviewForm.content,
    coverImage: interviewForm.coverImage,
    backgroundImage: interviewForm.backgroundImage,
    score: interviewForm.score,
    director: interviewForm.director,
    actors: interviewForm.actors,
    genres: interviewForm.genres,
    region: interviewForm.region,
    releaseDate: interviewForm.releaseDate || null,
  }
  interviewSubmitting.value = true
  try {
    if (editingInterviewId.value) {
      await movieApi.update(editingInterviewId.value, payload)
      ElMessage.success('电影更新成功')
    } else {
      await movieApi.create(payload)
      ElMessage.success('电影发布成功')
    }
    clearDraft()
    publishDialogVisible.value = false
    resetInterviewForm()
    await loadAdminInterviews()
  } catch (e: any) {
    ElMessage.error(e?.message || '电影提交失败')
  } finally {
    interviewSubmitting.value = false
  }
}

async function editInterview(id: number) {
  try {
    const res = await movieApi.adminDetail(id)
    const detail = res.data
    interviewForm.title = detail.title || ''
    interviewForm.content = detail.content || ''
    interviewForm.coverImage = detail.coverImage || ''
    interviewForm.backgroundImage = detail.backgroundImage || ''
    interviewForm.score = Number(detail.score ?? 8.0)
    interviewForm.director = detail.director || ''
    interviewForm.actors = detail.actors || ''
    interviewForm.genres = detail.genre ? String(detail.genre).split(',').map((item: string) => item.trim()).filter((item: string) => item) : []
    interviewForm.region = detail.region || ''
    interviewForm.releaseDate = detail.releaseDate || ''
    editingInterviewId.value = id
    activeMenu.value = 'interview'
    publishDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '加载电影详情失败')
  }
}

async function deleteInterview(id: number) {
  try {
    await movieApi.remove(id)
    ElMessage.success('电影删除成功')
    if (editingInterviewId.value === id) {
      resetInterviewForm()
    }
    await loadAdminInterviews()
  } catch (e: any) {
    ElMessage.error(e?.message || '电影删除失败')
  }
}

function handleMenuSelect(key: string) {
  activeMenu.value = key as AdminMenu
  if (key === 'news' && newsRows.value.length === 0) {
    void loadAdminNews()
  } else if (key === 'interview' && interviewRows.value.length === 0) {
    void loadAdminInterviews()
  }
}

onMounted(() => {
  if (!authStore.isAdminLike) return
  void loadAdminReviews()
})
</script>

<template>
  <section class="admin-page">
    <header class="admin-header">
      <h1>后台管理</h1>
      <el-button type="primary" @click="openPublishDialog">发布</el-button>
    </header>

    <section class="admin-body">
      <aside class="admin-side">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="review">音乐管理</el-menu-item>
          <el-menu-item index="news">动漫管理</el-menu-item>
          <el-menu-item index="interview">电影管理</el-menu-item>
        </el-menu>
      </aside>

      <main class="admin-main">
        <template v-if="activeMenu === 'review'">
          <el-card class="admin-card">
            <div class="review-filters">
              <el-input v-model="reviewQuery.keyword" placeholder="关键词搜索（标题/艺人/专辑）" clearable />
              <el-select v-model="reviewQuery.genre" placeholder="风格筛选" clearable>
                <el-option v-for="item in REVIEW_GENRE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button type="primary" @click="onReviewSearch">搜索</el-button>
              <el-button @click="resetReviewSearch">重置</el-button>
            </div>
          </el-card>

          <el-card class="admin-card" v-loading="reviewListLoading">
            <div v-for="row in pagedReviewRows" :key="row.id" class="review-row">
              <div class="row-cover-wrap">
                <img class="row-cover" :src="resolveCoverUrl(row.coverImage)" alt="cover" />
              </div>
              <div class="row-info">
                <p><span class="label">专辑标题：</span>{{ row.albumTitle || '-' }}</p>
                <p><span class="label">艺人：</span>{{ row.artist || '-' }}</p>
                <p><span class="label">风格：</span>{{ row.genre || '-' }}</p>
                <p><span class="label">评分：</span>{{ row.score }}</p>
              </div>
              <div class="row-actions">
                <el-button type="primary" plain @click="editReview(row.id)">修改</el-button>
                <el-popconfirm title="确认删除该乐评吗？" @confirm="deleteReview(row.id)">
                  <template #reference>
                    <el-button type="danger" plain>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-if="!reviewListLoading && reviewRows.length === 0" description="暂无乐评数据" />
            <div class="pager" v-if="reviewRows.length > 0">
              <el-pagination
                v-model:current-page="currentPage"
                layout="prev, pager, next, total"
                :page-size="PAGE_SIZE"
                :total="reviewRows.length"
              />
            </div>
          </el-card>
        </template>

        <template v-else-if="activeMenu === 'news'">
          <el-card class="admin-card">
            <div class="review-filters">
              <el-input v-model="newsQuery.keyword" placeholder="关键词搜索（动漫名称/制片商/内容）" clearable style="flex: 1" />
              <el-button type="primary" @click="onNewsSearch">搜索</el-button>
              <el-button @click="resetNewsSearch">重置</el-button>
            </div>
          </el-card>

          <el-card class="admin-card" v-loading="newsListLoading">
            <div v-for="row in pagedNewsRows" :key="row.id" class="review-row">
              <div class="row-cover-wrap" v-if="row.coverImage">
                <img class="row-cover" :src="resolveCoverUrl(row.coverImage)" alt="cover" />
              </div>
              <div class="row-info" :style="row.coverImage ? {} : { gridColumn: '1 / span 2' }">
                <p><span class="label">动漫名称：</span>{{ row.title }}</p>
                <p><span class="label">制片商：</span>{{ row.studio || '-' }}</p>
                <p><span class="label">风格：</span>{{ row.genre || '-' }}</p>
                <p><span class="label">评分：</span>{{ Number(row.score || 0).toFixed(1) }}</p>
                <p><span class="label">发行年月：</span>{{ row.releaseDate || '-' }}</p>
              </div>
              <div class="row-actions">
                <el-button type="primary" plain @click="editNews(row.id)">修改</el-button>
                <el-popconfirm title="确认删除该动漫吗？" @confirm="deleteNews(row.id)">
                  <template #reference>
                    <el-button type="danger" plain>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-if="!newsListLoading && newsRows.length === 0" description="暂无动漫数据" />
            <div class="pager" v-if="newsRows.length > 0">
              <el-pagination
                v-model:current-page="newsCurrentPage"
                layout="prev, pager, next, total"
                :page-size="PAGE_SIZE"
                :total="newsRows.length"
              />
            </div>
          </el-card>
        </template>

        <template v-else-if="activeMenu === 'interview'">
          <el-card class="admin-card">
            <div class="review-filters">
              <el-input v-model="interviewQuery.keyword" placeholder="关键词搜索（电影名称/导演/主演/简介）" clearable style="flex: 1" />
              <el-button type="primary" @click="onInterviewSearch">搜索</el-button>
              <el-button @click="resetInterviewSearch">重置</el-button>
            </div>
          </el-card>

          <el-card class="admin-card" v-loading="interviewListLoading">
            <div v-for="row in pagedInterviewRows" :key="row.id" class="review-row">
              <div class="row-cover-wrap" v-if="row.coverImage">
                <img class="row-cover" :src="resolveCoverUrl(row.coverImage)" alt="cover" />
              </div>
              <div class="row-info" :style="row.coverImage ? {} : { gridColumn: '1 / span 2' }">
                <p><span class="label">电影名称：</span>{{ row.title || '-' }}</p>
                <p><span class="label">导演：</span>{{ row.director || '-' }}</p>
                <p><span class="label">主演：</span>{{ row.actors || '-' }}</p>
                <p><span class="label">风格：</span>{{ row.genre || '-' }}</p>
                <p><span class="label">地区：</span>{{ row.region || '-' }}</p>
                <p><span class="label">评分：</span>{{ Number(row.score || 0).toFixed(1) }}</p>
                <p><span class="label">上映：</span>{{ row.releaseDate || '-' }}</p>
              </div>
              <div class="row-actions">
                <el-button type="primary" plain @click="editInterview(row.id)">修改</el-button>
                <el-popconfirm title="确认删除该电影吗？" @confirm="deleteInterview(row.id)">
                  <template #reference>
                    <el-button type="danger" plain>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-if="!interviewListLoading && interviewRows.length === 0" description="暂无电影数据" />
            <div class="pager" v-if="interviewRows.length > 0">
              <el-pagination
                v-model:current-page="interviewCurrentPage"
                layout="prev, pager, next, total"
                :page-size="PAGE_SIZE"
                :total="interviewRows.length"
              />
            </div>
          </el-card>
        </template>
      </main>
    </section>

    <el-dialog
      v-model="publishDialogVisible"
      :title="activeMenu === 'review' ? (editingReviewId ? '编辑乐评' : '发布乐评') : activeMenu === 'news' ? (editingNewsId ? '编辑动漫' : '发布动漫') : (editingInterviewId ? '编辑电影' : '发布电影')"
      width="760px"
      @close="onDialogClose"
    >
      <el-form v-if="activeMenu === 'review'" label-position="top">
        <el-form-item label="专辑名称"><el-input v-model="reviewForm.albumTitle" /></el-form-item>
        <el-form-item label="专辑封面（支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCover"
          >
            <div class="cover-upload-box">
              <img v-if="reviewForm.coverImage" :src="resolveCoverUrl(reviewForm.coverImage)" alt="cover" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择封面</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="详情背景图（支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadReviewBackground"
          >
            <div class="cover-upload-box">
              <img v-if="reviewForm.backgroundImage" :src="resolveCoverUrl(reviewForm.backgroundImage)" alt="background" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择背景</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="艺人"><el-input v-model="reviewForm.artist" /></el-form-item>
        <el-form-item label="风格（可多选）">
          <el-checkbox-group v-model="reviewForm.genres" class="genre-checkbox-group">
            <el-checkbox v-for="item in REVIEW_GENRE_OPTIONS" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="年份"><el-input v-model="reviewForm.year" placeholder="例如：2024" /></el-form-item>
        <el-form-item label="音乐曲目（用 / 分隔）">
          <el-input v-model="reviewForm.tracks" placeholder="例如：Track 1 / Track 2 / Track 3" />
        </el-form-item>
        <el-form-item label="描述">
          <RichEditor v-model="reviewForm.description" placeholder="支持图文混排，可插入图片和视频链接" />
        </el-form-item>
      </el-form>

      <el-form v-else-if="activeMenu === 'news'" label-position="top">
        <el-form-item label="动漫名称"><el-input v-model="newsForm.title" /></el-form-item>
        <el-form-item label="封面（选填，支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCover"
          >
            <div class="cover-upload-box">
              <img v-if="newsForm.coverImage" :src="resolveCoverUrl(newsForm.coverImage)" alt="cover" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择封面</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="背景图（类似朋友圈封面，支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadNewsBackground"
          >
            <div class="cover-upload-box">
              <img v-if="newsForm.backgroundImage" :src="resolveCoverUrl(newsForm.backgroundImage)" alt="background" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择背景</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="评分（0-10分）">
          <el-input-number v-model="newsForm.score" :min="0" :max="10" :step="0.1" :precision="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="制片商"><el-input v-model="newsForm.studio" /></el-form-item>
        <el-form-item label="动漫风格（可多选）">
          <el-checkbox-group v-model="newsForm.genres" class="genre-checkbox-group">
            <el-checkbox v-for="item in ANIME_GENRE_OPTIONS" :key="item" :label="item">
              {{ item }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="发行年月">
          <el-date-picker v-model="newsForm.releaseDate" type="month" value-format="YYYY-MM-01" placeholder="请选择发行年月" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评论内容">
          <RichEditor v-model="newsForm.content" placeholder="支持图文混排，可插入图片和视频链接" />
        </el-form-item>
      </el-form>

      <el-form v-else label-position="top">
        <el-form-item label="电影名称"><el-input v-model="interviewForm.title" /></el-form-item>
        <el-form-item label="封面（选填，支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCover"
          >
            <div class="cover-upload-box">
              <img v-if="interviewForm.coverImage" :src="resolveCoverUrl(interviewForm.coverImage)" alt="cover" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择封面</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="详情背景图（支持 JPG/PNG）">
          <el-upload
            class="cover-uploader"
            :auto-upload="true"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png"
            :before-upload="beforeCoverUpload"
            :http-request="uploadInterviewBackground"
          >
            <div class="cover-upload-box">
              <img v-if="interviewForm.backgroundImage" :src="resolveCoverUrl(interviewForm.backgroundImage)" alt="background" class="cover-preview" />
              <span v-else-if="!coverUploading">点击选择背景</span>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="评分（0-10分）">
          <el-input-number v-model="interviewForm.score" :min="0" :max="10" :step="0.1" :precision="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="导演"><el-input v-model="interviewForm.director" /></el-form-item>
        <el-form-item label="主演"><el-input v-model="interviewForm.actors" placeholder="可填写多个主演，逗号分隔" /></el-form-item>
        <el-form-item label="电影地区">
          <el-select v-model="interviewForm.region" placeholder="请选择电影地区" style="width: 100%">
            <el-option v-for="item in MOVIE_REGION_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="电影风格（可多选）">
          <el-checkbox-group v-model="interviewForm.genres" class="genre-checkbox-group">
            <el-checkbox v-for="item in MOVIE_GENRE_OPTIONS" :key="item" :label="item">
              {{ item }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="上映日期">
          <el-date-picker v-model="interviewForm.releaseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择上映日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="简介">
          <RichEditor v-model="interviewForm.content" placeholder="支持图文混排，可插入图片和视频链接" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="saveDraft">保存草稿</el-button>
          <el-button v-if="activeMenu === 'review'" type="primary" :loading="reviewSubmitting" @click="submitReview">
            {{ editingReviewId ? '保存修改' : '发布' }}
          </el-button>
          <el-button v-else-if="activeMenu === 'news'" type="primary" :loading="newsSubmitting" @click="submitNews">
            {{ editingNewsId ? '保存修改' : '发布' }}
          </el-button>
          <el-button v-else type="primary" :loading="interviewSubmitting" @click="submitInterview">
            {{ editingInterviewId ? '保存修改' : '发布' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.admin-page { min-height: 100vh; background: var(--dumb-bg); color: var(--dumb-text); padding: 22px; }
.admin-header { position: relative; display: flex; justify-content: flex-end; align-items: center; margin-bottom: 18px; }
.admin-header h1 {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  font-size: 42px;
  letter-spacing: 4px;
}
.admin-body { display: grid; grid-template-columns: 220px 1fr; gap: 16px; }
.admin-side :deep(.el-menu) { border-right: none; background: var(--dumb-panel); border: 1px solid var(--dumb-border); border-radius: 10px; }
.admin-main { display: grid; gap: 12px; }
.admin-card { background: var(--dumb-panel); border-color: var(--dumb-border); }
.review-filters { display: grid; grid-template-columns: 1.2fr 220px auto auto; gap: 10px; align-items: center; }
.review-row { display: grid; grid-template-columns: 84px 1fr 140px; gap: 14px; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--dumb-border); }
.review-row:last-child { border-bottom: none; }
.row-cover-wrap { width: 84px; height: 84px; border: 1px solid var(--dumb-border); border-radius: 8px; overflow: hidden; }
.row-cover { width: 100%; height: 100%; object-fit: cover; }
.row-info { display: grid; gap: 4px; }
.row-info p { margin: 0; }
.label { color: var(--dumb-muted); }
.row-actions { display: grid; gap: 8px; justify-items: end; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.cover-uploader { width: 100%; }
.cover-upload-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 240px;
  height: 130px;
  border: 1px dashed var(--dumb-border);
  border-radius: 8px;
  color: var(--dumb-muted);
  cursor: pointer;
  overflow: hidden;
}
.cover-preview { width: 100%; height: 100%; object-fit: cover; }
.genre-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  max-height: 220px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid var(--dumb-border);
}
@media (max-width: 1100px) {
  .admin-body { grid-template-columns: 1fr; }
  .admin-header h1 { position: static; transform: none; }
  .admin-header { justify-content: space-between; }
  .review-filters { grid-template-columns: 1fr; }
  .review-row { grid-template-columns: 1fr; }
  .row-actions { justify-items: start; grid-auto-flow: column; justify-content: start; }
}
</style>
