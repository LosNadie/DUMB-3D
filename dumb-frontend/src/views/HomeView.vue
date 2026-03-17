<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { animeApi, authApi, movieApi, reviewApi } from '../api'
import type { AnimeItem, MovieItem, ReviewItem } from '../types/models'
import { useAuthStore } from '../stores/auth'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'

interface SpotlightCard {
  id: number
  title: string
  subtitle: string
  image: string
  time: number
  route: string
  kicker: string
}

const HomeQuickNavRoute = {
  Music: '/music',
  Anime: '/anime',
  Movie: '/movie',
} as const

const BACKEND_ORIGIN = 'http://localhost:8080'
const router = useRouter()
const authStore = useAuthStore()
const spotlightCards = ref<SpotlightCard[]>([])
const spotlightViewportRef = ref<HTMLElement | null>(null)
const viewportWidth = ref(window.innerWidth)
const activeIndex = ref(0)
const dragOffsetX = ref(0)
const isDragging = ref(false)
const dragMovedDistance = ref(0)
const dragStartX = ref(0)
const isSnapping = ref(false)
const dragVelocityX = ref(0)
const pressedCardIndex = ref<number | null>(null)
let lastPointerX = 0
let lastPointerTs = 0
let snapTimer: number | null = null
const loginModalVisible = ref(false)
const loginLoading = ref(false)
const isRegisterMode = ref(false)
const userMenuOpen = ref(false)

const SPLASH_SESSION_KEY = 'dumb_splash_shown'
const SPLASH_REVEAL_MS = 3600
const SPLASH_EXTRA_HOLD_MS = 2000
const SPLASH_FADE_MS = 1000
const splashVisible = ref(false)
const splashFading = ref(false)
const loginForm = ref({
  username: '',
  password: '',
  email: '',
})
const quickNavItems = [
  { key: 'music', label: 'MUSIC', route: HomeQuickNavRoute.Music },
  { key: 'anime', label: 'ANIME', route: HomeQuickNavRoute.Anime },
  { key: 'movie', label: 'MOVIE', route: HomeQuickNavRoute.Movie },
] as const

function toTimestamp(time?: string) {
  if (!time) return 0
  const value = new Date(time).getTime()
  return Number.isNaN(value) ? 0 : value
}

function resolveImage(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
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
    const allSpotlights = [
      ...collectMusicSpotlights(reviewRes.data),
      ...collectAnimeSpotlights(animeRes.data),
      ...collectMovieSpotlights(movieRes.data),
    ]
      .sort((a, b) => b.time - a.time)
      .slice(0, 3)

    if (allSpotlights.length >= 3) {
      const latest = allSpotlights[0]
      const second = allSpotlights[1]
      const third = allSpotlights[2]
      if (!latest || !second || !third) {
        spotlightCards.value = allSpotlights
        activeIndex.value = 0
        return
      }
      spotlightCards.value = [second, latest, third]
      activeIndex.value = 1
    } else {
      spotlightCards.value = allSpotlights
      activeIndex.value = 0
    }
  } catch {
    spotlightCards.value = []
  }
}

function goLogin() {
  isRegisterMode.value = false
  loginForm.value = { username: '', password: '', email: '' }
  loginModalVisible.value = true
}

function closeLoginModal() {
  if (loginLoading.value) return
  loginModalVisible.value = false
}

