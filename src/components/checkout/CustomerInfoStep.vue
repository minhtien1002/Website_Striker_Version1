<template>
    <div>
        <div class="flex items-center gap-3 mb-4">
            <div
                class="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
                <span class="text-white font-bold text-sm">1</span>
            </div>
            <div>
                <h2 class="text-xl font-bold text-gray-900">Thông tin khách hàng</h2>
                <p class="text-sm text-gray-600">Nhập thông tin giao hàng</p>
            </div>
        </div>

        <!-- Authentication Status -->
        <div v-if="isAuthenticated" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl">
            <div class="flex items-center gap-3">
                <i class="fas fa-check-circle text-green-600"></i>
                <div>
                    <h4 class="font-semibold text-green-800">Đã đăng nhập</h4>
                    <p class="text-sm text-green-600">Chúng tôi sẽ sử dụng thông tin từ tài khoản của bạn</p>
                </div>
            </div>
        </div>

        <div v-else class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-xl">
            <div class="flex items-center gap-3">
                <i class="fas fa-info-circle text-blue-600"></i>
                <div>
                    <h4 class="font-semibold text-blue-800">Khách hàng chưa đăng nhập</h4>
                    <p class="text-sm text-blue-600">Vui lòng nhập đầy đủ thông tin giao hàng bên dưới</p>
                </div>
            </div>
        </div>

        <!-- Address Selection for Authenticated Users -->
        <div v-if="isAuthenticated" class="mb-8">
            <h3 class="text-xl font-bold text-gray-900 mb-4">Địa chỉ giao hàng</h3>

            <!-- Selected Address Display -->
            <div v-if="selectedAddress" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl">
                <div class="flex items-start justify-between">
                    <div class="flex items-center gap-3">
                        <i class="fas fa-map-marker-alt text-green-600"></i>
                        <div>
                            <h4 class="font-semibold text-green-800">Địa chỉ đã chọn</h4>
                            <p class="text-sm text-green-700 mt-1 font-medium">
                                {{ selectedAddress.tenKhachHang || selectedAddress.ten }} | {{
                                selectedAddress.soDienThoai }}
                            </p>
                            <p class="text-sm text-green-600">
                                <i class="fas fa-map-marker-alt mr-1"></i>{{ selectedAddress.diaChiCuThe }}, {{
                                selectedAddress.phuong }}, {{ selectedAddress.quan }}, {{ selectedAddress.thanhPho }}
                            </p>
                        </div>
                    </div>
                    <button @click="showAddressSelection = true"
                        class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium transition-colors">
                        <i class="fas fa-edit mr-2"></i>Thay đổi
                    </button>
                </div>
            </div>

            <!-- Address Management Tabs -->
            <div v-if="!selectedAddress || showAddressSelection"
                class="bg-white border border-gray-200 rounded-xl overflow-hidden">
                <!-- Tab Headers -->
                <div class="flex border-b border-gray-200">
                    <button @click="activeTab = 'list'" class="flex-1 px-6 py-4 text-sm font-medium transition-colors"
                        :class="activeTab === 'list' ? 'bg-blue-500 text-white' : 'bg-gray-50 text-gray-700 hover:bg-gray-100'">
                        <i class="fas fa-map-marker-alt mr-2"></i>Chọn địa chỉ có sẵn
                    </button>
                    <button @click="activeTab = 'add'" class="flex-1 px-6 py-4 text-sm font-medium transition-colors"
                        :class="activeTab === 'add' ? 'bg-blue-500 text-white' : 'bg-gray-50 text-gray-700 hover:bg-gray-100'">
                        <i class="fas fa-plus-circle mr-2"></i>Thêm địa chỉ mới
                    </button>
                </div>

                <!-- Tab Content -->
                <div class="p-6">
                    <!-- Address List Tab -->
                    <div v-if="activeTab === 'list'">
                        <div class="flex items-center justify-between mb-4">
                            <h4 class="text-lg font-semibold text-gray-900">Danh sách địa chỉ</h4>
                            <button v-if="selectedAddress && showAddressSelection" @click="showAddressSelection = false"
                                class="text-gray-500 hover:text-gray-700">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>

                        <!-- Loading State -->
                        <div v-if="loadingAddresses" class="text-center py-8">
                            <div
                                class="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mx-auto mb-4">
                            </div>
                            <p class="text-gray-600">Đang tải danh sách địa chỉ...</p>
                        </div>

                        <!-- Address Slideshow -->
                        <div v-else-if="addresses.length > 0" class="relative">
                            <!-- Address Cards Container -->
                            <div class="overflow-hidden rounded-xl">
                                <div class="flex transition-transform duration-300 ease-in-out"
                                    :style="{ transform: `translateX(-${currentAddressIndex * 100}%)` }">
                                    <div v-for="address in addresses" :key="address.id"
                                        class="w-full flex-shrink-0 p-4 bg-gradient-to-r from-blue-50 to-purple-50 border border-gray-200 cursor-pointer hover:shadow-lg transition-all"
                                        :class="{ 'ring-2 ring-accent': selectedAddress?.id === address.id }"
                                        @click="selectAddress(address)">
                                        <div class="flex items-start gap-4">
                                            <div class="w-6 h-6 rounded-full border-2 border-accent flex items-center justify-center mt-1"
                                                :class="{ 'bg-accent': selectedAddress?.id === address.id }">
                                                <div v-if="selectedAddress?.id === address.id"
                                                    class="w-3 h-3 bg-white rounded-full"></div>
                                            </div>
                                            <div class="flex-1">
                                                <div class="flex items-center gap-2 mb-3">
                                                    <h5 class="text-lg font-bold text-gray-900">{{ address.tenKhachHang
                                                        || address.ten }} | {{ address.soDienThoai }}</h5>
                                                    <span v-if="address.macDinh"
                                                        class="px-3 py-1 bg-accent text-white text-xs rounded-full font-medium">
                                                        Mặc định
                                                    </span>
                                                </div>
                                                <div class="space-y-1">
                                                    <p class="text-gray-600">
                                                        <i class="fas fa-map-marker-alt mr-2 text-accent"></i>
                                                        {{ address.diaChiCuThe }}, {{ address.phuong }}, {{ address.quan
                                                        }}, {{ address.thanhPho }}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Slideshow Controls -->
                            <div v-if="addresses.length > 1" class="flex items-center justify-between mt-4">
                                <button @click="prevAddress" :disabled="currentAddressIndex === 0"
                                    class="p-2 rounded-full bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                                    <i class="fas fa-chevron-left text-gray-600"></i>
                                </button>

                                <!-- Dots Indicator -->
                                <div class="flex gap-2">
                                    <button v-for="(address, index) in addresses" :key="`dot-${index}`"
                                        @click="currentAddressIndex = index"
                                        class="w-3 h-3 rounded-full transition-colors"
                                        :class="index === currentAddressIndex ? 'bg-accent' : 'bg-gray-300 hover:bg-gray-400'"></button>
                                </div>

                                <button @click="nextAddress" :disabled="currentAddressIndex === addresses.length - 1"
                                    class="p-2 rounded-full bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                                    <i class="fas fa-chevron-right text-gray-600"></i>
                                </button>
                            </div>

                            <!-- Address Counter -->
                            <div class="text-center mt-3">
                                <span class="text-sm text-gray-500">
                                    {{ currentAddressIndex + 1 }} / {{ addresses.length }} địa chỉ
                                </span>
                            </div>
                        </div>

                        <!-- No Addresses -->
                        <div v-else class="text-center py-8">
                            <div
                                class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <i class="fas fa-map-marker-alt text-2xl text-gray-400"></i>
                            </div>
                            <h4 class="text-lg font-semibold text-gray-700 mb-2">Chưa có địa chỉ nào</h4>
                            <p class="text-gray-500 mb-4">Bạn chưa có địa chỉ giao hàng nào. Vui lòng thêm địa chỉ mới.
                            </p>
                            <button @click="activeTab = 'add'"
                                class="px-6 py-2 bg-accent hover:bg-accent-dark text-white rounded-lg font-medium transition-colors">
                                <i class="fas fa-plus mr-2"></i>Thêm địa chỉ mới
                            </button>
                        </div>
                    </div>

                    <!-- Add Address Tab -->
                    <div v-if="activeTab === 'add'">
                        <h4 class="text-lg font-semibold text-gray-900 mb-4">Thêm địa chỉ giao hàng mới</h4>
                        <AddAddressForm @address-saved="handleAddressSaved" @cancel="activeTab = 'list'" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Guest User Information Form -->
        <div v-else class="mb-8">
            <h3 class="text-xl font-bold text-gray-900 mb-4">Thông tin giao hàng</h3>
            <div class="bg-white border border-gray-200 rounded-xl p-6">
                <form @submit.prevent="validateAndNext">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label for="customerName" class="block text-sm font-medium text-gray-700 mb-2">
                                Họ và tên <span class="text-red-500">*</span>
                            </label>
                            <input id="customerName" v-model="localCustomerInfo.ten" @blur="touched.ten = true" maxlength="50" type="text" required
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
                                placeholder="Nguyễn Văn A" />
                            <p v-if="showError('ten')" class="mt-1 text-sm text-red-600">{{ guestErrors.ten }}</p>
                        </div>
                        <div>
                            <label for="customerPhone" class="block text-sm font-medium text-gray-700 mb-2">
                                Số điện thoại <span class="text-red-500">*</span>
                            </label>
                            <input id="customerPhone" v-model="localCustomerInfo.soDienThoai" @blur="touched.soDienThoai = true" maxlength="10" type="tel" required
                                pattern="[0-9]{10}"
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
                                placeholder="0123456789" />
                            <p v-if="showError('soDienThoai')" class="mt-1 text-sm text-red-600">{{ guestErrors.soDienThoai }}</p>
                        </div>
                        <div>
                            <label for="customerEmail" class="block text-sm font-medium text-gray-700 mb-2">
                                Email <span class="text-red-500">*</span>
                            </label>
                            <input id="customerEmail" v-model="localCustomerInfo.email" @blur="touched.email = true" maxlength="100" type="email" required
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
                                placeholder="example@domain.com" />
                            <p v-if="showError('email')" class="mt-1 text-sm text-red-600">{{ guestErrors.email }}</p>
                        </div>
                        <div>
                            <label for="customerAddress" class="block text-sm font-medium text-gray-700 mb-2">
                                Địa chỉ <span class="text-red-500">*</span>
                            </label>
                            <input id="customerAddress" v-model="localCustomerInfo.diaChi" @blur="touched.diaChi = true" maxlength="255" type="text" required
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors"
                                placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố" />
                            <p v-if="showError('diaChi')" class="mt-1 text-sm text-red-600">{{ guestErrors.diaChi }}</p>
                        </div>
                        <div class="md:col-span-2">
                            <label for="customerNote" class="block text-sm font-medium text-gray-700 mb-2">
                                Ghi chú (không bắt buộc)
                            </label>
                            <textarea id="customerNote" v-model="localCustomerInfo.ghiChu" rows="3"
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-accent focus:border-accent transition-colors resize-none"
                                placeholder="Ghi chú thêm về đơn hàng..."></textarea>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Add Address Modal -->
        <div v-if="showAddAddressForm"
            class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
            <div class="bg-white rounded-2xl p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                <div class="flex items-center justify-between mb-6">
                    <h3 class="text-xl font-bold text-gray-900">Thêm địa chỉ mới</h3>
                    <button @click="closeAddAddressForm"
                        class="w-8 h-8 bg-gray-100 hover:bg-gray-200 rounded-lg flex items-center justify-center text-gray-600 transition-colors">
                        <i class="fas fa-times"></i>
                    </button>
                </div>

                <AddAddressForm @address-saved="handleAddressSaved" @cancel="closeAddAddressForm" />
            </div>
        </div>

        <!-- Navigation Buttons -->
        <div class="mt-6 pt-4 border-t border-gray-200">
            <div class="flex items-center justify-between">
                <button @click="$emit('prev-step')"
                    class="px-4 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors font-medium opacity-50 cursor-not-allowed"
                    disabled>
                    <i class="fas fa-arrow-left mr-2"></i>Quay lại
                </button>

                <div class="flex items-center gap-3">
                    <!-- Continue with validation -->
                    <button @click="validateAndNext" :disabled="!canProceed"
                        class="px-6 py-2 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors">
                        <i class="fas fa-arrow-right mr-2"></i>Tiếp tục
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { customerAPI } from '../../api/customerAPI.js'
import AddAddressForm from './AddAddressForm.vue'
import { validateHoTen, validateSoDienThoai, validateEmail, validateDiaChi } from '../../utils/validators.js'

