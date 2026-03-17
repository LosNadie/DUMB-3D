<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { reviewApi } from '../api'
import type { ReviewItem } from '../types/models'
import { ReviewCard } from '../components/cards'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'
import { consumeCubeNavigation } from '../composables/useCubeTransition'

const GENRE_OPTIONS = [
  { label: '流行', value: 'POP' },
  { label: '摇滚', value: 'ROCK' },
  { label: '嘻哈', value: 'HIP_HOP' },
  { label: '电子', value: 'ELECTRONIC' },
  { label: '民谣', value: 'FOLK' },
  { label: 'R&B', value: 'RNB' },
  { label: '爵士', value: 'JAZZ' },
  { label: '古典', value: 'CLASSICAL' },
]

const reviews = ref<ReviewItem[]>([])
const loading = ref(false)
const introFromCube = ref(false)
const cardsEntered = ref(false)

const PAGE_SIZE = 6
const savedPage = sessionStorage.getItem('music_page')
const currentPage = ref(savedPage ? Number(savedPage) : 1)

const filters = reactive({
  keyword: '',
  genre: '',
})

const pagedReviews = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return reviews.value.slice(start, start + PAGE_SIZE)
})

async function loadReviews(isInitial = false) {
  loading.value = true
  if (isInitial) cardsEntered.value = false
  try {
    const params: Record<string, string | number> = {}
    if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
    if (filters.genre) params.genre = filters.genre
    const res = await reviewApi.list(Object.keys(params).length > 0 ? params : undefined)
    reviews.value = [...res.data].sort((a, b) => {
      const da = a.releaseDate ? new Date(a.releaseDate).getTime() : 0
      const db = b.releaseDate ? new Date(b.releaseDate).getTime() : 0
      return db - da
    })
  } finally {
    loading.value = false
    if (isInitial) {
      requestAnimationFrame(() => {
        cardsEntered.value = true
      })
    }
  }
}

function onSearch() {
  currentPage.value = 1
  void loadReviews()
}

function onReset() {
  filters.keyword = ''
  filters.genre = ''
  currentPage.value = 1
  void loadReviews()
}

watch(currentPage, (val) => {
  sessionStorage.setItem('music_page', String(val))
})

onMounted(() => {
  introFromCube.value = consumeCubeNavigation('/music')
  void loadReviews(true)
})
</script>

<template>
  <section class="music-page" :class="{ 'cube-intro': introFromCube }">
    <FlowSceneBackdrop :intro-from-cube="introFromCube" />
    <div class="music-content">
      <h2 class="page-title">MUSIC</h2>
      <section class="filter-bar panel">
        <div class="filter-left">
          <el-input v-model="filters.keyword" placeholder="搜索专辑 / 艺人" clearable style="width: 220px" @keyup.enter="onSearch" />
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </div>
        <el-select v-model="filters.genre" placeholder="风格" clearable style="width: 140px" @change="onSearch">
          <el-option v-for="g in GENRE_OPTIONS" :key="g.value" :label="g.label" :value="g.value" />
        </el-select>
      </section>

      <div class="main-content">
        <div class="grid" v-show="!loading">
          <div
            v-for="(item, index) in pagedReviews"
            :key="item.id"
            class="floating-card"
            :class="{ entered: cardsEntered }"
            :style="{ '--card-delay': `${index * 0.14}s`, '--enter-delay': `${index * 0.08}s` }"
          >
            <ReviewCard :item="item" />
          </div>
        </div>
        <div class="pager" v-if="reviews.length > PAGE_SIZE">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="PAGE_SIZE"
            :total="reviews.length"
            layout="prev, pager, next"
            background
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.music-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.music-content {
  position: relative;
  z-index: 1;
}

.panel {
  background: rgba(8, 10, 22, 0.54);
  border: 1px solid rgba(120, 136, 255, 0.2);
  backdrop-filter: blur(7px);
}

.page-title {
  margin: 0;
  text-align: center;
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  letter-spacing: 4px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: min(100%, 980px);
  margin: 10px auto 36px;
  padding: 14px;
  border-radius: 12px;
  box-sizing: border-box;
}

.filter-left {
  display: flex;
  gap: 16px;
  align-items: center;
}


.main-content {
  min-width: 0;
}

.grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.floating-card {
  opacity: 0;
  transform: translateY(60px);
  transition: none;
}

.floating-card.entered {
  animation:
    cardRiseUp 0.62s cubic-bezier(0.22, 1, 0.36, 1) both,
    musicFloat 6.8s ease-in-out infinite;
  animation-delay: var(--enter-delay, 0s), calc(var(--card-delay, 0s) + 0.62s);
  opacity: 1;
  transform: none;
}

@keyframes cardRiseUp {
  from {
    opacity: 0;
    transform: translateY(72px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 1200px) {
  .main-content {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

.cube-intro .music-content {
  animation: cubePageReveal 0.9s ease-out both;
}

@keyframes cubePageReveal {
  0% {
    transform: scale(1.03);
    opacity: 0.2;
    filter: saturate(1.5) blur(2px);
  }
  100% {
    transform: scale(1);
    opacity: 1;
    filter: saturate(1) blur(0);
  }
}

@keyframes musicFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-9px); }
}
</style>
