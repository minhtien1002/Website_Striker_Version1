<script setup>
import { ref, watch, provide, computed, onMounted } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { useToast } from 'vue-toastification'; // Chỉ import useToast

import Header from './components/Header.vue';
import Footer from './components/Footer.vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import { useConfirm } from './composables/useConfirm.js';

const { confirm } = useConfirm();

// Base URL for the APIs
const baseURL = 'http://localhost:8080/api';
const banHangClientUrl = `${baseURL}/ban-hang-client`;

// Initialize router and toast
const route = useRoute();
const toast = useToast();

// State
const cart = ref([]);
const isLoading = ref(false);

const router = useRouter();

// Computed property to check if current route is authentication
const isAuthRoute = computed(() => {
  return router.currentRoute.value.path === '/auth';
});

// Function to format currency
const formatCurrency = (amount) => {
  if (typeof amount !== 'number') {
    return '0 VND';
  }
  let formatted = new Intl.NumberFormat('vi-VN', {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount);

  return `${formatted} VND`;
};

// Computed properties
const totalPrice = computed(() => {
  return cart.value.reduce((acc, item) => acc + (item.giaBanHienTai * item.soLuong), 0);
});

// Function to fetch cart details from the API
const fetchCartDetails = async () => {
  const savedInvoiceId = localStorage.getItem('currentInvoiceId');
  if (!savedInvoiceId) {
    cart.value = [];
    return;
  }
  isLoading.value = true;
  try {
    const res = await axios.get(`${banHangClientUrl}/gio-hang/${savedInvoiceId}`);
    if (res.data && res.data.items) {
      cart.value = res.data.items.map(item => ({
        id: item.id,
        maCtsp: item.maSanPham,
        tenSanPham: item.tenSanPham,
        tenMauSac: item.tenMauSac,
        tenKichCo: item.tenKichCo,
        soLuong: item.soLuong,
        giaBanHienTai: item.gia,
        price: item.thanhTien,
        cartItemId: item.id,
        urlAnhSanPham: item.urlAnhSanPham
      }));
    } else {
      cart.value = [];
    }
  } catch (err) {
    console.error('Lỗi khi lấy chi tiết giỏ hàng!', err);
    
    if (err.response?.status === 400) {
      console.log('Cart invoice may have been completed. Clearing localStorage.');
      localStorage.removeItem('currentInvoiceId');
      cart.value = [];
      
      const isSuccessPage = (route.path === '/orders' || route.path === '/account/purchase-history') &&
                           route.query.status === 'success'
      if (!isSuccessPage) {
        console.warn('Invoice may be completed, but not on success page. Current path:', route.path)
      }
    } else if (err.response?.status !== 404) {
      toast.error('Không thể tải giỏ hàng. Vui lòng thử lại.');
      cart.value = [];
    } else {
      cart.value = [];
    }
  } finally {
    isLoading.value = false;
  }
};

// Function to create pending invoice (new API)
const createPendingInvoice = async () => {
  try {
    const response = await axios.post(`${banHangClientUrl}/hoa-don-cho`);
    return response.data;
  } catch (err) {
    console.error('Lỗi khi tạo hóa đơn chờ:', err);
    toast.error('Không thể tạo hóa đơn chờ. Vui lòng thử lại.');
    return null;
  }
};

// Function to add an item to the cart
const addToCart = async (item) => {
  const ok = await confirm({
    title: 'Thêm sản phẩm vào giỏ hàng?',
    details: [item.tenSanPham || item.name || 'Sản phẩm'],
    confirmText: 'Thêm vào giỏ'
  });
  if (!ok) return;
  isLoading.value = true;

  try {
    let savedInvoiceId = localStorage.getItem('currentInvoiceId');

    if (!savedInvoiceId) {
      const invoiceData = await createPendingInvoice();
      if (!invoiceData) {
        isLoading.value = false;
        return;
      }
      savedInvoiceId = invoiceData.id;
      localStorage.setItem('currentInvoiceId', savedInvoiceId);
    }
    
    const addToCartRequest = {
      idChiTietSanPham: item.id,
      soLuong: 1,
      gia: item.giaBan || item.price
    };

    await axios.post(
      `${banHangClientUrl}/gio-hang/${savedInvoiceId}/them-san-pham`,
      addToCartRequest
    );
    
    await fetchCartDetails();
    router.push('/cart');
    toast.success('Sản phẩm đã được thêm vào giỏ hàng!');
  } catch (err) {
    console.error('Lỗi khi thêm sản phẩm vào giỏ hàng!', err);
    toast.error('Không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại.');
  } finally {
    isLoading.value = false;
  }
};

const removeFromCart = async (item) => {
  const ok = await confirm({
    title: 'Xóa sản phẩm khỏi giỏ hàng?',
    details: [`${item.tenSanPham} (${item.tenMauSac} / ${item.tenKichCo})`],
    confirmText: 'Xóa',
    tone: 'danger'
  });
  if (!ok) return;
  isLoading.value = true;
  try {
    const savedInvoiceId = localStorage.getItem('currentInvoiceId');
    if (!savedInvoiceId) {
      throw new Error('Không tìm thấy giỏ hàng');
    }

    await axios.delete(`${banHangClientUrl}/gio-hang/${savedInvoiceId}/san-pham/${item.cartItemId}`);
    
    await fetchCartDetails();
    toast.info('Sản phẩm đã được xóa khỏi giỏ hàng.');

  } catch (err) {
    console.error('Lỗi khi xóa sản phẩm khỏi giỏ hàng!', err);
    toast.error('Không thể xóa sản phẩm khỏi giỏ hàng. Vui lòng thử lại.');
  } finally {
    isLoading.value = false;
  }
};

const updateQuantity = async (id, change) => {
  const item = cart.value.find(cartItem => cartItem.id === id);
  if (!item) return;

  const newQuantity = item.soLuong + change;
  if (newQuantity < 0) return;

  if (newQuantity === 0) {
    await removeFromCart(item);
    return;
  }
  
  if (change > 0 && newQuantity > item.soLuongTonKho) {
    toast.warning(`Số lượng tối đa cho sản phẩm này là ${item.soLuongTonKho}`);
    return;
  }
  
  isLoading.value = true;
  
  try {
    const savedInvoiceId = localStorage.getItem('currentInvoiceId');
    await axios.put(`${banHangClientUrl}/gio-hang/${savedInvoiceId}/san-pham/${item.cartItemId}`, { soLuong: newQuantity });
    await fetchCartDetails();
    toast.success('Số lượng sản phẩm đã được cập nhật.');
    
  } catch (err) {
    console.error('Lỗi khi cập nhật số lượng sản phẩm:', err);
    toast.error('Không thể cập nhật số lượng sản phẩm. Vui lòng thử lại.');
  } finally {
    isLoading.value = false;
  }
};

provide('cartActions', {
  cart,
  addToCart,
  removeFromCart,
  updateQuantity,
  formatCurrency,
  totalPrice,
  isLoading,
});

const handlePaymentReturn = async (query) => {
  if (query.payment !== 'vnpay' || !query.status) return false;
  const key = JSON.stringify(query);
  if (sessionStorage.getItem('handledPaymentReturn') === key) return true;
  sessionStorage.setItem('handledPaymentReturn', key);
  const remainder = localStorage.getItem('pendingRemainderInvoiceId');
  localStorage.removeItem('pendingRemainderInvoiceId');
  if (query.status === 'success') {
    if (remainder) localStorage.setItem('currentInvoiceId', remainder);
    else localStorage.removeItem('currentInvoiceId');
  } else if (remainder) {
    const current = localStorage.getItem('currentInvoiceId');
    try { if (current) await axios.post(`${banHangClientUrl}/gio-hang/${remainder}/gop-vao/${current}`); } catch { /* bỏ qua */ }
  }
  await fetchCartDetails();
  return true;
};

onMounted(async () => {
  if (await handlePaymentReturn(route.query)) return;
  if (localStorage.getItem('currentInvoiceId')) fetchCartDetails();
});

watch(() => route.query, (q) => { handlePaymentReturn(q); });
</script>

<template>
  <!-- Conditional layout based on route -->
  <div v-if="isAuthRoute">
    <!-- Auth layout without header/footer -->
    <router-view></router-view>
  </div>
  
  <div v-else>
    <!-- Main layout with header/footer -->
    <Header :total-price="totalPrice" />
    <main class="pt-0">
      <router-view></router-view>
    </main>
    <Footer />
  </div>

  <!-- Hộp xác nhận dùng chung cho mọi thao tác của khách hàng -->
  <ConfirmDialog />
</template>

<style>
/* Global styles go here if needed, but Tailwind is preferred */
html {
  /* Cỡ chữ gốc lớn hơn mặc định trình duyệt để dễ đọc hơn - các đơn vị rem sẽ tự scale theo. */
  font-size: 17px;
}

body {
  background-color: #f7f9fc;
  /* Nunito: chữ tròn trịa, dễ đọc hơn - đồng bộ với toàn bộ trang. */
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  /* Đậm hơn mặc định (400) một chút để chữ rõ ràng, dễ đọc hơn. */
  font-weight: 500;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-container {
  min-height: calc(100vh - 56px * 2); /* Adjust based on your header/footer height */
}
</style>