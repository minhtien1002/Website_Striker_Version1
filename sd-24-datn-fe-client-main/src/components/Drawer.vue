<script setup>
import { inject, computed } from 'vue';
import DrawerHead from './DrawerHead.vue';
import CartItemList from './CartItemList.vue';
import { useRouter } from 'vue-router';
import { vAutoAnimate } from '@formkit/auto-animate/vue';

// Inject các giá trị và hàm cần thiết từ App.vue
const { closeDrawer, formatCurrency, cart, isCreatingOrder, totalPrice, isLoading } = inject('cartActions');
const router = useRouter();

// Điều hướng đến trang thanh toán và đóng drawer
const goToCheckout = () => {
  closeDrawer();
  router.push('/checkout');
};

// Tính toán trạng thái disabled cho nút "Đặt hàng"
const cartButtonDisabled = computed(() => cart.value.length === 0 || isCreatingOrder.value || isLoading.value);
</script>

<template v-auto-animate>
  <!-- Lớp phủ tối khi drawer mở -->
  <div @click="closeDrawer" class="fixed top-0 left-0 h-full w-full bg-black z-10 opacity-50"></div>

  <!-- Drawer chính -->
  <div v-auto-animate
    class="bg-white w-full md:w-[500px] h-full fixed right-0 top-0 z-20 p-8 flex flex-col shadow-2xl overflow-y-auto transform transition-transform duration-300 ease-in-out translate-x-0">
    <DrawerHead />

    <!-- Trạng thái tải -->
    <div v-if="isLoading" class="flex flex-1 items-center justify-center">
      <div class="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-accent"></div>
    </div>

    <!-- Danh sách sản phẩm trong giỏ hàng -->
    <div v-else-if="cart?.length > 0" class="flex flex-col flex-1 overflow-y-auto mt-6 pr-4">
      <CartItemList />
    </div>

    <!-- Thông báo giỏ hàng trống -->
    <div v-else class="flex flex-1 items-center justify-center">
      <InfoBlock title="Giỏ hàng trống" description="Thêm ít nhất một chiếc áo thể thao để đặt hàng."
        image-url="/empty-box.png" alt-text="Một chiếc hộp rỗng" />
    </div>

    <!-- Tóm tắt đơn hàng và nút -->
    <div v-if="cart?.length > 0 && !isLoading" class="flex flex-col gap-6 mt-6 p-6 bg-gray-50 rounded-xl">
      <div class="flex flex-col gap-2">
        <div class="flex justify-between items-center text-lg font-semibold text-gray-700">
          <span>Tổng cộng:</span>
          <b class="text-2xl text-accent font-extrabold">{{ formatCurrency(totalPrice) }}</b>
        </div>
      </div>

      <button type="button" :disabled="cartButtonDisabled" @click="goToCheckout"
        class="w-full py-4 rounded-full text-white font-bold transition-all duration-300 shadow-xl group hover:shadow-2xl"
        :class="{
          'bg-red-700 hover:bg-red-800 active:bg-red-900': !cartButtonDisabled,
          'bg-gray-400 cursor-not-allowed': cartButtonDisabled,
        }">
        <div class="flex items-center justify-center w-full">
          <span>Đặt hàng</span>
          <svg class="w-5 h-5 ml-2 transition-transform duration-300 group-hover:translate-x-1"
            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
          </svg>
        </div>
      </button>
    </div>
  </div>
</template>
