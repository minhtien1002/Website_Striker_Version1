<script setup>
import { useMainStore } from '../stores/MainStore.ts';
import { inject, ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router'; // Đảm bảo đã import useRouter
import Loading from './Loading.vue';

const mainStore = useMainStore();
const { isDeviceMobile } = mainStore;
const emit = defineEmits(['openDrawer', 'toggle-sidebar', 'toggle-theme']);

const { formatCurrency } = inject('cartActions');


const currentDateTime = ref('');
let datetimeInterval = null;
const isDarkTheme = ref(localStorage.getItem('theme') === 'dark');
const router = useRouter();

const isRefreshing = ref(false); // Trạng thái loading khi làm mới dữ liệu

const updateDateTime = () => {
  currentDateTime.value = new Date().toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
};

const refreshData = () => {
  // Close sidebar if it's open
  if (props.isSidebarActive) {
    emit('toggle-sidebar');
  }

  isRefreshing.value = true;
  // Simulate loading time before reload
  setTimeout(() => {
    window.location.reload();
  }, 1500);
};

const themeIcon = computed(() => {
  return isDarkTheme.value ? 'solar:sun-linear' : 'solar:moon-linear';
});

const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value;
  emit('toggle-theme');
};



// Add props to receive sidebar state
const props = defineProps({
  totalPrice: Number,
  cart: Array,
  isSidebarActive: Boolean, // Add this prop to track sidebar state
});
// --- Kết thúc phần thêm mới ---

onMounted(() => {
  updateDateTime();
  datetimeInterval = setInterval(updateDateTime, 1000);
  if (isDarkTheme.value) {
    document.body.classList.add('dark');
  } else {
    document.body.classList.remove('dark');
  }

});

onBeforeUnmount(() => {
  clearInterval(datetimeInterval);
});
</script>

<template>
  <header class="elegant-header">
    <!-- Animated background gradient -->
    <div class="header-bg-gradient"></div>

    <div class="header-content">
      <div class="header-left">
        <!-- Enhanced sidebar toggle with ripple effect -->
        <button @click="emit('toggle-sidebar')" class="sidebar-toggle-btn">
          <div class="ripple-effect"></div>
          <div class="hamburger-menu" :class="{ 'active': props.isSidebarActive }">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </button>
      </div>

      <div class="header-right">
        <!-- Enhanced datetime display with animated background -->
        <div class="datetime-display hidden md:flex">
          <div class="datetime-bg"></div>
          <div class="datetime-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
              <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2" />
            </svg>
          </div>
          <div class="datetime-content">
            <span class="datetime-text">{{ currentDateTime }}</span>
            <div class="datetime-pulse"></div>
          </div>
        </div>

        <div class="header-actions">

          <button @click="toggleTheme" class="action-btn theme-toggle">
            <div class="btn-icon">
              <svg v-if="themeIcon === 'solar:sun-linear'" width="18" height="18" viewBox="0 0 24 24" fill="none"
                xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2" />
                <line x1="12" y1="1" x2="12" y2="3" stroke="currentColor" stroke-width="2" />
                <line x1="12" y1="21" x2="12" y2="23" stroke="currentColor" stroke-width="2" />
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" stroke="currentColor" stroke-width="2" />
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" stroke="currentColor" stroke-width="2" />
                <line x1="1" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="2" />
                <line x1="21" y1="12" x2="23" y2="12" stroke="currentColor" stroke-width="2" />
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" stroke="currentColor" stroke-width="2" />
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" stroke="currentColor" stroke-width="2" />
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
          </button>

          <button @click="refreshData" class="action-btn refresh-btn hidden md:flex">
            <div class="btn-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <polyline points="23,4 23,10 17,10" stroke="currentColor" stroke-width="2" />
                <polyline points="1,20 1,14 7,14" stroke="currentColor" stroke-width="2" />
                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" stroke="currentColor"
                  stroke-width="2" />
              </svg>
            </div>
          </button>

        </div>
      </div>
    </div>
  </header>

  <!-- Loading component for refresh - moved outside header -->
  <Loading :show="isRefreshing" text="Đang làm mới dữ liệu..." subtext="Trang sẽ được tải lại sau giây lát"
    :fullscreen="true" />
