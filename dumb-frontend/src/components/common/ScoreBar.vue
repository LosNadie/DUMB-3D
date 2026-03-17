<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ score: number }>()
const percent = computed(() => Math.max(0, Math.min(100, props.score * 10)))
const scoreClass = computed(() => {
  if (props.score < 7) return 'low'
  if (props.score < 8) return 'mid'
  return 'high'
})
</script>

<template>
  <div class="score-wrap">
    <span class="score-text">{{ score.toFixed(1) }}</span>
    <div class="bar">
      <div class="fill" :class="scoreClass" :style="{ width: `${percent}%` }"></div>
    </div>
  </div>
</template>

<style scoped>
.score-wrap { display: flex; align-items: center; gap: 10px; }
.score-text { min-width: 38px; font-weight: 700; font-size: 1.2rem; font-family: 'Playfair Display', serif; }
.bar { flex: 1; height: 10px; border-radius: 2px; background: #222222; overflow: hidden; border: 1px solid #333333; }
.fill { height: 100%; transition: width .3s; background: #ffffff; }
.low { opacity: 0.4; }
.mid { opacity: 0.7; }
.high { opacity: 1; }
</style>
