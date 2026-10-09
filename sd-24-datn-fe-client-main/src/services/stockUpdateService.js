import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Create axios instance for stock updates
const stockClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  }
})

class StockUpdateService {
  constructor() {
    this.listeners = new Map() // Map<productDetailId, Set<callback>>
    this.isPolling = false
    this.pollInterval = null
    this.lastUpdates = new Map() // Cache last known stock levels
  }

  /**
   * Start polling for stock updates
   */
  startPolling(intervalMs = 5000) {
    if (this.isPolling) return

    this.isPolling = true
    console.log('Starting stock update polling...')

    this.pollInterval = setInterval(async () => {
      try {
        await this.fetchStockUpdates()
      } catch (error) {
        console.error('Error polling stock updates:', error)
      }
    }, intervalMs)

    // Initial fetch
    this.fetchStockUpdates()
  }

  /**
   * Stop polling for stock updates
   */
  stopPolling() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval)
      this.pollInterval = null
    }
    this.isPolling = false
    console.log('Stopped stock update polling')
  }

  /**
   * Fetch latest stock updates from server
   */
  async fetchStockUpdates() {
    try {
      const response = await stockClient.get('/stock-updates')
      const updates = response.data

      // Check for changes and notify listeners
      for (const [productDetailId, newStock] of Object.entries(updates)) {
        const id = parseInt(productDetailId)
        const oldStock = this.lastUpdates.get(id)

        if (oldStock !== newStock) {
          console.log(`Stock update: Product ${id} -> ${newStock} (was ${oldStock})`)
          this.lastUpdates.set(id, newStock)
          this.notifyListeners(id, newStock, oldStock)
        }
      }
    } catch (error) {
      console.error('Failed to fetch stock updates:', error)
    }
  }

  /**
   * Add listener for stock updates of a specific product
   */
  addListener(productDetailId, callback) {
    if (!this.listeners.has(productDetailId)) {
      this.listeners.set(productDetailId, new Set())
    }
    this.listeners.get(productDetailId).add(callback)

    console.log(`Added stock listener for product ${productDetailId}`)
  }

  /**
   * Remove listener for stock updates
   */
  removeListener(productDetailId, callback) {
    const callbacks = this.listeners.get(productDetailId)
    if (callbacks) {
      callbacks.delete(callback)
      if (callbacks.size === 0) {
        this.listeners.delete(productDetailId)
      }
    }

    console.log(`Removed stock listener for product ${productDetailId}`)
  }

  /**
   * Notify all listeners for a product
   */
  notifyListeners(productDetailId, newStock, oldStock) {
    const callbacks = this.listeners.get(productDetailId)
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(productDetailId, newStock, oldStock)
        } catch (error) {
          console.error('Error in stock update callback:', error)
        }
      })
    }
  }

  /**
   * Get current stock for a product (from cache)
   */
  getCurrentStock(productDetailId) {
    return this.lastUpdates.get(productDetailId)
  }

  /**
   * Get stock for specific product from server
   */
  async getStockForProduct(productDetailId) {
    try {
      const response = await stockClient.get(`/stock-updates/${productDetailId}`)
      return response.data
    } catch (error) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }

  /**
   * Clear all listeners and stop polling
   */
  destroy() {
    this.stopPolling()
    this.listeners.clear()
    this.lastUpdates.clear()
  }
}

// Create singleton instance
const stockUpdateService = new StockUpdateService()

export default stockUpdateService
