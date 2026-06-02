<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { animeApi } from '../api'
import type { AnimeItem } from '../types/models'
import { AnimeCard } from '../components/cards'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'
import { consumeCubeNavigation } from '../composables/useCubeTransition'
import { useDragScroll } from '../composables/useDragScroll'

const list = ref<AnimeItem[]>([])
const filters = reactive({
  keyword: '',
  year: '',
  season: '',
  genre: '',
})
const carouselRef = ref<HTMLElement>()

const SEASON_OPTIONS = [
  { label: '春', value: 'SPRING' },
  { label: '夏', value: 'SUMMER' },
  { label: '秋', value: 'AUTUMN' },
  { label: '冬', value: 'WINTER' },
] as const

function getYear(dateStr?: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  return String(date.getFullYear())
}

function getSeason(dateStr?: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  const month = date.getMonth() + 1
  if (month >= 3 && month <= 5) return 'SPRING'
  if (month >= 6 && month <= 8) return 'SUMMER'
  if (month >= 9 && month <= 11) return 'AUTUMN'
  return 'WINTER'
}

function getGenres(raw?: string) {
  if (!raw) return []
  return raw.split(',').map((item) => item.trim()).filter((item) => item.length > 0)
}

const yearOptions = computed(() => {
  const years = Array.from(new Set(list.value.map((item) => getYear(item.releaseDate)).filter(Boolean)))
  return years.sort((a, b) => Number(b) - Number(a))
})

const genreOptions = computed(() => {
  const genres = new Set<string>()
  list.value.forEach((item) => {
    getGenres(item.genre).forEach((genre) => genres.add(genre))
  })
  return Array.from(genres)
})

const filteredList = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return list.value.filter((item) => {
    const byKeyword = !keyword
      || item.title.toLowerCase().includes(keyword)
      || (item.studio || '').toLowerCase().includes(keyword)
    const byYear = !filters.year || getYear(item.releaseDate) === filters.year
    const bySeason = !filters.season || getSeason(item.releaseDate) === filters.season
    const byGenre = !filters.genre || getGenres(item.genre).includes(filters.genre)
    return byKeyword && byYear && bySeason && byGenre
  }).sort((a, b) => {
    const da = a.releaseDate ? new Date(a.releaseDate).getTime() : 0
    const db = b.releaseDate ? new Date(b.releaseDate).getTime() : 0
    return db - da
  })
})

const introFromCube = ref(false)
const cardsEntered = ref(false)

function resetFilters() {
  filters.keyword = ''
  filters.year = ''
  filters.season = ''
  filters.genre = ''
}

onMounted(async () => {
  introFromCube.value = consumeCubeNavigation('/anime')
  const res = await animeApi.list()
  list.value = res.data
  requestAnimationFrame(() => {
    cardsEntered.value = true
  })
  useDragScroll(carouselRef.value)
})
</script>

<template>
  <section class="anime-page" :class="{ 'cube-intro': introFromCube }">
    <FlowSceneBackdrop :intro-from-cube="introFromCube" />
    <div class="anime-content">
      <h2 class="page-title">ANIME</h2>
      <div class="filter-bar">
        <el-input v-model="filters.keyword" placeholder="搜索动漫名称/制片商" clearable class="filter-keyword" />
        <el-select v-model="filters.year" placeholder="年份" clearable class="filter-select">
          <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
        </el-select>
        <el-select v-model="filters.season" placeholder="季节" clearable class="filter-select">
          <el-option v-for="season in SEASON_OPTIONS" :key="season.value" :label="season.label" :value="season.value" />
        </el-select>
        <el-select v-model="filters.genre" placeholder="风格" clearable class="filter-select">
          <el-option v-for="genre in genreOptions" :key="genre" :label="genre" :value="genre" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div ref="carouselRef" class="carousel-wrapper">
        <div class="carousel-track">
          <div
            v-for="(item, index) in filteredList"
            :key="item.id"
            class="carousel-card floating-card"
            :class="{ entered: cardsEntered }"
            :style="{ '--card-delay': `${index * 0.1}s`, '--enter-delay': `${index * 0.07}s` }"
          >
            <AnimeCard :item="item" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.anime-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.anime-content {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 20px;
}

.page-title {
  margin: 0;
  text-align: center;
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  letter-spacing: 4px;
}

.filter-bar {
  display: grid;
  grid-template-columns: 1.2fr repeat(3, 160px) auto;
  gap: 10px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #222222;
}

.filter-keyword,
.filter-select {
  width: 100%;
}

/* 重置按钮：纯文字风格 */
.filter-bar .el-button {
  background: transparent;
  border: none;
  color: #555555;
  padding: 5px 12px;
  font-size: 13px;
}

.filter-bar .el-button:hover {
  color: #ffffff;
  background: transparent;
}

.carousel-wrapper {
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
  cursor: grab;
}
.carousel-wrapper::-webkit-scrollbar {
  display: none;
}
.carousel-wrapper:active {
  cursor: grabbing;
}

.carousel-track {
  display: flex;
  gap: 20px;
  scroll-snap-type: x mandatory;
  padding: 8px 4px 24px;
}

.carousel-card {
  flex-shrink: 0;
  width: 240px;
  scroll-snap-align: start;
}

.floating-card {
  opacity: 0;
  transform: translateY(24px);
}

.floating-card.entered {
  animation:
    cardRiseUp 0.62s cubic-bezier(0.22, 1, 0.36, 1) both,
    animeFloat 6.6s ease-in-out infinite;
  animation-delay: var(--enter-delay, 0s), calc(var(--card-delay, 0s) + 0.62s);
  opacity: 1;
  transform: none;
}

@keyframes cardRiseUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 960px) {
  .filter-bar {
    grid-template-columns: 1fr;
  }
}

.cube-intro .anime-content {
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

@keyframes animeFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
</style>
