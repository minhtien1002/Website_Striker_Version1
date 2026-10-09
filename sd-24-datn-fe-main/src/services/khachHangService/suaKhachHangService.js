import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter, useRoute } from 'vue-router'
import { useAddressSelection } from './addressService.js'
import khachHangAPI from '@/services/api/APIKhachHang/KhachHangAPI.js'

export function useSuaKhachHangService() {
  const toast = useToast()
  const router = useRouter()
  const route = useRoute()
  
  // Address service
  const {
    selectedProvince,
    selectedDistrict,
    selectedWard,
    filteredProvinces,
    filteredDistricts,
    filteredWards,
    provinceSearch,
    districtSearch,
    wardSearch,
    loading: addressLoading,
    getFullAddress,
    setAddressFromData
  } = useAddressSelection()

  // State
  const loading = ref(false)
  const customerId = ref(route.params.id)
  const showProvinceDropdown = ref(false)
  const showDistrictDropdown = ref(false)
  const showWardDropdown = ref(false)

  const customer = ref({
    id: null,
    tenKhachHang: '',
    ngaySinh: '',
    gioiTinh: true,
    soDienThoai: '',
    cccd: '',
    email: '',
    diaChiSoNhaTenDuong: '',
    diaChiPhuongXa: '',
    diaChiQuanHuyen: '',
    diaChiTinhThanh: '',
    provinceCode: null,
    districtCode: null,
    wardCode: null,
    // trangThai: true,
    taiKhoanID: null
  })

  // Breadcrumb configuration
  const breadcrumbItems = ref([
    { label: 'Quản lý tài khoản', path: '/khach-hang' },
    { label: 'Khách hàng', path: '/khach-hang' },
    { label: 'Chỉnh sửa' }
  ])

  const breadcrumbActions = ref([
    {
      label: 'Quét CCCD',
      type: 'primary',
      handler: () => scanCCCD()
    },
    {
      label: 'Danh sách khách hàng',
      type: 'default',
      handler: () => router.push('/khach-hang')
    },
    {
      label: 'Làm mới',
      type: 'default',
      handler: () => loadCustomerData()
    }
  ])

  const pageStats = ref([
    {
      icon: 'solar:settings-bold-duotone',
      value: 'Sửa',
      label: 'Khách hàng'
    },
    {
      icon: 'solar:refresh-bold-duotone',
      value: 'Cập nhật',
      label: 'Thông tin'
    }
  ])

  // Load customer data
  const loadCustomerData = async () => {
    if (!customerId.value) return
    
    loading.value = true
    try {
      console.log('Loading customer with ID:', customerId.value)
      
      // Call API to get customer details
      const response = await khachHangAPI.getKhachHangById(customerId.value)
      console.log('API Response:', response)
      
      if (response && response.data) {
        const backendCustomer = response.data
        console.log('Backend customer data:', backendCustomer)
        
        // Map backend data to frontend format
        customer.value = {
          id: backendCustomer.id,
          tenKhachHang: backendCustomer.ten || backendCustomer.tenKhachHang,
          ngaySinh: backendCustomer.ngaySinh ? backendCustomer.ngaySinh.split('T')[0] : '',
          gioiTinh: backendCustomer.gioiTinh === 1 || backendCustomer.gioiTinh === true,
          soDienThoai: backendCustomer.soDienThoai || '',
          cccd: backendCustomer.cccd || '',
          email: backendCustomer.email || '',
          diaChiSoNhaTenDuong: backendCustomer.diaChiCuThe || backendCustomer.diaChiSoNhaTenDuong || '',
          diaChiPhuongXa: backendCustomer.phuong || backendCustomer.diaChiPhuongXa || '',
          diaChiQuanHuyen: backendCustomer.quan || backendCustomer.diaChiQuanHuyen || '',
          diaChiTinhThanh: backendCustomer.thanhPho || backendCustomer.diaChiTinhThanh || '',
          // trangThai: backendCustomer.deleted, // Based on memory: deleted=true means active
          taiKhoanID: backendCustomer.taiKhoanId || backendCustomer.taiKhoanID
        }
        
        console.log('Mapped frontend customer:', customer.value)
        
        // Set address selections based on loaded data if available
        if (customer.value.diaChiTinhThanh && customer.value.diaChiQuanHuyen && customer.value.diaChiPhuongXa) {
          // Try to find and set address selections by name
          provinceSearch.value = customer.value.diaChiTinhThanh
          districtSearch.value = customer.value.diaChiQuanHuyen
          wardSearch.value = customer.value.diaChiPhuongXa
        }
        
        toast.success('Tải thông tin khách hàng thành công!')
      } else {
        throw new Error('Không tìm thấy thông tin khách hàng')
      }
    } catch (error) {
      console.error('Lỗi khi tải thông tin khách hàng:', error)
      toast.error('Lỗi khi tải thông tin khách hàng.')
      // Navigate back to list if customer not found
      router.push('/khach-hang')
    } finally {
      loading.value = false
    }
  }

  // Address selection functions
  const selectProvince = (province) => {
    selectedProvince.value = province
    provinceSearch.value = province.name
    showProvinceDropdown.value = false
  }

  const selectDistrict = (district) => {
    selectedDistrict.value = district
    districtSearch.value = district.name
    showDistrictDropdown.value = false
  }

  const selectWard = (ward) => {
    selectedWard.value = ward
    wardSearch.value = ward.name
    showWardDropdown.value = false
  }

  // Dropdown visibility functions
  const hideProvinceDropdown = () => {
    setTimeout(() => {
      showProvinceDropdown.value = false
    }, 200)
  }

  const hideDistrictDropdown = () => {
    setTimeout(() => {
      showDistrictDropdown.value = false
    }, 200)
  }

  const hideWardDropdown = () => {
    setTimeout(() => {
      showWardDropdown.value = false
    }, 200)
  }

  // Validation function
  const validateCustomer = (cust) => {
    const requiredFields = [
      { field: cust.tenKhachHang, label: 'Tên khách hàng' },
      { field: cust.ngaySinh, label: 'Ngày sinh' },
      { field: cust.soDienThoai, label: 'Số điện thoại' },
      { field: cust.diaChiSoNhaTenDuong, label: 'Số nhà, tên đường' }
    ]

    // Check address selections or existing address data
    if (!selectedProvince.value && !cust.diaChiTinhThanh) {
      toast.error('Vui lòng chọn Tỉnh/Thành phố.')
      return false
    }
    if (!selectedDistrict.value && !cust.diaChiQuanHuyen) {
      toast.error('Vui lòng chọn Quận/Huyện.')
      return false
    }
    if (!selectedWard.value && !cust.diaChiPhuongXa) {
      toast.error('Vui lòng chọn Phường/Xã.')
      return false
    }

    for (const item of requiredFields) {
      if (!item.field || String(item.field).trim() === '') {
        toast.error(`Vui lòng nhập ${item.label}.`)
        return false
      }
    }

    // Validate name contains only letters and spaces
    const nameRegex = /^[\p{L}\s]+$/u
    if (!nameRegex.test(cust.tenKhachHang.trim())) {
      toast.error('Tên khách hàng chỉ được chứa chữ cái và khoảng trắng.')
      return false
    }

    // Validate phone number
    const phoneRegex = /^0\d{9}$/
    if (!phoneRegex.test(cust.soDienThoai)) {
      toast.error('Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.')
      return false
    }

    // Validate CCCD if provided
    if (cust.cccd && cust.cccd.trim() !== '') {
      const cccdRegex = /^\d{12}$/
      if (!cccdRegex.test(cust.cccd)) {
        toast.error('CCCD phải gồm đúng 12 chữ số.')
        return false
      }
    }

    // Validate email if provided
    if (cust.email && cust.email.trim() !== '') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(cust.email.trim())) {
        toast.error('Email không đúng định dạng.')
        return false
      }
    }

    // Validate age (must be at least 16 years old)
    if (cust.ngaySinh) {
      const birthDate = new Date(cust.ngaySinh)
      const today = new Date()
      let age = today.getFullYear() - birthDate.getFullYear()
      const monthDiff = today.getMonth() - birthDate.getMonth()
      
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--
      }
      
      if (age < 16) {
        toast.error('Khách hàng phải từ 16 tuổi trở lên.')
        return false
      }
    }

    return true
  }

  // Form submission
  const submitForm = async () => {
    if (!validateCustomer(customer.value)) return

    loading.value = true
    try {
      console.log('Frontend customer data before update:', customer.value)
      console.log('Selected address:', {
        province: selectedProvince.value,
        district: selectedDistrict.value,
        ward: selectedWard.value
      })

      // Map frontend data to backend format for 3 tables update
      const backendData = {
        id: customer.value.id,
        // KhachHang fields
        ten: customer.value.tenKhachHang,
        ngaySinh: customer.value.ngaySinh,
        gioiTinh: customer.value.gioiTinh ? 1 : 0, // Convert boolean to Short (1/0)
        cccd: customer.value.cccd || null,
        
        // TaiKhoan fields
        email: customer.value.email || '',
        soDienThoai: customer.value.soDienThoai,
        
        // DiaChiKhachHang fields
        thanhPho: selectedProvince.value?.name || customer.value.diaChiTinhThanh,
        quan: selectedDistrict.value?.name || customer.value.diaChiQuanHuyen,
        phuong: selectedWard.value?.name || customer.value.diaChiPhuongXa,
        diaChiCuThe: customer.value.diaChiSoNhaTenDuong,
        
        // Do not send deleted field during regular updates
      }

      console.log('Backend data to send:', backendData)

      // Call API to update customer
      const response = await khachHangAPI.updateKhachHang(customerId.value, backendData)
      console.log('Update API Response:', response)
      
      if (response) {
        toast.success(response.message || 'Cập nhật khách hàng thành công!')
        router.push('/khach-hang')
      } else {
        throw new Error('Không nhận được phản hồi từ server')
      }
    } catch (error) {
      console.error('Lỗi khi cập nhật khách hàng:', error)
      console.error('Error details:', error.response?.data)
      const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật khách hàng'
      toast.error(errorMessage)
    } finally {
      loading.value = false
    }
  }

  // CCCD Scanning
  const scanCCCD = () => {
    toast.info('Đang khởi động chức năng quét CCCD...')
    
    setTimeout(() => {
      const mockCCCDData = {
        tenKhachHang: 'Lê Văn Cường',
        ngaySinh: '1988-12-10',
        gioiTinh: true,
        cccd: '456789123456',
        email: 'levancuong@example.com',
        diaChiSoNhaTenDuong: '789 Đường DEF',
        diaChiPhuongXa: 'Phường Đa Kao',
        diaChiQuanHuyen: 'Quận 1',
        diaChiTinhThanh: 'TP. Hồ Chí Minh'
      }
      
      Object.assign(customer.value, mockCCCDData)
      toast.success('Quét CCCD thành công! Thông tin đã được cập nhật.')
    }, 2000)
  }

  // Go back
  const goBack = () => {
    router.push('/khach-hang')
  }

  // Initialize data on mount
  const initializeData = () => {
    loadCustomerData()
  }

  return {
    // State
    loading,
    customer,
    customerId,
    showProvinceDropdown,
    showDistrictDropdown,
    showWardDropdown,

    // Address state
    selectedProvince,
    selectedDistrict,
    selectedWard,
    filteredProvinces,
    filteredDistricts,
    filteredWards,
    provinceSearch,
    districtSearch,
    wardSearch,
    addressLoading,
    getFullAddress,

    // Configuration
    breadcrumbItems,
    breadcrumbActions,
    pageStats,

    // Methods
    loadCustomerData,
    selectProvince,
    selectDistrict,
    selectWard,
    hideProvinceDropdown,
    hideDistrictDropdown,
    hideWardDropdown,
    validateCustomer,
    submitForm,
    scanCCCD,
    goBack,
    initializeData
  }
}
