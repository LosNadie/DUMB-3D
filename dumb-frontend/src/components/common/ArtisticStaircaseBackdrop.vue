<script setup lang="ts">
/*
 * ArtisticStaircaseBackdrop
 * 纯CSS/SVG艺术楼梯底图效果
 * 用于需要暖色调木质楼梯氛围的页面背景
 */
</script>

<template>
  <div class="staircase-backdrop">
    <!-- 底层深色背景 -->
    <div class="staircase-base"></div>

    <!-- SVG 定义：木纹滤镜和渐变 -->
    <svg class="staircase-svg-defs" width="0" height="0">
      <defs>
        <!-- 木纹噪点滤镜 -->
        <filter id="woodGrain" x="0%" y="0%" width="100%" height="100%">
          <feTurbulence type="fractalNoise" baseFrequency="0.008 0.15" numOctaves="4" result="noise" />
          <feColorMatrix type="matrix" values="
            0.5 0 0 0 0.1
            0 0.4 0 0 0.05
            0 0 0.3 0 0.02
            0 0 0 0.3 0
          " in="noise" result="coloredNoise" />
          <feBlend mode="multiply" in="coloredNoise" in2="SourceGraphic" result="grain" />
        </filter>

        <!-- 台阶顶面线性渐变 -->
        <linearGradient id="stepTopGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#2a1f12" />
          <stop offset="30%" stop-color="#1e150a" />
          <stop offset="70%" stop-color="#140e06" />
          <stop offset="100%" stop-color="#0d0803" />
        </linearGradient>

        <!-- 台阶侧面渐变 -->
        <linearGradient id="stepSideGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#1a1208" />
          <stop offset="50%" stop-color="#0f0a04" />
          <stop offset="100%" stop-color="#080502" />
        </linearGradient>

        <!-- 台阶前缘高光 -->
        <linearGradient id="stepEdgeGrad" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#8b6914" stop-opacity="0.5" />
          <stop offset="50%" stop-color="#b8860b" stop-opacity="0.2" />
          <stop offset="100%" stop-color="#8b6914" stop-opacity="0.5" />
        </linearGradient>

        <!-- 暖光径向渐变 -->
        <radialGradient id="warmLight" cx="50%" cy="0%" r="80%" fx="50%" fy="0%">
          <stop offset="0%" stop-color="#b8860b" stop-opacity="0.18" />
          <stop offset="25%" stop-color="#8b6914" stop-opacity="0.1" />
          <stop offset="55%" stop-color="#1a1208" stop-opacity="0.4" />
          <stop offset="100%" stop-color="#0d0803" stop-opacity="0.85" />
        </radialGradient>

        <!-- 侧壁阴影渐变 -->
        <linearGradient id="wallShadow" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#0d0803" stop-opacity="0.9" />
          <stop offset="30%" stop-color="#1a1208" stop-opacity="0.5" />
          <stop offset="70%" stop-color="#1a1208" stop-opacity="0.3" />
          <stop offset="100%" stop-color="#0d0803" stop-opacity="0.8" />
        </linearGradient>
      </defs>
    </svg>

    <!-- 楼梯容器 -->
    <div class="staircase-container">
      <!-- 左侧墙壁 -->
      <div class="wall wall-left"></div>

      <!-- 右侧墙壁 -->
      <div class="wall wall-right"></div>

      <!-- 台阶组 -->
      <div class="steps-group">
        <!-- 每个台阶由顶面+侧面组成 -->
        <div class="step" style="--step-index: 0;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 1;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 2;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 3;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 4;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 5;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 6;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 7;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 8;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 9;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 10;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
        <div class="step" style="--step-index: 11;">
          <div class="step-top"></div>
          <div class="step-face"></div>
        </div>
      </div>

      <!-- 顶部暖光照明层 -->
      <div class="warm-light-overlay"></div>

      <!-- 底部暗角 -->
      <div class="vignette-overlay"></div>
    </div>

    <!-- 尘埃粒子层 -->
    <div class="dust-particles">
      <span class="dust" style="--d: 0; --x: 15; --y: 20; --s: 1.5;"></span>
      <span class="dust" style="--d: 1; --x: 35; --y: 45; --s: 1;"></span>
      <span class="dust" style="--d: 2; --x: 55; --y: 15; --s: 2;"></span>
      <span class="dust" style="--d: 3; --x: 75; --y: 60; --s: 1.2;"></span>
      <span class="dust" style="--d: 4; --x: 25; --y: 70; --s: 0.8;"></span>
      <span class="dust" style="--d: 5; --x: 85; --y: 30; --s: 1.8;"></span>
      <span class="dust" style="--d: 6; --x: 45; --y: 80; --s: 1;"></span>
      <span class="dust" style="--d: 7; --x: 65; --y: 40; --s: 1.4;"></span>
      <span class="dust" style="--d: 8; --x: 10; --y: 55; --s: 0.9;"></span>
      <span class="dust" style="--d: 9; --x: 90; --y: 75; --s: 1.6;"></span>
      <span class="dust" style="--d: 10; --x: 30; --y: 35; --s: 1.1;"></span>
      <span class="dust" style="--d: 11; --x: 70; --y: 85; --s: 0.7;"></span>
      <span class="dust" style="--d: 12; --x: 50; --y: 50; --s: 2.2;"></span>
      <span class="dust" style="--d: 13; --x: 20; --y: 90; --s: 1.3;"></span>
      <span class="dust" style="--d: 14; --x: 80; --y: 10; --s: 0.6;"></span>
      <span class="dust" style="--d: 15; --x: 40; --y: 65; --s: 1.7;"></span>
      <span class="dust" style="--d: 16; --x: 60; --y: 25; --s: 0.5;"></span>
      <span class="dust" style="--d: 17; --x: 5; --y: 40; --s: 1.9;"></span>
      <span class="dust" style="--d: 18; --x: 95; --y: 55; --s: 1;"></span>
      <span class="dust" style="--d: 19; --x: 50; --y: 5; --s: 1.4;"></span>
    </div>
  </div>
