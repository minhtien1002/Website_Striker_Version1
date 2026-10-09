<script setup>
import CardSanPham from './CardSanPham.vue'

defineProps({
  items: Array
})

// Chỉ giữ lại emit cho toggle-favorite vì add-to-cart và buy-now đã chuyển hướng trực tiếp
const emit = defineEmits(['toggle-favorite'])
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
    <CardSanPham
      v-for="item in items"
      :key="item.id"
      :item="item"
      @toggle-favorite="(item) => emit('toggle-favorite', item)"
    />
  </div>
</template>
