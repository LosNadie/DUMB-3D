<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

const videoRef = ref<HTMLVideoElement>()

onMounted(() => {
  const video = videoRef.value
  if (!video) return

  video.volume = 0
  video.loop = false

  const pickRandomStart = () => {
    const duration = video.duration
    if (duration > 10) {
      video.currentTime = Math.random() * (duration - 10)
    }
  }

  video.addEventListener('loadedmetadata', pickRandomStart)
  video.play().catch(() => {})
})

onUnmounted(() => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
})
</script>

<template>
  <div class="video-bg">
    <video
      ref="videoRef"
      src="/videos/music-bg.mp4"
      autoplay
      muted
      playsinline
      preload="auto"
    />
    <div class="video-overlay" />
  </div>
</template>

<style scoped>
.video-bg {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  transform: translate(-50%, -50%);
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
}
</style>
