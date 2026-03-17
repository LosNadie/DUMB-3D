<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import * as THREE from 'three'

interface Props {
  introFromCube?: boolean
  backgroundImageUrl?: string
}

const props = withDefaults(defineProps<Props>(), {
  introFromCube: false,
  backgroundImageUrl: '',
})

const containerRef = ref<HTMLElement | null>(null)
const canvasReady = ref(false)

let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let planeMaterial: THREE.ShaderMaterial | null = null
let imageTexture: THREE.Texture | null = null
let frameId = 0
const clock = new THREE.Clock()
const introStrength = { value: props.introFromCube ? 1 : 0 }
const textureLoader = new THREE.TextureLoader()

const vertexShader = `
  varying vec2 vUv;
  void main() {
    vUv = uv;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
  }
`

const fragmentShader = `
  uniform float uTime;
  uniform float uIntro;
  uniform float uHasTexture;
  uniform sampler2D uImageTexture;
  varying vec2 vUv;

  vec3 auroraPalette(float t) {
    vec3 deep = vec3(0.02, 0.04, 0.09);
    vec3 cyan = vec3(0.08, 0.72, 0.86);
    vec3 mint = vec3(0.19, 0.95, 0.62);
    vec3 violet = vec3(0.43, 0.31, 0.9);
    vec3 base = mix(deep, cyan, smoothstep(0.0, 0.45, t));
    base = mix(base, mint, smoothstep(0.25, 0.78, t));
    base = mix(base, violet, smoothstep(0.62, 1.0, t));
    return base;
  }

  void main() {
    vec2 uv = vUv;
    vec2 centerUv = uv - 0.5;
    float t = uTime * 0.42;
    float ribbonA = sin((uv.y * 6.4) + t * 1.1 + sin(uv.x * 4.0 + t * 0.7));
    float ribbonB = cos((uv.y * 4.8) - t * 0.86 + cos(uv.x * 3.2 - t * 0.5));
    float ribbonC = sin((uv.x + uv.y) * 5.1 + t * 0.74);
    float flow = ribbonA * 0.44 + ribbonB * 0.33 + ribbonC * 0.23;
    float normalized = clamp(flow * 0.5 + 0.5, 0.0, 1.0);
    vec2 warpedUv = uv + vec2(ribbonA * 0.012, ribbonB * 0.016);

    float radial = 1.0 - smoothstep(0.15, 0.78, length(centerUv));
    float curtain = smoothstep(0.1, 0.9, 1.0 - abs(centerUv.x) * 1.6);
    vec3 color = auroraPalette(normalized) * (0.62 + curtain * 0.54);
    color += vec3(0.02, 0.05, 0.09) * radial;
    color += vec3(0.14, 0.3, 0.4) * uIntro * radial;
    if (uHasTexture > 0.5) {
      vec3 imageColor = texture2D(uImageTexture, warpedUv).rgb;
      color = mix(color, imageColor, 0.46);
      color += auroraPalette(normalized) * flow * 0.16;
    }

    gl_FragColor = vec4(color, 0.74 + radial * 0.13);
  }
`

function updateImageTexture(nextUrl: string) {
  if (!planeMaterial) return

  if (!nextUrl) {
    const uniforms = planeMaterial.uniforms as { uHasTexture: { value: number } }
    uniforms.uHasTexture.value = 0
    return
  }

  textureLoader.load(
    nextUrl,
    (texture) => {
      texture.colorSpace = THREE.SRGBColorSpace
      texture.minFilter = THREE.LinearFilter
      texture.magFilter = THREE.LinearFilter
      if (imageTexture) {
        imageTexture.dispose()
      }
      imageTexture = texture
      const uniforms = planeMaterial?.uniforms as {
        uImageTexture: { value: THREE.Texture }
        uHasTexture: { value: number }
      } | undefined
      if (!uniforms) return
      uniforms.uImageTexture.value = texture
      uniforms.uHasTexture.value = 1
    },
    undefined,
    () => {
      const uniforms = planeMaterial?.uniforms as { uHasTexture: { value: number } } | undefined
      if (!uniforms) return
      uniforms.uHasTexture.value = 0
    },
  )
}

const onResize = () => {
  if (!containerRef.value || !camera || !renderer) return
  camera.aspect = containerRef.value.clientWidth / containerRef.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(containerRef.value.clientWidth, containerRef.value.clientHeight)
}

const animate = () => {
  if (!renderer || !scene || !camera || !planeMaterial) return
  const uniforms = planeMaterial.uniforms as {
    uTime: { value: number }
    uIntro: { value: number }
    uHasTexture: { value: number }
  }
  uniforms.uTime.value = clock.getElapsedTime()
  introStrength.value += (0 - introStrength.value) * 0.06
  uniforms.uIntro.value = introStrength.value
  renderer.render(scene, camera)
  if (!canvasReady.value) canvasReady.value = true
  frameId = requestAnimationFrame(animate)
}

onMounted(() => {
  if (!containerRef.value) return
  const container = containerRef.value
  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(45, container.clientWidth / container.clientHeight, 0.1, 100)
  camera.position.z = 3

  planeMaterial = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uIntro: { value: introStrength.value },
      uHasTexture: { value: 0 },
      uImageTexture: { value: new THREE.Texture() },
    },
    vertexShader,
    fragmentShader,
    transparent: true,
    depthWrite: false,
  })
  const plane = new THREE.Mesh(new THREE.PlaneGeometry(8, 6), planeMaterial)
  scene.add(plane)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: 'high-performance' })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.appendChild(renderer.domElement)
  updateImageTexture(props.backgroundImageUrl)
  window.addEventListener('resize', onResize)
  animate()
})

watch(
  () => props.backgroundImageUrl,
  (nextUrl) => {
    updateImageTexture(nextUrl || '')
  },
)

onUnmounted(() => {
  cancelAnimationFrame(frameId)
  window.removeEventListener('resize', onResize)
  if (planeMaterial) {
    planeMaterial.dispose()
    planeMaterial = null
  }
  if (renderer) {
    renderer.dispose()
    renderer.domElement.remove()
    renderer = null
  }
  if (imageTexture) {
    imageTexture.dispose()
    imageTexture = null
  }
  scene = null
  camera = null
})
</script>

<template>
  <div ref="containerRef" class="flow-scene-backdrop" :class="{ ready: canvasReady }"></div>
</template>

<style scoped>
.flow-scene-backdrop {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.flow-scene-backdrop.ready {
  opacity: 1;
}
</style>