</template>

<style scoped>
/* ============================================================
   艺术楼梯底图 - 纯CSS/SVG实现
   色调：深棕(#1a1208)、琥珀(#b8860b)、暗金(#8b6914)、暖黑(#0d0803)
   ============================================================ */

/* ---- 基础容器 ---- */
.staircase-backdrop {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  background: #0d0803;
  pointer-events: none;
}

/* 底层深色背景 */
.staircase-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% 0%, #1a1208 0%, transparent 70%),
    linear-gradient(180deg, #0d0803 0%, #080502 100%);
}

/* SVG 定义隐藏 */
.staircase-svg-defs {
  position: absolute;
  width: 0;
  height: 0;
  pointer-events: none;
}

/* ---- 楼梯透视容器 ---- */
.staircase-container {
  position: absolute;
  inset: 0;
  perspective: 900px;
  perspective-origin: 50% 30%;
  transform-style: preserve-3d;
}

/* ---- 侧墙壁 ---- */
.wall {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 18%;
  background: linear-gradient(180deg, #1a1208 0%, #0d0803 60%, #080502 100%);
  z-index: 1;
}

.wall-left {
  left: 0;
  background:
    linear-gradient(90deg, #080502 0%, #0d0803 30%, transparent 100%),
    linear-gradient(180deg, #1a1208 0%, #0f0a04 50%, #080502 100%);
  box-shadow: inset -20px 0 40px rgba(0, 0, 0, 0.7);
}

.wall-right {
  right: 0;
  background:
    linear-gradient(-90deg, #080502 0%, #0d0803 30%, transparent 100%),
    linear-gradient(180deg, #1a1208 0%, #0f0a04 50%, #080502 100%);
  box-shadow: inset 20px 0 40px rgba(0, 0, 0, 0.7);
}

/* ---- 台阶组 - 透视排列 ---- */
.steps-group {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%) rotateX(55deg);
  transform-style: preserve-3d;
  width: 64%;
  height: 120%;
  display: flex;
  flex-direction: column-reverse;
  align-items: center;
  justify-content: flex-start;
  gap: 0;
}

/* ---- 单个台阶 ---- */
.step {
  position: relative;
  width: calc(55% + var(--step-index) * 4.5%);
  height: calc(5% + var(--step-index) * 0.6%);
  flex-shrink: 0;
  transform-style: preserve-3d;
  /* 越远越小，模拟透视 */
  transform: translateZ(calc(var(--step-index) * -12px));
}

/* 台阶顶面 */
.step-top {
  position: absolute;
  inset: 0;
  background:
    /* 木纹纹理 - 多层线性渐变模拟 */
    repeating-linear-gradient(
      90deg,
      transparent 0px,
      transparent 2px,
      rgba(139, 105, 20, 0.03) 2px,
      rgba(139, 105, 20, 0.03) 3px,
      transparent 3px,
      transparent 8px,
      rgba(184, 134, 11, 0.02) 8px,
      rgba(184, 134, 11, 0.02) 10px,
      transparent 10px,
      transparent 18px,
      rgba(139, 105, 20, 0.025) 18px,
      rgba(139, 105, 20, 0.025) 20px,
      transparent 20px,
      transparent 35px
    ),
    /* 顶面基础渐变 */
    linear-gradient(
      180deg,
      hsl(28, 35%, calc(12% + var(--step-index) * 0.8%)) 0%,
      hsl(28, 30%, calc(8% + var(--step-index) * 0.5%)) 50%,
      hsl(28, 25%, calc(5% + var(--step-index) * 0.3%)) 100%
    );
  /* 顶面光泽 */
  box-shadow:
    inset 0 1px 0 rgba(184, 134, 11, 0.15),
    inset 0 -1px 0 rgba(0, 0, 0, 0.4);
  border-radius: 1px;
}

/* 台阶顶面前缘高光 */
.step-top::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(184, 134, 11, calc(0.08 + var(--step-index) * 0.015)) 20%,
    rgba(184, 134, 11, calc(0.12 + var(--step-index) * 0.02)) 50%,
    rgba(184, 134, 11, calc(0.08 + var(--step-index) * 0.015)) 80%,
    transparent 100%
  );
}

/* 台阶顶面微妙的木纹横纹 */
.step-top::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 4px,
    rgba(26, 18, 8, 0.08) 4px,
    rgba(26, 18, 8, 0.08) 5px,
    transparent 5px,
    transparent 12px,
    rgba(13, 8, 3, 0.06) 12px,
    rgba(13, 8, 3, 0.06) 14px,
    transparent 14px,
    transparent 22px
  );
  opacity: 0.6;
  mix-blend-mode: multiply;
}