const props = defineProps({
    customerInfo: {
        type: Object,
        required: true
    },
    selectedAddress: {
        type: Object,
        default: null
    },
    isAuthenticated: {
        type: Boolean,
        required: true
    }
})

const emit = defineEmits(['update:customerInfo', 'update:selectedAddress', 'update:customerId', 'next-step', 'prev-step'])

const toast = useToast()

// Local reactive data
const localCustomerInfo = ref({ ...props.customerInfo })
const addresses = ref([])
const loadingAddresses = ref(false)
const showAddressSelection = ref(false)
const showAddAddressForm = ref(false)
const activeTab = ref('list')
const currentAddressIndex = ref(0)

// Lỗi từng ô của form khách không đăng nhập (trước đây chỉ kiểm tra không rỗng)
const touched = ref({ ten: false, soDienThoai: false, email: false, diaChi: false })
const guestErrors = computed(() => ({
    ten: validateHoTen(localCustomerInfo.value.ten),
    soDienThoai: validateSoDienThoai(localCustomerInfo.value.soDienThoai),
    email: validateEmail(localCustomerInfo.value.email),
    diaChi: validateDiaChi(localCustomerInfo.value.diaChi)
}))
const showError = (field) => touched.value[field] && guestErrors.value[field]

// Computed properties
const canProceed = computed(() => {
    if (props.isAuthenticated) {
        return props.selectedAddress !== null
    } else {
        return Object.values(guestErrors.value).every(e => !e)
    }
})

