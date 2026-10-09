<template>
  <div class="customer-edit-container">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" :show-page-info="true" page-title="Chỉnh Sửa Khách Hàng"
      page-description="Cập nhật thông tin khách hàng trong hệ thống" page-icon="solar:settings-bold-duotone"
      :page-stats="pageStats" :actions="breadcrumbActions" />

    <!-- Main Content -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- Customer Form -->
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
                    <p class="section-description">Cập nhật thông tin cơ bản của khách hàng</p>
                  </div>

                  <div class="form-grid personal-info">
                    <!-- Tên khách hàng -->
                    <div class="form-group col-span-full">
                      <label class="form-label">
                        <Icon icon="solar:user-bold-duotone" class="label-icon" />
                        Tên khách hàng <span class="required">*</span>
                      </label>
                      <input v-model="customer.tenKhachHang" type="text" class="form-input"
                        placeholder="Nhập họ và tên đầy đủ" required />
                    </div>

                    <!-- Row with Ngày sinh, CCCD, and Giới tính -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:calendar-bold-duotone" class="label-icon" />
                        Ngày sinh <span class="required">*</span>
                      </label>
                      <input v-model="customer.ngaySinh" type="date" class="form-input" required />
                    </div>

                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:card-bold-duotone" class="label-icon" />
                        CCCD
                      </label>
                      <input v-model="customer.cccd" type="text" class="form-input"
                        placeholder="Số căn cước công dân" />
                    </div>

                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:user-speak-bold-duotone" class="label-icon" />
                        Giới tính
                      </label>
                      <select v-model="customer.gioiTinh" class="form-input">
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
                    <p class="section-description">Thông tin liên lạc của khách hàng</p>
                  </div>

                  <div class="form-grid contact-info">
                    <!-- Số điện thoại -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:phone-calling-bold-duotone" class="label-icon" />
                        Số điện thoại <span class="required">*</span>
                      </label>
                      <input v-model="customer.soDienThoai" type="text" class="form-input" placeholder="0123456789"
                        required />
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:letter-bold-duotone" class="label-icon" />
                        Email
                      </label>
                      <input v-model="customer.email" type="email" class="form-input" placeholder="example@email.com" />
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
                    <p class="section-description">Địa chỉ nơi khách hàng đang sinh sống</p>
                  </div>

                  <div class="form-grid address-grid">
                    <!-- Số nhà, tên đường -->
                    <div class="form-group col-span-full">
                      <label class="form-label">
                        <Icon icon="solar:home-bold-duotone" class="label-icon" />
                        Số nhà, tên đường <span class="required">*</span>
                      </label>
                      <input v-model="customer.diaChiSoNhaTenDuong" type="text" class="form-input"
                        placeholder="Ví dụ: 123 Nguyễn Văn Linh" required />
                    </div>

                    <!-- Tỉnh/Thành phố -->
                    <div class="form-group">
                      <label class="form-label">
                        <Icon icon="solar:global-bold-duotone" class="label-icon" />
                        Tỉnh/Thành phố <span class="required">*</span>
                      </label>
                      <div class="address-select-wrapper">
                        <input v-model="provinceSearch" type="text" class="form-input address-search"
                          placeholder="Tìm kiếm tỉnh/thành phố..." @focus="showProvinceDropdown = true"
                          @blur="hideProvinceDropdown" />
                        <div v-if="showProvinceDropdown" class="address-dropdown">
                          <div v-if="addressLoading.provinces" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div v-for="province in filteredProvinces" :key="province.code" class="dropdown-item"
                            @mousedown="selectProvince(province); showProvinceDropdown = false">
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
                        <input v-model="districtSearch" type="text" class="form-input address-search"
                          placeholder="Tìm kiếm quận/huyện..." :disabled="!selectedProvince"
                          @focus="showDistrictDropdown = true" @blur="hideDistrictDropdown" />
                        <div v-if="showDistrictDropdown && selectedProvince" class="address-dropdown">
                          <div v-if="addressLoading.districts" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div v-for="district in filteredDistricts" :key="district.code" class="dropdown-item"
                            @mousedown="selectDistrict(district); showDistrictDropdown = false">
                            {{ district.name }}
                          </div>
                          <div v-if="!filteredDistricts.length && !addressLoading.districts" class="dropdown-empty">
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
                        <input v-model="wardSearch" type="text" class="form-input address-search"
                          placeholder="Tìm kiếm phường/xã..." :disabled="!selectedDistrict"
                          @focus="showWardDropdown = true" @blur="hideWardDropdown" />
                        <div v-if="showWardDropdown && selectedDistrict" class="address-dropdown">
                          <div v-if="addressLoading.wards" class="dropdown-loading">
                            <Icon icon="solar:loading-bold" class="animate-spin" />
                            Đang tải...
                          </div>
                          <div v-for="ward in filteredWards" :key="ward.code" class="dropdown-item"
                            @mousedown="selectWard(ward); showWardDropdown = false">
                            {{ ward.name }}
                          </div>
                          <div v-if="!filteredWards.length && !addressLoading.wards" class="dropdown-empty">
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
                    <button type="button" class="btn-secondary" @click="goBack" :disabled="loading">
                      Quay lại
                    </button>
                    <button type="submit" class="btn-primary" :disabled="loading">
                      <Icon v-if="loading" icon="solar:loading-bold" class="animate-spin btn-icon" />
                      Cập nhật khách hàng
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
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import khachHangService from '@/services/khachHangService/khachHangService.js'
import { useKhachHangAddressSelection } from '@/services/khachHangService/addressService.js'

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
  loadDistricts,
  loadWards,
  selectProvince,
  selectDistrict,
  selectWard,
  hideProvinceDropdown,
  hideDistrictDropdown,
  hideWardDropdown,
  setAddressFromData
} = useKhachHangAddressSelection()

