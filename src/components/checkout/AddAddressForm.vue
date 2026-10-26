<template>
  <form @submit.prevent="saveAddress">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <label for="addressName" class="block text-sm font-medium text-gray-700 mb-2">
          Họ và tên <span class="text-red-500">*</span>
        </label>
        <input
          id="addressName"
          v-model="addressForm.ten"
          type="text"
          required
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
          placeholder="Nguyễn Văn A"
        />
      </div>
      <div>
        <label for="addressPhone" class="block text-sm font-medium text-gray-700 mb-2">
          Số điện thoại <span class="text-red-500">*</span>
        </label>
        <input
          id="addressPhone"
          v-model="addressForm.soDienThoai"
          type="tel"
          required
          pattern="[0-9]{10}"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
          placeholder="0123456789"
        />
      </div>
      <div>
        <label for="province" class="block text-sm font-medium text-gray-700 mb-2">
          Tỉnh/Thành phố <span class="text-red-500">*</span>
        </label>
        <select
          id="province"
          v-model="addressForm.thanhPho"
          required
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
          @change="onProvinceChange"
        >
          <option value="">Chọn tỉnh/thành phố</option>
          <option v-for="province in provinces" :key="province.code" :value="province.name">
            {{ province.name }}
          </option>
        </select>
      </div>
      <div>
        <label for="district" class="block text-sm font-medium text-gray-700 mb-2">
          Quận/Huyện <span class="text-red-500">*</span>
        </label>
        <select
          id="district"
          v-model="addressForm.quan"
          required
          :disabled="!addressForm.thanhPho"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors disabled:bg-gray-100"
          @change="onDistrictChange"
        >
          <option value="">Chọn quận/huyện</option>
          <option v-for="district in districts" :key="district.code" :value="district.name">
            {{ district.name }}
          </option>
        </select>
      </div>
      <div>
        <label for="ward" class="block text-sm font-medium text-gray-700 mb-2">
          Phường/Xã <span class="text-red-500">*</span>
        </label>
        <select
          id="ward"
          v-model="addressForm.phuong"
          required
          :disabled="!addressForm.quan"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors disabled:bg-gray-100"
        >
          <option value="">Chọn phường/xã</option>
          <option v-for="ward in wards" :key="ward.code" :value="ward.name">
            {{ ward.name }}
          </option>
        </select>
      </div>
      <div>
        <label for="specificAddress" class="block text-sm font-medium text-gray-700 mb-2">
          Số nhà, tên đường <span class="text-red-500">*</span>
        </label>
        <input
          id="specificAddress"
          v-model="addressForm.diaChiCuThe"
          type="text"
          required
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
          placeholder="123 Đường ABC"
        />
      </div>
      <div class="md:col-span-2">
        <div class="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
          <div class="relative">
            <input
              v-model="addressForm.macDinh"
              type="checkbox"
              id="defaultAddressCheckout"
              class="w-5 h-5 text-accent bg-white border-2 border-gray-300 rounded focus:ring-2 focus:ring-accent focus:border-accent checked:bg-accent checked:border-accent transition-all duration-200"
            />
            <div v-if="addressForm.macDinh" class="absolute inset-0 flex items-center justify-center pointer-events-none">
              <svg class="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
              </svg>
            </div>
          </div>
          <div class="flex flex-col">
            <label for="defaultAddressCheckout" class="text-sm font-semibold text-gray-700 cursor-pointer select-none hover:text-accent transition-colors">
              Đặt làm địa chỉ mặc định
            </label>
            <span class="text-xs text-gray-500 mt-1">
              {{ addressForm.macDinh ? 'Địa chỉ này sẽ được đặt làm mặc định' : 'Địa chỉ mặc định sẽ được chọn tự động khi đặt hàng' }}
            </span>
          </div>
          <div class="flex items-center">
            <span v-if="addressForm.macDinh" class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-xs font-semibold flex items-center">
              <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
              </svg>
              Mặc định
            </span>
            <span v-else class="bg-gray-100 text-gray-600 px-3 py-1 rounded-full text-xs font-semibold">
              Thường
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Form Actions -->
    <div class="flex justify-end gap-4 mt-8 pt-6 border-t border-gray-200">
      <button
        type="button"
        @click="$emit('cancel')"
        class="px-6 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors font-medium"
      >
        Hủy
      </button>
      <button
        type="submit"
        :disabled="!canSave || saving"
        class="px-8 py-3 bg-accent hover:bg-accent-dark disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors"
      >
        <span v-if="saving">
          <i class="fas fa-spinner fa-spin mr-2"></i>Đang lưu...
        </span>
        <span v-else>
          <i class="fas fa-save mr-2"></i>Lưu địa chỉ
        </span>
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { customerAPI } from '../../api/customerAPI.js'
import { validateHoTen, validateSoDienThoai, validateDiaChi, validateBatBuoc } from '../../utils/validators.js'

const emit = defineEmits(['address-saved', 'cancel'])

const toast = useToast()

// Form data
const addressForm = ref({
  ten: '',
  soDienThoai: '',
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
  macDinh: false
})

// Location data
const provinces = ref([])
const districts = ref([])
const wards = ref([])
const saving = ref(false)

// Lỗi từng ô (trước đây chỉ kiểm tra không rỗng)
const addressErrors = computed(() => ({
  ten: validateHoTen(addressForm.value.ten),
  soDienThoai: validateSoDienThoai(addressForm.value.soDienThoai),
  thanhPho: validateBatBuoc(addressForm.value.thanhPho, 'Tỉnh/Thành phố'),
  quan: validateBatBuoc(addressForm.value.quan, 'Quận/Huyện'),
  phuong: validateBatBuoc(addressForm.value.phuong, 'Phường/Xã'),
  diaChiCuThe: validateDiaChi(addressForm.value.diaChiCuThe)
}))

