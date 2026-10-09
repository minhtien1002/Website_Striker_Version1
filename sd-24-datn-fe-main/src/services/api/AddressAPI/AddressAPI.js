import axios from 'axios'

const API_BASE_URL = 'https://provinces.open-api.vn/api'

const addressAPI = {
  // Get all provinces
  getProvinces: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/p/`)
      return response.data
    } catch (error) {
      console.error('Error fetching provinces:', error)
      throw error
    }
  },

  // Get districts by province code
  getDistrictsByProvince: async (provinceCode) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/p/${provinceCode}?depth=2`)
      return response.data.districts || []
    } catch (error) {
      console.error('Error fetching districts:', error)
      throw error
    }
  },

  // Get wards by district code
  getWardsByDistrict: async (districtCode) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/d/${districtCode}?depth=2`)
      return response.data.wards || []
    } catch (error) {
      console.error('Error fetching wards:', error)
      throw error
    }
  },

  // Search provinces by name
  searchProvinces: async (searchTerm) => {
    try {
      const provinces = await addressAPI.getProvinces()
      if (!searchTerm) return provinces
      
      return provinces.filter(province => 
        province.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        province.name_en.toLowerCase().includes(searchTerm.toLowerCase())
      )
    } catch (error) {
      console.error('Error searching provinces:', error)
      throw error
    }
  },

  // Search districts by name within a province
  searchDistricts: async (provinceCode, searchTerm) => {
    try {
      const districts = await addressAPI.getDistrictsByProvince(provinceCode)
      if (!searchTerm) return districts
      
      return districts.filter(district => 
        district.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        district.name_en.toLowerCase().includes(searchTerm.toLowerCase())
      )
    } catch (error) {
      console.error('Error searching districts:', error)
      throw error
    }
  },

  // Search wards by name within a district
  searchWards: async (districtCode, searchTerm) => {
    try {
      const wards = await addressAPI.getWardsByDistrict(districtCode)
      if (!searchTerm) return wards
      
      return wards.filter(ward => 
        ward.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        ward.name_en.toLowerCase().includes(searchTerm.toLowerCase())
      )
    } catch (error) {
      console.error('Error searching wards:', error)
      throw error
    }
  }
}

export default addressAPI