// State
const loading = ref(false)
const customerId = ref(route.params.id)
const showProvinceDropdown = ref(false)
const showDistrictDropdown = ref(false)
const showWardDropdown = ref(false)

const customer = ref({
  id: null,
  taiKhoanID: null,
  tenKhachHang: '',
  email: '',
  ngaySinh: '',
  gioiTinh: true,
  soDienThoai: '',
  cccd: '',
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
  { label: 'Quản lý khách hàng', path: '/khach-hang' },
  { label: 'Khách hàng', path: '/khach-hang' },
  { label: 'Chỉnh sửa' }
])

const breadcrumbActions = ref([
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
    console.log('Loading customer data for ID:', customerId.value)
    
    // Import and call API directly to get customer by ID (similar to employee edit)
    const { default: khachHangAPI } = await import('@/services/api/APIKhachHang/KhachHangAPI.js')
    const response = await khachHangAPI.getKhachHangById(customerId.value)
    console.log('API Response:', response)
    
    if (response && response.data) {
      const customerData = response.data
      console.log('Customer data received:', customerData)
      
      // Map backend data to frontend format (similar to employee mapping)
      customer.value = {
        id: customerData.id,
        taiKhoanID: customerData.taiKhoanId || null,
        tenKhachHang: customerData.ten || '',
        email: customerData.email || '',
        ngaySinh: customerData.ngaySinh ? (typeof customerData.ngaySinh === 'string' && customerData.ngaySinh.includes('T') ? customerData.ngaySinh.split('T')[0] : customerData.ngaySinh) : '',
        gioiTinh: customerData.gioiTinh === 1 || customerData.gioiTinh === true,
        soDienThoai: customerData.soDienThoai || '',
        cccd: customerData.cccd || '',
        diaChiSoNhaTenDuong: customerData.diaChiCuThe || '',
        diaChiPhuongXa: customerData.phuong || '',
        diaChiQuanHuyen: customerData.quan || '',
        diaChiTinhThanh: customerData.thanhPho || '',
        provinceCode: null, // Will be set from address lookup
        districtCode: null, // Will be set from address lookup
        wardCode: null, // Will be set from address lookup
        trangThai: customerData.deleted !== undefined ? customerData.deleted : true
      }
      
      console.log('Mapped customer data:', customer.value)
      
      // Set address selections based on loaded data (similar to employee edit)
      setTimeout(async () => {
        if (customerData.thanhPho) {
          try {
            console.log('Setting address data:', {
              province: customerData.thanhPho,
              district: customerData.quan,
              ward: customerData.phuong
            })
            
            // Find and set address selections by name (similar to employee edit)
            // Find province by name
            const province = provinces.value.find(p => 
              p.name.toLowerCase() === customerData.thanhPho.toLowerCase()
            )
            
            if (province) {
              selectedProvince.value = province
              provinceSearch.value = province.name
              console.log('Province selected:', province.name)
              
              // Load districts for this province and wait for completion
              await loadDistricts(province.code)
              console.log('Districts loaded:', districts.value.length)
              
              // Find district by name after districts are loaded
              if (customerData.quan && districts.value.length > 0) {
                setTimeout(async () => {
                  const district = districts.value.find(d => 
                    d.name.toLowerCase() === customerData.quan.toLowerCase()
                  )
                  
                  if (district) {
                    selectedDistrict.value = district
                    districtSearch.value = district.name
                    console.log('District selected:', district.name)
                    
                    // Load wards for this district and wait for completion
                    await loadWards(district.code)
                    console.log('Wards loaded:', wards.value.length)
                    
                    // Find ward by name after wards are loaded
                    if (customerData.phuong && wards.value.length > 0) {
                      setTimeout(() => {
                        const ward = wards.value.find(w => 
                          w.name.toLowerCase() === customerData.phuong.toLowerCase()
                        )
                        
                        if (ward) {
                          selectedWard.value = ward
                          wardSearch.value = ward.name
                          console.log('Ward selected:', ward.name)
                        } else {
                          wardSearch.value = customerData.phuong
                          console.log('Ward not found, setting search value:', customerData.phuong)
                        }
                      }, 100)
                    }
                  } else {
                    districtSearch.value = customerData.quan
                    console.log('District not found, setting search value:', customerData.quan)
                  }
                }, 100)
              }
            } else {
              provinceSearch.value = customerData.thanhPho
              console.log('Province not found, setting search value:', customerData.thanhPho)
            }
          } catch (addressError) {
            console.warn('Could not load address data:', addressError)
            // Fallback to setting search values directly
            provinceSearch.value = customerData.thanhPho || ''
            districtSearch.value = customerData.quan || ''
            wardSearch.value = customerData.phuong || ''
          }
        }
      }, 200)

      toast.success('Tải thông tin khách hàng thành công!')
    } else {
      throw new Error('Không nhận được dữ liệu từ server')
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin khách hàng:', error)
    console.error('Error details:', error.response?.data)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi tải thông tin khách hàng'
    toast.error(errorMessage)
    
    // Navigate back to customer list if customer not found
    if (error.response?.status === 404) {
      setTimeout(() => {
        router.push('/khach-hang')
      }, 2000)
    }
  } finally {
    loading.value = false
  }
}

// Form submission
const submitForm = async () => {
  // Update address fields from selected values
  if (selectedProvince.value) {
    customer.value.diaChiTinhThanh = selectedProvince.value.name
    customer.value.provinceCode = selectedProvince.value.code
  }
  if (selectedDistrict.value) {
    customer.value.diaChiQuanHuyen = selectedDistrict.value.name
    customer.value.districtCode = selectedDistrict.value.code
  }
  if (selectedWard.value) {
    customer.value.diaChiPhuongXa = selectedWard.value.name
    customer.value.wardCode = selectedWard.value.code
  }

  // Validate using service
  const validationErrors = khachHangService.validateCustomerData(customer.value, true)
  if (validationErrors.length > 0) {
    validationErrors.forEach(error => toast.error(error))
    return
  }

  loading.value = true
  try {
    await khachHangService.updateCustomer(customerId.value, customer.value)
    toast.success('Cập nhật khách hàng thành công!')
    router.push('/khach-hang')
  } catch (error) {
    console.error('Lỗi khi cập nhật khách hàng:', error)
    const errorMsg = error.response?.data?.error || error.message || 'Lỗi khi cập nhật khách hàng'
    toast.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// Go back
const goBack = () => {
  router.push('/khach-hang')
}

// Load data on component mount
onMounted(() => {
  loadCustomerData()
})
</script>

<style scoped>
/* ===== GENERAL STYLES ===== */
.customer-edit-container {
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
  grid-template-columns: 1fr 1fr 1fr;
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
  .customer-edit-container {
    padding: 16px;
  }

  .card-content {
    padding: 28px;
  }

  .personal-info,
  .contact-info {
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

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
