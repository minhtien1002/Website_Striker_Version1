import { ref, reactive } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter } from 'vue-router'
import { useAddressSelection } from './addressService.js'
import nhanVienAPI from '@/services/api/APINhanVien/NhanVienAPI.js'

export function useThemNhanVienService() {
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

  const employee = ref({
    chucVuID: null,
    taiKhoanID: null,
    tenNhanVien: '',
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
    { label: 'Quản lý nhân sự', path: '/nhan-vien' },
    { label: 'Nhân viên', path: '/nhan-vien' },
    { label: 'Thêm mới' }
  ])

  const breadcrumbActions = ref([
    {
      label: 'Quét CCCD',
      type: 'primary',
      handler: () => scanCCCD()
    },
    {
      label: 'Danh sách nhân viên',
      type: 'default',
      handler: () => router.push('/nhan-vien')
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
      label: 'Nhân viên'
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
  const validateEmployee = (emp) => {
    const requiredFields = [
      { field: emp.tenNhanVien, label: 'Tên nhân viên' },
      { field: emp.ngaySinh, label: 'Ngày sinh' },
      { field: emp.soDienThoai, label: 'Số điện thoại' },
      { field: emp.cccd, label: 'CCCD' },
      { field: emp.email, label: 'Email' },
      { field: emp.diaChiSoNhaTenDuong, label: 'Số nhà, tên đường' }
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

    const nameRegex = /^[^\d]+$/
    if (!nameRegex.test(emp.tenNhanVien.trim())) {
      toast.error('Tên nhân viên không được chứa số.')
      return false
    }

    const phoneRegex = /^\d{10}$/
    if (!phoneRegex.test(emp.soDienThoai)) {
      toast.error('Số điện thoại phải gồm đúng 10 chữ số.')
      return false
    }

    const cccdRegex = /^\d{12}$/
    if (!cccdRegex.test(emp.cccd)) {
      toast.error('CCCD phải gồm đúng 12 chữ số.')
      return false
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(emp.email)) {
      toast.error('Email không đúng định dạng.')
      return false
    }

    return true
  }

  // Form submission
  const submitForm = async () => {
    if (!validateEmployee(employee.value)) return

    loading.value = true
    try {
      console.log('Frontend employee data:', employee.value)
      console.log('Selected address:', {
        province: selectedProvince.value,
        district: selectedDistrict.value,
        ward: selectedWard.value
      })

      // Map frontend data to backend format (same as nhanVienCRUD.js)
      const backendData = {
        // NhanVien fields - let backend auto-generate ma from name and birthdate
        ma: null,
        tenNhanVien: employee.value.tenNhanVien,
        ngaySinh: employee.value.ngaySinh,
        gioiTinh: employee.value.gioiTinh,
        cccd: employee.value.cccd,
        diaChiCuThe: employee.value.diaChiSoNhaTenDuong,
        thanhPho: selectedProvince.value?.name || employee.value.diaChiTinhThanh,
        quan: selectedDistrict.value?.name || employee.value.diaChiQuanHuyen,
        phuong: selectedWard.value?.name || employee.value.diaChiPhuongXa,
        
        // TaiKhoan fields - let backend auto-generate username
        tenDangNhap: null,
        email: employee.value.email,
        soDienThoai: employee.value.soDienThoai,
        matKhau: '123456', // Default password
        idQuyenHan: 2 // Default role ID for employee
      }

      console.log('Backend data to send:', backendData)

      // Call API directly
      const response = await nhanVienAPI.createNhanVien(backendData)
      console.log('API Response:', response)
      
      if (response && response.data) {
        toast.success(response.message || 'Thêm nhân viên thành công!')
        router.push('/nhan-vien')
      } else {
        throw new Error('Không nhận được dữ liệu từ server')
      }
    } catch (error) {
      console.error('Lỗi khi thêm nhân viên:', error)
      console.error('Error details:', error.response?.data)
      const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi thêm nhân viên'
      toast.error(errorMessage)
    } finally {
      loading.value = false
    }
  }

  // Reset form
  const resetForm = () => {
    employee.value = {
      chucVuID: null,
      taiKhoanID: null,
      tenNhanVien: '',
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
        tenNhanVien: 'Nguyễn Văn An',
        ngaySinh: '1990-05-15',
        gioiTinh: true,
        cccd: '123456789012',
        email: 'nguyenvanan@example.com',
        diaChiSoNhaTenDuong: '123 Đường ABC',
        diaChiPhuongXa: 'Phường Tân Bình',
        diaChiQuanHuyen: 'Quận 1',
        diaChiTinhThanh: 'TP. Hồ Chí Minh'
      }
      
      Object.assign(employee.value, mockCCCDData)
      toast.success('Quét CCCD thành công! Thông tin đã được điền tự động.')
    }, 2000)
  }

  // Go back
  const goBack = () => {
    router.push('/nhan-vien')
  }

  return {
    // State
    loading,
    employee,
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
    validateEmployee,
    submitForm,
    resetForm,
    scanCCCD,
    goBack
  }
}
