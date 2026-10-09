import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/nhan-vien'

const nhanVienAPI = {
  // Get all employees with pagination and filtering
  getAllNhanVien: async (params = {}) => {
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

      const queryParams = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy,
        sortDir
      })

      if (keyword && keyword.trim()) {
        queryParams.append('keyword', keyword.trim())
      }
      if (gioiTinh !== null && gioiTinh !== undefined) {
        queryParams.append('gioiTinh', gioiTinh.toString())
      }
      if (trangThai !== null && trangThai !== undefined) {
        queryParams.append('trangThai', trangThai.toString())
      }

      const response = await axios.get(`${API_BASE_URL}/home?${queryParams}`)
      return response.data
    } catch (error) {
      console.error('Error fetching employees:', error)
      throw error
    }
  },

  // Get employee by ID
  getNhanVienById: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/${id}`)
      return response.data
    } catch (error) {
      console.error('Error fetching employee by ID:', error)
      throw error
    }
  },

  // Create new employee
  createNhanVien: async (employeeData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/them-nhan-vien`, employeeData)
      return response.data
    } catch (error) {
      console.error('Error creating employee:', error)
      throw error
    }
  },

  // Update employee
  updateNhanVien: async (id, employeeData) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/${id}`, employeeData)
      return response.data
    } catch (error) {
      console.error('Error updating employee:', error)
      throw error
    }
  },

  // Delete employee (soft delete)
  deleteNhanVien: async (id) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/${id}/deactivate`) // không xóa: chuyển sang Ngừng hoạt động
      return response.data
    } catch (error) {
      console.error('Error deleting employee:', error)
      throw error
    }
  },

  // Restore employee
  restoreNhanVien: async (id) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/${id}/restore`)
      return response.data
    } catch (error) {
      console.error('Error restoring employee:', error)
      throw error
    }
  }
}

export default nhanVienAPI