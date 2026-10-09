<template>
  <div class="app">
    <component :is="currentLayout">
      <router-view />
    </component>

    <!-- Hộp thoại xác nhận dùng chung - mount đúng một lần tại đây, mọi màn gọi qua
         useConfirm().confirm(). Xem src/composables/useConfirm.js. -->
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

// Layouts
import AuthLayout from './layouts/AuthLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'

// Route tracking
const route = useRoute()

// Dynamic layout selection
const currentLayout = computed(() => {
  // Sử dụng AuthLayout cho trang đăng nhập
  if (route.path === '/login') {
    return AuthLayout
  }
  // Sử dụng AdminLayout cho tất cả các trang khác
  return AdminLayout
})
</script>

<style scoped>
.app {
  width: 100%;
  height: 100vh;
}
</style>
