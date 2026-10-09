<template>
  <div v-if="show" class="loading-overlay" :class="{ 'fullscreen': fullscreen }">
    <div class="loading-container">
      <div class="loading-spinner" :class="size">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      <div v-if="text" class="loading-text">{{ text }}</div>
      <div v-if="subtext" class="loading-subtext">{{ subtext }}</div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  show: {
    type: Boolean,
    default: true
  },
  text: {
    type: String,
    default: 'Đang tải...'
  },
  subtext: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  fullscreen: {
    type: Boolean,
    default: true
  }
})
</script>

<style lang="scss" scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  transition: all 0.3s ease;

  &:not(.fullscreen) {
    position: absolute;
    border-radius: 12px;
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 20px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    animation: fadeInUp 0.5s ease;

    .loading-spinner {
      position: relative;
      
      &.small {
        width: 40px;
        height: 40px;
      }
      
      &.medium {
        width: 60px;
        height: 60px;
      }
      
      &.large {
        width: 80px;
        height: 80px;
      }

      .spinner-ring {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border: 3px solid transparent;
        border-radius: 50%;
        animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;

        &:nth-child(1) {
          border-top-color: #ff3b18;
          animation-delay: -0.45s;
        }

        &:nth-child(2) {
          border-right-color: #28a745;
          animation-delay: -0.3s;
        }

        &:nth-child(3) {
          border-bottom-color: #ffc107;
          animation-delay: -0.15s;
        }

        &:nth-child(4) {
          border-left-color: #dc3545;
        }
      }
    }

    .loading-text {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      text-align: center;
      margin: 0;
      animation: pulse 2s ease-in-out infinite;
    }

    .loading-subtext {
      font-size: 14px;
      color: #64748b;
      text-align: center;
      margin: 0;
      opacity: 0.8;
    }
  }
}

// Dark theme support
.dark .loading-overlay {
  background: rgba(15, 23, 42, 0.95);

  .loading-container {
    background: rgba(15, 23, 42, 0.9);
    border: 1px solid rgba(255, 255, 255, 0.1);

    .loading-text {
      color: #f1f5f9;
    }

    .loading-subtext {
      color: #94a3b8;
    }
  }
}

// Animations
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

// Responsive design
@media (max-width: 768px) {
  .loading-overlay {
    .loading-container {
      padding: 30px 20px;
      margin: 20px;
      
      .loading-text {
        font-size: 16px;
      }
      
      .loading-subtext {
        font-size: 13px;
      }
    }
  }
}
</style>
