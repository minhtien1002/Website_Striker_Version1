<template>
  <div class="employee-edit-container">
    <!-- Breadcrumb -->
    <Breadcrumb
      :items="breadcrumbItems"
      :show-page-info="true"
      page-title="Chỉnh Sửa Nhân Viên"
      page-description="Cập nhật thông tin nhân viên trong hệ thống"
      page-icon="solar:settings-bold-duotone"
      :page-stats="pageStats"
      :actions="breadcrumbActions"
    />

    <!-- Main Content -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- Employee Form -->
        <div class="form-panel">
          <div class="card">
            <div class="loading-spinner" v-if="loading"></div>
            <div class="card-content" :class="{ 'loading-overlay': loading }">
              <form @submit.prevent="submitForm">
                <!-- Personal Information Section -->
                <div class="form-section">
                  <div class="section-header">
                    <h3 class="section-title">
                      <Icon icon="solar:user-id-bold-duotone" class="section-icon" />
                      Thông tin cá nhân
                    </h3>
                    <p class="section-description">Cập nhật thông tin cơ bản của nhân viên</p>
                  </div>

                  <div class="form-grid personal-info">
                    <!-- Tên nhân viên -->
                    <div class="form-group col-span-2">
                      <label class="form-label">
                        <Icon icon="solar:user-bold-duotone" class="label-icon" />
                        Tên nhân viên <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.tenNhanVien"
                        type="text"
                        class="form-input"
                        placeholder="Nhập họ và tên đầy đủ"
                        required
                      />
                    </div>

                    <!-- Ngày sinh -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:calendar-bold-duotone" class="label-icon" />
                        Ngày sinh <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.ngaySinh"
                        type="date"
                        class="form-input"
                        :class="{ 'input-error': currentAge > 0 && currentAge < 18 }"
                        required
                      />
                      <div v-if="currentAge > 0" class="age-indicator" :class="{ 'age-error': currentAge < 18, 'age-valid': currentAge >= 18 }">
                        <Icon :icon="currentAge >= 18 ? 'solar:check-circle-bold' : 'solar:close-circle-bold'" class="age-icon" />
                        Tuổi hiện tại: {{ currentAge }} {{ currentAge < 18 ? '(Yêu cầu tối thiểu 18 tuổi)' : '' }}
                      </div>
                    </div>

                    <!-- Giới tính -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:user-speak-bold-duotone" class="label-icon" />
                        Giới tính
                      </label>
                      <select v-model="employee.gioiTinh" class="form-input">
                        <option :value="true">Nam</option>
                        <option :value="false">Nữ</option>
                      </select>
                    </div>
                  </div>
                </div>

                <!-- Contact Information Section -->
                <div class="form-section">
                  <div class="section-header">
                    <h3 class="section-title">
                      <Icon icon="solar:phone-bold-duotone" class="section-icon" />
                      Thông tin liên hệ
                    </h3>
                    <p class="section-description">Thông tin để liên lạc với nhân viên</p>
                  </div>

                  <div class="form-grid contact-info">
                    <!-- Số điện thoại -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:phone-calling-bold-duotone" class="label-icon" />
                        Số điện thoại <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.soDienThoai"
                        type="text"
                        class="form-input"
                        placeholder="0123456789"
                        required
                      />
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:letter-bold-duotone" class="label-icon" />
                        Email <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.email"
                        type="email"
                        class="form-input"
                        placeholder="example@company.com"
                        required
                      />
                    </div>

                    <!-- CCCD -->
                    <div class="form-group col-span-full">
                      <label class="form-label">
                        <Icon icon="solar:card-bold-duotone" class="label-icon" />
                        CCCD <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.cccd"
                        type="text"
                        class="form-input"
                        placeholder="123456789012"
                        required
                      />
                    </div>

                  </div>
                </div>

                <!-- Address Section -->
                <div class="form-section address-section">
                  <div class="section-header">
                    <h3 class="section-title">
                      <Icon icon="solar:map-point-bold-duotone" class="section-icon" />
                      Địa chỉ thường trú
                    </h3>
                    <p class="section-description">Địa chỉ nơi nhân viên đang sinh sống</p>
                  </div>
                  
                  <div class="form-grid address-grid">
                    <!-- Số nhà, tên đường -->
                    <div class="form-group col-span-full">
                      <label class="form-label">
                        <Icon icon="solar:home-bold-duotone" class="label-icon" />
                        Số nhà, tên đường <span class="required">*</span>
                      </label>
                      <input
                        v-model="employee.diaChiSoNhaTenDuong"
                        type="text"
                        class="form-input"
                        placeholder="Ví dụ: 123 Nguyễn Văn Linh"
                        required
                      />
                    </div>

                    <!-- Tỉnh/Thành phố -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:global-bold-duotone" class="label-icon" />
                        Tỉnh/Thành phố <span class="required">*</span>
                      </label>
                      <div class="address-select-wrapper">
                        <input
                          v-model="provinceSearch"
                          type="text"
                          class="form-input address-search"
                          placeholder="Tìm kiếm tỉnh/thành phố..."
                          @focus="showProvinceDropdown = true"
                          @blur="hideProvinceDropdown"
                        />
                        <div v-if="showProvinceDropdown" class="address-dropdown">
                          <div v-if="addressLoading.provinces" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div
                            v-for="province in filteredProvinces"
                            :key="province.code"
                            class="dropdown-item"
                            @mousedown="selectProvince(province)"
                          >
                            {{ province.name }}
                          </div>
                          <div v-if="!filteredProvinces.length && !addressLoading.provinces" class="dropdown-empty">
                            Không tìm thấy tỉnh/thành phố
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Quận/Huyện -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:city-bold-duotone" class="label-icon" />
                        Quận/Huyện <span class="required">*</span>
                      </label>
                      <div class="address-select-wrapper">
                        <input
                          v-model="districtSearch"
                          type="text"
                          class="form-input address-search"
                          placeholder="Tìm kiếm quận/huyện..."
                          :disabled="!selectedProvince"
                          @focus="showDistrictDropdown = true"
                          @blur="hideDistrictDropdown"
                        />
                        <div v-if="showDistrictDropdown && selectedProvince" class="address-dropdown">
                          <div v-if="addressLoading.districts" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div
                            v-for="district in filteredDistricts"
                            :key="district.code"
                            class="dropdown-item"
                            @mousedown="selectDistrict(district)"
                          >
                            {{ district.name }}
                          </div>
                          <div v-if="!filteredDistricts.length && !addressLoading.districts && districts.length === 0" class="dropdown-empty">
                            Không tìm thấy quận/huyện
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Phường/Xã -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:buildings-bold-duotone" class="label-icon" />
                        Phường/Xã <span class="required">*</span>
                      </label>
                      <div class="address-select-wrapper">
                        <input
                          v-model="wardSearch"
                          type="text"
                          class="form-input address-search"
                          placeholder="Tìm kiếm phường/xã..."
                          :disabled="!selectedDistrict"
                          @focus="showWardDropdown = true"
                          @blur="hideWardDropdown"
                        />
                        <div v-if="showWardDropdown && selectedDistrict" class="address-dropdown">
                          <div v-if="addressLoading.wards" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div
                            v-for="ward in filteredWards"
                            :key="ward.code"
                            class="dropdown-item"
                            @mousedown="selectWard(ward)"
                          >
                            {{ ward.name }}
                          </div>
                          <div v-if="!filteredWards.length && !addressLoading.wards && wards.length === 0" class="dropdown-empty">
                            Không tìm thấy phường/xã
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="action-section">
                  <div class="action-buttons">
                    <button
                      type="button"
                      class="btn-secondary"
                      @click="goBack"
                      :disabled="loading"
                    >
                      Quay lại
                    </button>
                    <button
                      type="submit"
                      class="btn-primary"
                      :disabled="loading"
                    >
                      <Icon v-if="loading" icon="solar:loading-bold" class="animate-spin btn-icon" />
                      Cập nhật nhân viên
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { useAddressSelection } from '@/services/nhanVienService/addressService.js'