async function submitLogin() {
  const username = loginForm.value.username.trim()
  const password = loginForm.value.password.trim()
  const email = loginForm.value.email.trim()
  if (!username || !password || (isRegisterMode.value && !email)) {
    ElMessage.warning(isRegisterMode.value ? '请填写用户名、邮箱和密码' : '请输入用户名和密码')
    return
  }

  loginLoading.value = true
  try {
    const res = isRegisterMode.value
      ? await authApi.register({ username, password, email })
      : await authApi.login({ username, password })
    authStore.setAuth(res.data.token, res.data.username, res.data.role)
    ElMessage.success(isRegisterMode.value ? '注册成功' : '登录成功')
    loginModalVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

function toggleAuthMode() {
  isRegisterMode.value = !isRegisterMode.value
  loginForm.value = { username: '', password: '', email: '' }
}

function handleLogout() {
  authStore.logout()
  userMenuOpen.value = false
}

function goAdmin() {
  userMenuOpen.value = false
  void router.push('/admin')
}

function openSpotlight(route: string) {
  void router.push(route)
}

function resolveCardIndexFromTarget(target: EventTarget | null) {
  if (!(target instanceof Element)) return null
  const cardEl = target.closest('.spotlight-card')
  if (!(cardEl instanceof HTMLElement)) return null
  const indexText = cardEl.dataset.index
  if (typeof indexText !== 'string') return null
  const parsed = Number.parseInt(indexText, 10)
  if (!Number.isFinite(parsed)) return null
  return parsed
}

const cardGap = computed(() => (viewportWidth.value <= 900 ? 12 * 8 : 18 * 8))
const cardWidth = computed(() => {
  if (viewportWidth.value <= 900) {
    return Math.min(viewportWidth.value * 0.615, 375)
  }
  return Math.min(viewportWidth.value * 0.405, 570)
})

const baseTrackTranslate = computed(() => {
  return viewportWidth.value / 2 - cardWidth.value / 2 - activeIndex.value * (cardWidth.value + cardGap.value)
})

const trackTransform = computed(() => {
  return `translate3d(${baseTrackTranslate.value + dragOffsetX.value}px, 0, 0)`
})

const trackStyle = computed(() => ({
  transform: trackTransform.value,
  gap: `${cardGap.value}px`,
}))
const trackTransitionStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 420ms cubic-bezier(0.22, 1, 0.36, 1)',
}))

function updateViewportSize() {
  viewportWidth.value = spotlightViewportRef.value?.clientWidth || window.innerWidth
}

function applyRubberBand(offset: number, dimension: number) {
  const c = 0.55
  return (dimension * c * offset) / (dimension + c * Math.abs(offset))
}

function onSpotlightPointerDown(event: PointerEvent) {
  if (spotlightCards.value.length <= 1) return
  pressedCardIndex.value = resolveCardIndexFromTarget(event.target)
  isDragging.value = true
  isSnapping.value = false
  dragStartX.value = event.clientX
  dragMovedDistance.value = 0
  dragVelocityX.value = 0
  lastPointerX = event.clientX
  lastPointerTs = performance.now()
  const target = event.currentTarget as HTMLElement | null
  target?.setPointerCapture?.(event.pointerId)
}

function onSpotlightPointerMove(event: PointerEvent) {
  if (!isDragging.value) return
  const rawOffset = event.clientX - dragStartX.value
  const atFirstAndPullingRight = activeIndex.value === 0 && rawOffset > 0
  const atLastAndPullingLeft = activeIndex.value === spotlightCards.value.length - 1 && rawOffset < 0
  const now = performance.now()
  const dt = Math.max(now - lastPointerTs, 1)
  const instantVelocity = (event.clientX - lastPointerX) / dt
  dragVelocityX.value = dragVelocityX.value * 0.78 + instantVelocity * 0.22
  lastPointerX = event.clientX
  lastPointerTs = now

  if (atFirstAndPullingRight || atLastAndPullingLeft) {
    const stepDistance = cardWidth.value + cardGap.value
    dragOffsetX.value = applyRubberBand(rawOffset, stepDistance)
  } else {
    const stepDistance = cardWidth.value + cardGap.value
    const maxOffset = stepDistance * Math.max(1, spotlightCards.value.length - 1)
    dragOffsetX.value = Math.max(Math.min(rawOffset, maxOffset), -maxOffset)
  }
  dragMovedDistance.value = Math.max(dragMovedDistance.value, Math.abs(rawOffset))
}

function clearSnapTimer() {
  if (snapTimer !== null) {
    window.clearTimeout(snapTimer)
    snapTimer = null
  }
}

