<script setup lang="ts">
/**
 * StairRecordShop - 楼梯唱片店 3D 场景
 *
 * 纯 CSS 3D 实现：
 * - 楼梯向下延伸，近大远小透视
 * - 台阶有 tread（水平面）+ riser（垂直面）
 * - 左右墙面挂唱片（纯 CSS 绘制）
 * - 使用 perspective + rotateX/Y/Z + translateZ
 */

interface StepConfig {
  index: number
  total: number
}

const STEP_COUNT = 14

function generateSteps(): StepConfig[] {
  return Array.from({ length: STEP_COUNT }, (_, i) => ({
    index: i,
    total: STEP_COUNT,
  }))
}

function generateLeftRecords() {
  // 左墙唱片：奇数台阶上方挂一张
  return Array.from({ length: STEP_COUNT }, (_, i) => ({
    stepIndex: i,
    hasRecord: i % 2 === 0,
    colorHue: (i * 25) % 360,
  })).filter(r => r.hasRecord)
}

function generateRightRecords() {
  // 右墙唱片：偶数台阶上方挂一张
  return Array.from({ length: STEP_COUNT }, (_, i) => ({
    stepIndex: i,
    hasRecord: i % 2 === 1,
    colorHue: (i * 35 + 180) % 360,
  })).filter(r => r.hasRecord)
}

const steps = generateSteps()
const leftRecords = generateLeftRecords()
const rightRecords = generateRightRecords()
</script>

<template>
  <div class="scene-wrapper">
    <!-- 3D 场景容器 -->
    <div class="scene-3d">
      <!-- 楼梯主体 -->
      <div class="staircase">
        <!-- 左墙 -->
        <div class="wall wall-left">
          <div
            v-for="rec in leftRecords"
            :key="`L-${rec.stepIndex}`"
            class="record"
            :style="{
              '--rec-hue': rec.colorHue,
              '--rec-step': rec.stepIndex,
            }"
          >
            <div class="record-vinyl">
              <div class="record-label" />
              <div class="record-grooves" />
            </div>
            <div class="record-sleeve" />
          </div>
        </div>

        <!-- 右墙 -->
        <div class="wall wall-right">
          <div
            v-for="rec in rightRecords"
            :key="`R-${rec.stepIndex}`"
            class="record"
            :style="{
              '--rec-hue': rec.colorHue,
              '--rec-step': rec.stepIndex,
            }"
          >
            <div class="record-vinyl">
              <div class="record-label" />
              <div class="record-grooves" />
            </div>
            <div class="record-sleeve" />
          </div>
        </div>

        <!-- 台阶组 -->
        <div
          v-for="step in steps"
          :key="step.index"
          class="step"
          :style="{
            '--step-index': step.index,
            '--step-total': step.total,
          }"
        >
          <!-- 水平面 tread -->
          <div class="step-tread">
            <div class="tread-highlight" />
          </div>
          <!-- 垂直面 riser -->
          <div class="step-riser" />
        </div>

        <!-- 底部地面 -->
        <div class="floor" />
      </div>

      <!-- 顶部装饰光带 -->
      <div class="ceiling-light" />
    </div>

    <!-- 前景遮罩（营造暗角效果） -->
    <div class="vignette" />
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════════════
   楼梯唱片店 - CSS 3D 透视场景
   ═══════════════════════════════════════════════ */

/* ── 场景容器 ── */
.scene-wrapper {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: linear-gradient(180deg, #0a0e1a 0%, #040814 40%, #02040a 100%);
}

/* ── 暗角效果 ── */
.vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(
    ellipse 70% 60% at 50% 40%,
    transparent 30%,
    rgba(2, 4, 10, 0.6) 70%,
    rgba(2, 4, 10, 0.95) 100%
  );
  z-index: 10;
}

/* ═══════════════════════════════════════════════
   核心 3D 设置
   ═══════════════════════════════════════════════ */

.scene-3d {
  position: absolute;
  inset: 0;
  /*
   * perspective: 透视距离
   * 值越小 = 透视越强烈（变形越大）
   * 值越大 = 透视越平缓
   * 推荐 800-1200px 获得自然的近大远小
   */
  perspective: 900px;
  perspective-origin: 50% 35%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 8vh;
}

/* 楼梯主体 - 3D变换的根基 */
.staircase {
  position: relative;
  width: 420px;
  height: 600px;
  /*
   * rotateX(58deg): 将楼梯整体向后倾斜
   * 让 viewer 从上方俯视楼梯向下延伸
   * 这是产生"向下看"感觉的关键
   */
  transform-style: preserve-3d;
  transform: rotateX(58deg) translateZ(0);
}

/* ═══════════════════════════════════════════════
   墙面系统
   ═══════════════════════════════════════════════ */

.wall {
  position: absolute;
  top: 0;
  width: 120px;
  height: 100%;
  transform-style: preserve-3d;
  background: linear-gradient(
    180deg,
    #1a1f2e 0%,
    #121620 50%,
    #0a0e1a 100%
  );
}

/* 左墙：向左侧展开 */
.wall-left {
  left: 0;
  /*
   * rotateY(90deg): 墙面绕Y轴旋转90度，面向右侧
   * translateZ: 将墙面推到楼梯左侧边缘
   */
  transform-origin: left center;
  transform: rotateY(90deg) translateZ(-60px);
  border-right: 1px solid rgba(255, 255, 255, 0.04);
}

/* 右墙：向右侧展开 */
.wall-right {
  right: 0;
  transform-origin: right center;
  transform: rotateY(-90deg) translateZ(-60px);
  border-left: 1px solid rgba(255, 255, 255, 0.04);
}

