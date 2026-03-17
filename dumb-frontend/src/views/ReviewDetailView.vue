<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { commentApi, reviewApi } from '../api'
import type { CommentItem } from '../types/models'
import { useAuthStore } from '../stores/auth'
import ScoreBar from '../components/common/ScoreBar.vue'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'
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
    return { description: '', tracks: [] as string[], backgroundImage: '' }
  }
  const lines = String(detail.value.content).split('\n')
  let tracksRaw = ''
  let backgroundImage = ''
  const remaining: string[] = []
  for (const line of lines) {
    if (line.startsWith('风格：') || line.startsWith('年份：')) {
      continue
    }
    if (line.startsWith('曲目：')) {
      tracksRaw = line.replace('曲目：', '').trim()
      continue
    }
    if (line.startsWith('背景：')) {
      backgroundImage = line.replace('背景：', '').trim()
      continue
    }
    remaining.push(line)
  }
  const tracks = tracksRaw
    .split('/')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
  return {
    description: remaining.join('\n').trim(),
    tracks,
    backgroundImage,
  }
})

function resolveCoverUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return `${BACKEND_ORIGIN}${url}`
  return url
}

const heroBackgroundStyle = computed(() => {
  const bg = parsedContent.value.backgroundImage
  const resolved = resolveCoverUrl(bg || detail.value?.coverImage)
  if (!resolved) return {}
  return {
    backgroundImage: `url('${resolved}')`,
  }
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

watch(
  () => route.params.id,
  () => {
    void init()
  },
)
</script>

<template>
  <el-skeleton :loading="loading" animated :rows="6">
    <div v-if="detail" class="review-detail-page">
      <FlowSceneBackdrop />
      <section class="hero-banner" :style="heroBackgroundStyle"></section>
      <div class="review-detail-container">
        <article class="review-content">
          <h1>{{ detail.title }}</h1>
          <ScoreBar :score="Number(detail.score)" />
          <div class="content" v-html="parsedContent.description"></div>
        </article>

        <aside class="review-sidebar">
          <div class="album-cover-box">
            <img
              v-if="detail.coverImage"
              :src="resolveCoverUrl(detail.coverImage)"
              alt="专辑封面"
              class="album-cover"
            />
            <div v-else class="no-cover">暂无封面</div>
          </div>
          <div class="album-info">
            <p class="track-title">专辑曲目</p>
            <ul v-if="parsedContent.tracks.length > 0" class="track-list">
              <li v-for="(item, index) in parsedContent.tracks" :key="`${index}-${item}`">{{ item }}</li>
            </ul>
            <p v-else class="track-empty">暂无曲目信息</p>
          </div>
        </aside>
      </div>

      <section class="comment-box">
        <h3>评论区</h3>
        <el-empty v-if="comments.length === 0" description="还没有评论" />
        <ul v-else class="comment-list">
          <li v-for="item in comments" :key="item.id" class="comment-item">
            <div class="comment-head">
              <span>{{ item.username || `用户${item.userId}` }}</span>
              <div class="comment-actions">
                <span v-if="item.score !== undefined && item.score !== null" class="comment-score">评分：{{ item.score }}</span>
                <el-button
                  v-if="authStore.isLoggedIn"
                  type="danger"
                  link
                  size="small"
                  @click="deleteComment(item.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
            <p class="comment-content">{{ item.content }}</p>
          </li>
        </ul>
        <template v-if="authStore.isLoggedIn">
          <div class="rating-row">
            <span>你的评分：</span>
            <el-rate v-model="rating" :max="10" :allow-half="true" />
            <span class="rating-value">{{ rating.toFixed(1) }}</span>
          </div>
          <el-input v-model="content" type="textarea" :rows="3" placeholder="写下你的观点..." />
          <el-button type="primary" :loading="submitting" @click="submitComment">发布评论</el-button>
        </template>
        <el-alert v-else title="登录后可评论" type="info" :closable="false" />
      </section>
    </div>
  </el-skeleton>
</template>

<style scoped>
.review-detail-page {
  position: relative;
  display: grid;
  gap: 0;
}

.review-detail-page > :not(.flow-scene-backdrop) {
  position: relative;
  z-index: 2;
}

.hero-banner {
  min-height: 360px;
  border-top: none;
  border-bottom: none;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center center;
  background-color: transparent;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  margin-bottom: 22px;
}

.review-detail-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  margin-bottom: 40px;
}

.review-content {
  min-width: 0;
}

.review-sidebar {
  display: grid;
  gap: 16px;
  align-content: start;
}

.album-cover-box {
  width: 100%;
  aspect-ratio: 1;
  border: none;
  border-radius: 0;
  overflow: hidden;
  background: transparent;
}

.album-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dumb-muted);
  font-size: 14px;
}

.album-info {
  padding: 16px;
  background: transparent;
  border: none;
  border-radius: 0;
}

.album-info p {
  margin: 0;
  padding: 8px 0;
  border-bottom: none;
}

.album-info p:last-child {
  border-bottom: none;
}

.track-title { font-weight: 600; font-size: 15px; color: #fff; margin-bottom: 6px !important; }
.track-list { margin: 0; padding-left: 18px; display: grid; gap: 6px; color: var(--dumb-muted); font-size: 13px; }
.track-list li { line-height: 1.5; }
.track-empty { color: var(--dumb-muted); font-size: 13px; }

.meta { color: var(--dumb-muted); }
.content { line-height: 1.8; margin: 12px 0 30px; white-space: pre-wrap; }
.comment-box { display: grid; gap: 10px; margin-top: 24px; }
.comment-list { display: grid; gap: 8px; }
.comment-item { padding: 10px; border: none; border-radius: 0; background: transparent; }
.comment-head { display: flex; justify-content: space-between; color: var(--dumb-muted); font-size: 13px; }
.comment-actions { display: flex; align-items: center; gap: 8px; }
.comment-score { color: var(--dumb-accent); }
.comment-content { margin: 6px 0 0; }
.rating-row { display: flex; align-items: center; gap: 8px; }
.rating-value { color: var(--dumb-muted); }


@media (max-width: 900px) {
  .review-detail-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .review-sidebar {
    grid-template-columns: 200px 1fr;
    gap: 16px;
  }
  
  .album-cover-box {
    width: 200px;
  }
}

@media (max-width: 600px) {
  .review-sidebar {
    grid-template-columns: 1fr;
  }
  
  .album-cover-box {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
}
</style>
