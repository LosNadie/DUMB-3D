import { onUnmounted } from 'vue'

/**
 * 为元素添加鼠标/触摸拖拽滚动支持
 * 用法：在组件中 const elRef = ref<HTMLElement>()
 *       onMounted(() => useDragScroll(elRef.value))
 */
export function useDragScroll(element: HTMLElement | null | undefined) {
  if (!element) return

  let isDown = false
  let startX = 0
  let scrollLeft = 0

  const onPointerDown = (e: PointerEvent) => {
    isDown = true
    element!.classList.add('dragging')
    startX = e.pageX - element!.offsetLeft
    scrollLeft = element!.scrollLeft
  }

  const onPointerLeave = () => {
    isDown = false
    element!.classList.remove('dragging')
  }

  const onPointerUp = () => {
    isDown = false
    element!.classList.remove('dragging')
  }

  const onPointerMove = (e: PointerEvent) => {
    if (!isDown) return
    e.preventDefault()
    const x = e.pageX - element!.offsetLeft
    const walk = (x - startX) * 1.5
    element!.scrollLeft = scrollLeft - walk
  }

  element.addEventListener('pointerdown', onPointerDown)
  element.addEventListener('pointerleave', onPointerLeave)
  element.addEventListener('pointerup', onPointerUp)
  element.addEventListener('pointermove', onPointerMove)

  onUnmounted(() => {
    element.removeEventListener('pointerdown', onPointerDown)
    element.removeEventListener('pointerleave', onPointerLeave)
    element.removeEventListener('pointerup', onPointerUp)
    element.removeEventListener('pointermove', onPointerMove)
  })
}
