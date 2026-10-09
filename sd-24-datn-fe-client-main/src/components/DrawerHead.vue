<script setup>
import { inject } from 'vue';

const { closeDrawer } = inject('cartActions');
</script>

<template>
  <div class="flex items-center justify-between p-4 mb-4 border-b border-gray-200">
    <div class="flex items-center">
      <h2 class="text-2xl font-bold font-josefin-sans text-primary">Giỏ hàng</h2>
    </div>
    <button
      @click="closeDrawer"
      class="ml-4 rounded-full size-8 flex items-center justify-center cursor-pointer text-gray-400 hover:text-primary transition-all duration-200"
    >
      <!-- Biểu tượng đóng (X) -->
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M1 17L17 1M1 1L17 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
  </div>
</template>
