<template>
  <!-- Mount đúng MỘT lần ở App.vue; mọi nơi gọi qua useConfirm().confirm() -->
  <Teleport to="body">
    <Transition name="confirm-fade">
      <div v-if="state.open" class="confirm-overlay" @click.self="cancel">
        <div class="confirm-box" :class="`tone-${state.tone}`" role="dialog" aria-modal="true">
          <div class="confirm-icon">
            <Icon :icon="toneIcon" />
          </div>

          <h3 class="confirm-title">{{ state.title }}</h3>
          <p class="confirm-message">{{ state.message }}</p>

          <ul v-if="state.details.length" class="confirm-details">
            <li v-for="(line, index) in state.details" :key="index">{{ line }}</li>
          </ul>

          <div v-if="state.requireReason" class="confirm-reason">
            <label :for="reasonId">{{ state.reasonLabel }} <span class="required">*</span></label>
            <textarea
              :id="reasonId"
              ref="reasonInput"
              :value="state.reason"
              :placeholder="state.reasonPlaceholder"
              rows="3"
              @input="setReason($event.target.value)"></textarea>
            <span v-if="showReasonError" class="reason-error">Vui lòng nhập {{ state.reasonLabel.toLowerCase() }}.</span>
          </div>

          <div class="confirm-actions">
            <button type="button" class="btn-cancel" @click="cancel">{{ state.cancelText }}</button>
            <button type="button" class="btn-accept" @click="handleAccept">{{ state.confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { useConfirm } from '@/composables/useConfirm.js';
import { Icon } from '@iconify/vue';

const { state, setReason, accept, cancel } = useConfirm();

const reasonInput = ref(null);
const showReasonError = ref(false);
const reasonId = `confirm-reason-${Math.random().toString(36).slice(2, 8)}`;

const toneIcon = computed(() => {
  if (state.tone === 'danger') return 'solar:danger-triangle-bold';
  if (state.tone === 'warning') return 'solar:shield-warning-bold';
  return 'solar:question-circle-bold';
});

const handleAccept = () => {
  // accept() tự im lặng khi thiếu lý do, nên tự bật thông báo lỗi ở đây để người dùng
  // biết vì sao bấm mà không có gì xảy ra.
  if (state.requireReason && !state.reason.trim()) {
    showReasonError.value = true;
    reasonInput.value?.focus();
    return;
  }
  accept();
};

// Cho phép đóng nhanh bằng Esc. Chỉ gắn listener khi hộp thoại đang mở để không giữ
// một handler toàn cục suốt vòng đời ứng dụng.
const onKeydown = (event) => {
  if (event.key === 'Escape') cancel();
};

watch(() => state.open, async (open) => {
  if (!open) {
    showReasonError.value = false;
    window.removeEventListener('keydown', onKeydown);
    return;
  }
  window.addEventListener('keydown', onKeydown);
  await nextTick();
  if (state.requireReason) reasonInput.value?.focus();
});

onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown));
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(2px);
}

.confirm-box {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border-radius: 16px;
  padding: 28px 24px 20px;
  border: 1px solid #d1d5db;
  text-align: left;
}

.confirm-box .confirm-icon { margin: 0 0 12px; }

.confirm-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

/* Một màu nhấn duy nhất cho mọi loại xác nhận (giao diện tối đa 3 màu) */
.confirm-icon { background: #f3f4f6; color: #ff3b18; }

.confirm-title {
  margin: 0 0 8px;
  font-size: 19px;
  font-weight: 700;
  color: #0f172a;
}

.confirm-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
}

.confirm-details {
  margin: 14px 0 0;
  padding: 12px 14px;
  list-style: none;
  text-align: left;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #334155;
}

.confirm-details li + li { margin-top: 6px; }

.confirm-reason {
  margin-top: 16px;
  text-align: left;
}

.confirm-reason label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.confirm-reason .required { color: #dc2626; }

.confirm-reason textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  color: #0f172a;
  background: #ffffff;
}

.confirm-reason textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.reason-error {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #dc2626;
}

.confirm-actions {
  display: flex;
  gap: 10px;
  margin-top: 22px;
}

.confirm-actions button {
  flex: 1;
  padding: 11px 16px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.15s ease;
}

.confirm-actions button:hover { filter: brightness(0.94); }

.btn-cancel {
  background: #e2e8f0;
  color: #334155;
}

.btn-accept { background: #ff3b18; color: #fff; }

.confirm-fade-enter-active,
.confirm-fade-leave-active { transition: opacity 0.16s ease; }

.confirm-fade-enter-from,
.confirm-fade-leave-to { opacity: 0; }
</style>
