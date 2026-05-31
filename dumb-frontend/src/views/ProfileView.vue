<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'

const authStore = useAuthStore()
</script>

<template>
  <section class="profile-page">
    <FlowSceneBackdrop />
    <div class="profile-content">
      <h2 class="page-title">个人主页</h2>
      <el-descriptions class="panel" border :column="1">
        <el-descriptions-item label="用户名">{{ authStore.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ authStore.role || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ authStore.isLoggedIn ? '已登录' : '未登录' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </section>
</template>

<style scoped>
.profile-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.profile-content {
  position: relative;
  z-index: 1;
}

.page-title {
  margin: 0 0 14px;
  text-align: center;
  letter-spacing: 4px;
}

.panel {
  background: #0d0d0d;
  border: 1px solid #222222;
  backdrop-filter: blur(7px);
  padding: 10px;
}
</style>
