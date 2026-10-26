import { ref, onUnmounted } from 'vue'
import stockUpdateService from '@/services/stockUpdateService'

export function useStockUpdates() {
  const stockLevels = ref(new Map())
  const listeners = new Map()

  /**
   * Watch stock updates for a specific product detail
   */
  const watchStock = (productDetailId, callback) => {
    // Create callback wrapper
    const wrappedCallback = (id, newStock, oldStock) => {
      // Update local cache
      stockLevels.value.set(id, newStock)
      
      // Call user callback
      if (callback) {
        callback(id, newStock, oldStock)
      }
    }

    // Store listener reference for cleanup
    listeners.set(productDetailId, wrappedCallback)
    
    // Add listener to service
    stockUpdateService.addListener(productDetailId, wrappedCallback)
    
    // Get current stock level
    const currentStock = stockUpdateService.getCurrentStock(productDetailId)
    if (currentStock !== undefined) {
      stockLevels.value.set(productDetailId, currentStock)
    }

    console.log(`Watching stock for product ${productDetailId}`)
  }

  /**
   * Stop watching stock updates for a product
   */
  const unwatchStock = (productDetailId) => {
    const listener = listeners.get(productDetailId)
    if (listener) {
      stockUpdateService.removeListener(productDetailId, listener)
      listeners.delete(productDetailId)
      stockLevels.value.delete(productDetailId)
      console.log(`Stopped watching stock for product ${productDetailId}`)
    }
  }

  /**
   * Get current stock level for a product
   */
  const getStock = (productDetailId) => {
    return stockLevels.value.get(productDetailId) || stockUpdateService.getCurrentStock(productDetailId)
  }

  /**
   * Watch multiple products at once
   */
  const watchMultipleProducts = (productDetailIds, callback) => {
    productDetailIds.forEach(id => {
      watchStock(id, callback)
    })
  }

  /**
   * Cleanup all listeners
   */
  const cleanup = () => {
    listeners.forEach((listener, productDetailId) => {
      stockUpdateService.removeListener(productDetailId, listener)
    })
    listeners.clear()
    stockLevels.value.clear()
  }

  // Cleanup on unmount
  onUnmounted(() => {
    cleanup()
  })

  return {
    stockLevels,
    watchStock,
    unwatchStock,
    getStock,
    watchMultipleProducts,
    cleanup
  }
}
