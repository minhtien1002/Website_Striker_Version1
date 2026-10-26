<template>
  <Teleport to="body">
    <Transition name="toast" appear>
      <div
        v-if="visible"
        class="toast-container"
        :class="[`toast-${type}`, positionClass]"
      >
        <div class="toast-content">
          <div class="toast-icon">
            <iconify-icon :icon="iconName"></iconify-icon>
          </div>
          <div class="toast-message">
            <h4 v-if="title" class="toast-title">{{ title }}</h4>
            <p class="toast-text">{{ message }}</p>
          </div>
          <button
            v-if="closable"
            @click="close"
            class="toast-close"
          >
            <iconify-icon icon="solar:close-circle-bold"></iconify-icon>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  message: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000
  },
  position: {
    type: String,
    default: 'top-right',
    validator: (value) => [
      'top-left', 'top-center', 'top-right',
      'bottom-left', 'bottom-center', 'bottom-right'
    ].includes(value)
  },
  closable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close'])

const visible = ref(false)

const iconName = computed(() => {
  const icons = {
    success: 'solar:check-circle-bold-duotone',
    error: 'solar:danger-circle-bold-duotone',
    warning: 'solar:danger-triangle-bold-duotone',
    info: 'solar:info-circle-bold-duotone'
  }
  return icons[props.type]
})

const positionClass = computed(() => {
  return `toast-${props.position}`
})

const close = () => {
  visible.value = false
  setTimeout(() => {
    emit('close')
  }, 300)
}

onMounted(() => {
  visible.value = true
  
  if (props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  z-index: 9999;
  max-width: 400px;
  min-width: 300px;
  pointer-events: auto;
}

/* Position classes */
.toast-top-left {
  top: 20px;
  left: 20px;
}

.toast-top-center {
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.toast-top-right {
  top: 20px;
  right: 20px;
}

.toast-bottom-left {
  bottom: 20px;
  left: 20px;
}

.toast-bottom-center {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.toast-bottom-right {
  bottom: 20px;
  right: 20px;
}

.toast-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 
    0 10px 25px rgba(0, 0, 0, 0.1),
    0 4px 6px rgba(0, 0, 0, 0.05);
  border-left: 4px solid;
  position: relative;
}

.toast-icon {
  flex-shrink: 0;
  font-size: 1.5rem;
  margin-top: 2px;
}

.toast-message {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #1a202c;
}

.toast-text {
  font-size: 0.9rem;
  margin: 0;
  color: #4a5568;
  line-height: 1.4;
}

.toast-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s ease;
  margin-top: -2px;
}

.toast-close:hover {
  color: #718096;
  background: #f7fafc;
}

/* Type-specific styles */
.toast-success .toast-content {
  border-left-color: #48bb78;
}

.toast-success .toast-icon {
  color: #48bb78;
}

.toast-error .toast-content {
  border-left-color: #f56565;
}

.toast-error .toast-icon {
  color: #f56565;
}

.toast-warning .toast-content {
  border-left-color: #ed8936;
}

.toast-warning .toast-icon {
  color: #ed8936;
}

.toast-info .toast-content {
  border-left-color: #4299e1;
}

.toast-info .toast-icon {
  color: #4299e1;
}

/* Transitions */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* Responsive */
@media (max-width: 640px) {
  .toast-container {
    max-width: calc(100vw - 40px);
    min-width: calc(100vw - 40px);
  }
  
  .toast-top-left,
  .toast-top-center,
  .toast-top-right {
    top: 20px;
    left: 20px;
    right: 20px;
    transform: none;
  }
  
  .toast-bottom-left,
  .toast-bottom-center,
  .toast-bottom-right {
    bottom: 20px;
    left: 20px;
    right: 20px;
    transform: none;
  }
}
</style>
