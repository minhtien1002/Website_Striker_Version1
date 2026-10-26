<template>
  <div
    class="bg-white/90 backdrop-blur-xl border border-white/30 shadow-lg rounded-2xl p-6 mb-6 relative overflow-hidden">
    <div class="absolute inset-0 bg-gradient-to-r from-accent/5 to-accent/10"></div>
    <div class="relative">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-8 h-8 bg-gradient-to-br from-accent to-accent-dark rounded-lg flex items-center justify-center">
          <i class="fas fa-shopping-cart text-lg text-white"></i>
        </div>
        <div>
          <h2 class="text-xl font-bold text-gray-900">Thanh toán</h2>
          <p class="text-sm text-gray-600">{{ steps.length }} bước</p>
        </div>
      </div>

      <div class="modern-progress-timeline">
        <div v-for="(step, index) in steps" :key="index" class="progress-item" :class="{
          'progress-completed': currentStep > index + 1,
          'progress-current': currentStep === index + 1,
          'progress-pending': currentStep < index + 1
        }">
          <div class="progress-circle">
            <div class="progress-icon-wrapper">
              <i v-if="currentStep > index + 1" class="fas fa-check progress-icon"></i>
              <span v-else class="progress-icon">{{ index + 1 }}</span>
            </div>
            <div class="progress-pulse"></div>
            <div class="progress-glow"></div>
          </div>
          <div class="progress-content">
            <h3 class="progress-title">{{ step.label }}</h3>
            <div class="progress-description">{{ step.description }}</div>
          </div>
          <div v-if="index < steps.length - 1" class="progress-connector"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  currentStep: {
    type: Number,
    required: true
  },
  steps: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
/* Modern Progress Timeline */
.modern-progress-timeline {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 100%;
  margin: 0 auto;
  padding: 1.5rem 0;
  gap: 1rem;
}

/* Progress Item */
.progress-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
  padding: 1rem;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Progress Connector */
.progress-connector {
  position: absolute;
  top: 3rem;
  left: calc(100% - 1rem);
  width: 2rem;
  height: 4px;
  background: linear-gradient(90deg,
      rgba(200, 200, 200, 0.3) 0%,
      rgba(200, 200, 200, 0.5) 100%);
  border-radius: 2px;
  z-index: 1;
}

.progress-item.progress-completed+.progress-item .progress-connector {
  background: linear-gradient(90deg,
      #10b981 0%,
      #059669 100%);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.3);
}

/* Progress Circle */
.progress-circle {
  position: relative;
  width: 3.5rem;
  height: 3.5rem;
  margin-bottom: 1rem;
  z-index: 4;
}

.progress-icon-wrapper {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  border: 4px solid white;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(10px);
}

.progress-icon {
  font-size: 1.25rem;
  transition: all 0.4s ease;
}

/* Progress Pulse Effect */
.progress-pulse {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  z-index: 1;
  opacity: 0;
  animation: progressPulse 2s ease-in-out infinite;
}

/* Progress Glow Effect */
.progress-glow {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border-radius: 50%;
  z-index: 0;
  opacity: 0;
  animation: progressGlow 3s ease-in-out infinite;
}

/* Progress States - Color-coded */

/* Pending Progress - Gray */
.progress-item.progress-pending .progress-icon-wrapper {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  color: white;
}

.progress-item.progress-pending .progress-pulse {
  background: radial-gradient(circle, rgba(156, 163, 175, 0.3) 0%, transparent 70%);
}

.progress-item.progress-pending .progress-glow {
  background: linear-gradient(45deg, transparent 0%, rgba(156, 163, 175, 0.2) 25%, transparent 50%, rgba(156, 163, 175, 0.2) 75%, transparent 100%);
}

/* Completed Progress - Green */
.progress-item.progress-completed .progress-icon-wrapper {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04), 0 0 20px rgba(16, 185, 129, 0.4);
}

.progress-item.progress-completed .progress-pulse {
  background: radial-gradient(circle, rgba(16, 185, 129, 0.4) 0%, transparent 70%);
  opacity: 1;
}

.progress-item.progress-completed .progress-glow {
  background: linear-gradient(45deg, transparent 0%, rgba(16, 185, 129, 0.3) 25%, transparent 50%, rgba(16, 185, 129, 0.3) 75%, transparent 100%);
  opacity: 1;
}

/* Current Progress - Accent Color */
.progress-item.progress-current .progress-icon-wrapper {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  transform: scale(1.15);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 30px rgba(59, 130, 246, 0.6);
  animation: currentGlow 2s ease-in-out infinite alternate;
}

.progress-item.progress-current .progress-pulse {
  background: radial-gradient(circle, rgba(59, 130, 246, 0.4) 0%, transparent 70%);
  opacity: 1;
  animation: progressPulse 1.5s ease-in-out infinite;
}

.progress-item.progress-current .progress-glow {
  background: linear-gradient(45deg, transparent 0%, rgba(59, 130, 246, 0.3) 25%, transparent 50%, rgba(59, 130, 246, 0.3) 75%, transparent 100%);
  opacity: 1;
  animation: progressGlow 2s ease-in-out infinite;
}

/* Progress Content */
.progress-content {
  padding: 0 0.5rem;
  transition: all 0.4s ease;
}

.progress-title {
  margin: 0 0 0.25rem 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  font-family: 'Nunito', 'Be Vietnam Pro', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: all 0.4s ease;
}

.progress-description {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
  font-weight: 400;
  line-height: 1.3;
  text-align: center;
  max-width: 150px;
}

/* Enhanced States for Content */
.progress-item.progress-completed .progress-title {
  color: #059669;
  text-shadow: 0 1px 2px rgba(5, 150, 105, 0.2);
}

.progress-item.progress-current .progress-title {
  color: #2563eb;
  transform: scale(1.05);
  text-shadow: 0 2px 4px rgba(37, 99, 235, 0.3);
}

/* Animations */
@keyframes progressPulse {

  0%,
  100% {
    opacity: 0;
    transform: scale(1);
  }

  50% {
    opacity: 0.6;
    transform: scale(1.1);
  }
}

@keyframes progressGlow {

  0%,
  100% {
    opacity: 0;
    transform: rotate(0deg);
  }

  50% {
    opacity: 0.8;
    transform: rotate(180deg);
  }
}

@keyframes currentGlow {
  0% {
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 30px rgba(59, 130, 246, 0.6);
  }

  100% {
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 40px rgba(59, 130, 246, 0.8);
  }
}

/* Responsive Design */
@media (max-width: 1024px) {
  .modern-progress-timeline {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .progress-connector {
    display: none;
  }

  .progress-circle {
    width: 4rem;
    height: 4rem;
  }

  .progress-icon {
    font-size: 1.5rem;
  }

  .progress-title {
    font-size: 1.125rem;
  }
}

@media (max-width: 768px) {
  .progress-circle {
    width: 3rem;
    height: 3rem;
  }

  .progress-icon {
    font-size: 1.25rem;
  }

  .progress-title {
    font-size: 1rem;
  }

  .progress-description {
    font-size: 0.75rem;
  }
}
</style>