function finishDragging() {
  if (!isDragging.value) return
  isDragging.value = false
  const tappedIndex = pressedCardIndex.value
  pressedCardIndex.value = null
  const hasDragged = dragMovedDistance.value > 10
  if (!hasDragged) {
    dragOffsetX.value = 0
    dragVelocityX.value = 0
    isSnapping.value = false
    clearSnapTimer()
    if (tappedIndex !== null) {
      if (tappedIndex !== activeIndex.value) {
        activeIndex.value = tappedIndex
        isSnapping.value = true
        clearSnapTimer()
        snapTimer = window.setTimeout(() => {
          isSnapping.value = false
        }, 300)
      } else {
        const route = spotlightCards.value[tappedIndex]?.route
        if (route) openSpotlight(route)
      }
    }
    return
  }
  const projectedOffset = dragOffsetX.value + dragVelocityX.value * 180
  const switchThreshold = cardWidth.value * 0.15
  const shouldSwitch = Math.abs(projectedOffset) > switchThreshold
  const stepDistance = cardWidth.value + cardGap.value

  if (shouldSwitch && stepDistance > 0) {
    const rawSteps = Math.round(Math.abs(projectedOffset) / stepDistance)
    const stepCount = Math.max(1, rawSteps)
    if (projectedOffset < 0) {
      activeIndex.value = Math.min(activeIndex.value + stepCount, spotlightCards.value.length - 1)
    } else {
      activeIndex.value = Math.max(activeIndex.value - stepCount, 0)
    }
  }
  dragOffsetX.value = 0
  dragVelocityX.value = 0
  isSnapping.value = true
  clearSnapTimer()
  snapTimer = window.setTimeout(() => {
    isSnapping.value = false
  }, 300)
}

function onSpotlightPointerUp() {
  finishDragging()
}

function onSpotlightPointerCancel() {
  finishDragging()
}

function onSpotlightLostPointerCapture() {
  finishDragging()
}

function openSpotlightByIndex(index: number) {
  if (index < 0 || index >= spotlightCards.value.length) return
  if (index !== activeIndex.value) {
    activeIndex.value = index
    dragOffsetX.value = 0
    dragVelocityX.value = 0
    isSnapping.value = true
    clearSnapTimer()
    snapTimer = window.setTimeout(() => {
      isSnapping.value = false
    }, 300)
    return
  }
  const route = spotlightCards.value[index]?.route
  if (!route) return
  openSpotlight(route)
}

onMounted(() => {
  updateViewportSize()
  window.addEventListener('resize', updateViewportSize)
  void initBackground()

  if (!sessionStorage.getItem(SPLASH_SESSION_KEY)) {
    splashVisible.value = true
    sessionStorage.setItem(SPLASH_SESSION_KEY, '1')
    setTimeout(() => {
      splashFading.value = true
      setTimeout(() => {
        splashVisible.value = false
      }, SPLASH_FADE_MS)
    }, SPLASH_REVEAL_MS + SPLASH_EXTRA_HOLD_MS)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportSize)
  clearSnapTimer()
})
</script>

