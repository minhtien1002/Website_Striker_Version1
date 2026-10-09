import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/khach-hang/validate'

const khachHangValidationAPI = {
  // Validate customer data for creation
  validateCreateCustomer: async (customerData) => {
    try {
      console.log('Validating create customer data:', customerData)
      const response = await axios.post(`${API_BASE_URL}/create`, customerData)
      console.log('Validation response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error validating create customer:', error)
      throw error
    }
  },

  // Validate customer data for update
  validateUpdateCustomer: async (customerData) => {
    try {
      console.log('Validating update customer data:', customerData)
      const response = await axios.post(`${API_BASE_URL}/update`, customerData)
      console.log('Validation response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error validating update customer:', error)
      throw error
    }
  }
}

export default khachHangValidationAPI
