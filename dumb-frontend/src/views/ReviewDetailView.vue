<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { commentApi, reviewApi } from '../api'
import type { CommentItem } from '../types/models'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const detail = ref<any>(null)
const comments = ref<CommentItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const content = ref('')
const rating = ref(8)
const reviewId = computed(() => Number(route.params.id))
const hasValidReviewId = computed(() => Number.isInteger(reviewId.value) && reviewId.value > 0)
const BACKEND_ORIGIN = 'http://localhost:8080'

const parsedContent = computed(() => {
  if (!detail.value?.content) {
    return { description: '', tracks: [] as string[], mvUrl: '', backgroundImage: '' }
  }
  const lines = String(detail.value.content).split('\n')
  let tracksRaw = ''
  let mvUrl = ''
  let backgroundImage = ''
  const remaining: string[] = []
  for (const line of lines) {
    if (line.startsWith('风格：') || line.startsWith('年份：')) continue
    if (line.startsWith('曲目：')) {
      tracksRaw = line.replace('曲目：', '').trim()
      continue
    }
    if (line.startsWith('MV：') || line.startsWith('mv：')) {
      mvUrl = line.replace(/^[Mm][Vv]：/, '').trim()
      continue
    }
    if (line.startsWith('背景：')) {
      backgroundImage = line.replace('背景：', '').trim()
      continue
    }
    remaining.push(line)
  }
  const tracks = tracksRaw.split('/').map((item) => item.trim()).filter((item) => item.length > 0)
  return { description: remaining.join('\n').trim(), tracks, mvUrl, backgroundImage }
})

// 解析 title "艺人 - 专辑名"
const parsedTitle = computed(() => {
  const raw = detail.value?.title || ''
  if (raw.includes(' - ')) {
    const parts = raw.split(' - ', 2)
    return { artist: parts[0].trim(), albumTitle: parts[1].trim() }
  }
  return { artist: '', albumTitle: raw }
})

function resolveCoverUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/mock-covers/')) return url
  if (url.startsWith('/')) return `${BACKEND_ORIGIN}${url}`
  return url
}

const hasVideoBg = computed(() => !!parsedContent.value.mvUrl)
const bgImageUrl = computed(() => {
  const bg = parsedContent.value.backgroundImage
  return resolveCoverUrl(bg || detail.value?.coverImage)
})

async function loadDetail() {
  if (!hasValidReviewId.value) return
  const detailRes = await reviewApi.detail(reviewId.value)
  detail.value = detailRes.data
}

async function loadComments() {
  if (!hasValidReviewId.value) return
  const commentRes = await commentApi.list('REVIEW', reviewId.value)
  comments.value = commentRes.data
}