<template>
  <Teleport to="body">
    <div v-if="splashVisible" class="splash-screen" :class="{ fading: splashFading }">
      <div class="splash-content">
        <h1 class="splash-title">
          <span class="splash-letter" style="--l: 0">D</span>
          <span class="splash-letter" style="--l: 1">U</span>
          <span class="splash-letter" style="--l: 2">M</span>
          <span class="splash-letter" style="--l: 3">B</span>
        </h1>
        <div class="splash-bar-wrap">
          <span class="splash-bar-text">Music, Anime &amp; Film Rating Platform</span>
          <span class="splash-bar-text splash-bar-glow" aria-hidden="true">Music, Anime &amp; Film Rating Platform</span>
        </div>
      </div>
    </div>
  </Teleport>
  <section class="home-3d">
    <FlowSceneBackdrop />
    <div class="overlay title-overlay">
      <h1>DUMB</h1>
    </div>
    <div
      v-if="spotlightCards.length > 0"
      ref="spotlightViewportRef"
      class="spotlight-viewport"
      @pointerdown="onSpotlightPointerDown"
      @pointermove="onSpotlightPointerMove"
      @pointerup="onSpotlightPointerUp"
      @pointercancel="onSpotlightPointerCancel"
      @lostpointercapture="onSpotlightLostPointerCapture"
    >
      <div class="spotlight-track" :style="[trackStyle, trackTransitionStyle]">
        <button
          v-for="(item, index) in spotlightCards"
          :key="item.id"
          class="spotlight-card"
          :class="{ active: index === activeIndex }"
          :data-index="index"
          @click.prevent="openSpotlightByIndex(index)"
          :style="{ '--anime-bg': `url(${item.image})`, '--card-delay': `${index * 0.22}s`, width: `${cardWidth}px` }"
        >
          <div class="spotlight-copy">
            <p class="spotlight-kicker">{{ item.kicker }}</p>
            <h3>{{ item.title }}</h3>
            <p class="spotlight-meta">{{ item.subtitle }}</p>
          </div>
        </button>
      </div>
    </div>
    <div class="user-fab-wrap">
      <button v-if="!authStore.isLoggedIn" class="login-fab" @click="goLogin">LOGIN</button>
      <div v-else class="user-fab" @click="userMenuOpen = !userMenuOpen">
        <span class="user-fab-name">{{ authStore.username }}</span>
        <span class="user-fab-arrow" :class="{ open: userMenuOpen }">▾</span>
      </div>
      <div v-if="authStore.isLoggedIn && userMenuOpen" class="user-menu" @click.stop>
        <button v-if="authStore.isAdminLike" class="user-menu-item" @click="goAdmin">后台</button>
        <button class="user-menu-item logout" @click="handleLogout">退出登录</button>
      </div>
    </div>
    <nav class="quick-nav" aria-label="home quick nav">
      <button
        v-for="item in quickNavItems"
        :key="item.key"
        class="quick-nav-item"
        @click="openSpotlight(item.route)"
      >
        {{ item.label }}
      </button>
    </nav>
    <div v-if="loginModalVisible" class="login-modal-mask" @click.self="closeLoginModal">
      <div class="login-modal-card">
        <h3>{{ isRegisterMode ? 'REGISTER' : 'LOGIN' }}</h3>
        <p class="login-tip">{{ isRegisterMode ? '创建 DUMB 新账户' : '进入 DUMB 账户中心' }}</p>
        <input
          v-model="loginForm.username"
          class="login-input"
          type="text"
          placeholder="用户名"
          @keyup.enter="submitLogin"
        />
        <input
          v-if="isRegisterMode"
          v-model="loginForm.email"
          class="login-input"
          type="email"
          placeholder="邮箱"
          @keyup.enter="submitLogin"
        />
        <input
          v-model="loginForm.password"
          class="login-input"
          type="password"
          placeholder="密码"
          @keyup.enter="submitLogin"
        />
        <button class="mode-switch" @click="toggleAuthMode">
          {{ isRegisterMode ? '已有账号？去登录' : '没有账号？去注册' }}
        </button>
        <div class="login-actions">
          <button class="login-btn ghost" @click="closeLoginModal">取消</button>
          <button class="login-btn primary" :disabled="loginLoading" @click="submitLogin">
            {{ loginLoading ? (isRegisterMode ? '注册中...' : '登录中...') : (isRegisterMode ? '注册' : '登录') }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* ── Splash screen ─────────────────────────────────────── */
.splash-screen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  pointer-events: all;
}

.splash-screen::before,
.splash-screen::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 50%;
  background: #030308;
  z-index: 0;
  transform: scaleY(1);
  transition: transform 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.splash-screen::before {
  top: 0;
  transform-origin: top;
}

.splash-screen::after {
  top: 50%;
  transform-origin: bottom;
}

.splash-screen.fading::before,
.splash-screen.fading::after {
  transform: scaleY(0);
}

.splash-screen.fading {
  pointer-events: none;
}

.splash-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 36px;
}

