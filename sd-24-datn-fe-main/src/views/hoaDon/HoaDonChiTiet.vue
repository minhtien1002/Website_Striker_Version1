<template>
  <div class="ui-page">
    <div v-if="loading" class="ui-panel ui-empty">Đang tải...</div>
    <div v-else-if="error" class="ui-panel ui-empty">{{ error }}</div>

    <template v-else-if="hd">
      <div class="ui-header">
        <div>
          <h1 class="ui-title">Hóa đơn {{ hd.maHoaDon }}</h1>
          <p class="ui-sub">
            {{ isTaiQuay ? 'Bán tại quầy' : 'Đơn online' }} · Tạo lúc {{ formatDate(hd.ngayTao, true) }} ·
            <span class="ui-badge on">{{ getStatusName(hd.trangThai) }}</span>
          </p>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <button class="ui-btn" @click="router.push('/hoa-don')"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Danh sách</button>
          <button class="ui-btn" @click="printPdf"><iconify-icon icon="solar:printer-linear"></iconify-icon> In hóa đơn</button>
          <button v-if="nextStatus !== null && !isTaiQuay" class="ui-btn primary" :disabled="busy" @click="advance">
            Chuyển sang "{{ getStatusName(nextStatus) }}"
          </button>
          <button v-if="canMarkDeliveryFailed(hd.trangThai)" class="ui-btn" :disabled="busy" @click="failDelivery">Giao thất bại</button>
          <button v-if="canRestock(hd.trangThai)" class="ui-btn" :disabled="busy" @click="restock">Hoàn kho</button>
          <button v-if="canCancelOrder(hd.trangThai) && !isTaiQuay" class="ui-btn" :disabled="busy" @click="cancel">Hủy đơn</button>
          <button v-if="canConfirmReturn(hd.trangThai)" class="ui-btn" :disabled="busy" @click="confirmReturn">Xác nhận hoàn hàng</button>
          <button v-if="canRejectReturn(hd.trangThai)" class="ui-btn" :disabled="busy" @click="rejectReturn">Từ chối hoàn hàng</button>
        </div>
      </div>

      <!-- Tiến trình -->
      <div class="ui-panel" v-if="!isTaiQuay">
        <h3 class="ui-panel-title">Trạng thái đơn hàng</h3>
        <div class="steps">
          <div v-for="s in steps" :key="s" class="step" :class="{ done: stepDone(s), current: Number(hd.trangThai) === s }">
            <div class="dot">{{ STATUS_FLOW.indexOf(s) + 1 }}</div>
            <div class="name">{{ getStatusName(s) }}</div>
          </div>
        </div>
        <p v-if="[6, 7, 8, 9, 10].includes(Number(hd.trangThai))" class="ui-hint" style="margin-top:10px">
          Đơn đang ở nhánh: <strong>{{ getStatusName(hd.trangThai) }}</strong>
          <span v-if="lyDoGanNhat"> · {{ lyDoGanNhat }}</span>
        </p>
      </div>

      <div class="ui-grid-2" style="align-items:start">
        <div class="ui-panel">
          <h3 class="ui-panel-title">Khách hàng</h3>
          <dl class="ui-dl">
            <dt>Tên khách hàng</dt><dd>{{ hd.tenKhachHang || 'Khách lẻ' }}</dd>
            <dt>Số điện thoại</dt><dd>{{ hd.soDienThoaiKhachHang || '—' }}</dd>
            <dt>Email</dt><dd>{{ hd.email || '—' }}</dd>
            <dt>Địa chỉ</dt><dd>{{ hd.diaChiKhachHang || '—' }}</dd>
            <dt>Ghi chú</dt><dd style="font-weight:500">{{ hd.ghiChu || '—' }}</dd>
          </dl>
        </div>
        <div class="ui-panel">
          <h3 class="ui-panel-title">Thanh toán</h3>
          <dl class="ui-dl">
            <dt>Nhân viên thanh toán</dt><dd>{{ hd.tenNhanVien ? `${hd.tenNhanVien} (${hd.maNhanVien})` : '—' }}</dd>
            <dt>Ngày thanh toán</dt><dd>{{ formatDate(hd.ngayThanhToan, true) }}</dd>
            <dt>Hình thức</dt>
            <dd>
              <div v-for="(t, i) in hd.thanhToanInfos" :key="i">{{ t.kieuThanhToan }}: {{ formatMoney(Number(t.tienMat || 0) + Number(t.tienChuyenKhoan || 0)) }}</div>
              <span v-if="!hd.thanhToanInfos?.length">—</span>
            </dd>
            <dt>Tiền hàng</dt><dd>{{ formatMoney(hd.tienSanPham) }}</dd>
            <dt>Phí vận chuyển</dt><dd>{{ formatMoney(hd.phiVanChuyen || 0) }}</dd>
            <dt>Giảm giá</dt><dd>{{ hd.tienGiam > 0 ? `- ${formatMoney(hd.tienGiam)}` : '—' }}<span v-if="hd.maGiamGia" class="ui-muted"> (phiếu {{ hd.maGiamGia }})</span></dd>
            <dt>Tổng thanh toán</dt><dd class="ui-accent" style="font-size:1.15rem">{{ formatMoney(hd.tongTienSauGiam) }}</dd>
          </dl>
        </div>
      </div>

      <div class="ui-panel">
        <h3 class="ui-panel-title">Sản phẩm ({{ hd.sanPhamChiTietInfos?.length || 0 }})</h3>
        <div class="ui-table-wrap">
          <table class="ui-table">
            <thead>
              <tr><th style="width:60px">Ảnh</th><th>Mã sản phẩm</th><th>Mã chi tiết</th><th>Tên sản phẩm</th><th>Thương hiệu</th><th>Màu / Size</th><th class="num">SL</th><th class="num">Đơn giá</th><th class="num">Thành tiền</th></tr>
            </thead>
            <tbody>
              <tr v-for="p in hd.sanPhamChiTietInfos" :key="p.hoaDonChiTietId">
                <td><img v-if="p.duongDan" :src="imageUrl(p.duongDan)" class="ui-thumb" alt="" /><div v-else class="ui-thumb"></div></td>
                <td>{{ p.maSanPham }}</td>
                <td><strong>{{ p.maChiTietSanPham }}</strong></td>
                <td>{{ p.tenSanPham }}<div class="ui-hint">{{ p.theLoai }} · {{ p.chatLieu }}</div></td>
                <td>{{ p.thuongHieu || '—' }}</td>
                <td>{{ p.mauSac }} / {{ p.kichCo }}</td>
                <td class="num">{{ p.soLuong }}</td>
                <td class="num">{{ formatMoney(p.giaBan) }}</td>
                <td class="num">{{ formatMoney(Number(p.giaBan) * Number(p.soLuong)) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="ui-panel">
        <h3 class="ui-panel-title">Lịch sử đơn hàng</h3>
        <table class="ui-table">
          <thead><tr><th style="width:160px">Thời gian</th><th>Thao tác</th><th>Ghi chú / lý do</th><th>Người thực hiện</th><th>Trạng thái sau</th></tr></thead>
          <tbody>
            <tr v-for="(h, i) in history" :key="i">
              <td>{{ formatDate(h.thoiGian, true) }}</td>
              <td>{{ h.hanhDong }}</td>
              <td class="ui-muted">{{ h.ghiChu || '—' }}</td>
              <td>{{ h.tenNhanVien ? `${h.tenNhanVien} (${h.maNhanVien})` : 'Hệ thống / khách hàng' }}</td>
              <td>{{ getStatusName(h.trangThai) }}</td>
            </tr>
            <tr v-if="!history.length"><td colspan="5" class="ui-empty">Chưa có lịch sử</td></tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import authService from '@/services/authService.js'
import { useConfirm } from '@/composables/useConfirm.js'
import {
  getHoaDonDetail, updateHoaDonStatus, cancelHoaDon, confirmReturnHoaDon, rejectReturnHoaDon, markDeliveryFailed, restockFailedDelivery
} from '@/services/api/APIHoaDon/HoaDonChiTietAPI.js'
import { downloadInvoicePDF } from '@/services/api/APIHoaDon/HoaDonAPI.js'
import {
  STATUS_FLOW, getStatusName, getNextStatus, isStockDeducted, normalizeLoaiDon,
  canCancelOrder, canConfirmReturn, canRejectReturn, canMarkDeliveryFailed, canRestock
} from '@/services/hoaDonService/hoaDonStatus.js'
import { errorMessage, formatDate, formatMoney, imageUrl } from '@/services/api/productAPI.js'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()
const nhanVienId = authService.getNhanVienId()

const hd = ref(null)
const loading = ref(true)
const error = ref('')
const busy = ref(false)

const isTaiQuay = computed(() => normalizeLoaiDon(hd.value?.loaiDon) === 'Tại quầy')
const nextStatus = computed(() => getNextStatus(hd.value?.trangThai))
const steps = STATUS_FLOW
const history = computed(() => [...(hd.value?.lichSuHoaDonInfos || [])].sort((a, b) => new Date(a.thoiGian) - new Date(b.thoiGian)))
const lyDoGanNhat = computed(() => [...history.value].reverse().find((h) => h.ghiChu)?.ghiChu || '')

const stepDone = (s) => {
  const cur = Number(hd.value.trangThai)
  if (cur === 9 || cur === 10) return STATUS_FLOW.indexOf(s) <= STATUS_FLOW.indexOf(4)
  if (cur === 7 || cur === 8) return true
  const i = STATUS_FLOW.indexOf(cur)
  return i >= 0 && STATUS_FLOW.indexOf(s) <= i
}

const load = async () => {
  try {
    hd.value = await getHoaDonDetail(route.params.id)
  } catch (e) {
    error.value = errorMessage(e, 'Không tìm thấy hóa đơn')
  } finally {
    loading.value = false
  }
}

const run = async (fn, okMsg) => {
  busy.value = true
  try { await fn(); toast.success(okMsg); await load() }
  catch (e) { toast.error(errorMessage(e)) }
  finally { busy.value = false }
}

const advance = async () => {
  const to = nextStatus.value
  const truKho = !isStockDeducted(hd.value.trangThai) && isStockDeducted(to)
  const ok = await confirm({
    title: `Chuyển đơn sang "${getStatusName(to)}"?`,
    message: truKho ? 'Tồn kho của các sản phẩm trong đơn sẽ bị trừ ngay sau bước này.' : 'Trạng thái không thể lùi lại sau khi chuyển.',
    details: [`Hóa đơn ${hd.value.maHoaDon}`, `${getStatusName(hd.value.trangThai)} → ${getStatusName(to)}`],
    confirmText: 'Xác nhận'
  })
  if (!ok) return
  run(() => updateHoaDonStatus(hd.value.id, to, nhanVienId), `Đã chuyển sang "${getStatusName(to)}"`)
}

const failDelivery = async () => {
  const lyDo = await confirm({
    title: 'Giao hàng thất bại?',
    message: 'Hàng chưa về kho; khi hàng về hãy bấm "Hoàn kho" để cộng lại tồn kho, hoặc chuyển giao lại.',
    requireReason: true, reasonLabel: 'Lý do giao thất bại', reasonPlaceholder: 'VD: Khách không nghe máy 3 lần, sai địa chỉ...',
    confirmText: 'Xác nhận giao thất bại', tone: 'warning'
  })
  if (!lyDo) return
  if (lyDo.length < 5) { toast.error('Lý do giao thất bại tối thiểu 5 ký tự'); return }
  run(() => markDeliveryFailed(hd.value.id, nhanVienId, lyDo), 'Đã chuyển sang Giao thất bại')
}

const restock = async () => {
  const ok = await confirm({
    title: 'Hoàn kho đơn giao thất bại?',
    message: 'Chỉ xác nhận khi hàng đã về đến kho. Tồn kho sẽ được cộng lại và số lượng đã bán được trừ đi.',
    details: (hd.value.sanPhamChiTietInfos || []).map((p) => `${p.maChiTietSanPham}: +${p.soLuong}`),
    confirmText: 'Hoàn kho', tone: 'warning'
  })
  if (!ok) return
  run(() => restockFailedDelivery(hd.value.id, nhanVienId), 'Đã hoàn kho')
}

const cancel = async () => {
  const lyDo = await confirm({
    title: 'Hủy đơn hàng?',
    message: isStockDeducted(hd.value.trangThai) ? 'Tồn kho sẽ được cộng lại. Thao tác không thể hoàn tác.' : 'Đơn chưa trừ kho nên tồn kho không đổi. Thao tác không thể hoàn tác.',
    requireReason: true, reasonLabel: 'Lý do hủy', confirmText: 'Hủy đơn', tone: 'danger'
  })
  if (!lyDo) return
  run(() => cancelHoaDon(hd.value.id, nhanVienId, lyDo), 'Đã hủy đơn hàng')
}

const confirmReturn = async () => {
  const ok = await confirm({ title: 'Xác nhận đã nhận hàng hoàn?', message: 'Tồn kho sẽ được cộng lại ngay.', confirmText: 'Xác nhận', tone: 'warning' })
  if (!ok) return
  run(() => confirmReturnHoaDon(hd.value.id, nhanVienId), 'Đã xác nhận hoàn hàng')
}

const rejectReturn = async () => {
  const lyDo = await confirm({ title: 'Từ chối yêu cầu hoàn hàng?', requireReason: true, reasonLabel: 'Lý do từ chối', confirmText: 'Từ chối' })
  if (!lyDo) return
  run(() => rejectReturnHoaDon(hd.value.id, nhanVienId, lyDo), 'Đã từ chối hoàn hàng')
}

const printPdf = async () => {
  try {
    const blob = await downloadInvoicePDF(hd.value.id)
    const url = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
    window.open(url, '_blank')
  } catch (e) {
    toast.error(errorMessage(e, 'Không in được hóa đơn'))
  }
}

onMounted(load)
</script>

<style scoped>
.steps { display: flex; gap: 0; }
.step { flex: 1; display: flex; flex-direction: column; align-items: flex-start; gap: 6px; padding-top: 8px; border-top: 3px solid var(--line); }
.step.done { border-top-color: var(--accent); }
.step .dot { width: 26px; height: 26px; display: flex; align-items: center; justify-content: center; background: var(--muted); border: 1px solid var(--line); font-weight: 700; font-size: .8rem; }
.step.done .dot { background: var(--accent); color: #fff; border-color: var(--accent); }
.step .name { font-size: .85rem; font-weight: 600; }
.step.current .name { color: var(--accent); }
</style>
