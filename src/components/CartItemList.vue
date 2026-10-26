<script setup>
import { inject } from 'vue'
import CartItem from './CartItem.vue'

// Inject cart actions and new updateQuantity function from App.vue
const { cart, removeFromCart, updateQuantity } = inject('cartActions')
</script>

<template>
  <!-- Container chứa danh sách sản phẩm, sử dụng v-auto-animate để có hiệu ứng chuyển động mượt mà -->
  <div class="flex flex-col gap-4 flex-1" v-auto-animate>
    <!-- Vòng lặp để render từng sản phẩm trong giỏ hàng -->
    <CartItem
      v-for="item in cart"
      :key="item.id + (item.tenKichCo ? '-' + item.tenKichCo : '') + (item.tenMauSac ? '-' + item.tenMauSac : '')"
      :cart-item-id="item.id"
      :title="item.tenSanPham"
      :price="item.giaBanHienTai"
      :image-url="item.urlAnhDaiDien"
      :selected-size="item.tenKichCo"
      :selected-color="item.tenMauSac"
      :quantity="item.soLuong"
      :so-luong-ton-kho="item.soLuongTonKho"
      @on-click-remove="() => removeFromCart(item)"
      @on-increase-quantity="() => updateQuantity(item.id, 1)"
      @on-decrease-quantity="() => updateQuantity(item.id, -1)"
    />
  </div>
</template>

<style scoped>
/* Thêm thanh cuộn tùy chỉnh cho phần danh sách sản phẩm */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: #e2e8f0; /* bg-slate-200 */
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: #cbd5e1; /* bg-slate-300 */
}
</style>
