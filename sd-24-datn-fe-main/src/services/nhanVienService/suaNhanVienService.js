import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter, useRoute } from 'vue-router'
import { useAddressSelection } from './addressService.js'
import nhanVienAPI from '@/services/api/APINhanVien/NhanVienAPI.js'

export function useSuaNhanVienService() {
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
  const employeeId = ref(route.params.id)
  const showProvinceDropdown = ref(false)
  const showDistrictDropdown = ref(false)
  const showWardDropdown = ref(false)

  const employee = ref({
    id: null,
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
    // trangThai: true
  })

  // Breadcrumb configuration
  const breadcrumbItems = ref([
    { label: 'Quản lý nhân sự', path: '/nhan-vien' },
    { label: 'Nhân viên', path: '/nhan-vien' },
    { label: 'Chỉnh sửa' }
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
      handler: () => loadEmployeeData()
    }
  ])

  const pageStats = ref([
    {
      icon: 'solar:settings-bold-duotone',
      value: 'Sửa',
      label: 'Nhân viên'
    },
    {
      icon: 'solar:refresh-bold-duotone',
      value: 'Cập nhật',
      label: 'Thông tin'
    }
  ])

  // Load employee data
  const loadEmployeeData = async () => {
    if (!employeeId.value) return
    
    loading.value = true
    try {
      // Use actual API call to get employee data
      const { getEmployeeById } = await import('./nhanVienCRUD.js')
      const result = await getEmployeeById(employeeId.value)
      
      if (result) {
        employee.value = result
      } else {
        throw new Error('Không tìm thấy thông tin nhân viên')
      }
      
      // Set address selections based on loaded data
      if (employee.value.provinceCode) {
        await setAddressFromData(
          employee.value.provinceCode,
          employee.value.districtCode,
          employee.value.wardCode
        )
      }
      
      toast.success('Tải thông tin nhân viên thành công!')
    } catch (error) {
      console.error('Lỗi khi tải thông tin nhân viên:', error)
      toast.error('Lỗi khi tải thông tin nhân viên.')
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
      const now = new Date().toISOString()
      const updatedEmployee = {
        ...employee.value,
        diaChiTinhThanh: selectedProvince.value.name,
        diaChiQuanHuyen: selectedDistrict.value.name,
        diaChiPhuongXa: selectedWard.value.name,
        provinceCode: selectedProvince.value.code,
        districtCode: selectedDistrict.value.code,
        wardCode: selectedWard.value.code,
        ngayCapNhat: now
      }

      // Use actual API call to update employee
      const { updateEmployee } = await import('./nhanVienCRUD.js')
      const result = await updateEmployee(employeeId.value, updatedEmployee, toast)
      
      toast.success('Cập nhật nhân viên thành công!')
      router.push('/nhan-vien')
    } catch (error) {
      console.error('Lỗi khi cập nhật nhân viên:', error)
      toast.error('Lỗi khi cập nhật nhân viên.')
    } finally {
      loading.value = false
    }
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
      toast.success('Quét CCCD thành công! Thông tin đã được cập nhật.')
    }, 2000)
  }

  // Go back
  const goBack = () => {
    router.push('/nhan-vien')
  }

  // Initialize data on mount
  const initializeData = () => {
    loadEmployeeData()
  }

  return {
    // State
    loading,
    employee,
    employeeId,
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
    loadEmployeeData,
    selectProvince,
    selectDistrict,
    selectWard,
    hideProvinceDropdown,
    hideDistrictDropdown,
    hideWardDropdown,
    validateEmployee,
    submitForm,
    scanCCCD,
    goBack,
    initializeData
  }
}
