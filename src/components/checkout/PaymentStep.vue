<template>
  <div>
    <div class="flex items-center gap-3 mb-4">
      <div class="w-8 h-8 bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg flex items-center justify-center">
        <span class="text-white font-bold text-sm">3</span>
      </div>
      <div>
        <h2 class="text-xl font-bold text-gray-900">Phương thức thanh toán</h2>
        <p class="text-sm text-gray-600">Chọn phương thức thanh toán</p>
      </div>
    </div>

    <!-- Order Summary Review -->
    <div class="bg-gray-50 rounded-xl p-6 mb-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Xác nhận thông tin đơn hàng</h3>
      
      <!-- Customer Info -->
      <div class="mb-4 p-4 bg-white rounded-lg">
        <h4 class="font-medium text-gray-900 mb-2">Thông tin khách hàng</h4>
        <div v-if="selectedAddress" class="text-sm text-gray-600">
          <p><strong>{{ selectedAddress.ten }}</strong> | {{ selectedAddress.soDienThoai }}</p>
          <p>{{ selectedAddress.diaChiCuThe }}, {{ selectedAddress.phuong }}, {{ selectedAddress.quan }}, {{ selectedAddress.thanhPho }}</p>
        </div>
        <div v-else-if="customerInfo.ten" class="text-sm text-gray-600">
          <p><strong>{{ customerInfo.ten }}</strong> | {{ customerInfo.soDienThoai }}</p>
          <p>{{ customerInfo.email }}</p>
          <p>{{ customerInfo.diaChi }}</p>
        </div>
      </div>

      <!-- Cart Items Summary -->
      <div class="mb-4 p-4 bg-white rounded-lg">
        <h4 class="font-medium text-gray-900 mb-3">Sản phẩm ({{ cartItems.length }} món)</h4>
        <div class="space-y-2 max-h-40 overflow-y-auto">
          <div
            v-for="item in cartItems"
            :key="item.id"
            class="flex items-center justify-between text-sm"
          >
            <div class="flex items-center gap-2">
              <img
                :src="item.urlAnhSanPham || item.urlAnhDaiDien || '/placeholder-product.svg'"
                :alt="item.tenSanPham"
                class="w-8 h-8 object-cover rounded"
                @error="$event.target.src = '/placeholder-product.svg'"
              />
              <div>
                <span class="text-gray-900">{{ item.tenSanPham }}</span>
                <div class="text-gray-500 text-xs">
                  <span v-if="item.tenKichCo">{{ item.tenKichCo }}</span>
                  <span v-if="item.tenKichCo && item.tenMauSac"> • </span>
                  <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                  <span> • SL: {{ item.soLuong }}</span>
                </div>
              </div>
            </div>
            <span class="font-medium text-gray-900">
              {{ formatCurrency((item.giaBanHienTai || item.gia) * item.soLuong) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Price Breakdown -->
      <div class="p-4 bg-white rounded-lg">
        <div class="space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-600">Tạm tính:</span>
            <span class="font-medium">{{ formatCurrency(subtotal) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Phí vận chuyển:</span>
            <span class="font-medium">{{ formatCurrency(shippingFee) }}</span>
          </div>
          <div v-if="discountAmount > 0" class="flex justify-between text-green-600">
            <span>Giảm giá:</span>
            <span class="font-medium">-{{ formatCurrency(discountAmount) }}</span>
          </div>
          <div class="border-t pt-2 flex justify-between items-center">
            <span class="font-bold text-gray-900">Tổng cộng:</span>
            <span class="text-lg font-bold text-accent">{{ formatCurrency(finalTotal) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment Methods -->
    <div class="bg-white border border-gray-200 rounded-xl p-6 mb-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Chọn phương thức thanh toán</h3>
      
      <div class="space-y-4">
        <!-- COD Payment -->
        <div
          class="p-4 border border-gray-200 rounded-lg cursor-pointer hover:border-accent hover:bg-accent/5 transition-all"
          :class="{ 'border-accent bg-accent/10': localPaymentMethod === 'COD' }"
          @click="selectPaymentMethod('COD')"
        >
          <div class="flex items-center gap-4">
            <div class="w-6 h-6 rounded-full border-2 border-accent flex items-center justify-center"
                 :class="{ 'bg-accent': localPaymentMethod === 'COD' }">
              <div v-if="localPaymentMethod === 'COD'" class="w-2 h-2 bg-white rounded-full"></div>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
              <i class="fas fa-money-bill-wave text-green-600 text-xl"></i>
            </div>
            <div class="flex-1">
              <h4 class="font-semibold text-gray-900">Thanh toán khi nhận hàng (COD)</h4>
              <p class="text-sm text-gray-600 mt-1">Thanh toán bằng tiền mặt khi nhận hàng</p>
              <div class="flex items-center gap-2 mt-2">
                <i class="fas fa-check-circle text-green-500 text-xs"></i>
                <span class="text-xs text-green-600">An toàn, tiện lợi</span>
              </div>
            </div>
          </div>
        </div>

        <!-- VNPay Payment -->
        <div
          class="p-4 border border-gray-200 rounded-lg cursor-pointer hover:border-accent hover:bg-accent/5 transition-all"
          :class="{ 'border-accent bg-accent/10': localPaymentMethod === 'VNPAY' }"
          @click="selectPaymentMethod('VNPAY')"
        >
          <div class="flex items-center gap-4">
            <div class="w-6 h-6 rounded-full border-2 border-accent flex items-center justify-center"
                 :class="{ 'bg-accent': localPaymentMethod === 'VNPAY' }">
              <div v-if="localPaymentMethod === 'VNPAY'" class="w-2 h-2 bg-white rounded-full"></div>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
              <i class="fas fa-credit-card text-blue-600 text-xl"></i>
            </div>
            <div class="flex-1">
              <h4 class="font-semibold text-gray-900">Thanh toán qua VNPay</h4>
              <p class="text-sm text-gray-600 mt-1">Thanh toán online qua cổng VNPay</p>
              <div class="flex items-center gap-4 mt-2">
                <div class="flex items-center gap-1">
                  <i class="fas fa-shield-alt text-blue-500 text-xs"></i>
                  <span class="text-xs text-blue-600">Bảo mật cao</span>
                </div>
                <div class="flex items-center gap-1">
                  <i class="fas fa-bolt text-yellow-500 text-xs"></i>
                  <span class="text-xs text-yellow-600">Xử lý nhanh</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Method Details -->
      <div v-if="localPaymentMethod" class="mt-6 p-4 bg-gray-50 rounded-lg">
        <div v-if="localPaymentMethod === 'COD'">
          <h5 class="font-medium text-gray-900 mb-2">Lưu ý khi thanh toán COD:</h5>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>• Vui lòng chuẩn bị đúng số tiền: <strong class="text-accent">{{ formatCurrency(finalTotal) }}</strong></li>
            <li>• Kiểm tra kỹ sản phẩm trước khi thanh toán</li>
            <li>• Đơn hàng sẽ được giao trong 2-3 ngày làm việc</li>
            <li>• Phí COD đã bao gồm trong tổng tiền</li>
          </ul>
        </div>
        <div v-else-if="localPaymentMethod === 'VNPAY'">
          <h5 class="font-medium text-gray-900 mb-2">Thanh toán qua VNPay:</h5>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>• Hỗ trợ thanh toán qua thẻ ATM, Internet Banking</li>
            <li>• Giao dịch được mã hóa và bảo mật tuyệt đối</li>
            <li>• Đơn hàng sẽ được xử lý ngay sau khi thanh toán thành công</li>
            <li>• Bạn sẽ được chuyển đến trang VNPay để hoàn tất thanh toán</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Terms and Conditions -->
    <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6">
      <label class="flex items-start gap-3 cursor-pointer">
        <input
          v-model="agreeToTerms"
          type="checkbox"
          class="checkbox-custom mt-1 w-5 h-5 text-accent border-2 border-gray-300 rounded focus:ring-accent focus:ring-2"
        />
        <div class="text-sm text-blue-800">
          <p>Tôi đồng ý với <a href="#" class="underline hover:text-blue-900">Điều khoản và Điều kiện</a> 
          và <a href="#" class="underline hover:text-blue-900">Chính sách Bảo mật</a> của STRIKER.</p>
          <p class="mt-1 text-blue-600">Bằng việc đặt hàng, bạn xác nhận rằng tất cả thông tin đã cung cấp là chính xác.</p>
        </div>
      </label>
    </div>

    <!-- Navigation Buttons -->
    <div class="mt-6 pt-4 border-t border-gray-200">
      <div class="flex items-center justify-between">
        <button
          @click="$emit('prev-step')"
          class="px-4 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors font-medium"
        >
          <i class="fas fa-arrow-left mr-2"></i>Quay lại
        </button>
        
        <button
          @click="completeOrder"
          :disabled="!canCompleteOrder || processingOrder"
          class="px-6 py-2 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors min-w-[160px]"
        >
          <span v-if="processingOrder">
            <i class="fas fa-spinner fa-spin mr-2"></i>Đang xử lý...
          </span>
          <span v-else>
            <i class="fas fa-credit-card mr-2"></i>Hoàn tất đơn hàng
          </span>
        </button>
      </div>
    </div>

    <!-- Order Confirmation Modal -->
    <div v-if="showConfirmModal" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl p-8 max-w-md w-full mx-4">
        <div class="text-center">
          <div class="w-16 h-16 bg-accent rounded-full flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-check text-2xl text-white"></i>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-4">Xác nhận đặt hàng</h3>
          <p class="text-gray-600 mb-6">
            Bạn có chắc chắn muốn đặt hàng với tổng giá trị 
            <span class="font-bold text-accent">{{ formatCurrency(finalTotal) }}</span>?
          </p>
          <div class="flex gap-4">
            <button
              @click="showConfirmModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Hủy
            </button>
            <button
              @click="confirmOrder"
              :disabled="processingOrder"
              class="flex-1 px-4 py-2 bg-accent hover:bg-accent-dark disabled:bg-gray-300 text-white rounded-lg transition-colors"
            >
              <span v-if="processingOrder">
                <i class="fas fa-spinner fa-spin mr-2"></i>Đang xử lý
              </span>
              <span v-else>Xác nhận</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { vnpayService } from '@/services/vnpayAPI.js'
import { calcShippingFee } from '@/utils/shipping'

const props = defineProps({
  paymentMethod: {
    type: String,
    default: ''
  },
  customerInfo: {
    type: Object,
    required: true
  },
  selectedAddress: {
    type: Object,
    default: null
  },
  cartItems: {
    type: Array,
    required: true
  },
  totalPrice: {
    type: Number,
    required: true
  },
  discountAmount: {
    type: Number,
    default: 0
  },
  voucherCode: {
    type: String,
    default: ''
  },
  voucherId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:paymentMethod', 'prev-step', 'complete-order'])

const toast = useToast()

// Local reactive data
const localPaymentMethod = ref(props.paymentMethod)
const agreeToTerms = ref(false)
const showConfirmModal = ref(false)
const processingOrder = ref(false)

// Computed properties
const subtotal = computed(() => {
  if (!props.cartItems || props.cartItems.length === 0) return 0
  
  return props.cartItems.reduce((sum, item) => {
    const price = item.giaBanHienTai || item.gia || 0
    return sum + (price * item.soLuong)
  }, 0)
})

// Dùng chung quy tắc phí ship với giỏ hàng, trang thanh toán và backend.
// Bản cũ tự đặt riêng "miễn phí từ 1.000.000đ, phí 50.000đ" trong khi mọi nơi khác là
// "miễn phí từ 500.000đ, phí 30.000đ" -> bước cuối báo cho khách một con số còn hệ thống
// ghi nợ một con số khác.
const shippingFee = computed(() => calcShippingFee(subtotal.value))

// Tổng khách phải trả, tự tính lại từ tiền hàng thay vì tin prop `totalPrice`.
// Prop đó do CheckoutPage truyền xuống và ĐÃ gồm phí ship + giảm giá, nhưng khối tóm tắt
// bên dưới lại hiển thị nó cạnh dòng "Phí vận chuyển" như thể chưa cộng -> bảng tính
// không khớp với dòng tổng (750.000 + 50.000 - 100.000 mà tổng lại hiện 650.000).
const finalTotal = computed(() => Math.max(0, subtotal.value + shippingFee.value - props.discountAmount))

const canCompleteOrder = computed(() => {
  return localPaymentMethod.value && agreeToTerms.value
})

// Methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const selectPaymentMethod = (method) => {
  localPaymentMethod.value = method
  emit('update:paymentMethod', method)
}

const completeOrder = () => {
  if (!canCompleteOrder.value) {
    if (!localPaymentMethod.value) {
      toast.warning('Vui lòng chọn phương thức thanh toán')
      return
    }
    if (!agreeToTerms.value) {
      toast.warning('Vui lòng đồng ý với điều khoản và điều kiện')
      return
    }
  }
  
  showConfirmModal.value = true
}

const confirmOrder = async () => {
  processingOrder.value = true
  showConfirmModal.value = false
  
  try {
    const orderData = {
      customerInfo: props.customerInfo,
      selectedAddress: props.selectedAddress,
      cartItems: props.cartItems,
      paymentMethod: localPaymentMethod.value,
      totalPrice: props.totalPrice,
      discountAmount: props.discountAmount,
      voucherCode: props.voucherCode,
      shippingFee: shippingFee.value,
      subtotal: subtotal.value
    }
    
    // Handle VNPay payment
    if (localPaymentMethod.value === 'VNPAY') {
      await handleVNPayPayment(orderData)
    } else {
      // Handle COD or other payment methods
      emit('complete-order', orderData)
    }
  } catch (error) {
    console.error('Error in confirmOrder:', error)
    toast.error('Có lỗi xảy ra khi xử lý đơn hàng')
    processingOrder.value = false
  }
}

const handleVNPayPayment = async (orderData) => {
  try {
    console.log('Processing VNPay payment...')
    
    // Debug: Check if invoice ID exists
    const currentInvoiceId = localStorage.getItem('currentInvoiceId')
    console.log('Current invoice ID from localStorage:', currentInvoiceId)
    
    if (!currentInvoiceId) {
      console.error('No currentInvoiceId found in localStorage!')
      toast.error('Không tìm thấy giỏ hàng. Vui lòng thêm sản phẩm lại.')
      processingOrder.value = false
      return
    }
    
    // Prepare customer info for VNPay
    let customerInfo = {}
    
    if (props.selectedAddress) {
      // Use selected address for logged-in users
      customerInfo = {
        name: props.selectedAddress.ten || props.selectedAddress.tenKhachHang,
        phone: props.selectedAddress.soDienThoai,
        email: props.selectedAddress.email || props.customerInfo.email || '',
        address: `${props.selectedAddress.diaChiCuThe}, ${props.selectedAddress.phuong}, ${props.selectedAddress.quan}, ${props.selectedAddress.thanhPho}`
      }
    } else {
      // Use customer info for guest users
      customerInfo = {
        name: props.customerInfo.ten || props.customerInfo.name,
        phone: props.customerInfo.soDienThoai || props.customerInfo.phone,
        email: props.customerInfo.email || '',
        address: props.customerInfo.diaChi || props.customerInfo.address
      }
    }
    
    // Validate VNPay data
    const validation = vnpayService.validateVNPayData({ customerInfo })
    if (!validation.isValid) {
      toast.error(validation.errors.join(', '))
      processingOrder.value = false
      return
    }
    
    // Prepare VNPay payment data
    const vnpayData = {
      customerInfo,
      shippingFee: orderData.shippingFee || 0,
      discount: orderData.discountAmount || 0,
      notes: `Đơn hàng từ ${customerInfo.name} - ${customerInfo.phone}`,
      voucherId: props.voucherId || null
    }
    
    console.log('VoucherId from props:', props.voucherId, 'Type:', typeof props.voucherId)
    
    console.log('VNPay payment data:', vnpayData)
    
    // Show processing message
    toast.info('Đang chuyển hướng đến VNPay...', {
      timeout: 2000
    })
    
    // Create VNPay payment and redirect
    await vnpayService.createVNPayPayment(vnpayData)
    
    // Note: User will be redirected to VNPay, so this code won't execute
    // The return handling is done in the VNPay return URL
    
  } catch (error) {
    console.error('VNPay payment error:', error)
    toast.error(error.message || 'Không thể tạo thanh toán VNPay')
    processingOrder.value = false
  }
}

// Watch for prop changes
watch(() => props.paymentMethod, (newValue) => {
  localPaymentMethod.value = newValue
})
</script>