// Computed properties
const canSave = computed(() => Object.values(addressErrors.value).every(e => !e))

// Methods
const loadProvinces = async () => {
  try {
    // Mock data - replace with actual API call
    provinces.value = [
      { code: '01', name: 'Hà Nội' },
      { code: '79', name: 'Thành phố Hồ Chí Minh' },
      { code: '48', name: 'Đà Nẵng' },
      { code: '92', name: 'Cần Thơ' },
      { code: '36', name: 'Nam Định' },
      { code: '33', name: 'Hưng Yên' },
      { code: '31', name: 'Hải Dương' },
      { code: '30', name: 'Hải Phòng' }
    ]
  } catch (error) {
    console.error('Error loading provinces:', error)
    toast.error('Không thể tải danh sách tỉnh/thành phố')
  }
}

const onProvinceChange = async () => {
  addressForm.value.quan = ''
  addressForm.value.phuong = ''
  districts.value = []
  wards.value = []
  
  if (!addressForm.value.thanhPho) return
  
  try {
    // Mock data - replace with actual API call
    const mockDistricts = {
      'Hà Nội': [
        { code: '001', name: 'Ba Đình' },
        { code: '002', name: 'Hoàn Kiếm' },
        { code: '003', name: 'Tây Hồ' },
        { code: '004', name: 'Long Biên' },
        { code: '005', name: 'Cầu Giấy' },
        { code: '006', name: 'Đống Đa' },
        { code: '007', name: 'Hai Bà Trưng' },
        { code: '008', name: 'Hoàng Mai' },
        { code: '009', name: 'Thanh Xuân' }
      ],
      'Thành phố Hồ Chí Minh': [
        { code: '760', name: 'Quận 1' },
        { code: '761', name: 'Quận 2' },
        { code: '762', name: 'Quận 3' },
        { code: '763', name: 'Quận 4' },
        { code: '764', name: 'Quận 5' },
        { code: '765', name: 'Quận 6' },
        { code: '766', name: 'Quận 7' },
        { code: '767', name: 'Quận 8' },
        { code: '768', name: 'Quận 9' },
        { code: '769', name: 'Quận 10' }
      ]
    }
    
    districts.value = mockDistricts[addressForm.value.thanhPho] || []
  } catch (error) {
    console.error('Error loading districts:', error)
    toast.error('Không thể tải danh sách quận/huyện')
  }
}

const onDistrictChange = async () => {
  addressForm.value.phuong = ''
  wards.value = []
  
  if (!addressForm.value.quan) return
  
  try {
    // Mock data - replace with actual API call
    const mockWards = {
      'Ba Đình': [
        { code: '00001', name: 'Phúc Xá' },
        { code: '00002', name: 'Trúc Bạch' },
        { code: '00003', name: 'Vĩnh Phúc' },
        { code: '00004', name: 'Cống Vị' },
        { code: '00005', name: 'Liễu Giai' }
      ],
      'Hoàn Kiếm': [
        { code: '00006', name: 'Hàng Bài' },
        { code: '00007', name: 'Hàng Bồ' },
        { code: '00008', name: 'Hàng Buồm' },
        { code: '00009', name: 'Hàng Đào' },
        { code: '00010', name: 'Hàng Gai' }
      ],
      'Quận 1': [
        { code: '26734', name: 'Tân Định' },
        { code: '26737', name: 'Đa Kao' },
        { code: '26740', name: 'Bến Nghé' },
        { code: '26743', name: 'Bến Thành' },
        { code: '26746', name: 'Nguyễn Thái Bình' }
      ]
    }
    
    wards.value = mockWards[addressForm.value.quan] || []
  } catch (error) {
    console.error('Error loading wards:', error)
    toast.error('Không thể tải danh sách phường/xã')
  }
}

const saveAddress = async () => {
  if (!canSave.value) {
    toast.warning(Object.values(addressErrors.value).find(e => e) || 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  
  saving.value = true
  try {
    // Map frontend field names to backend field names
    const addressPayload = {
      tenNguoiNhan: addressForm.value.ten, // Backend expects 'tenNguoiNhan' not 'ten'
      soDienThoai: addressForm.value.soDienThoai,
      thanhPho: addressForm.value.thanhPho,
      quan: addressForm.value.quan,
      phuong: addressForm.value.phuong,
      diaChiCuThe: addressForm.value.diaChiCuThe,
      macDinh: addressForm.value.macDinh
    }
    
    console.log('Sending address payload:', addressPayload)
    const response = await customerAPI.addAddress(addressPayload)
    toast.success('Đã thêm địa chỉ mới thành công!')
    emit('address-saved', response.data)
  } catch (error) {
    console.error('Error saving address:', error)
    if (error.response?.data?.message) {
      toast.error(error.response.data.message)
    } else {
      toast.error('Không thể lưu địa chỉ. Vui lòng thử lại!')
    }
  } finally {
    saving.value = false
  }
}

// Debug: Watch checkbox state changes
watch(() => addressForm.value.macDinh, (newValue) => {
  console.log('Default address checkbox changed:', newValue)
})

// Load provinces on mount
onMounted(() => {
  loadProvinces()
  console.log('AddAddressForm mounted, initial macDinh value:', addressForm.value.macDinh)
})
</script>
