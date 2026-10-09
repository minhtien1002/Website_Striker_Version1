import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/nhan-vien/validate'

const nhanVienValidationAPI = {
  // Validate employee data for creation
  validateCreateEmployee: async (employeeData) => {
    try {
      console.log('Validating create employee data:', employeeData)
      const response = await axios.post(`${API_BASE_URL}/create`, employeeData)
      console.log('Validation response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error validating create employee:', error)
      throw error
    }
  },

  // Validate employee data for update
  validateUpdateEmployee: async (employeeData) => {
    try {
      console.log('Validating update employee data:', employeeData)
      const response = await axios.post(`${API_BASE_URL}/update`, employeeData)
      console.log('Validation response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error validating update employee:', error)
      throw error
    }
  }
}

export default nhanVienValidationAPI
