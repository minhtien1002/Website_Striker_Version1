import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', () => {
  // Cart items state - starts empty
  const cartItems = ref([])

  // Computed properties
  const itemCount = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0)
  })

  const subtotal = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  })

  const shippingFee = computed(() => {
    return subtotal.value >= 1000000 ? 0 : 50000
  })

  // Actions
  const addToCart = (product) => {
    const existingItem = cartItems.value.find(item => 
      item.productId === product.productId && item.size === product.size
    )

    if (existingItem) {
      existingItem.quantity += product.quantity || 1
    } else {
      cartItems.value.push({
        id: `${product.productId}-${product.size}`,
        ...product,
        quantity: product.quantity || 1
      })
    }
  }

  const updateQuantity = (itemId, newQuantity) => {
    if (newQuantity < 1) return
    
    const item = cartItems.value.find(item => item.id === itemId)
    if (item) {
      item.quantity = newQuantity
    }
  }

  const removeFromCart = (itemId) => {
    const index = cartItems.value.findIndex(item => item.id === itemId)
    if (index > -1) {
      cartItems.value.splice(index, 1)
    }
  }

  const clearCart = () => {
    cartItems.value = []
  }

  // Format price utility
  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  return {
    cartItems,
    itemCount,
    subtotal,
    shippingFee,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    formatPrice
  }
})
