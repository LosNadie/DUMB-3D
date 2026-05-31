<script setup lang="ts">
import FlowSceneBackdrop from '../components/common/FlowSceneBackdrop.vue'
</script>

<template>
  <section class="about-page">
    <FlowSceneBackdrop />
    <div class="about-content-wrap">
      <h1>关于 DUMB</h1>
      <section class="about-content panel">
        <p>DUMB 是一个专注于音乐、动漫、电影评论的平台。</p>
        <p>我们致力于为用户提供高质量的内容和独特的视角。</p>
      </section>
    </div>
  </section>
</template>

<style scoped>
.about-page {
  position: relative;
  min-height: calc(100vh - 30px);
}

.about-content-wrap {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 60px 20px;
}

h1 {
  font-family: 'Playfair Display', serif;
  font-size: 3rem;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  margin-bottom: 60px;
  letter-spacing: 2px;
}

.about-content {
  background: #0d0d0d;
  border: 1px solid #222222;
  backdrop-filter: blur(7px);
  padding: 40px;
  border-radius: 0;
}

.about-content p {
  font-family: 'Inter', sans-serif;
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--dumb-text);
  margin-bottom: 20px;
}

.about-content p:last-child {
  margin-bottom: 0;
}
</style>
