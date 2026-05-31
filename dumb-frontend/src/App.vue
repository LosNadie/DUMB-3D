<script setup lang="ts">
import { onMounted } from 'vue'
import BgmPlayer from './components/common/BgmPlayer.vue'

onMounted(() => {
  // 确保暗色主题始终激活
  document.documentElement.classList.add('dark')
})
</script>

<template>
  <router-view />
  <BgmPlayer />
</template>

<style>
/* 确保整个应用始终使用暗色背景 */
html {
  background-color: #0a0a0a;
  color-scheme: dark;
}

html.dark {
  background-color: #0a0a0a;
}
</style>