.splash-title {
  margin: 0;
  font-size: clamp(3rem, 7vw, 6rem);
  letter-spacing: 0.55rem;
  font-weight: 800;
  color: rgba(236, 242, 255, 0.96);
  text-shadow: 0 0 40px rgba(140, 110, 255, 0.28);
  line-height: 1;
}

.splash-letter {
  display: inline-block;
  opacity: 0;
  animation: splashLetterIn 0.55s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  animation-delay: calc(var(--l) * 0.1s);
}

.splash-bar-wrap {
  position: relative;
  height: 1.2em;
}

.splash-bar-text {
  display: block;
  font-size: clamp(0.72rem, 1.2vw, 0.92rem);
  letter-spacing: 0.2rem;
  color: rgba(255, 255, 255, 0.82);
  white-space: nowrap;
  clip-path: inset(0 100% 0 0);
  animation: splashBarReveal 3s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  animation-delay: 0.45s;
}

.splash-bar-glow {
  position: absolute;
  inset: 0;
  color: rgba(255, 255, 255, 0.35);
  filter: blur(6px);
  animation-delay: 0.45s;
}

.splash-screen.fading .splash-letter {
  animation-duration: 1s;
  animation-delay: 0s;
  animation-fill-mode: both;
}

.splash-screen.fading .splash-letter:nth-child(1) {
  animation-name: splashLetterOutD;
}

.splash-screen.fading .splash-letter:nth-child(2) {
  animation-name: splashLetterOutU;
}

.splash-screen.fading .splash-letter:nth-child(3) {
  animation-name: splashLetterOutM;
}

.splash-screen.fading .splash-letter:nth-child(4) {
  animation-name: splashLetterOutB;
}

.splash-screen.fading .splash-bar-wrap {
  animation: splashBarExit 1s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes splashLetterIn {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes splashBarReveal {
  from { clip-path: inset(0 100% 0 0); }
  to   { clip-path: inset(0 0%   0 0); }
}

@keyframes splashLetterOutD {
  from { opacity: 1; transform: translate3d(0, 0, 0) rotate(0deg); }
  to { opacity: 0; transform: translate3d(-120%, -80%, 0) rotate(-25deg); }
}

@keyframes splashLetterOutU {
  from { opacity: 1; transform: translate3d(0, 0, 0) rotate(0deg); }
  to { opacity: 0; transform: translate3d(0, -150%, 0) rotate(-6deg); }
}

@keyframes splashLetterOutM {
  from { opacity: 1; transform: translate3d(0, 0, 0) rotate(0deg); }
  to { opacity: 0; transform: translate3d(0, 150%, 0) rotate(8deg); }
}

@keyframes splashLetterOutB {
  from { opacity: 1; transform: translate3d(0, 0, 0) rotate(0deg); }
  to { opacity: 0; transform: translate3d(120%, -80%, 0) rotate(25deg); }
}

@keyframes splashBarExit {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(24px); }
}

/* ── Homepage ──────────────────────────────────────────── */
.home-3d {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: #040914;
}

.overlay {
  position: absolute;
  width: 100%;
  text-align: center;
  pointer-events: none;
  color: rgba(236, 242, 255, 0.96);
  text-shadow: 0 0 16px rgba(76, 105, 255, 0.35);
  z-index: 2;
}

.title-overlay {
  top: 18px;
}

.title-overlay h1 {
  margin: 0;
  font-size: clamp(2rem, 4.2vw, 3.5rem);
  letter-spacing: 0.18rem;
  font-weight: 800;
  line-height: 1;
}

.spotlight-viewport {
  position: absolute;
  top: 50%;
  left: 0;
  z-index: 3;
  width: 100vw;
  transform: translateY(-44%);
  background: transparent;
  border: none;
  box-shadow: none;
  overflow: hidden;
  touch-action: pan-y;
  user-select: none;
  cursor: grab;
}

.spotlight-viewport:active {
  cursor: grabbing;
}

.spotlight-track {
  display: flex;
  align-items: stretch;
  will-change: transform;
}

.spotlight-card {
  position: relative;
  flex: 0 0 auto;
  aspect-ratio: 16 / 9.5;
  transform: translateY(0) scale(1);
  border: none;
  border-radius: 0;
  background: transparent;
  overflow: hidden;
  text-align: center;
  padding: clamp(12px, 1.5vw, 18px);
  color: rgba(250, 252, 255, 0.96);
  box-shadow: none;
  backdrop-filter: none;
  cursor: pointer;
  transition: box-shadow 0.28s ease;
  opacity: 0.82;
  animation: spotlightWave var(--wave-dur, 6.4s) ease-in-out infinite;
  animation-delay: var(--card-delay, 0s);
}

.spotlight-card::before,
.spotlight-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 0;
  pointer-events: none;
}

