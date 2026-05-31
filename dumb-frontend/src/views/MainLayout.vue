<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '../components/common/AppFooter.vue'

const route = useRoute()
const isAdminPage = computed(() => route.name === 'admin')
const isHomePage = computed(() => route.name === 'home')
</script>

<template>
  <div class="layout">
    <main class="content" :class="{ 'admin-content': isAdminPage, 'home-content': isHomePage }">
      <router-view v-slot="{ Component }">
        <keep-alive :include="['HomeView']">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
    <AppFooter v-if="!isAdminPage && !isHomePage" />
  </div>
</template>

<style scoped>
.layout { min-height: 100vh; background: var(--dumb-bg); color: var(--dumb-text); }
.content { max-width: 1200px; margin: 0 auto; padding: 18px; }
.content.admin-content { max-width: none; margin: 0; padding: 0; }
.content.home-content { max-width: none; margin: 0; padding: 0; min-height: 100vh; }
</style>
