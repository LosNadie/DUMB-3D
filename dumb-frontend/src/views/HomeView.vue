<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { animeApi, movieApi, reviewApi } from '../api'
import type { AnimeItem, MovieItem, ReviewItem } from '../types/models'
import { useAuthStore } from '../stores/auth'
import HomeVideoBackground from '../components/common/HomeVideoBackground.vue'
import UserFabMenu from '../components/home/UserFabMenu.vue'
import SplashScreen from '../components/home/SplashScreen.vue'
import SpotlightCarousel from '../components/home/SpotlightCarousel.vue'
import LoginModal from '../components/home/LoginModal.vue'
import type { SpotlightCard } from '../components/home/SpotlightCarousel.vue'

const BACKEND_ORIGIN = 'http://localhost:8080'
const router = useRouter()
void useAuthStore() // side-effect: initialize auth
const spotlightCards = ref<SpotlightCard[]>([])
const activeIndex = ref(0)
const loginModalVisible = ref(false)

function toTimestamp(time?: string) {
  if (!time) return 0
  const value = new Date(time).getTime()
  return Number.isNaN(value) ? 0 : value
}

function resolveImage(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/mock-covers/')) return url
  if (url.startsWith('/')) return `${BACKEND_ORIGIN}${url}`
  return url
}

function extractReviewBackground(content?: string) {
  if (!content) return ''
  const match = content.match(/背景：(.+)/)
  return match?.[1]?.trim() || ''
}

function collectMusicSpotlights(reviews: ReviewItem[]) {
  return reviews
    .map((item) => ({
      id: item.id,
      title: item.albumTitle || item.title,
      subtitle: item.artist || '-',
      image: resolveImage(extractReviewBackground(item.content) || item.coverImage),
      time: Math.max(toTimestamp(item.publishTime), toTimestamp(item.updateTime)),
      route: `/music/${item.id}`,
      kicker: 'MUSIC',
    }))
    .filter((item) => !!item.image)
    .sort((a, b) => b.time - a.time)
}

function collectAnimeSpotlights(animes: AnimeItem[]) {
  return animes
    .map((item) => ({
      id: item.id,
      title: item.title,
      subtitle: item.studio || '-',
      image: resolveImage(item.backgroundImage || item.coverImage),
      time: Math.max(toTimestamp(item.publishTime), toTimestamp(item.updateTime)),
      route: `/anime/${item.id}`,
      kicker: 'ANIME',
    }))
    .filter((item) => !!item.image)
    .sort((a, b) => b.time - a.time)
}

function collectMovieSpotlights(movies: MovieItem[]) {
  return movies
    .map((item) => ({
      id: item.id,
      title: item.title,
      subtitle: item.director || '-',
      image: resolveImage(item.backgroundImage || item.coverImage),
      time: Math.max(toTimestamp(item.publishTime), toTimestamp(item.updateTime)),
      route: `/movie/${item.id}`,
      kicker: 'MOVIE',
    }))
    .filter((item) => !!item.image)
    .sort((a, b) => b.time - a.time)
}

async function initBackground() {
  try {
    const [reviewRes, animeRes, movieRes] = await Promise.all([
      reviewApi.list(),
      animeApi.list(),
      movieApi.list(),
    ])

    // 按发布时间排序，取最新的3个，最新的放中间
    const allSpotlights = [
      ...collectMusicSpotlights(reviewRes.data),
      ...collectAnimeSpotlights(animeRes.data),
      ...collectMovieSpotlights(movieRes.data),
    ]
      .sort((a, b) => b.time - a.time)
      .slice(0, 3)

    if (allSpotlights.length >= 3) {
      const newest = allSpotlights[0]!, second = allSpotlights[1]!, third = allSpotlights[2]!
      spotlightCards.value = [second, newest, third]
      activeIndex.value = 1
    } else {
      spotlightCards.value = allSpotlights
      activeIndex.value = 0
    }
  } catch {
    spotlightCards.value = []
  }
}

function handleActiveIndexUpdate(index: number) {
  activeIndex.value = index
}

function goLogin() {
  loginModalVisible.value = true
}

function openSpotlight(route: string) {
  void router.push(route)
}

onMounted(() => {
  void initBackground()
})
</script>

<template>
  <SplashScreen />
  <HomeVideoBackground />
  <section class="velvet-home">
    <!-- 背景层 -->
    <div class="grain-overlay"></div>
    <div class="vignette"></div>

    <!-- 顶部标题 -->
    <header class="velvet-header">
      <div class="header-topline">
        <div class="equalizer">
          <span v-for="i in 24" :key="i" class="eq-bar"></span>
        </div>
      </div>
      <h1 class="velvet-logo">DUMB</h1>
      <nav class="velvet-nav">
        <router-link to="/music" class="nav-link">MUSIC</router-link>
        <span class="nav-divider">/</span>
        <router-link to="/anime" class="nav-link">ANIME</router-link>
        <span class="nav-divider">/</span>
        <router-link to="/movie" class="nav-link">MOVIE</router-link>
      </nav>
    </header>

    <!-- 轮播区 -->
    <SpotlightCarousel
      v-if="spotlightCards.length > 0"
      :cards="spotlightCards"
      :initial-index="activeIndex"
      @update:active-index="handleActiveIndexUpdate"
      @open="openSpotlight"
    />

    <!-- 底部 -->
    <footer class="velvet-footer">
      <div class="footer-line"></div>
      <p>DUMB ARCHIVE /// ALL RIGHTS RESERVED</p>
    </footer>

    <UserFabMenu @open-login="goLogin" />
    <LoginModal v-model:visible="loginModalVisible" />
  </section>
