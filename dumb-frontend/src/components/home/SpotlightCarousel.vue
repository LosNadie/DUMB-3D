<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

export interface SpotlightCard {
  id: number
  title: string
  subtitle: string
  image: string
  time: number
  route: string
  kicker: string
}

interface Props {
  cards: SpotlightCard[]
  initialIndex?: number
}

const props = withDefaults(defineProps<Props>(), {
  initialIndex: 0,
})

const emit = defineEmits<{
  (e: 'update:activeIndex', index: number): void
  (e: 'open', route: string): void
}>()

const viewportRef = ref<HTMLElement | null>(null)
const viewportWidth = ref(window.innerWidth)
const activeIndex = ref(props.initialIndex)
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

const cardGap = computed(() => (viewportWidth.value <= 900 ? 12 * 8 : 16 * 8))
const cardWidth = computed(() => {
  if (viewportWidth.value <= 900) {
    return Math.min(viewportWidth.value * 0.72, 380)
  }
  return Math.min(viewportWidth.value * 0.38, 520)
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
  transition: isDragging.value ? 'none' : 'transform 500ms cubic-bezier(0.22, 1, 0.36, 1)',
}))

function updateViewportSize() {
  viewportWidth.value = viewportRef.value?.clientWidth || window.innerWidth
}

function applyRubberBand(offset: number, dimension: number) {
  const c = 0.55
  return (dimension * c * offset) / (dimension + c * Math.abs(offset))
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

function onPointerDown(event: PointerEvent) {
  if (props.cards.length <= 1) return
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

function onPointerMove(event: PointerEvent) {
  if (!isDragging.value) return
  const rawOffset = event.clientX - dragStartX.value
  const atFirstAndPullingRight = activeIndex.value === 0 && rawOffset > 0
  const atLastAndPullingLeft = activeIndex.value === props.cards.length - 1 && rawOffset < 0
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
    const maxOffset = stepDistance * Math.max(1, props.cards.length - 1)
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
        emit('update:activeIndex', tappedIndex)
        isSnapping.value = true
        clearSnapTimer()
        snapTimer = window.setTimeout(() => {
          isSnapping.value = false
        }, 300)
      } else {
        const route = props.cards[tappedIndex]?.route
        if (route) emit('open', route)
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
      activeIndex.value = Math.min(activeIndex.value + stepCount, props.cards.length - 1)
    } else {
      activeIndex.value = Math.max(activeIndex.value - stepCount, 0)
    }
    emit('update:activeIndex', activeIndex.value)
  }
  dragOffsetX.value = 0
  dragVelocityX.value = 0
  isSnapping.value = true
  clearSnapTimer()
  snapTimer = window.setTimeout(() => {
    isSnapping.value = false
  }, 300)
}

function onPointerUp() {
  finishDragging()
}

function onPointerCancel() {
  finishDragging()
}

function onLostPointerCapture() {
  finishDragging()
}

function openByIndex(index: number) {
  if (index < 0 || index >= props.cards.length) return
  if (index !== activeIndex.value) {
    activeIndex.value = index
    emit('update:activeIndex', index)
    dragOffsetX.value = 0
    dragVelocityX.value = 0
    isSnapping.value = true
    clearSnapTimer()
    snapTimer = window.setTimeout(() => {
      isSnapping.value = false
    }, 300)
    return
  }
  const route = props.cards[index]?.route
  if (!route) return
  emit('open', route)
}

function goToIndex(index: number) {
  if (index < 0 || index >= props.cards.length) return
  activeIndex.value = index
  emit('update:activeIndex', index)
  isSnapping.value = true
  clearSnapTimer()
  snapTimer = window.setTimeout(() => {
    isSnapping.value = false
  }, 300)
}

onMounted(() => {
  updateViewportSize()
  window.addEventListener('resize', updateViewportSize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportSize)
  clearSnapTimer()
})
</script>