/* 台阶侧面（立板） */
.step-face {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  height: 28px;
  background:
    linear-gradient(
      180deg,
      hsl(25, 30%, calc(8% + var(--step-index) * 0.4%)) 0%,
      hsl(25, 25%, calc(5% + var(--step-index) * 0.2%)) 60%,
      #080502 100%
    );
  transform-origin: top;
  transform: rotateX(-90deg);
  box-shadow:
    inset 0 -4px 8px rgba(0, 0, 0, 0.5),
    0 4px 12px rgba(0, 0, 0, 0.4);
}

/* 台阶侧面底部阴影 */
.step-face::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.5) 100%);
}

/* ---- 暖光照明层 ---- */
.warm-light-overlay {
  position: absolute;
  inset: 0;
  background:
    /* 顶部中央暖光 */
    radial-gradient(
      ellipse 70% 55% at 50% 5%,
      rgba(184, 134, 11, 0.14) 0%,
      rgba(139, 105, 20, 0.08) 30%,
      transparent 65%
    ),
    /* 左侧壁灯暖光 */
    radial-gradient(
      ellipse 25% 40% at 12% 25%,
      rgba(184, 134, 11, 0.08) 0%,
      transparent 60%
    ),
    /* 右侧壁灯暖光 */
    radial-gradient(
      ellipse 25% 40% at 88% 35%,
      rgba(184, 134, 11, 0.06) 0%,
      transparent 60%
    ),
    /* 底部环境反射 */
    radial-gradient(
      ellipse 80% 30% at 50% 100%,
      rgba(139, 105, 20, 0.04) 0%,
      transparent 50%
    );
  pointer-events: none;
  z-index: 5;
  mix-blend-mode: screen;
}