// Methods
const loadAddresses = async () => {
    if (!props.isAuthenticated) return

    loadingAddresses.value = true
    try {
        const response = await customerAPI.getAddresses()
        addresses.value = response.data || []

        // Auto-select default address if available
        const defaultAddress = addresses.value.find(addr => addr.macDinh)
        if (defaultAddress && !props.selectedAddress) {
            selectAddress(defaultAddress)
        }
    } catch (error) {
        console.error('Error loading addresses:', error)
        toast.error('Không thể tải danh sách địa chỉ')
    } finally {
        loadingAddresses.value = false
    }
}

const selectAddress = (address) => {
    emit('update:selectedAddress', address)
    showAddressSelection.value = false
}

const validateAndNext = () => {
    if (!canProceed.value) {
        if (!props.isAuthenticated) {
            touched.value = { ten: true, soDienThoai: true, email: true, diaChi: true }
            const firstError = Object.values(guestErrors.value).find(e => e)
            toast.warning(firstError || 'Vui lòng điền đầy đủ thông tin bắt buộc')
        } else {
            toast.warning('Vui lòng chọn địa chỉ giao hàng')
        }
        return
    }

    // Update customer info for all users
    if (!props.isAuthenticated) {
        // For guest users, use form data
        emit('update:customerInfo', { ...localCustomerInfo.value })
    } else if (props.selectedAddress) {
        // For authenticated users with selected address, use address data
        console.log('Selected address in validateAndNext:', props.selectedAddress)
        console.log('Address ten field:', props.selectedAddress.ten)

        // Get email from user profile if not in address
        const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
        
        const customerData = {
            ten: props.selectedAddress.tenKhachHang || props.selectedAddress.ten,
            soDienThoai: props.selectedAddress.soDienThoai,
            email: props.selectedAddress.email || userInfo.email || '',
            diaChi: `${props.selectedAddress.diaChiCuThe}, ${props.selectedAddress.phuong}, ${props.selectedAddress.quan}, ${props.selectedAddress.thanhPho}`
        }

        console.log('Emitting customer data:', customerData)
        emit('update:customerInfo', customerData)
    }

    emit('next-step')
}