/* 墙面纹理 */
.wall::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 28px,
    rgba(255, 255, 255, 0.015) 28px,
    rgba(255, 255, 255, 0.015) 29px
  );
}

/* ═══════════════════════════════════════════════
   唱片 - 纯CSS绘制
   ═══════════════════════════════════════════════ */

.record {
  position: absolute;
  left: 50%;
  width: 56px;
  height: 56px;
  /* 根据台阶位置垂直排列 */
  top: calc(var(--rec-step) * 52px + 16px);
  transform: translateX(-50%) rotateY(0deg);
  transform-style: preserve-3d;
}

/* 唱片本体（黑胶） */
.record-vinyl {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background:
    radial-gradient(circle at 30% 30%, #333 0%, #111 40%, #0a0a0a 100%);
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.5),
    inset 0 1px 1px rgba(255, 255, 255, 0.08);
}

/* 唱片纹路 */
.record-grooves {
  position: absolute;
  inset: 6px;
  border-radius: 50%;
  background: repeating-radial-gradient(
    circle at center,
    transparent 0px,
    transparent 2px,
    rgba(255, 255, 255, 0.03) 2px,
    rgba(255, 255, 255, 0.03) 3px
  );
}

/* 唱片标签 */
.record-label {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  background: conic-gradient(
    from 0deg,
    hsl(var(--rec-hue), 60%, 45%),
    hsl(calc(var(--rec-hue) + 60), 55%, 40%),
    hsl(calc(var(--rec-hue) + 120), 60%, 45%),
    hsl(calc(var(--rec-hue) + 180), 55%, 40%),
    hsl(calc(var(--rec-hue) + 240), 60%, 45%),
    hsl(var(--rec-hue), 60%, 45%)
  );
  box-shadow: inset 0 0 3px rgba(0, 0, 0, 0.4);
}

/* 唱片中心孔 */
.record-label::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  background: #111;
}

/* 唱片封套（部分露出） */
.record-sleeve {
  position: absolute;
  bottom: -4px;
  left: 50%;
  width: 62px;
  height: 62px;
  transform: translateX(-50%) translateZ(-2px);
  background: linear-gradient(
    135deg,
    hsl(var(--rec-hue), 30%, 20%) 0%,
    hsl(var(--rec-hue), 25%, 12%) 100%
  );
  border-radius: 2px;
  opacity: 0.7;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.4);
}

/* ═══════════════════════════════════════════════
   台阶系统
   ═══════════════════════════════════════════════ */

.step {
  position: absolute;
  left: 50%;
  width: 420px;
  height: 70px;
  /* 每个台阶向下（屏幕下方）和向里（Z轴深处）偏移 */
  top: calc(var(--step-index) * 32px);
  transform: translateX(-50%) translateZ(calc(var(--step-index) * -70px));
  transform-style: preserve-3d;
}

/* ── 水平面 tread ── */
.step-tread {
  position: absolute;
  inset: 0;
  /* tread 是水平面，不需要额外旋转 */
  transform: rotateX(0deg);
  background: linear-gradient(
    180deg,
    #1e2436 0%,
    #181d2b 50%,
    #131824 100%
  );
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  border-left: 1px solid rgba(255, 255, 255, 0.03);
  border-right: 1px solid rgba(255, 255, 255, 0.03);
}

/* tread 高光 - 营造立体感 */
.tread-highlight {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.1) 20%,
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.1) 80%,
    transparent 100%
  );
}

/* ── 垂直面 riser ── */
.step-riser {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: 32px;
  /*
   * rotateX(-90deg): 将面从水平翻转为垂直
   * transform-origin: top - 以顶部为轴翻转
   */
  transform-origin: top;
  transform: rotateX(-90deg);
  background: linear-gradient(
    180deg,
    #151a28 0%,
    #111520 100%
  );
  border-left: 1px solid rgba(255, 255, 255, 0.02);
  border-right: 1px solid rgba(255, 255, 255, 0.02);
}

/* ═══════════════════════════════════════════════
   底部地面
   ═══════════════════════════════════════════════ */

.floor {
  position: absolute;
  left: 50%;
  bottom: -200px;
  width: 600px;
  height: 400px;
  transform: translateX(-50%) translateZ(calc(var(--step-total) * -70px)) rotateX(90deg);
  transform-origin: top;
  background: linear-gradient(
    180deg,
    #0d111c 0%,
    #06080f 100%
  );
  opacity: 0.8;
}

/* ═══════════════════════════════════════════════
   顶部光带
   ═══════════════════════════════════════════════ */

.ceiling-light {
  position: absolute;
  top: -20px;
  left: 50%;
  width: 300px;
  height: 4px;
  transform: translateX(-50%);
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(200, 180, 255, 0.15) 30%,
    rgba(220, 200, 255, 0.25) 50%,
    rgba(200, 180, 255, 0.15) 70%,
    transparent 100%
  );
  border-radius: 2px;
  filter: blur(2px);
}

/* ═══════════════════════════════════════════════
   远处台阶雾化（增强深度感）
   ═══════════════════════════════════════════════ */

.step:nth-child(n + 8) .step-tread {
  opacity: calc(1 - (var(--step-index) - 7) * 0.12);
}

.step:nth-child(n + 8) .step-riser {
  opacity: calc(1 - (var(--step-index) - 7) * 0.12);
}

/* ═══════════════════════════════════════════════
   响应式调整
   ═══════════════════════════════════════════════ */

@media (max-width: 768px) {
  .scene-3d {
    perspective: 600px;
    perspective-origin: 50% 30%;
    padding-top: 12vh;
  }

  .staircase {
    transform: rotateX(65deg) scale(0.75);
  }
}
</style>
