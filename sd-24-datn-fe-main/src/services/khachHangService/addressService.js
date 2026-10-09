import { ref, reactive, computed, watch } from 'vue'
import addressAPI from '@/services/api/AddressAPI/AddressAPI.js'

export function useKhachHangAddressSelection() {
  // State
  const provinces = ref([])
  const districts = ref([])
  const wards = ref([])

  const selectedProvince = ref(null)
  const selectedDistrict = ref(null)
  const selectedWard = ref(null)

  const provinceSearch = ref('')
  const districtSearch = ref('')
  const wardSearch = ref('')

  const loading = reactive({
    provinces: false,
    districts: false,
    wards: false
  })

  // Computed filtered lists
  const filteredProvinces = computed(() => {
    if (!provinceSearch.value) return provinces.value
    return provinces.value.filter(province =>
      province.name.toLowerCase().includes(provinceSearch.value.toLowerCase())
    )
  })

  const filteredDistricts = computed(() => {
    if (!districtSearch.value) return districts.value
    return districts.value.filter(district =>
      district.name.toLowerCase().includes(districtSearch.value.toLowerCase())
    )
  })

  const filteredWards = computed(() => {
    if (!wardSearch.value) return wards.value
    return wards.value.filter(ward =>
      ward.name.toLowerCase().includes(wardSearch.value.toLowerCase())
    )
  })

  // Load provinces on initialization
  const loadProvinces = async () => {
    try {
      loading.provinces = true
      provinces.value = await addressAPI.getProvinces()
    } catch (error) {
      console.error('Error loading provinces:', error)
    } finally {
      loading.provinces = false
    }
  }

  // Load districts when province is selected
  const loadDistricts = async (provinceCode) => {
    try {
      loading.districts = true
      districts.value = await addressAPI.getDistrictsByProvince(provinceCode)
      // Reset district and ward selections
      selectedDistrict.value = null
      selectedWard.value = null
      wards.value = []
      districtSearch.value = ''
      wardSearch.value = ''
    } catch (error) {
      console.error('Error loading districts:', error)
      districts.value = []
    } finally {
      loading.districts = false
    }
  }

  // Load wards when district is selected
  const loadWards = async (districtCode) => {
    try {
      loading.wards = true
      wards.value = await addressAPI.getWardsByDistrict(districtCode)
      // Reset ward selection
      selectedWard.value = null
      wardSearch.value = ''
    } catch (error) {
      console.error('Error loading wards:', error)
      wards.value = []
    } finally {
      loading.wards = false
    }
  }

  // Selection methods
  const selectProvince = (province) => {
    selectedProvince.value = province
    provinceSearch.value = province.name
    // Hide dropdown after selection
    setTimeout(() => {
      const event = new CustomEvent('hideProvinceDropdown')
      document.dispatchEvent(event)
    }, 100)
  }

  const selectDistrict = (district) => {
    selectedDistrict.value = district
    districtSearch.value = district.name
    // Hide dropdown after selection
    setTimeout(() => {
      const event = new CustomEvent('hideDistrictDropdown')
      document.dispatchEvent(event)
    }, 100)
  }

  const selectWard = (ward) => {
    selectedWard.value = ward
    wardSearch.value = ward.name
    // Hide dropdown after selection
    setTimeout(() => {
      const event = new CustomEvent('hideWardDropdown')
      document.dispatchEvent(event)
    }, 100)
  }

  // Watch for province selection changes
  watch(selectedProvince, (newProvince) => {
    if (newProvince) {
      loadDistricts(newProvince.code)
    } else {
      districts.value = []
      wards.value = []
      selectedDistrict.value = null
      selectedWard.value = null
    }
  })

  // Watch for district selection changes
  watch(selectedDistrict, (newDistrict) => {
    if (newDistrict) {
      loadWards(newDistrict.code)
    } else {
      wards.value = []
      selectedWard.value = null
    }
  })

  // Select province by code (for editing existing data)
  const selectProvinceByCode = async (code) => {
    if (!provinces.value.length) {
      await loadProvinces()
    }
    const province = provinces.value.find(p => p.code === code)
    if (province) {
      selectProvince(province)
    }
  }

  // Select district by code (for editing existing data)
  const selectDistrictByCode = async (code) => {
    const district = districts.value.find(d => d.code === code)
    if (district) {
      selectDistrict(district)
    }
  }

  // Select ward by code (for editing existing data)
  const selectWardByCode = async (code) => {
    const ward = wards.value.find(w => w.code === code)
    if (ward) {
      selectWard(ward)
    }
  }

  // Get full address string
  const getFullAddress = computed(() => {
    const parts = []
    if (selectedWard.value) parts.push(selectedWard.value.name)
    if (selectedDistrict.value) parts.push(selectedDistrict.value.name)
    if (selectedProvince.value) parts.push(selectedProvince.value.name)
    return parts.join(', ')
  })

  // Reset all selections
  const resetSelection = () => {
    selectedProvince.value = null
    selectedDistrict.value = null
    selectedWard.value = null
    provinceSearch.value = ''
    districtSearch.value = ''
    wardSearch.value = ''
    districts.value = []
    wards.value = []
  }

  // Set address from existing data
  const setAddressFromData = async (provinceCode, districtCode, wardCode) => {
    if (provinceCode) {
      await selectProvinceByCode(provinceCode)
      if (districtCode) {
        await selectDistrictByCode(districtCode)
        if (wardCode) {
          await selectWardByCode(wardCode)
        }
      }
    }
  }

  // Dropdown visibility handlers
  const hideProvinceDropdown = () => {
    setTimeout(() => {
      // Delay to allow click events to fire
    }, 150)
  }

  const hideDistrictDropdown = () => {
    setTimeout(() => {
      // Delay to allow click events to fire
    }, 150)
  }

  const hideWardDropdown = () => {
    setTimeout(() => {
      // Delay to allow click events to fire
    }, 150)
  }

  // Initialize
  loadProvinces()

  return {
    // State
    provinces,
    districts,
    wards,
    selectedProvince,
    selectedDistrict,
    selectedWard,
    provinceSearch,
    districtSearch,
    wardSearch,
    loading,

    // Computed
    filteredProvinces,
    filteredDistricts,
    filteredWards,
    getFullAddress,

    // Methods
    loadProvinces,
    loadDistricts,
    loadWards,
    selectProvince,
    selectDistrict,
    selectWard,
    selectProvinceByCode,
    selectDistrictByCode,
    selectWardByCode,
    resetSelection,
    setAddressFromData,
    hideProvinceDropdown,
    hideDistrictDropdown,
    hideWardDropdown
  }
}
