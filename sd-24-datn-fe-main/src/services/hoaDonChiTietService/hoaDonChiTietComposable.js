/**
 * Composable for HoaDonChiTiet functionality
 */

import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';
import { useHoaDonLogic } from '@/services/hoaDonService/hoaDonComposable.js';
import {
  fetchInvoiceDetail,
  updateOrderStatusAPI,
  updateToStatusAPI,
  cancelOrderAPI,
  confirmReturnAPI,
  rejectReturnAPI
} from './hoaDonChiTietCRUD.js';
import {
  formatShortDate,
  getOrderStatuses,
  getProgressWidth,
  getStatusStepClass,
  getStatusDate,
  canUpdateToStatus,
  getAvailableStatuses,
  printInvoice
} from './hoaDonChiTietUI.js';
import { breadcrumbItems } from './hoaDonChiTietConfig.js';
import {
  canCancelOrder,
  canConfirmReturn,
  canRejectReturn,
  getStatusName,
  isStockDeducted
} from '@/services/hoaDonService/hoaDonStatus.js';
import { useConfirm } from '@/composables/useConfirm.js';

export function useHoaDonChiTietLogic() {
  const route = useRoute();
  const router = useRouter();
  const toast = useToast();
  const { confirm } = useConfirm();

  // Use the existing HoaDon composable for detail functionality
  const {
    invoiceDetail,
    isLoadingDetail,
    detailError,
    loadInvoiceDetail,
    clearInvoiceDetail,
    formatDate,
    formatCurrency,
    getStatusText,
    getStatusColor
  } = useHoaDonLogic();

  // Local state
  const invoiceId = ref(route.params.id);
  const showStatusModal = ref(false);
  const newStatus = ref('');
  const statusNote = ref('');

  // Cancel / return flow state
  const showCancelModal = ref(false);
  const showReturnConfirmModal = ref(false);
  const showReturnRejectModal = ref(false);
  const cancelReason = ref('');
  const returnNote = ref('');
  const rejectReason = ref('');
  const isProcessingAction = ref(false);

  // Computed properties
  const availableStatuses = computed(() => getAvailableStatuses(invoiceDetail.value));
  const canCancel = computed(() => !!invoiceDetail.value && canCancelOrder(invoiceDetail.value.trangThai?.id));
  const canConfirmReturnNow = computed(() => !!invoiceDetail.value && canConfirmReturn(invoiceDetail.value.trangThai?.id));
  const canRejectReturnNow = computed(() => !!invoiceDetail.value && canRejectReturn(invoiceDetail.value.trangThai?.id));
  /** Đơn này hiện có đang bị trừ tồn kho hay không - quyết định câu chữ ở modal hủy đơn. */
  const stockAlreadyDeducted = computed(() => isStockDeducted(invoiceDetail.value?.trangThai?.id));

  // Methods
  const fetchInvoiceDetailData = async () => {
    try {
      await fetchInvoiceDetail(invoiceId.value, loadInvoiceDetail);
    } catch (error) {
      console.error('Error loading invoice detail:', error);
    }
  };

  /**
   * Dựng nội dung hộp thoại xác nhận cho một lần chuyển trạng thái. Nêu rõ ảnh hưởng tồn
   * kho vì "Đã xác nhận" là điểm DUY NHẤT trừ kho - người dùng cần biết trước khi bấm.
   */
  const buildStatusConfirmOptions = (targetStatusId) => {
    const target = Number(targetStatusId);
    const currentStatusId = Number(invoiceDetail.value?.trangThai?.id);
    const willDeductStock = !isStockDeducted(currentStatusId) && isStockDeducted(target);

    const details = [
      `Mã hóa đơn: ${invoiceDetail.value?.maHoaDon || invoiceId.value}`,
      `Trạng thái hiện tại: ${getStatusName(currentStatusId)}`,
      `Trạng thái mới: ${getStatusName(target)}`
    ];
    if (willDeductStock) {
      details.push('Tồn kho của các sản phẩm trong đơn sẽ bị TRỪ ngay sau bước này.');
    }

    return {
      title: `Chuyển sang "${getStatusName(target)}"?`,
      message: willDeductStock
        ? 'Đây là bước chốt đơn. Nếu kho không đủ hàng, hệ thống sẽ từ chối và giữ nguyên trạng thái.'
        : 'Trạng thái đơn hàng sẽ được cập nhật và ghi vào lịch sử hóa đơn.',
      details,
      confirmText: 'Xác nhận',
      tone: willDeductStock ? 'warning' : 'info'
    };
  };

  const updateOrderStatus = async () => {
    if (!newStatus.value) {
      toast.error('Vui lòng chọn trạng thái mới!');
      return;
    }

    if (!(await confirm(buildStatusConfirmOptions(newStatus.value)))) return;

    try {
      const result = await updateOrderStatusAPI(invoiceId.value, newStatus.value, statusNote.value);
      
      if (result.success) {
        // Update local state with new status
        if (invoiceDetail.value) {
          invoiceDetail.value.trangThai = {
            id: result.status.id,
            ten: result.status.name
          };
        }
        
        // Refresh invoice detail to get updated data
        await fetchInvoiceDetailData();
        
        showStatusModal.value = false;
        newStatus.value = '';
        statusNote.value = '';
        
        toast.success('Cập nhật trạng thái đơn hàng thành công!');
      }
    } catch (error) {
      // Backend trả lý do cụ thể dưới dạng text (vd "Không đủ tồn kho để xác nhận...").
      // Hiện nguyên văn để người dùng biết phải nhập thêm hàng, thay vì báo chung chung.
      toast.error(error.response?.data || 'Lỗi khi cập nhật trạng thái!');
      console.error(error);
    }
  };

  const updateToStatus = async (status) => {
    const statusId = typeof status === 'object' ? status.id : status;
    if (!(await confirm(buildStatusConfirmOptions(statusId)))) return;

    try {
      const result = await updateToStatusAPI(invoiceId.value, status);
      
      if (result.success) {
        // Update local state with new status
        if (invoiceDetail.value) {
          invoiceDetail.value.trangThai = {
            id: result.status.id,
            ten: result.status.name
          };
        }
        
        // Refresh invoice detail to get updated data
        await fetchInvoiceDetailData();
        
        toast.success(result.message);
      }
    } catch (error) {
      // Backend trả lý do cụ thể dưới dạng text (vd "Không đủ tồn kho để xác nhận...").
      // Hiện nguyên văn để người dùng biết phải nhập thêm hàng, thay vì báo chung chung.
      toast.error(error.response?.data || 'Lỗi khi cập nhật trạng thái!');
      console.error(error);
    }
  };

  const goBack = () => {
    router.push({ name: 'HoaDon' });
  };

  // Hủy đơn hàng - restores stock immediately (order never left the warehouse for real).
  const handleCancelOrder = async () => {
    if (!cancelReason.value.trim()) {
      toast.error('Vui lòng nhập lý do hủy đơn');
      return;
    }
    isProcessingAction.value = true;
    try {
      // Ghi lại trạng thái TRƯỚC khi hủy: sau lời gọi đơn đã sang "Đã hủy" nên không còn
      // suy ra được đơn này có từng bị trừ kho hay không để báo cho đúng.
      const trangThaiTruocKhiHuy = Number(invoiceDetail.value?.trangThai?.id);
      const result = await cancelOrderAPI(invoiceId.value, null, cancelReason.value.trim(), trangThaiTruocKhiHuy);
      if (result.success) {
        await fetchInvoiceDetailData();
        showCancelModal.value = false;
        cancelReason.value = '';
        toast.success(result.message);
      }
    } catch (error) {
      toast.error(error.response?.data || 'Lỗi khi hủy đơn hàng!');
      console.error(error);
    } finally {
      isProcessingAction.value = false;
    }
  };

  // Xác nhận hoàn hàng - hàng đã thực sự về kho, đây là lúc cộng lại tồn kho.
  const handleConfirmReturn = async () => {
    isProcessingAction.value = true;
    try {
      const result = await confirmReturnAPI(invoiceId.value, null, returnNote.value.trim());
      if (result.success) {
        await fetchInvoiceDetailData();
        showReturnConfirmModal.value = false;
        returnNote.value = '';
        toast.success(result.message);
      }
    } catch (error) {
      toast.error(error.response?.data || 'Lỗi khi xác nhận hoàn hàng!');
      console.error(error);
    } finally {
      isProcessingAction.value = false;
    }
  };

  // Từ chối yêu cầu hoàn hàng của khách.
  const handleRejectReturn = async () => {
    if (!rejectReason.value.trim()) {
      toast.error('Vui lòng nhập lý do từ chối');
      return;
    }
    isProcessingAction.value = true;
    try {
      const result = await rejectReturnAPI(invoiceId.value, null, rejectReason.value.trim());
      if (result.success) {
        await fetchInvoiceDetailData();
        showReturnRejectModal.value = false;
        rejectReason.value = '';
        toast.success(result.message);
      }
    } catch (error) {
      toast.error(error.response?.data || 'Lỗi khi từ chối yêu cầu hoàn hàng!');
      console.error(error);
    } finally {
      isProcessingAction.value = false;
    }
  };

  // Print invoice wrapper function
  const handlePrintInvoice = () => {
    if (invoiceDetail.value) {
      printInvoice(invoiceDetail.value);
    } else {
      console.error('No invoice detail available for printing');
    }
  };

  // Breadcrumb actions
  const breadcrumbActions = ref([
    {
      label: 'Quay lại',
      type: 'default',
      handler: goBack
    },
    {
      label: 'In hóa đơn',
      type: 'primary',
      handler: handlePrintInvoice
    }
  ]);

  // UI helper functions
  const getOrderStatusesForInvoice = () => getOrderStatuses(invoiceDetail.value);
  const getProgressWidthForInvoice = () => getProgressWidth(invoiceDetail.value);
  const getStatusStepClassForInvoice = (status, index) => getStatusStepClass(status, index, invoiceDetail.value);
  const getStatusDateForInvoice = (status) => getStatusDate(status, invoiceDetail.value);
  const canUpdateToStatusForInvoice = (status) => canUpdateToStatus(status, invoiceDetail.value);

  return {
    // State
    invoiceDetail,
    isLoadingDetail,
    detailError,
    invoiceId,
    showStatusModal,
    newStatus,
    statusNote,
    showCancelModal,
    showReturnConfirmModal,
    showReturnRejectModal,
    cancelReason,
    returnNote,
    rejectReason,
    isProcessingAction,

    // Computed
    availableStatuses,
    canCancel,
    canConfirmReturnNow,
    canRejectReturnNow,
    stockAlreadyDeducted,

    // Methods
    fetchInvoiceDetailData,
    updateOrderStatus,
    updateToStatus,
    handleCancelOrder,
    handleConfirmReturn,
    handleRejectReturn,
    goBack,
    
    // UI helpers
    formatDate,
    formatCurrency,
    formatShortDate,
    getStatusText,
    getStatusColor,
    getOrderStatusesForInvoice,
    getProgressWidthForInvoice,
    getStatusStepClassForInvoice,
    getStatusDateForInvoice,
    canUpdateToStatusForInvoice,
    printInvoice: handlePrintInvoice,
    
    // Configuration
    breadcrumbItems,
    breadcrumbActions
  };
}