<template>
  <div v-if="cards.length > 0" class="spotlight-wrap">
    <!-- 轮播区 -->
    <div
      ref="viewportRef"
      class="spotlight-viewport"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerCancel"
      @lostpointercapture="onLostPointerCapture"
    >
      <div class="spotlight-track" :style="[trackStyle, trackTransitionStyle]">
        <button
          v-for="(item, index) in cards"
          :key="item.id"
          class="spotlight-card"
          :class="{ active: index === activeIndex }"
          :data-index="index"
          @click.prevent="openByIndex(index)"
          :style="{ '--card-delay': `${index * 0.18}s`, width: `${cardWidth}px` }"
        >
          <div class="card-frame">
            <div class="card-image" :style="{ backgroundImage: `url(${item.image})` }" />
            <div class="card-gradient" />
            <div class="card-text">
              <p class="spotlight-kicker">{{ item.kicker }}</p>
              <h3>{{ item.title }}</h3>
              <p class="spotlight-meta">{{ item.subtitle }}</p>
            </div>
            <div class="card-shine" />
          </div>
        </button>
      </div>
    </div>

    <!-- 导航点 -->
    <div class="spotlight-dots">
      <button
        v-for="(_, i) in cards"
        :key="i"
        class="dot"
        :class="{ active: i === activeIndex }"
        @click="goToIndex(i)"
      />
    </div>
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════════════
   SPOTLIGHT CAROUSEL — Cinematic / Glass / Immersive
   ═══════════════════════════════════════════════ */

.spotlight-wrap {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  z-index: 3;
}

/* ── 轮播视口 ── */
.spotlight-viewport {
  width: 100vw;
  overflow: hidden;
  touch-action: pan-y;
  user-select: none;
  cursor: grab;
  padding: 40px 0;
}

.spotlight-viewport:active {
  cursor: grabbing;
}

.spotlight-track {
  display: flex;
  align-items: stretch;
  will-change: transform;
}

/* ── 卡片 ── */
.spotlight-card {
  position: relative;
  flex: 0 0 auto;
  aspect-ratio: 16 / 10;
  border: none;
  background: transparent;
  overflow: visible;
  text-align: center;
  padding: 0;
  color: rgba(250, 252, 255, 0.96);
  cursor: pointer;
  opacity: 0.5;
  transform: scale(0.92);
  transition: opacity 0.5s ease, transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.spotlight-card.active {
  opacity: 1;
  transform: scale(1);
}

/* 卡片外框 */
.card-frame {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.03) inset;
  transition: box-shadow 0.4s ease, border-color 0.4s ease;
}

.spotlight-card.active .card-frame {
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset,
    0 0 60px rgba(255, 255, 255, 0.03);
}

/* 图片 */
.card-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transition: transform 0.6s ease;
}

.spotlight-card:hover .card-image {
  transform: scale(1.05);
}

/* 渐变遮罩 — 底部更暗确保文字可读 */
.card-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.92) 0%, rgba(0,0,0,0.5) 30%, rgba(0,0,0,0.1) 55%, transparent 75%);
  pointer-events: none;
}

/* 文字直接浮在图片上 */
.card-text {
  position: absolute;
  left: 50%;
  bottom: 7%;
  transform: translateX(-50%);
  width: min(90%, 440px);
  text-align: center;
  pointer-events: none;
}

/* 高光扫过效果 */
.card-shine {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  pointer-events: none;
  opacity: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.03) 45%, rgba(255,255,255,0.06) 50%, rgba(255,255,255,0.03) 55%, transparent 60%);
  transition: opacity 0.4s ease;
}

.spotlight-card:hover .card-shine {
  opacity: 1;
}

/* ── 文字 ── */
.spotlight-kicker {
  margin: 0 0 6px;
  font-family: 'Cinzel', serif;
  font-size: 0.62rem;
  letter-spacing: 0.2rem;
  color: rgba(255, 255, 255, 0.65);
  text-transform: uppercase;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.8);
}

.spotlight-card h3 {
  margin: 0;
  font-family: 'Playfair Display', serif;
  font-size: clamp(1.1rem, 1.8vw, 1.5rem);
  font-weight: 600;
  line-height: 1.25;
  color: #fff;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.9), 0 1px 3px rgba(0, 0, 0, 0.6);
}

.spotlight-meta {
  margin: 8px 0 0;
  font-size: 0.78rem;
  letter-spacing: 0.06rem;
  color: rgba(255, 255, 255, 0.5);
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.8);
}

/* ── 导航点 ── */
.spotlight-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 28px;
  position: relative;
  z-index: 3;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  padding: 0;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.dot.active {
  width: 28px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.9);
}

.dot:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* ── 响应式 ── */
@media (max-width: 900px) {
  .spotlight-wrap {
    transform: translateY(-48%);
  }

  .spotlight-viewport {
    padding: 24px 0;
  }

  .spotlight-card {
    aspect-ratio: 16 / 10.5;
  }

  .card-frame {
    border-radius: 12px;
  }

  .card-glass {
    padding: 12px 14px 10px;
    border-radius: 8px;
  }

  .spotlight-card h3 {
    font-size: 1.05rem;
  }

  .spotlight-meta {
    font-size: 0.72rem;
  }

  .spotlight-dots {
    margin-top: 20px;
  }
}
</style>