// Composables
const toast = useToast()
const router = useRouter()
const route = useRoute()
const {
  provinces,
  districts,
  wards,
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
  setAddressFromData,
  loadDistricts,
  loadWards
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
  trangThai: true
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

// Computed property to calculate current age
const currentAge = computed(() => {
  if (!employee.value.ngaySinh) return 0
  
  const today = new Date()
  const birthDate = new Date(employee.value.ngaySinh)
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  
  return age
})

// Load employee data
const loadEmployeeData = async () => {
  if (!employeeId.value) {
    toast.error('Không tìm thấy ID nhân viên')
    return
  }
  
  loading.value = true
  try {
    console.log('Loading employee data for ID:', employeeId.value)
    
    // Import and call API to get employee by ID
    const { default: nhanVienAPI } = await import('@/services/api/APINhanVien/NhanVienAPI.js')
    const response = await nhanVienAPI.getNhanVienById(employeeId.value)
    console.log('API Response:', response)
    
    if (response && response.data) {
      const employeeData = response.data
      console.log('Employee data received:', employeeData)
      
      // Map backend data to frontend format
      employee.value = {
        id: employeeData.id,
        chucVuID: employeeData.chucVuID || null,
        taiKhoanID: employeeData.taiKhoanID || null,
        tenNhanVien: employeeData.tenNhanVien || '',
        ngaySinh: employeeData.ngaySinh || '',
        gioiTinh: employeeData.gioiTinh !== undefined ? employeeData.gioiTinh : true,
        soDienThoai: employeeData.soDienThoai || '',
        cccd: employeeData.cccd || '',
        email: employeeData.email || '',
        diaChiSoNhaTenDuong: employeeData.diaChiCuThe || '',
        diaChiPhuongXa: employeeData.phuong || '',
        diaChiQuanHuyen: employeeData.quan || '',
        diaChiTinhThanh: employeeData.thanhPho || '',
        provinceCode: employeeData.provinceCode || null,
        districtCode: employeeData.districtCode || null,
        wardCode: employeeData.wardCode || null,
        trangThai: employeeData.deletedTrangThai !== undefined ? employeeData.deletedTrangThai : true
      }
      
      console.log('Mapped employee data:', employee.value)
      
      // Set address selections based on loaded data
      // Use setTimeout to ensure address data is loaded after component is fully rendered
      setTimeout(async () => {
        if (employeeData.thanhPho) {
          try {
            console.log('Setting address data:', {
              province: employeeData.thanhPho,
              district: employeeData.quan,
              ward: employeeData.phuong
            })
            
            // Find province by name
            const province = provinces.value.find(p => 
              p.name.toLowerCase() === employeeData.thanhPho.toLowerCase()
            )
            
            if (province) {
              selectedProvince.value = province
              provinceSearch.value = province.name
              console.log('Province selected:', province.name)
              
              // Load districts for this province and wait for completion
              await loadDistricts(province.code)
              console.log('Districts loaded:', districts.value.length)
              
              // Find district by name after districts are loaded
              if (employeeData.quan && districts.value.length > 0) {
                // Use setTimeout to ensure districts are rendered
                setTimeout(async () => {
                  const district = districts.value.find(d => 
                    d.name.toLowerCase() === employeeData.quan.toLowerCase()
                  )
                  
                  if (district) {
                    selectedDistrict.value = district
                    districtSearch.value = district.name
                    console.log('District selected:', district.name)
                    
                    // Load wards for this district and wait for completion
                    await loadWards(district.code)
                    console.log('Wards loaded:', wards.value.length)
                    
                    // Find ward by name after wards are loaded
                    if (employeeData.phuong && wards.value.length > 0) {
                      // Use setTimeout to ensure wards are rendered
                      setTimeout(() => {
                        const ward = wards.value.find(w => 
                          w.name.toLowerCase() === employeeData.phuong.toLowerCase()
                        )
                        
                        if (ward) {
                          selectedWard.value = ward
                          wardSearch.value = ward.name
                          console.log('Ward selected:', ward.name)
                        } else {
                          wardSearch.value = employeeData.phuong
                          console.log('Ward not found, setting search value:', employeeData.phuong)
                        }
                      }, 100)
                    }
                  } else {
                    districtSearch.value = employeeData.quan
                    console.log('District not found, setting search value:', employeeData.quan)
                  }
                }, 100)
              }
            } else {
              provinceSearch.value = employeeData.thanhPho
              console.log('Province not found, setting search value:', employeeData.thanhPho)
            }
          } catch (addressError) {
            console.warn('Could not load address data:', addressError)
            // Fallback to setting search values directly
            provinceSearch.value = employeeData.thanhPho || ''
            districtSearch.value = employeeData.quan || ''
            wardSearch.value = employeeData.phuong || ''
          }
        }
      }, 200)
      
      toast.success('Tải thông tin nhân viên thành công!')
    } else {
      throw new Error('Không nhận được dữ liệu từ server')
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin nhân viên:', error)
    console.error('Error details:', error.response?.data)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi tải thông tin nhân viên'
    toast.error(errorMessage)
    
    // Navigate back to employee list if employee not found
    if (error.response?.status === 404) {
      setTimeout(() => {
        router.push('/nhan-vien')
      }, 2000)
    }
  } finally {
    loading.value = false
  }
}

// Client-side validation function
const validateEmployee = async (emp) => {
  try {
    // Import nhanVienService for validation
    const { default: nhanVienService } = await import('@/services/nhanVienService/nhanVienService.js')
    
    // Check address selections first (client-side check)
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

    // Prepare data for validation
    const validationData = {
      id: emp.id,
      tenNhanVien: emp.tenNhanVien,
      ngaySinh: emp.ngaySinh,
      gioiTinh: emp.gioiTinh,
      cccd: emp.cccd,
      diaChiCuThe: emp.diaChiSoNhaTenDuong,
      thanhPho: selectedProvince.value?.name || emp.diaChiTinhThanh,
      quan: selectedDistrict.value?.name || emp.diaChiQuanHuyen,
      phuong: selectedWard.value?.name || emp.diaChiPhuongXa,
      anhNhanVien: emp.anhNhanVien || null,
      ghiChu: emp.ghiChu || null,
      email: emp.email,
      soDienThoai: emp.soDienThoai,
      deleted: emp.trangThai
    }

    // Client-side validation using service
    const clientErrors = nhanVienService.validateEmployeeData(validationData, true) // isUpdate = true
    if (clientErrors.length > 0) {
      toast.error(clientErrors[0])
      return false
    }

    // Call backend validation API if client validation passes
    const { default: nhanVienValidationAPI } = await import('@/services/api/APINhanVien/NhanVienValidationAPI.js')
    const validationResult = await nhanVienValidationAPI.validateUpdateEmployee(validationData)
    
    if (validationResult.valid) {
      return true
    } else {
      // Display first error from backend
      if (validationResult.errors && validationResult.errors.length > 0) {
        toast.error(validationResult.errors[0])
      } else {
        toast.error(validationResult.message || 'Dữ liệu không hợp lệ')
      }
      return false
    }
  } catch (error) {
    console.error('Validation error:', error)
    toast.error('Lỗi khi kiểm tra dữ liệu')
    return false
  }
}

// Form submission
const submitForm = async () => {
  console.log('submitForm called for update!')
  if (!(await validateEmployee(employee.value))) return

  loading.value = true
  try {
    console.log('Frontend employee data:', employee.value)
    console.log('Selected address:', {
      province: selectedProvince.value,
      district: selectedDistrict.value,
      ward: selectedWard.value
    })

    // Map frontend data to backend format for update
    const backendData = {
      id: employee.value.id,
      // NhanVien fields
      tenNhanVien: employee.value.tenNhanVien,
      ngaySinh: employee.value.ngaySinh,
      gioiTinh: employee.value.gioiTinh,
      cccd: employee.value.cccd,
      diaChiCuThe: employee.value.diaChiSoNhaTenDuong,
      thanhPho: selectedProvince.value?.name || employee.value.diaChiTinhThanh,
      quan: selectedDistrict.value?.name || employee.value.diaChiQuanHuyen,
      phuong: selectedWard.value?.name || employee.value.diaChiPhuongXa,
      anhNhanVien: employee.value.anhNhanVien || null,
      ghiChu: employee.value.ghiChu || null,
      
      // TaiKhoan fields
      email: employee.value.email,
      soDienThoai: employee.value.soDienThoai,
      
      // Status
      deleted: employee.value.trangThai
    }

    console.log('Backend data to send:', backendData)

    // Import and call API
    const { default: nhanVienAPI } = await import('@/services/api/APINhanVien/NhanVienAPI.js')
    const response = await nhanVienAPI.updateNhanVien(employee.value.id, backendData)
    console.log('API Response:', response)
    
    if (response && (response.data || response.message)) {
      toast.success(response.message || 'Cập nhật nhân viên thành công!')
      router.push('/nhan-vien')
    } else {
      throw new Error('Không nhận được dữ liệu từ server')
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật nhân viên:', error)
    console.error('Error details:', error.response?.data)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật nhân viên'
    toast.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// CCCD Scanning
const scanCCCD = () => {
  // Simulate CCCD scanning functionality
  toast.info('Đang khởi động chức năng quét CCCD...')
  
  // Here you would integrate with actual CCCD scanning API/library
  // For now, we'll simulate the process
  setTimeout(() => {
    // Simulated CCCD data
    const mockCCCDData = {
      tenNhanVien: 'Nguyễn Văn An',
      ngaySinh: '1990-05-15',
      gioiTinh: true,
      cccd: '123456789012',
      diaChiSoNhaTenDuong: '123 Đường ABC',
      diaChiPhuongXa: 'Phường Tân Bình',
      diaChiQuanHuyen: 'Quận 1',
      diaChiTinhThanh: 'TP. Hồ Chí Minh'
    }
    
    // Fill form with scanned data
    Object.assign(employee.value, mockCCCDData)
    toast.success('Quét CCCD thành công! Thông tin đã được cập nhật.')
  }, 2000)
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

// Go back
const goBack = () => {
  router.push('/nhan-vien')
}

// Load data on component mount
onMounted(() => {
  loadEmployeeData()
})
</script>

<style scoped>
/* ===== GENERAL STYLES ===== */
.employee-edit-container {
  min-height: 100vh;
  font-family: 'Nunito', 'Be Vietnam Pro', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ===== MAIN CONTENT ===== */
.main-content {
  width: 100%;
}

.content-wrapper {
  width: 100%;
}

.form-panel {
  width: 100%;
}

/* ===== CARD STYLES ===== */
.card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-content {
  padding: 40px;
}

/* ===== FORM SECTIONS ===== */
.form-section {
  margin-bottom: 40px;
  position: relative;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.section-header {
  margin-bottom: 28px;
}

.section-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.section-icon {
  font-size: 1.5rem;
  color: #3b82f6;
}

.section-description {
  color: #64748b;
  font-size: 0.9rem;
  margin: 0;
  padding-left: 36px;
}

/* ===== FORM GRID LAYOUTS ===== */
.form-grid {
  display: grid;
  gap: 32px;
}

.personal-info {
  grid-template-columns: 1fr 1fr 1fr 1fr;
}

.contact-info {
  grid-template-columns: 1fr 1fr;
}

.address-grid {
  grid-template-columns: repeat(3, 1fr);
}

.col-span-2 {
  grid-column: span 2;
}

.col-span-full {
  grid-column: 1 / -1;
}

/* ===== FORM ELEMENTS ===== */
.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 1rem;
  color: #6b7280;
}

.required {
  color: #ef4444;
  font-weight: 700;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: #fafbfc;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  transform: translateY(-1px);
}

.form-input:hover:not(:focus) {
  border-color: #d1d5db;
  background: white;
}

/* ===== ADDRESS SECTION STYLING ===== */
.address-section {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 32px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  position: relative;
}

.address-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  border-radius: 20px 20px 0 0;
}

/* ===== ACTION SECTION ===== */
.action-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 2px solid #f1f5f9;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

.btn-primary,
.btn-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  min-width: 160px;
  position: relative;
  overflow: hidden;
}

.btn-icon {
  font-size: 1.1rem;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover:not(:disabled) {
  background: #f1f5f9;
  color: #374151;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* ===== LOADING STYLES ===== */
.loading-overlay {
  opacity: 0.6;
  pointer-events: none;
}

.loading-spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  100% {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

/* ===== RESPONSIVE DESIGN ===== */
@media (max-width: 1024px) {
  .content-wrapper {
    max-width: 800px;
  }
  
  .personal-info {
    grid-template-columns: 1fr 1fr;
  }
  
  .col-span-2 {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .employee-edit-container {
    padding: 16px;
  }

  .card-content {
    padding: 28px;
  }

  .personal-info,
  .contact-info,
  .address-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .col-span-2,
  .col-span-full {
    grid-column: 1;
  }

  .action-buttons {
    flex-direction: column-reverse;
    gap: 12px;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
    min-width: unset;
  }

  .section-title {
    font-size: 1.25rem;
  }

  .section-description {
    padding-left: 32px;
  }

  .address-section {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .card-content {
    padding: 20px;
  }

  .form-section {
    margin-bottom: 32px;
  }

  .form-label {
    font-size: 0.9rem;
  }

  .form-input {
    padding: 12px 16px;
    font-size: 0.9rem;
  }

  .address-section {
    padding: 20px;
  }

  .section-title {
    font-size: 1.1rem;
  }

  .section-description {
    font-size: 0.85rem;
    padding-left: 28px;
  }

  .action-section {
    margin-top: 32px;
    padding-top: 24px;
  }
}

/* ===== ADDRESS DROPDOWN STYLES ===== */
.address-select-wrapper {
  position: relative;
}

.address-search {
  width: 100%;
}

.address-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #e5e7eb;
  border-top: none;
  border-radius: 0 0 16px 16px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  padding: 12px 18px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid #f3f4f6;
}

.dropdown-item:hover {
  background-color: #f8fafc;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-loading,
.dropdown-empty {
  padding: 12px 18px;
  color: #6b7280;
  font-style: italic;
  text-align: center;
}

.dropdown-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.form-input:disabled {
  background-color: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

/* ===== AGE VALIDATION STYLES ===== */
.input-error {
  border-color: #ef4444 !important;
  background-color: #fef2f2 !important;
}

.input-error:focus {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1) !important;
}

.age-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.age-valid {
  background-color: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.age-error {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.age-icon {
  font-size: 1rem;
  flex-shrink: 0;
}

.age-valid .age-icon {
  color: #16a34a;
}

.age-error .age-icon {
  color: #dc2626;
}
</style>
