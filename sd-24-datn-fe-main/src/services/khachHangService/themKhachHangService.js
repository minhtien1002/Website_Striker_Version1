import { ref, reactive } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter } from 'vue-router'
import { useAddressSelection } from './addressService.js'
import khachHangAPI from '@/services/api/APIKhachHang/KhachHangAPI.js'

export function useThemKhachHangService() {
  const toast = useToast()
  const router = useRouter()

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
    resetSelection
  } = useAddressSelection()

  // State
  const loading = ref(false)
  const showProvinceDropdown = ref(false)
  const showDistrictDropdown = ref(false)
  const showWardDropdown = ref(false)

  const customer = ref({
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
    trangThai: true
  })

  // Breadcrumb configuration
  const breadcrumbItems = ref([
    { label: 'Quản lý tài khoản', path: '/khach-hang' },
    { label: 'Khách hàng', path: '/khach-hang' },
    { label: 'Thêm mới' }
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
      handler: () => resetForm()
    }
  ])

  const pageStats = ref([
    {
      icon: 'solar:user-plus-bold-duotone',
      value: 'Mới',
      label: 'Khách hàng'
    },
    {
      icon: 'solar:document-add-bold-duotone',
      value: 'Thêm',
      label: 'Thông tin'
    }
  ])

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

    // Check address selections
    if (!selectedProvince.value) {
      toast.error('Vui lòng chọn Tỉnh/Thành phố.')
      return false
    }
    if (!selectedDistrict.value) {
      toast.error('Vui lòng chọn Quận/Huyện.')
      return false
    }
    if (!selectedWard.value) {
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
      toast.error('Số điện thoai phải bắt đầu bằng 0 và có đúng 10 chữ số.')
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
      console.log('Frontend customer data:', customer.value)
      console.log('Selected address:', {
        province: selectedProvince.value,
        district: selectedDistrict.value,
        ward: selectedWard.value
      })

      // Map frontend data to backend format for 3 tables (KhachHang, TaiKhoan, DiaChiKhachHang)
      const backendData = {
        // KhachHang fields
        ten: customer.value.tenKhachHang,
        ngaySinh: customer.value.ngaySinh,
        gioiTinh: customer.value.gioiTinh ? 1 : 0, // Convert boolean to Short (1/0)
        cccd: customer.value.cccd || null,

        // TaiKhoan fields
        email: customer.value.email || '',
        soDienThoai: customer.value.soDienThoai,
        matKhau: 'default123', // Default password for new customers

        // DiaChiKhachHang fields
        thanhPho: selectedProvince.value?.name || customer.value.diaChiTinhThanh,
        quan: selectedDistrict.value?.name || customer.value.diaChiQuanHuyen,
        phuong: selectedWard.value?.name || customer.value.diaChiPhuongXa,
        diaChiCuThe: customer.value.diaChiSoNhaTenDuong
      }

      console.log('Backend data to send:', backendData)

      // Call API directly
      const response = await khachHangAPI.createKhachHang(backendData)
      console.log('API Response:', response)

      if (response && response.data) {
        toast.success(response.message || 'Thêm khách hàng thành công!')
        router.push('/khach-hang')
      } else {
        throw new Error('Không nhận được dữ liệu từ server')
      }
    } catch (error) {
      console.error('Lỗi khi thêm khách hàng:', error)
      console.error('Error details:', error.response?.data)
      const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi thêm khách hàng'
      toast.error(errorMessage)
    } finally {
      loading.value = false
    }
  }

  // Reset form
  const resetForm = () => {
    customer.value = {
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
      trangThai: true
    }
    resetSelection()
    toast.info('Đã làm mới form!')
  }

  // CCCD Scanning
  const scanCCCD = () => {
    toast.info('Đang khởi động chức năng quét CCCD...')

    setTimeout(() => {
      const mockCCCDData = {
        tenKhachHang: 'Trần Thị Bình',
        ngaySinh: '1995-08-20',
        gioiTinh: false,
        cccd: '987654321098',
        email: 'tranthiminh@example.com',
        diaChiSoNhaTenDuong: '456 Đường XYZ',
        diaChiPhuongXa: 'Phường Bến Nghé',
        diaChiQuanHuyen: 'Quận 1',
        diaChiTinhThanh: 'TP. Hồ Chí Minh'
      }

      Object.assign(customer.value, mockCCCDData)
      toast.success('Quét CCCD thành công! Thông tin đã được điền tự động.')
    }, 2000)
  }

  // Go back
  const goBack = () => {
    router.push('/khach-hang')
  }

  return {
    // State
    loading,
    customer,
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
    selectProvince,
    selectDistrict,
    selectWard,
    hideProvinceDropdown,
    hideDistrictDropdown,
    hideWardDropdown,
    validateCustomer,
    submitForm,
    resetForm,
    scanCCCD,
    goBack
  }
}
