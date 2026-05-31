<script setup lang="ts">
import { onMounted, ref } from 'vue'

const SPLASH_SESSION_KEY = 'dumb_splash_shown'
const SPLASH_REVEAL_MS = 3600
const SPLASH_EXTRA_HOLD_MS = 2000
const SPLASH_FADE_MS = 1000

const splashVisible = ref(false)
const splashFading = ref(false)

onMounted(() => {
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
</template>

<style scoped>
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
</style>