</template>

<style lang="scss" scoped>
.elegant-header {
  position: relative;
  height: 80px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-bottom: 1px solid rgba(255, 59, 24, 0.1);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.5) inset;
  z-index: 99;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  .header-bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg,
        rgba(255, 59, 24, 0.03) 0%,
        rgba(255, 59, 24, 0.01) 50%,
        rgba(255, 59, 24, 0.03) 100%);
    animation: gradientShift 8s ease-in-out infinite;
  }

  @keyframes gradientShift {

    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0.7;
    }
  }

  .header-content {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    padding: 0 32px;
    max-width: 100%;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 24px;

    .sidebar-toggle-btn {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border: none;
      border-radius: 16px;
      background: rgba(255, 59, 24, 0.08);
      cursor: pointer;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      overflow: hidden;

      .ripple-effect {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 59, 24, 0.3);
        transform: scale(0);
        animation: ripple 0.6s linear;
        pointer-events: none;
      }

      @keyframes ripple {
        to {
          transform: scale(4);
          opacity: 0;
        }
      }

      &:hover {
        background: rgba(255, 59, 24, 0.12);
        transform: translateY(-3px) scale(1.05);
        box-shadow:
          0 12px 40px rgba(255, 59, 24, 0.15),
          0 0 0 1px rgba(255, 59, 24, 0.1);
      }

      &:active .ripple-effect {
        animation: ripple 0.6s linear;
      }

      .hamburger-menu {
        width: 22px;
        height: 18px;
        position: relative;
        z-index: 1;

        span {
          display: block;
          position: absolute;
          height: 2.5px;
          width: 100%;
          background: #ff3b18;
          border-radius: 3px;
          opacity: 1;
          left: 0;
          transform: rotate(0deg);
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

          &:nth-child(1) {
            top: 0px;
          }

          &:nth-child(2) {
            top: 8px;
          }

          &:nth-child(3) {
            top: 16px;
          }
        }

        &.active span {
          &:nth-child(1) {
            top: 8px;
            transform: rotate(135deg);
          }

          &:nth-child(2) {
            opacity: 0;
            left: -60px;
          }

          &:nth-child(3) {
            top: 8px;
            transform: rotate(-135deg);
          }
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .datetime-display {
      position: relative;
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 20px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 16px;
      border: 1px solid rgba(255, 59, 24, 0.1);
      backdrop-filter: blur(12px);
      box-shadow:
        0 4px 16px rgba(0, 0, 0, 0.04),
        0 0 0 1px rgba(255, 255, 255, 0.5) inset;
      transition: all 0.3s ease;
      overflow: hidden;

      .datetime-bg {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, rgba(255, 59, 24, 0.02), rgba(255, 59, 24, 0.05));
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

        .datetime-bg {
          opacity: 1;
        }
      }

      .datetime-icon {
        color: #ff3b18;
        display: flex;
        align-items: center;
        z-index: 1;
      }

      .datetime-content {
        position: relative;
        z-index: 1;

        .datetime-text {
          font-size: 14px;
          font-weight: 600;
          color: #1e293b;
          letter-spacing: 0.3px;
        }

        .datetime-pulse {
          position: absolute;
          top: 50%;
          right: -10px;
          width: 6px;
          height: 6px;
          background: #10b981;
          border-radius: 50%;
          transform: translateY(-50%);
          animation: pulse 2s infinite;
        }

        @keyframes pulse {

          0%,
          100% {
            opacity: 1;
            transform: translateY(-50%) scale(1);
          }

          50% {
            opacity: 0.5;
            transform: translateY(-50%) scale(1.2);
          }
        }
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;


      .action-btn {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        border: none;
        border-radius: 16px;
        background: rgba(255, 255, 255, 0.7);
        border: 1px solid rgba(255, 59, 24, 0.1);
        cursor: pointer;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, rgba(255, 59, 24, 0.1), rgba(255, 59, 24, 0.05));
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          background: rgba(255, 255, 255, 0.9);
          transform: translateY(-3px) scale(1.05);
          box-shadow:
            0 12px 32px rgba(0, 0, 0, 0.1),
            0 0 0 1px rgba(255, 59, 24, 0.1);

          &::before {
            opacity: 1;
          }
        }

        .btn-icon {
          color: #64748b;
          display: flex;
          align-items: center;
          transition: all 0.3s ease;
          z-index: 1;
        }

        &.theme-toggle:hover .btn-icon {
          color: #f59e0b;
          transform: rotate(180deg);
        }

        &.refresh-btn:hover .btn-icon {
          color: #10b981;
          transform: rotate(180deg);
        }

      }
    }
  }
}