.spotlight-card::before {
  background-image: var(--anime-bg);
  background-size: cover;
  background-position: center;
  opacity: 1;
  transform: scale(1.04);
  animation: spotlightImageWave 9s ease-in-out infinite;
}

.spotlight-card::after {
  background: transparent;
}

.spotlight-card > * {
  position: relative;
  z-index: 1;
}

.spotlight-copy {
  position: absolute;
  left: 50%;
  bottom: 9%;
  transform: translateX(-50%);
  width: min(84%, 460px);
  text-align: center;
  background: transparent;
  padding: 10px 8px 4px;
  pointer-events: none;
}

.spotlight-card:hover {
  --wave-dur: 5.12s;
  --hover-scale: 1.03;
  box-shadow: none;
}

.spotlight-card.active {
  opacity: 1;
}

.spotlight-kicker {
  margin: 0 0 6px;
  font-size: 0.72rem;
  letter-spacing: 0.16rem;
  opacity: 0.95;
}

.spotlight-card h3 {
  margin: 0;
  font-size: clamp(1.08rem, 1.95vw, 1.7rem);
  line-height: 1.2;
}

.spotlight-meta {
  margin: 6px 0 0;
  font-size: 0.82rem;
  letter-spacing: 0.08rem;
  opacity: 0.96;
}

@keyframes spotlightWave {
  0%, 100% {
    transform: translateY(0) scale(var(--hover-scale, 1));
    box-shadow: none;
  }
  50% {
    transform: translateY(-10px) scale(var(--hover-scale, 1));
    box-shadow: none;
  }
}

@keyframes spotlightImageWave {
  0%, 100% {
    transform: scale(1.04) translate3d(0, 0, 0);
    filter: saturate(1.06) brightness(1);
  }
  50% {
    transform: scale(1.09) translate3d(0, -7px, 0);
    filter: saturate(1.14) brightness(1.05);
  }
}

.user-fab-wrap {
  position: absolute;
  right: 24px;
  top: 16px;
  z-index: 3;
}

.login-fab {
  min-width: 110px;
  height: 36px;
  border: 1px solid rgba(232, 240, 255, 0.42);
  border-radius: 0;
  background: rgba(8, 12, 28, 0.28);
  color: rgba(242, 247, 255, 0.96);
  font-size: 0.72rem;
  letter-spacing: 0.2rem;
  font-weight: 600;
  cursor: pointer;
  backdrop-filter: blur(6px);
  box-shadow: none;
  transition: transform 0.24s ease, background-color 0.24s ease, border-color 0.24s ease;
}

.login-fab:hover {
  transform: translateY(-1px);
  background: rgba(12, 18, 40, 0.5);
  border-color: rgba(246, 251, 255, 0.72);
}

.user-fab {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 14px;
  border: 1px solid rgba(232, 240, 255, 0.42);
  background: rgba(8, 12, 28, 0.28);
  color: rgba(242, 247, 255, 0.96);
  font-size: 0.72rem;
  letter-spacing: 0.12rem;
  font-weight: 600;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.22s ease, border-color 0.22s ease;
  user-select: none;
}

.user-fab:hover {
  background: rgba(12, 18, 40, 0.5);
  border-color: rgba(246, 251, 255, 0.72);
}

