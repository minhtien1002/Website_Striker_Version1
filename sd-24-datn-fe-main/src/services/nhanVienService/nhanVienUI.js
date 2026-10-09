// UI state management for NhanVien components
import { ref } from 'vue'
import { nhanVienConfig } from './nhanVienConfig.js'

export const useNhanVienUI = () => {
  // Modal states
  const showAddModal = ref(false)
  const showEditModal = ref(false)
  const showDetailModal = ref(false)
  
  // Loading states
  const isUpdating = ref(false)
  const isDeleting = ref(false)
  
  // Data states
  const viewingEmployee = ref({})
  const editingEmployee = ref({})
  const highlightedEmployee = ref(null)
  const errorMessage = ref('')

  // Modal actions
  const openAddModal = (resetForm) => {
    resetForm()
    showAddModal.value = true
  }

  const openEditModal = (employee) => {
    editingEmployee.value = { ...employee }
    showEditModal.value = true
  }

  const openDetailModal = (employee) => {
    viewingEmployee.value = employee
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

  // Employee highlighting
  const highlightEmployee = (employeeId) => {
    highlightedEmployee.value = employeeId
    setTimeout(() => {
      highlightedEmployee.value = null
    }, 3000)
  }

  return {
    // States
    showAddModal,
    showEditModal,
    showDetailModal,
    isUpdating,
    isDeleting,
    viewingEmployee,
    editingEmployee,
    highlightedEmployee,
    errorMessage,
    
    // Actions
    openAddModal,
    openEditModal,
    openDetailModal,
    closeAllModals,
    setLoading,
    setError,
    clearError,
    highlightEmployee
  }
}