const handleAddressSaved = (newAddress) => {
    addresses.value.push(newAddress)
    selectAddress(newAddress)
    activeTab.value = 'list'
    toast.success('Đã thêm địa chỉ mới thành công')
}

const closeAddAddressForm = () => {
    showAddAddressForm.value = false
}

// Slideshow methods
const nextAddress = () => {
    if (currentAddressIndex.value < addresses.value.length - 1) {
        currentAddressIndex.value++
    }
}

const prevAddress = () => {
    if (currentAddressIndex.value > 0) {
        currentAddressIndex.value--
    }
}

// Watch for changes in customer info prop
watch(() => props.customerInfo, (newValue) => {
    localCustomerInfo.value = { ...newValue }
}, { deep: true })

// Load addresses and emit customer ID on mount for authenticated users
onMounted(async () => {
    if (props.isAuthenticated) {
        loadAddresses()
        
        // Get and emit customer ID for voucher system
        try {
            const response = await customerAPI.getProfile()
            console.log('CustomerInfoStep - Full profile response:', response)
            
            // Backend returns { success: true, data: profile }
            const profileData = response.data || response
            console.log('CustomerInfoStep - Profile data:', profileData)
            
            // Try different possible field names for customer ID
            const customerId = profileData.id || profileData.customerId || profileData.idKhachHang || profileData.khachHangId
            console.log('CustomerInfoStep - Customer ID from profile:', customerId)
            
            if (customerId) {
                emit('update:customerId', customerId)
            } else {
                console.warn('No customer ID found in profile response')
            }
        } catch (error) {
            console.error('Error getting customer ID:', error)
        }
    }
})
</script>
