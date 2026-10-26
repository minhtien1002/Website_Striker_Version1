import { ref, computed, reactive } from 'vue'
import { cartService } from '@/services/cartAPI'
import { useNotification } from '@/composables/useNotification'

// Global cart state
const cartState = reactive({
  cart: null,
  loading: false,
  error: null
})

export function useCart() {
  const loading = ref(false)
  const error = ref(null)
  const { success, error: showError } = useNotification()

  // Computed properties
  const cart = computed(() => cartState.cart)
  const itemCount = computed(() => cartState.cart?.totalItems || 0)
  const totalAmount = computed(() => cartState.cart?.tongTien || 0)
  const isEmpty = computed(() => !cartState.cart || cartState.cart.items?.length === 0)

  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  }

  // Load cart data
  const loadCart = async () => {
    try {
      loading.value = true
      error.value = null
      
      const cartData = await cartService.getCart()
      console.log('useCart loadCart received data:', cartData)
      console.log('useCart setting cartState.cart to:', cartData)
      console.log('useCart cartData.totalItems:', cartData?.totalItems)
      console.log('useCart cartData.items.length:', cartData?.items?.length)
      
      cartState.cart = cartData
      
      // If cartData is null, it means cart is empty (not an error)
      if (!cartData) {
        console.log('Cart is empty (null response)')
      }
      
      return cartData
    } catch (err) {
      // Only set error for actual API errors, not empty cart
      if (err.message && !err.message.includes('Giỏ hàng trống')) {
        error.value = err.message
        showError(err.message)
      }
      console.error('Error loading cart:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // Add product to cart
  const addToCart = async (productDetail, quantity = 1) => {
    try {
      loading.value = true
      error.value = null

      // Validate input
      if (!productDetail || !productDetail.id) {
        throw new Error('Thông tin sản phẩm không hợp lệ')
      }

      if (quantity <= 0) {
        throw new Error('Số lượng phải lớn hơn 0')
      }

      if (quantity > productDetail.soLuongTonKho) {
        throw new Error(`Chỉ còn ${productDetail.soLuongTonKho} sản phẩm trong kho`)
      }

      const cartData = await cartService.addToCart(
        productDetail.id,
        quantity,
        productDetail.giaBan || productDetail.price
      )

      cartState.cart = cartData
      // itemCount is now computed from cartState.cart.totalItems
      
      success('Đã thêm sản phẩm vào giỏ hàng!')
      return cartData
    } catch (err) {
      error.value = err.message
      showError(err.message || 'Không thể thêm sản phẩm vào giỏ hàng')
      console.error('Error adding to cart:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Update item quantity
  const updateQuantity = async (cartItemId, quantity) => {
    try {
      loading.value = true
      error.value = null

      if (quantity <= 0) {
        throw new Error('Số lượng phải lớn hơn 0')
      }

      const cartData = await cartService.updateQuantity(cartItemId, quantity)
      cartState.cart = cartData
      // itemCount is now computed from cartState.cart.totalItems
      
      success('Đã cập nhật số lượng sản phẩm!')
      return cartData
    } catch (err) {
      error.value = err.message
      showError(err.message || 'Không thể cập nhật số lượng sản phẩm')
      console.error('Error updating quantity:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Remove item from cart
  const removeItem = async (cartItemId) => {
    try {
      loading.value = true
      error.value = null

      const cartData = await cartService.removeItem(cartItemId)
      cartState.cart = cartData
      // itemCount is now computed from cartState.cart.totalItems
      
      success('Đã xóa sản phẩm khỏi giỏ hàng!')
      return cartData
    } catch (err) {
      error.value = err.message
      showError(err.message || 'Không thể xóa sản phẩm khỏi giỏ hàng')
      console.error('Error removing item:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Clear entire cart
  const clearCart = async () => {
    try {
      loading.value = true
      error.value = null

      await cartService.clearCart()
      cartState.cart = null
      // itemCount will be 0 when cart is null (computed)
      
      success('Đã xóa tất cả sản phẩm khỏi giỏ hàng!')
      return true
    } catch (err) {
      error.value = err.message
      showError(err.message || 'Không thể xóa giỏ hàng')
      console.error('Error clearing cart:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Process payment
  const processPayment = async (paymentData) => {
    try {
      loading.value = true
      error.value = null

      const result = await cartService.processPayment(paymentData)
      
      // Clear cart state after successful payment
      cartState.cart = null
      // itemCount will be 0 when cart is null (computed)

      return result
    } catch (err) {
      error.value = err.message
      console.error('Error processing payment:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Initialize cart (load on app start)
  const initializeCart = async () => {
    try {
      // Just load the cart data, itemCount will be computed
      await loadCart()
    } catch (err) {
      console.error('Error initializing cart:', err)
    }
  }

  // Set customer ID when user logs in
  const setCustomerId = (customerId) => {
    cartService.setCustomerId(customerId)
  }

  // Clear customer ID when user logs out
  const clearCustomerId = () => {
    cartService.clearCustomerId()
  }

  // Clear error
  const clearError = () => {
    error.value = null
  }

  return {
    // State
    cart,
    loading,
    error,
    itemCount,
    totalAmount,
    isEmpty,

    // Methods
    loadCart,
    addToCart,
    updateQuantity,
    removeItem,
    clearCart,
    processPayment,
    initializeCart,
    setCustomerId,
    clearCustomerId,
    clearError,
    formatCurrency
  }
}
