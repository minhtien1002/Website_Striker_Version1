<script setup>
import { inject } from 'vue';
const emit = defineEmits(['onClickRemove', 'onIncreaseQuantity', 'onDecreaseQuantity'])

// Inject hàm formatCurrency từ App.vue
const { formatCurrency } = inject('cartActions');

// Định nghĩa các props mà component này nhận được
defineProps({
  id: Number,
  title: String,
  imageUrl: String,
  price: Number,
  selectedSize: [String, Number], 
  selectedColor: String,
  quantity: { // Prop số lượng sản phẩm
    type: Number,
    required: true,
  },
  soLuongTonKho: { // Prop số lượng tồn kho của sản phẩm
    type: Number,
    required: true,
  }
})

</script>

<template>
  <!-- Thẻ bọc ngoài với các style Tailwind để tạo khung cho sản phẩm trong giỏ hàng -->
  <div class="flex items-center p-6 rounded-2xl gap-6 font-sans bg-white shadow-md border border-gray-100 hover:shadow-xl transition-all duration-300">
    <!-- Hình ảnh sản phẩm -->
    <img class="w-20 h-20 object-cover rounded-xl" :src="imageUrl" :alt="title" />

    <!-- Phần thông tin sản phẩm -->
    <div class="flex flex-col flex-1">
      <p class="font-bold text-gray-800 text-lg">{{ title }}</p>
      
      <!-- Hiển thị size và màu sắc nếu có -->
      <div v-if="selectedSize || selectedColor" class="flex items-center gap-2 text-gray-500 text-sm mt-2">
        <span v-if="selectedSize">Size: {{ selectedSize }}</span>
        <span v-if="selectedSize && selectedColor">•</span>
        <span v-if="selectedColor">Màu: {{ selectedColor }}</span>
      </div>
      
      <!-- Stock warning -->
      <div v-if="soLuongTonKho <= 5" class="text-xs mt-1">
        <span v-if="soLuongTonKho === 0" class="text-red-600 font-semibold">
          ⚠️ Hết hàng
        </span>
        <span v-else-if="soLuongTonKho <= 5" class="text-orange-600 font-semibold">
          ⚠️ Chỉ còn {{ soLuongTonKho }} sản phẩm
        </span>
      </div>
      
      <!-- Giá và nút xóa -->
      <div class="flex justify-between items-center mt-3">
        <!-- Div bọc ngoài để quản lý font-size linh hoạt -->
        <div class="flex-grow">
          <b class="text-xl text-accent font-extrabold" style="white-space: nowrap;">
            <span class="inline-block transition-all duration-300 ease-in-out" :style="{ 'font-size': price * quantity > 9999999 ? '1rem' : '1.25rem' }">
              {{ formatCurrency(price * quantity) }}
            </span>
          </b>
        </div>
        
        <!-- Các nút tăng/giảm số lượng và nút xóa -->
        <div class="flex items-center gap-3 ml-auto">
          <!-- Button giảm số lượng -->
          <button
            @click="emit('onDecreaseQuantity')"
            class="p-2 rounded-full text-gray-500 bg-gray-100 hover:bg-gray-200 transition-all duration-200"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path>
            </svg>
          </button>

          <!-- Hiển thị số lượng hiện tại -->
          <span class="font-bold text-gray-700 text-lg">{{ quantity }}</span>
          
          <!-- Button tăng số lượng -->
          <button
            @click="emit('onIncreaseQuantity')"
            :disabled="quantity >= soLuongTonKho"
            :class="[
              'p-2 rounded-full transition-all duration-200',
              quantity >= soLuongTonKho 
                ? 'text-gray-300 bg-gray-50 cursor-not-allowed' 
                : 'text-gray-500 bg-gray-100 hover:bg-gray-200'
            ]"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