async function init() {
  if (!hasValidReviewId.value) {
    ElMessage.warning('无效的乐评 ID')
    void router.replace('/music')
    return
  }
  loading.value = true
  try {
    await Promise.all([loadDetail(), loadComments()])
  } catch (e: any) {
    ElMessage.error(e?.message || '加载乐评详情失败')
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!content.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submitting.value = true
  try {
    await commentApi.create({ contentType: 'REVIEW', contentId: reviewId.value, content: content.value.trim(), score: rating.value })
    ElMessage.success('评论发布成功')
    content.value = ''
    rating.value = 8
    await loadComments()
  } catch (e: any) {
    ElMessage.error(e?.message || '发布评论失败')
  } finally {
    submitting.value = false
  }
}

async function deleteComment(commentId: number) {
  try {
    await commentApi.remove(commentId)
    ElMessage.success('评论删除成功')
    await loadComments()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除评论失败')
  }
}

onMounted(() => {
  void init()
})

watch(() => route.params.id, () => {
  void init()
})
</script>

<template>
  <el-skeleton :loading="loading" animated :rows="6">
    <div v-if="detail" class="music-detail">
      <!-- 全屏视频/图片背景 -->
      <div class="detail-bg">
        <video
          v-if="hasVideoBg"
          class="bg-video"
          :src="parsedContent.mvUrl"
          autoplay
          muted
          loop
          playsinline
        />
        <div v-else class="bg-image" :style="{ backgroundImage: `url(${bgImageUrl})` }" />
        <div class="bg-overlay" />
      </div>

      <!-- 返回按钮 -->
      <button class="back-btn" @click="router.push('/music')">←</button>

      <!-- 主内容 -->
      <div class="detail-content">
        <!-- 专辑信息区 -->
        <section class="album-hero">
          <div class="hero-cover">
            <img :src="resolveCoverUrl(detail.coverImage)" :alt="detail.albumTitle" />
          </div>
          <div class="hero-info">
            <h1 class="info-title">{{ parsedTitle.albumTitle }}</h1>
            <p class="info-artist">{{ parsedTitle.artist }}</p>
            <div class="info-score-row">
              <span class="info-stars">★ {{ Number(detail.score || 0).toFixed(1) }}</span>
              <span class="info-sep">·</span>
              <span class="info-year">{{ detail.releaseDate || '' }}</span>
            </div>
          </div>
        </section>

        <!-- 曲目 + 正文 + 评论 -->
        <div class="detail-body">
          <div class="body-main">
            <!-- 乐评正文 -->
            <article class="review-text" v-html="parsedContent.description" />

            <!-- 评论区 -->
            <section class="comment-section">
              <h3>评论</h3>
              <el-empty v-if="comments.length === 0" description="还没有评论" />
              <ul v-else class="comment-list">
                <li v-for="item in comments" :key="item.id" class="comment-item">
                  <div class="comment-head">
                    <span>{{ item.username || `用户${item.userId}` }}</span>
                    <div class="comment-actions">
                      <span v-if="item.score !== undefined && item.score !== null" class="comment-score">评分 {{ item.score }}</span>
                      <el-button v-if="authStore.isLoggedIn" type="danger" link size="small" @click="deleteComment(item.id)">删除</el-button>
                    </div>
                  </div>
                  <p class="comment-content">{{ item.content }}</p>
                </li>
              </ul>

              <template v-if="authStore.isLoggedIn">
                <div class="rating-row">
                  <span>评分</span>
                  <el-rate v-model="rating" :max="10" :allow-half="true" />
                  <span class="rating-value">{{ rating.toFixed(1) }}</span>
                </div>
                <el-input v-model="content" type="textarea" :rows="3" placeholder="写下你的观点..." />
                <el-button type="primary" :loading="submitting" @click="submitComment">发布</el-button>
              </template>
              <el-alert v-else title="登录后可评论" type="info" :closable="false" />
            </section>
          </div>

          <!-- 侧边曲目列表 -->
          <aside class="body-sidebar">
            <div class="sidebar-card" v-if="parsedContent.tracks.length > 0">
              <h4>曲目</h4>
              <ol class="track-list">
                <li v-for="(track, idx) in parsedContent.tracks" :key="idx">{{ track }}</li>
              </ol>
            </div>
          </aside>
        </div>
      </div>
    </div>
  </el-skeleton>
</template>

<style scoped>
.music-detail {
  position: relative;
  min-height: 100vh;
  color: #ffffff;
}

/* ====== 全屏背景 ====== */
.detail-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.bg-video,
.bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bg-image {
  background-size: cover;
  background-position: center;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(to bottom, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.6) 50%, rgba(0,0,0,0.95) 100%);
}

/* ====== 返回按钮 ====== */
.back-btn {
  position: fixed;
  top: 20px;
  left: 24px;
  z-index: 10;
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.7);
  font-family: 'Inter', sans-serif;
  font-size: 1.4rem;
  cursor: pointer;
  transition: color 0.2s;
  line-height: 1;
}
.back-btn:hover {
  color: #ffffff;
}

