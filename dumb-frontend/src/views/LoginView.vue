<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '', email: '' })
const isRegister = reactive({ value: false })

async function submit() {
  try {
    const res = isRegister.value
      ? await authApi.register(form)
      : await authApi.login({ username: form.username, password: form.password })
    authStore.setAuth(res.data.token, res.data.username, res.data.role)
    ElMessage.success('登录成功')
    if (res.data.role === 'ADMIN' || res.data.role === 'EDITOR') {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}
</script>

<template>
  <section class="login-page">
    <FlowSceneBackdrop />
    <div class="login-content">
      <el-card class="login-card">
        <h2>{{ isRegister.value ? '注册 DUMB' : '登录 DUMB' }}</h2>
        <el-form label-width="70px">
          <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
          <el-form-item v-if="isRegister.value" label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">提交</el-button>
            <el-button text @click="isRegister.value = !isRegister.value">{{ isRegister.value ? '去登录' : '去注册' }}</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
}

.login-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  place-items: center;
}

.login-card {
  width: 420px;
  max-width: 90vw;
  background: #0d0d0d;
  color: var(--dumb-text);
  border-color: #222222;
  backdrop-filter: blur(8px);
}
</style>
