<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
}>(), {
  placeholder: '请输入内容...',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = ref<HTMLDivElement | null>(null)

function focusEditor() {
  editorRef.value?.focus()
}

function applyCommand(command: string, value?: string) {
  focusEditor()
  document.execCommand(command, false, value)
  emitChange()
}

function emitChange() {
  emit('update:modelValue', editorRef.value?.innerHTML || '')
}

function insertLink() {
  const url = window.prompt('请输入链接地址')
  if (!url) return
  applyCommand('createLink', url.trim())
}

function insertImage() {
  const url = window.prompt('请输入图片链接')
  if (!url) return
  applyCommand('insertImage', url.trim())
}

function normalizeVideoUrl(raw: string) {
  const value = raw.trim()
  if (!value) return ''
  if (value.includes('youtube.com/watch?v=')) {
    const matched = value.match(/[?&]v=([^&]+)/)
    if (matched?.[1]) {
      return `https://www.youtube.com/embed/${matched[1]}`
    }
  }
  if (value.includes('youtu.be/')) {
    const matched = value.match(/youtu\.be\/([^?&/]+)/)
    if (matched?.[1]) {
      return `https://www.youtube.com/embed/${matched[1]}`
    }
  }
  if (value.includes('bilibili.com/video/')) {
    const matched = value.match(/\/video\/(BV[0-9A-Za-z]+)/)
    if (matched?.[1]) {
      return `https://player.bilibili.com/player.html?bvid=${matched[1]}&page=1`
    }
  }
  return value
}

function insertVideo() {
  const url = window.prompt('请输入视频链接（YouTube/B站）')
  if (!url) return
  const src = normalizeVideoUrl(url)
  if (!src) return
  const iframe = `<div class="video-block"><iframe src="${src}" frameborder="0" allowfullscreen style="width:100%;height:420px;"></iframe></div><p><br></p>`
  applyCommand('insertHTML', iframe)
}

watch(
  () => props.modelValue,
  (value) => {
    if (!editorRef.value) return
    if (editorRef.value.innerHTML !== value) {
      editorRef.value.innerHTML = value || ''
    }
  },
)

onMounted(() => {
  if (editorRef.value) {
    editorRef.value.innerHTML = props.modelValue || ''
  }
})
</script>

<template>
  <div class="rich-editor">
    <div class="toolbar">
      <button type="button" @click="applyCommand('bold')"><b>B</b></button>
      <button type="button" @click="applyCommand('italic')"><i>I</i></button>
      <button type="button" @click="applyCommand('underline')"><u>U</u></button>
      <button type="button" @click="applyCommand('insertUnorderedList')">• 列表</button>
      <button type="button" @click="applyCommand('insertOrderedList')">1. 列表</button>
      <button type="button" @click="insertLink">链接</button>
      <button type="button" @click="insertImage">图片</button>
      <button type="button" @click="insertVideo">视频</button>
      <button type="button" @click="applyCommand('removeFormat')">清除样式</button>
    </div>
    <div
      ref="editorRef"
      class="editor"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="emitChange"
      @blur="emitChange"
    />
  </div>
</template>

<style scoped>
.rich-editor {
  border: 1px solid var(--dumb-border);
  background: var(--dumb-panel);
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid var(--dumb-border);
  background: #111;
}
.toolbar button {
  border: 1px solid var(--dumb-border);
  background: #000;
  color: #fff;
  padding: 4px 10px;
  cursor: pointer;
}
.editor {
  min-height: 220px;
  padding: 12px;
  outline: none;
  line-height: 1.7;
}
.editor:empty::before {
  content: attr(data-placeholder);
  color: var(--dumb-muted);
}
</style>