/* ====== 主内容 ====== */
.detail-content {
  position: relative;
  z-index: 1;
  padding: 80px 40px 60px;
  max-width: 1100px;
  margin: 0 auto;
}

/* ====== 专辑信息区 ====== */
.album-hero {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 60px;
}

.hero-cover {
  flex-shrink: 0;
  width: 280px;
  height: 280px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.6), 0 0 0 1px rgba(255,255,255,0.05);
}
.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-info {
  padding-bottom: 8px;
}

.info-title {
  margin: 0 0 8px;
  font-family: 'Playfair Display', serif;
  font-size: 3rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.1;
}

.info-artist {
  margin: 0 0 16px;
  font-family: 'Inter', sans-serif;
  font-size: 1.1rem;
  font-weight: 300;
  color: rgba(255,255,255,0.7);
}

.info-score-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-stars {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
}

.info-sep {
  color: rgba(255,255,255,0.3);
}

.info-year {
  font-family: 'Inter', sans-serif;
  font-size: 0.9rem;
  color: rgba(255,255,255,0.5);
}

/* ====== 下方内容区 ====== */
.detail-body {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 40px;
}

/* 正文 */
.review-text {
  line-height: 1.9;
  font-size: 1rem;
  color: rgba(255,255,255,0.85);
  margin-bottom: 48px;
  white-space: pre-wrap;
}
.review-text :deep(p) {
  margin: 0 0 1.2em;
}
.review-text :deep(h2),
.review-text :deep(h3) {
  font-family: 'Playfair Display', serif;
  margin: 1.5em 0 0.5em;
}

/* 评论区 */
.comment-section {
  padding-top: 24px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.comment-section h3 {
  margin: 0 0 20px;
  font-family: 'Playfair Display', serif;
  font-size: 1.3rem;
  font-weight: 600;
}

.comment-list {
  display: grid;
  gap: 12px;
  margin: 0 0 24px;
  padding: 0;
  list-style: none;
}
.comment-item {
  padding: 14px 16px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 8px;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 0.82rem;
  color: rgba(255,255,255,0.5);
}
.comment-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.comment-score {
  color: #ffffff;
}
.comment-content {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: rgba(255,255,255,0.8);
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.rating-value {
  color: rgba(255,255,255,0.5);
  font-size: 0.85rem;
}

/* 侧边栏 */
.body-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  padding: 20px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 10px;
  backdrop-filter: blur(8px);
}
.sidebar-card h4 {
  margin: 0 0 14px;
  font-family: 'Playfair Display', serif;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.track-list {
  margin: 0;
  padding: 0 0 0 18px;
  display: grid;
  gap: 6px;
}
.track-list li {
  font-size: 0.82rem;
  color: rgba(255,255,255,0.65);
  line-height: 1.5;
}

.meta-list {
  display: grid;
  gap: 10px;
}
.meta-list p {
  display: flex;
  justify-content: space-between;
  margin: 0;
  font-size: 0.82rem;
}
.meta-list p span:first-child {
  color: rgba(255,255,255,0.4);
}
.meta-list p span:last-child {
  color: rgba(255,255,255,0.8);
}

/* 响应式 */
@media (max-width: 900px) {
  .album-hero {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 24px;
    min-height: auto;
    margin-bottom: 40px;
  }
  .hero-cover {
    width: 220px;
    height: 220px;
  }
  .info-title {
    font-size: 2rem;
  }
  .detail-body {
    grid-template-columns: 1fr;
  }
  .body-sidebar {
    order: -1;
    flex-direction: row;
    flex-wrap: wrap;
  }
  .sidebar-card {
    flex: 1;
    min-width: 200px;
  }
}

@media (max-width: 600px) {
  .detail-content {
    padding: 60px 20px 40px;
  }
  .hero-cover {
    width: 180px;
    height: 180px;
  }
  .info-title {
    font-size: 1.6rem;
  }
}
</style>
