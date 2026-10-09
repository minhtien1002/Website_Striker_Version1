import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/khach-hang'

const khachHangAPI = {
  // Get all active customers without pagination (for dropdowns)
  getAllActiveKhachHang: async () => {
    try {
      console.log('Fetching all active customers')
      const response = await axios.get(`${API_BASE_URL}`)
      console.log('Active customers response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error fetching active customers:', error)
      throw error
    }
  },

  // Get all customers with pagination and filtering
  getAllKhachHang: async (params = {}) => {
    try {
      const {
        page = 0,
        size = 10,
        keyword = '',
        gioiTinh = null,
        trangThai = null,
        sortBy = 'id',
        sortDir = 'desc'
      } = params

      console.log('Fetching customers with params:', params)
      
      const queryParams = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy,
        sortDir
      })

      if (keyword && keyword.trim()) {
        queryParams.append('keyword', keyword.trim())
      }
      if (gioiTinh !== null && gioiTinh !== '') {
        queryParams.append('gioiTinh', gioiTinh.toString())
      }
      if (trangThai !== null && trangThai !== '') {
        queryParams.append('trangThai', trangThai.toString())
      }

      const response = await axios.get(`${API_BASE_URL}/home?${queryParams}`)
      console.log('API Response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error fetching customers:', error)
      throw error
    }
  },

  // Get customer by ID
  getKhachHangById: async (id) => {
    try {
      console.log('Fetching customer by ID:', id)
      const response = await axios.get(`${API_BASE_URL}/${id}`)
      console.log('Customer details response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error fetching customer by ID:', error)
      throw error
    }
  },

  // Create new customer
  createKhachHang: async (customerData) => {
    try {
      console.log('Creating customer:', customerData)
      const response = await axios.post(`${API_BASE_URL}/them-khach-hang`, customerData)
      console.log('Create customer response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error creating customer:', error)
      throw error
    }
  },

  // Update customer
  updateKhachHang: async (id, customerData) => {
    try {
      console.log('Updating customer:', id, customerData)
      const response = await axios.put(`${API_BASE_URL}/${id}`, customerData)
      console.log('Update customer response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error updating customer:', error)
      throw error
    }
  },

  // Delete customer (soft delete)
  deleteKhachHang: async (id) => {
    try {
      console.log('Deleting customer:', id)
      const response = await axios.put(`${API_BASE_URL}/${id}/deactivate`) // không xóa: chuyển sang Ngừng hoạt động
      console.log('Delete customer response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error deleting customer:', error)
      throw error
    }
  },

  // Restore customer
  restoreKhachHang: async (id) => {
    try {
      console.log('Restoring customer:', id)
      const response = await axios.put(`${API_BASE_URL}/${id}/restore`)
      console.log('Restore customer response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error restoring customer:', error)
      throw error
    }
  }
}

export default khachHangAPI