/* ---- 暗角层 ---- */
.vignette-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      ellipse 75% 60% at 50% 45%,
      transparent 40%,
      rgba(8, 5, 2, 0.4) 75%,
      rgba(13, 8, 3, 0.7) 100%
    );
  pointer-events: none;
  z-index: 6;
}

/* ---- 尘埃粒子效果 ---- */
.dust-particles {
  position: absolute;
  inset: 0;
  z-index: 7;
  pointer-events: none;
  overflow: hidden;
}

.dust {
  position: absolute;
  left: calc(var(--x) * 1%);
  top: calc(var(--y) * 1%);
  width: calc(var(--s) * 2px);
  height: calc(var(--s) * 2px);
  background: radial-gradient(
    circle,
    rgba(184, 134, 11, calc(0.25 + var(--s) * 0.08)) 0%,
    rgba(184, 134, 11, calc(0.1 + var(--s) * 0.03)) 40%,
    transparent 70%
  );
  border-radius: 50%;
  filter: blur(0.5px);
  animation: dustFloat calc(12s + var(--d) * 1.5s) ease-in-out infinite;
  animation-delay: calc(var(--d) * -0.8s);
  opacity: 0;
}

@keyframes dustFloat {
  0% {
    transform: translate(0, 0) scale(0.5);
    opacity: 0;
  }
  15% {
    opacity: calc(0.3 + var(--s) * 0.15);
  }
  50% {
    transform: translate(
      calc((var(--d) % 3 - 1) * 30px + sin(var(--d)) * 20px),
      calc(-20px - var(--d) * 2px)
    ) scale(1);
    opacity: calc(0.2 + var(--s) * 0.1);
  }
  85% {
    opacity: calc(0.15 + var(--s) * 0.08);
  }
  100% {
    transform: translate(
      calc((var(--d) % 3 - 1) * 50px + sin(var(--d)) * 35px),
      calc(-50px - var(--d) * 4px)
    ) scale(0.3);
    opacity: 0;
  }
}

/* ---- 额外的大颗粒尘埃（更慢、更朦胧） ---- */
.dust-particles::before {
  content: '';
  position: absolute;
  left: 25%;
  top: 30%;
  width: 3px;
  height: 3px;
  background: radial-gradient(circle, rgba(184, 134, 11, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(1px);
  animation: dustFloatLarge 18s ease-in-out infinite;
}

.dust-particles::after {
  content: '';
  position: absolute;
  left: 70%;
  top: 55%;
  width: 4px;
  height: 4px;
  background: radial-gradient(circle, rgba(184, 134, 11, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(1.5px);
  animation: dustFloatLarge 22s ease-in-out infinite;
  animation-delay: -8s;
}

@keyframes dustFloatLarge {
  0%, 100% {
    transform: translate(0, 0);
    opacity: 0;
  }
  20% {
    opacity: 0.2;
  }
  50% {
    transform: translate(20px, -40px);
    opacity: 0.15;
  }
  80% {
    opacity: 0.1;
  }
}

/* ---- 响应式调整 ---- */
@media (max-width: 768px) {
  .steps-group {
    width: 80%;
    transform: translate(-50%, -50%) rotateX(60deg);
  }

  .wall {
    width: 10%;
  }

  .step-face {
    height: 18px;
  }
}

@media (max-width: 480px) {
  .steps-group {
    width: 90%;
    transform: translate(-50%, -50%) rotateX(65deg);
  }

  .wall {
    width: 5%;
  }
}
</style>