</template>

<style scoped>
/* ═══════════════════════════════════════════════
   VELVET HOME — Dark / High-Impact / Grain
   ═══════════════════════════════════════════════ */

.velvet-home {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: transparent;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* ── 颗粒噪点叠加 ── */
.grain-overlay {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.035;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 512 512' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

/* ── 暗角 ── */
.vignette {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(ellipse at center, transparent 0%, transparent 50%, rgba(0,0,0,0.5) 100%);
}

/* ═══════════════════════════════════════════════
   HEADER
   ═══════════════════════════════════════════════ */
.velvet-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 3;
  padding: 20px 32px;
  text-align: center;
}

.header-topline {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
}

/* 24段均衡器 */
.equalizer {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 16px;
}

.eq-bar {
  width: 2px;
  background: #333;
  border-radius: 1px;
  animation: eqPulse 1.2s ease-in-out infinite;
}

.eq-bar:nth-child(1) { height: 30%; animation-delay: 0.0s; }
.eq-bar:nth-child(2) { height: 60%; animation-delay: 0.1s; }
.eq-bar:nth-child(3) { height: 40%; animation-delay: 0.2s; }
.eq-bar:nth-child(4) { height: 80%; animation-delay: 0.3s; }
.eq-bar:nth-child(5) { height: 50%; animation-delay: 0.4s; }
.eq-bar:nth-child(6) { height: 70%; animation-delay: 0.5s; }
.eq-bar:nth-child(7) { height: 35%; animation-delay: 0.6s; }
.eq-bar:nth-child(8) { height: 90%; animation-delay: 0.7s; }
.eq-bar:nth-child(9) { height: 45%; animation-delay: 0.8s; }
.eq-bar:nth-child(10) { height: 65%; animation-delay: 0.9s; }
.eq-bar:nth-child(11) { height: 55%; animation-delay: 1.0s; }
.eq-bar:nth-child(12) { height: 75%; animation-delay: 1.1s; }
.eq-bar:nth-child(13) { height: 40%; animation-delay: 0.05s; }
.eq-bar:nth-child(14) { height: 85%; animation-delay: 0.15s; }
.eq-bar:nth-child(15) { height: 50%; animation-delay: 0.25s; }
.eq-bar:nth-child(16) { height: 70%; animation-delay: 0.35s; }
.eq-bar:nth-child(17) { height: 60%; animation-delay: 0.45s; }
.eq-bar:nth-child(18) { height: 45%; animation-delay: 0.55s; }
.eq-bar:nth-child(19) { height: 80%; animation-delay: 0.65s; }
.eq-bar:nth-child(20) { height: 55%; animation-delay: 0.75s; }
.eq-bar:nth-child(21) { height: 70%; animation-delay: 0.85s; }
.eq-bar:nth-child(22) { height: 40%; animation-delay: 0.95s; }
.eq-bar:nth-child(23) { height: 90%; animation-delay: 1.05s; }
.eq-bar:nth-child(24) { height: 50%; animation-delay: 1.15s; }

@keyframes eqPulse {
  0%, 100% { opacity: 0.3; transform: scaleY(0.6); }
  50% { opacity: 1; transform: scaleY(1); }
}

.velvet-logo {
  margin: 0;
  font-family: 'Cinzel', 'Playfair Display', serif;
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 400;
  letter-spacing: 12px;
  color: #ffffff;
  line-height: 1;
}

/* ── 导航：MUSIC / ANIME / MOVIE ── */
.velvet-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 14px;
}

.nav-link {
  font-family: 'Inter', -apple-system, sans-serif;
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 3px;
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  padding: 6px 16px;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #ffffff;
}

.nav-divider {
  color: rgba(255, 255, 255, 0.15);
  font-size: 0.7rem;
  font-weight: 300;
  user-select: none;
}

/* ═══════════════════════════════════════════════
   FOOTER
   ═══════════════════════════════════════════════ */
.velvet-footer {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  z-index: 3;
  text-align: center;
}

.footer-line {
  width: 40px;
  height: 1px;
  background: #222;
  margin: 0 auto 8px;
}

.velvet-footer p {
  margin: 0;
  font-size: 0.55rem;
  color: #333;
  letter-spacing: 3px;
}

/* ═══════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════ */
@media (max-width: 768px) {
  .velvet-header {
    padding: 16px 20px;
  }

  .equalizer {
    display: none;
  }

  .velvet-logo {
    font-size: 2rem;
    letter-spacing: 8px;
  }

  .nav-link {
    font-size: 0.65rem;
    letter-spacing: 2px;
    padding: 6px 10px;
  }
}
</style>
