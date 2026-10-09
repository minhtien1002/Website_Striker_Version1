// UI state management for KhachHang components
import { ref } from 'vue'
import { khachHangConfig } from './khachHangConfig.js'

export const useKhachHangUI = () => {
  // Modal states
  const showAddModal = ref(false)
  const showEditModal = ref(false)
  const showDetailModal = ref(false)
  
  // Loading states
  const isUpdating = ref(false)
  const isDeleting = ref(false)
  
  // Data states
  const viewingCustomer = ref({})
  const editingCustomer = ref({})
  const highlightedCustomer = ref(null)
  const errorMessage = ref('')

  // Modal actions
  const openAddModal = (resetForm) => {
    resetForm()
    showAddModal.value = true
  }

  const openEditModal = (customer) => {
    editingCustomer.value = { ...customer }
    showEditModal.value = true
  }

  const openDetailModal = (customer) => {
    viewingCustomer.value = customer
    showDetailModal.value = true
  }

  const closeAllModals = () => {
    showAddModal.value = false
    showEditModal.value = false
    showDetailModal.value = false
  }

  // Loading states
  const setLoading = (type, value) => {
    if (type === 'updating') {
      isUpdating.value = value
    } else if (type === 'deleting') {
      isDeleting.value = value
    }
  }

  // Error handling
  const setError = (message) => {
    errorMessage.value = message
  }

  const clearError = () => {
    errorMessage.value = ''
  }

  // Customer highlighting
  const highlightCustomer = (customerId) => {
    highlightedCustomer.value = customerId
    setTimeout(() => {
      highlightedCustomer.value = null
    }, 3000)
  }

  return {
    // States
    showAddModal,
    showEditModal,
    showDetailModal,
    isUpdating,
    isDeleting,
    viewingCustomer,
    editingCustomer,
    highlightedCustomer,
    errorMessage,
    
    // Actions
    openAddModal,
    openEditModal,
    openDetailModal,
    closeAllModals,
    setLoading,
    setError,
    clearError,
    highlightCustomer
  }
}
