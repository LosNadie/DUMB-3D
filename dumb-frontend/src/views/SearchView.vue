<script setup lang="ts">
import { ref } from 'vue'
import { reviewApi } from '../api'
import type { ReviewItem } from '../types/models'
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'

const keyword = ref('')
const list = ref<ReviewItem[]>([])

async function doSearch() {
  const res = await reviewApi.list({ keyword: keyword.value })
  list.value = res.data
}
</script>

<template>
  <section class="search-page">
    <FlowSceneBackdrop />
    <div class="search-content">
      <h2 class="page-title">搜索</h2>
      <div class="search-wrap panel">
        <el-input v-model="keyword" placeholder="输入艺人、专辑或关键词" />
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </div>
      <el-table :data="list" class="panel table-panel">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="artist" label="艺人" />
        <el-table-column prop="albumTitle" label="专辑" />
        <el-table-column prop="score" label="评分" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.search-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.search-content {
  position: relative;
  z-index: 1;
}

.page-title {
  margin: 0 0 14px;
  text-align: center;
  letter-spacing: 4px;
}

.panel {
  background: rgba(8, 10, 22, 0.54);
  border: 1px solid rgba(120, 136, 255, 0.2);
  backdrop-filter: blur(7px);
}

.search-wrap {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  padding: 12px;
}

.table-panel {
  padding: 8px;
}
</style>
