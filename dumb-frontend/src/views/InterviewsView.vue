<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { movieApi } from '../api'
import type { MovieItem } from '../types/models'
import { MovieCard } from '../components/cards'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'
import { consumeCubeNavigation } from '../composables/useCubeTransition'

const list = ref<MovieItem[]>([])
const introFromCube = ref(false)
const cardsEntered = ref(false)
const filters = reactive({
  keyword: '',
  year: '',
  genre: '',
  region: '',
})

function getYear(dateStr?: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  return String(date.getFullYear())
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

const regionOptions = computed(() => {
  const regions = new Set<string>()
  list.value.forEach((item) => {
    if (item.region && item.region.trim()) {
      regions.add(item.region.trim())
    }
  })
  return Array.from(regions)
})

const filteredList = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return list.value.filter((item) => {
    const byKeyword = !keyword
      || item.title.toLowerCase().includes(keyword)
      || (item.director || '').toLowerCase().includes(keyword)
      || (item.actors || '').toLowerCase().includes(keyword)
    const byYear = !filters.year || getYear(item.releaseDate) === filters.year
    const byGenre = !filters.genre || getGenres(item.genre).includes(filters.genre)
    const byRegion = !filters.region || (item.region || '').trim() === filters.region
    return byKeyword && byYear && byGenre && byRegion
  })
})

function resetFilters() {
  filters.keyword = ''
  filters.year = ''
  filters.genre = ''
  filters.region = ''
}

onMounted(async () => {
  introFromCube.value = consumeCubeNavigation('/movie')
  const res = await movieApi.list()
  list.value = res.data
  requestAnimationFrame(() => {
    cardsEntered.value = true
  })
})
</script>

<template>
  <section class="movie-page" :class="{ 'cube-intro': introFromCube }">
    <FlowSceneBackdrop :intro-from-cube="introFromCube" />
    <div class="movie-content">
      <h2 class="page-title">MOVIE</h2>
      <div class="filter-bar">
        <el-input v-model="filters.keyword" placeholder="搜索电影名称/导演/主演" clearable class="filter-keyword" />
        <el-select v-model="filters.year" placeholder="年份" clearable class="filter-select">
          <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
        </el-select>
        <el-select v-model="filters.genre" placeholder="风格" clearable class="filter-select">
          <el-option v-for="genre in genreOptions" :key="genre" :label="genre" :value="genre" />
        </el-select>
        <el-select v-model="filters.region" placeholder="地区" clearable class="filter-select">
          <el-option v-for="region in regionOptions" :key="region" :label="region" :value="region" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div class="grid">
        <div
          v-for="(item, index) in filteredList"
          :key="item.id"
          class="floating-card"
          :class="{ entered: cardsEntered }"
          :style="{ '--card-delay': `${index * 0.09}s`, '--enter-delay': `${index * 0.07}s` }"
        >
          <MovieCard :item="item" />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.movie-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.movie-content {
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
  grid-template-columns: 1.2fr 140px 180px 140px auto;
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
.grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
}

.floating-card {
  opacity: 0;
  transform: translateY(60px);
}

.floating-card.entered {
  animation:
    cardRiseUp 0.62s cubic-bezier(0.22, 1, 0.36, 1) both,
    movieFloat 6.2s ease-in-out infinite;
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

@media (max-width: 960px) {
  .filter-bar { grid-template-columns: 1fr; }
}

.cube-intro .movie-content {
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

@keyframes movieFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
</style>