.user-fab-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-fab-arrow {
  display: inline-block;
  font-size: 0.85rem;
  transition: transform 0.22s ease;
  line-height: 1;
}

.user-fab-arrow.open {
  transform: rotate(180deg);
}

.user-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  min-width: 120px;
  border: 1px solid rgba(200, 216, 255, 0.28);
  background: rgba(10, 14, 30, 0.92);
  backdrop-filter: blur(8px);
  z-index: 20;
}

.user-menu-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: rgba(228, 236, 255, 0.9);
  font-size: 0.78rem;
  letter-spacing: 0.1rem;
  text-align: left;
  cursor: pointer;
  transition: background 0.18s;
}

.user-menu-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(246, 250, 255, 1);
}

.user-menu-item.logout:hover {
  background: rgba(255, 80, 80, 0.12);
  color: rgba(255, 160, 160, 0.96);
}

.quick-nav {
  position: absolute;
  left: 50%;
  top: 112px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: clamp(26px, 4.2vw, 72px);
  transform: translateX(-50%);
}

.quick-nav-item {
  border: none;
  background: transparent;
  padding: 0;
  color: rgba(228, 236, 255, 0.88);
  font-size: clamp(0.82rem, 1.1vw, 1rem);
  letter-spacing: 0.22rem;
  font-weight: 600;
  cursor: pointer;
  text-shadow: 0 0 14px rgba(88, 114, 255, 0.24);
  transition: color 0.24s ease, text-shadow 0.24s ease, transform 0.24s ease;
}

.quick-nav-item:hover {
  color: rgba(246, 250, 255, 0.98);
  text-shadow: 0 0 18px rgba(170, 148, 255, 0.46);
  transform: translateY(-1px);
}

.login-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(6, 8, 16, 0.5);
  backdrop-filter: blur(6px);
}

.login-modal-card {
  width: min(86vw, 380px);
  border: 1px solid rgba(216, 228, 255, 0.34);
  background: rgba(10, 14, 30, 0.82);
  padding: 20px;
  color: rgba(244, 248, 255, 0.96);
}

.login-modal-card h3 {
  margin: 0;
  letter-spacing: 0.18rem;
  font-size: 1rem;
  font-weight: 700;
}

.login-tip {
  margin: 8px 0 14px;
  color: rgba(205, 218, 255, 0.72);
  font-size: 0.78rem;
}

.login-input {
  width: 100%;
  height: 38px;
  margin: 0 0 10px;
  border: 1px solid rgba(204, 220, 255, 0.3);
  background: rgba(8, 12, 25, 0.78);
  color: rgba(244, 248, 255, 0.95);
  padding: 0 12px;
  outline: none;
}

.login-input:focus {
  border-color: rgba(236, 244, 255, 0.72);
}

.login-actions {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.mode-switch {
  margin: 2px 0 8px;
  border: none;
  background: transparent;
  color: rgba(214, 228, 255, 0.82);
  padding: 0;
  font-size: 0.78rem;
  cursor: pointer;
  text-align: left;
}

.mode-switch:hover {
  color: rgba(241, 247, 255, 0.96);
}

.login-btn {
  min-width: 82px;
  height: 34px;
  border: 1px solid rgba(208, 222, 255, 0.45);
  background: transparent;
  color: rgba(240, 246, 255, 0.94);
  cursor: pointer;
}

.login-btn.primary {
  background: rgba(225, 236, 255, 0.16);
}

.login-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .title-overlay {
    top: 12px;
  }

  .spotlight-card {
    border-radius: 0;
  }

  .spotlight-viewport {
    width: 100vw;
    transform: translateY(-40%);
  }

  .spotlight-card {
    border-radius: 0;
  }

  .login-fab {
    right: 12px;
    top: 10px;
    min-width: 96px;
    height: 32px;
    font-size: 0.68rem;
    letter-spacing: 0.16rem;
  }

  .quick-nav {
    top: 92px;
    gap: 18px;
  }

  .quick-nav-item {
    font-size: 0.76rem;
    letter-spacing: 0.14rem;
  }
}
</style>
