import { useCart } from '@/composables/useCart'
import stockUpdateService from '@/services/stockUpdateService'

// Plugin để khởi tạo cart service
export default {
  install(app) {
    const { initializeCart } = useCart()
    
    // Khởi tạo cart khi app start
    initializeCart()
    
    // Start stock update polling
    stockUpdateService.startPolling(3000) // Poll every 3 seconds
    
    // Provide services globally
    app.config.globalProperties.$cart = useCart()
    app.config.globalProperties.$stockUpdates = stockUpdateService
    
    // Cleanup on app unmount
    app.unmount = () => {
      stockUpdateService.destroy()
    }
  }
}
