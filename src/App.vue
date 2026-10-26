<script setup>
import { ref, watch, provide, computed, onMounted } from 'vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { useToast } from 'vue-toastification';

import Header from './components/Header.vue';
import Footer from './components/Footer.vue';

// Base URL for the APIs
const baseURL = 'http://localhost:8080/api';
const banHangClientUrl = `${baseURL}/ban-hang-client`;
const gioHangChiTietsUrl = `${baseURL}/gioHangChiTiets`;

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

// Cart actions - removed drawer functions

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
    
    // If cart loading fails with 400 error, it might be because the invoice is no longer a "pending cart"
    // (e.g., after successful payment). Clear the localStorage to prevent repeated errors.
    if (err.response?.status === 400) {
      console.log('Cart invoice may have been completed. Clearing localStorage.');
      localStorage.removeItem('currentInvoiceId');
      cart.value = [];
      
      // Don't show error toast for completed invoices on success pages
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
    // Use new cart API to create pending invoice
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
  isLoading.value = true;

  try {
    // 1. Check if invoice ID already exists
    let savedInvoiceId = localStorage.getItem('currentInvoiceId');

    // 2. If no invoice ID exists, create a new pending invoice
    if (!savedInvoiceId) {
      const invoiceData = await createPendingInvoice();
      if (!invoiceData) {
        isLoading.value = false;
        return;
      }
      savedInvoiceId = invoiceData.id;
      localStorage.setItem('currentInvoiceId', savedInvoiceId);
    }
    
    // 3. Add item to cart using new API
    const addToCartRequest = {
      idChiTietSanPham: item.id, // Use product detail ID
      soLuong: 1,
      gia: item.giaBan || item.price
    };

    await axios.post(
      `${banHangClientUrl}/gio-hang/${savedInvoiceId}/them-san-pham`,
      addToCartRequest
    );
    
    // 4. Update local cart state
    await fetchCartDetails();
    
    // Redirect to cart page instead of opening drawer
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
    const updatedItem = {
      ...item,
      soLuong: newQuantity
    };
    
    await axios.put(`${gioHangChiTietsUrl}/${id}`, updatedItem);
    await fetchCartDetails();
    toast.success('Số lượng sản phẩm đã được cập nhật.');
    
  } catch (err) {
    console.error('Lỗi khi cập nhật số lượng sản phẩm:', err);
    toast.error('Không thể cập nhật số lượng sản phẩm. Vui lòng thử lại.');
  } finally {
    isLoading.value = false;
  }
};

// Removed createOrder and clearCart functions - not needed for new cart system

// Provide data and actions to child components
provide('cartActions', {
  cart,
  addToCart,
  removeFromCart,
  updateQuantity,
  formatCurrency,
  totalPrice,
  isLoading,
});


// Load cart on app start
onMounted(() => {
  // Check for payment success BEFORE loading cart
  const isPaymentSuccessPage = (route.path === '/orders' || route.path === '/account/purchase-history') &&
                               (route.query.payment === 'vnpay' || route.query.payment === 'cod') &&
                               route.query.status === 'success'
  
  // Also check for any URL containing payment success indicators
  const urlContainsPaymentSuccess = window.location.href.includes('payment=vnpay') || 
                                   window.location.href.includes('payment=cod') ||
                                   window.location.href.includes('status=success')
  
  if (isPaymentSuccessPage || urlContainsPaymentSuccess) {
    console.log('Payment success detected on mount, clearing cart localStorage immediately');
    console.log('Current URL:', window.location.href);
    console.log('Route path:', route.path);
    console.log('Route query:', route.query);
    
    localStorage.removeItem('currentInvoiceId');
    cart.value = [];
    return; // Don't load cart for payment success pages
  }
  
  const savedInvoiceId = localStorage.getItem('currentInvoiceId');
  if (savedInvoiceId) {
    console.log('Loading cart with invoice ID:', savedInvoiceId);
    fetchCartDetails();
  }
});

// Watch for route changes to clear cart after successful payment
watch(() => route.path, (newPath) => {
  console.log('Route changed to:', newPath, 'Query:', route.query)
  
  // Clear cart when navigating to order success pages after payment
  if ((newPath === '/orders' || newPath === '/account/purchase-history') && 
      (route.query.payment === 'vnpay' || route.query.payment === 'cod') && 
      route.query.status === 'success') {
    console.log('Payment success detected, clearing cart localStorage');
    localStorage.removeItem('currentInvoiceId');
    cart.value = [];
  }
});

// Also watch for query changes (for cases where path doesn't change but query does)
watch(() => route.query, (newQuery) => {
  if ((route.path === '/orders' || route.path === '/account/purchase-history') && 
      (newQuery.payment === 'vnpay' || newQuery.payment === 'cod') && 
      newQuery.status === 'success') {
    console.log('Payment success detected via query change, clearing cart localStorage');
    localStorage.removeItem('currentInvoiceId');
    cart.value = [];
  }
});

// Watch for any payment-related query params and clear cart
watch(() => route.query.payment, (paymentMethod) => {
  if (paymentMethod && route.query.status === 'success') {
    console.log('Payment success detected via payment query param, clearing cart localStorage');
    localStorage.removeItem('currentInvoiceId');
    cart.value = [];
  }
});
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