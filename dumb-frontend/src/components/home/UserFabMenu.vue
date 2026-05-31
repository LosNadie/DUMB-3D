<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const emit = defineEmits<{
  (e: 'openLogin'): void
}>()

const router = useRouter()
const authStore = useAuthStore()
const userMenuOpen = ref(false)

function handleLogout() {
  authStore.logout()
  userMenuOpen.value = false
}

function goAdmin() {
  userMenuOpen.value = false
  void router.push('/admin')
}

function openLogin() {
  emit('openLogin')
}
</script>

<template>
  <div class="user-fab-wrap">
    <button v-if="!authStore.isLoggedIn" class="login-fab" @click="openLogin">LOGIN</button>
    <div v-else class="user-fab" @click="userMenuOpen = !userMenuOpen">
      <span class="user-fab-name">{{ authStore.username }}</span>
      <span class="user-fab-arrow" :class="{ open: userMenuOpen }">▾</span>
    </div>
    <div v-if="authStore.isLoggedIn && userMenuOpen" class="user-menu" @click.stop>
      <button v-if="authStore.isAdminLike" class="user-menu-item" @click="goAdmin">后台</button>
      <button class="user-menu-item logout" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<style scoped>
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
  border-radius: 18px;
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
  border-radius: 18px;
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

@media (max-width: 900px) {
  .login-fab {
    right: 12px;
    top: 10px;
    min-width: 96px;
    height: 32px;
    font-size: 0.68rem;
    letter-spacing: 0.16rem;
  }
}
</style>
