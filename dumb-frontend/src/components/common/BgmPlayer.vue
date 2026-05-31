<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

const audioRef = ref<HTMLAudioElement>()
const muted = ref(false)
const playing = ref(false)
const currentIndex = ref(-1)

const PLAYLIST = [
  '/audio/playlist/01-the-cure.m4a',
  '/audio/playlist/02-no-surprises.m4a',
  '/audio/playlist/03-choke-enough.m4a',
  '/audio/playlist/04-last-man.m4a',
  '/audio/playlist/05-goodbye.m4a',
  '/audio/playlist/06-like.m4a',
  '/audio/playlist/07-the-field.m4a',
  '/audio/playlist/08-coma.m4a',
]

let playedIndices: number[] = []

function shuffleNext(): number {
  if (playedIndices.length === 0 || playedIndices.length >= PLAYLIST.length) {
    playedIndices = []
  }
  const available = PLAYLIST.map((_, i) => i).filter(i => !playedIndices.includes(i))
  const pick = available[Math.floor(Math.random() * available.length)]!
  playedIndices.push(pick)
  return pick
}

function playRandom() {
  const audio = audioRef.value
  if (!audio) return
  const idx = shuffleNext()
  currentIndex.value = idx
  audio.src = PLAYLIST[idx]!
  audio.load()
  audio.play().then(() => { playing.value = true }).catch(() => {})
}

function toggleMute() {
  const audio = audioRef.value
  if (!audio) return
  muted.value = !muted.value
  audio.muted = muted.value
}

onMounted(() => {
  const audio = audioRef.value
  if (!audio) return
  audio.volume = 0.5
  audio.muted = false

  // play random track
  playRandom()

  // when track ends, play next randomly
  audio.addEventListener('ended', playRandom)

  // if autoplay blocked, start on first interaction
  if (audio.paused) {
    const startPlay = () => {
      if (audio.paused) playRandom()
      document.removeEventListener('click', startPlay)
      document.removeEventListener('keydown', startPlay)
    }
    document.addEventListener('click', startPlay, { once: false })
    document.addEventListener('keydown', startPlay, { once: false })
  }
})

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value.removeEventListener('ended', playRandom)
  }
})
</script>

<template>
  <div class="bgm-player">
    <audio ref="audioRef" preload="auto" />
    <button
      class="bgm-btn"
      :class="{ 'is-muted': muted }"
      @click="toggleMute"
      :title="muted ? 'Unmute' : 'Mute'"
    >
      <span class="note" :class="{ spinning: !muted }">♪</span>
    </button>
  </div>
</template>

<style scoped>
.bgm-player {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.bgm-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.25s ease;
}

.bgm-btn:hover {
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.25);
}

.bgm-btn.is-muted {
  color: rgba(255, 255, 255, 0.3);
}

.note {
  font-size: 20px;
  line-height: 1;
  display: block;
}

.note.spinning {
  animation: spin 3s linear infinite;
}

.bgm-btn.is-muted .note {
  animation: none;
  opacity: 0.35;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .bgm-player {
    bottom: 16px;
    right: 16px;
  }
  .bgm-btn {
    width: 36px;
    height: 36px;
  }
}
</style>
