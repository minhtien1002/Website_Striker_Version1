import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const statisticsAPI = {
  // Get dashboard statistics (default last 30 days)
  getDashboardStats() {
    return axios.get(`${API_BASE_URL}/statistics/dashboard`)
  },

  // Get dashboard statistics by date range
  getDashboardStatsByRange(startDate, endDate) {
    return axios.get(`${API_BASE_URL}/statistics/dashboard/range`, {
      params: {
        startDate,
        endDate
      }
    })
  },

  // Get revenue data by period (daily, weekly, monthly)
  getRevenueByPeriod(startDate, endDate, period = 'daily') {
    return axios.get(`${API_BASE_URL}/statistics/revenue/period`, {
      params: {
        startDate,
        endDate,
        period
      }
    })
  },

  /**
   * Bóc tách doanh thu theo kênh bán: Tại quầy / COD / Chuyển khoản, kèm chi tiết từng
   * phương thức thanh toán. Tổng các kênh luôn bằng totalRevenue của dashboard cùng kỳ.
   */
  getRevenueBreakdown(startDate, endDate) {
    return axios.get(`${API_BASE_URL}/statistics/revenue/breakdown`, {
      params: { startDate, endDate }
    })
  }
}

export default statisticsAPI