// Enhanced Dark theme styles
.dark .elegant-header {
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(255, 59, 24, 0.2);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 1px 0 rgba(255, 255, 255, 0.05) inset;

  .header-bg-gradient {
    background: linear-gradient(135deg,
        rgba(255, 59, 24, 0.08) 0%,
        rgba(255, 59, 24, 0.03) 50%,
        rgba(255, 59, 24, 0.08) 100%);
  }

  .header-left {
    .sidebar-toggle-btn {
      background: rgba(255, 59, 24, 0.15);

      &:hover {
        background: rgba(255, 59, 24, 0.25);
        box-shadow:
          0 12px 40px rgba(255, 59, 24, 0.3),
          0 0 0 1px rgba(255, 59, 24, 0.2);
      }
    }
  }

  .header-right {
    .datetime-display {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 59, 24, 0.2);
      box-shadow:
        0 4px 16px rgba(0, 0, 0, 0.2),
        0 0 0 1px rgba(255, 255, 255, 0.05) inset;

      .datetime-bg {
        background: linear-gradient(135deg, rgba(255, 59, 24, 0.1), rgba(255, 59, 24, 0.15));
      }

      .datetime-content .datetime-text {
        color: #e2e8f0;
      }

      .datetime-icon {
        color: #66b3ff;
      }
    }


    .action-btn {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 59, 24, 0.2);

      &::before {
        background: linear-gradient(135deg, rgba(255, 59, 24, 0.2), rgba(255, 59, 24, 0.1));
      }

      &:hover {
        background: rgba(255, 255, 255, 0.12);
        box-shadow:
          0 12px 32px rgba(0, 0, 0, 0.2),
          0 0 0 1px rgba(255, 59, 24, 0.2);
      }

      .btn-icon {
        color: #94a3b8;
      }

      &.theme-toggle:hover .btn-icon {
        color: #fbbf24;
      }

      &.refresh-btn:hover .btn-icon {
        color: #34d399;
      }

    }
  }
}

@media (max-width: 768px) {
  .elegant-header {
    height: 70px;

    .header-content {
      padding: 0 16px;
    }

    .header-left {
      gap: 16px;

      .sidebar-toggle-btn {
        width: 44px;
        height: 44px;
      }
    }

    .header-right {
      gap: 12px;

      .datetime-display {
        padding: 10px 16px;
        gap: 8px;
      }

      .header-actions {
        gap: 12px;

        .action-btn {
          width: 44px;
          height: 44px;
        }
      }
    }
  }

  .hidden.md\:flex,
  .hidden.md\:block,
  .hidden.lg\:block {
    display: none !important;
  }
}

@media (max-width: 480px) {
  .elegant-header {
    .header-content {
      padding: 0 12px;
    }

    .header-left {
      gap: 12px;
    }

    .header-right {
      gap: 8px;
    }
  }
}
</style>