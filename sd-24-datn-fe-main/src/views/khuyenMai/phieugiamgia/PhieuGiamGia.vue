<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Quản lý phiếu giảm giá</h1>
        <p class="ui-sub">Phiếu giảm giá không thể sửa hay xóa sau khi tạo - chỉ đổi trạng thái.</p>
      </div>
      <button v-if="isAdmin" class="ui-btn primary" @click="router.push('/them-phieu-giam-gia')">
        <iconify-icon icon="solar:add-square-linear"></iconify-icon> Thêm phiếu giảm giá
      </button>
    </div>

    <div class="ui-panel">
      <div class="ui-filters">
        <div class="ui-field">
          <label>Tìm kiếm</label>
          <input v-model="filters.keyword" class="ui-input" placeholder="Mã hoặc tên phiếu" />
        </div>
        <div class="ui-field">
          <label>Tình trạng</label>
          <select v-model="filters.tinhTrang" class="ui-select">
            <option value="">Tất cả</option>
            <option value="SAP_DIEN_RA">Sắp diễn ra</option>
            <option value="DANG_DIEN_RA">Đang diễn ra</option>
            <option value="DA_KET_THUC">Đã kết thúc</option>
            <option value="NGUNG">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Loại giảm</label>
          <select v-model="filters.loai" class="ui-select">
            <option value="">Tất cả</option>
            <option value="PHAN_TRAM">Phần trăm</option>
            <option value="TIEN">Số tiền</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Phạm vi</label>
          <select v-model="filters.phamVi" class="ui-select">
            <option value="">Tất cả</option>
            <option value="cong-khai">Công khai</option>
            <option value="rieng-tu">Riêng tư</option>
          </select>
        </div>
        <div class="ui-field"><button class="ui-btn" @click="resetFilters">Đặt lại bộ lọc</button></div>
      </div>
    </div>

    <div class="ui-panel">
      <div class="ui-table-wrap">
        <table class="ui-table">
          <thead>
            <tr>
              <th style="width:50px">STT</th>
              <th>Mã</th>
              <th>Tên phiếu</th>
              <th>Mức giảm</th>
              <th>Thời gian</th>
              <th class="num">Còn lại</th>
              <th>Tình trạng</th>
              <th style="width:110px">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="8" class="ui-empty">Đang tải...</td></tr>
            <tr v-else-if="!pageItems.length"><td colspan="8" class="ui-empty">Không có phiếu giảm giá phù hợp.</td></tr>
            <tr v-for="(p, i) in pageItems" :key="p.id">
              <td>{{ (page - 1) * pageSize + i + 1 }}</td>
              <td><strong>{{ p.ma }}</strong></td>
              <td>{{ p.tenPhieuGiamGia }}</td>
              <td>{{ mucGiam(p) }}</td>
              <td>{{ formatDate(p.ngayBatDau) }} – {{ formatDate(p.ngayKetThuc) }}</td>
              <td class="num">{{ p.soLuongDung ?? '∞' }}</td>
              <td><span class="ui-badge" :class="{ on: tinhTrang(p) === 'DANG_DIEN_RA' }">{{ TINH_TRANG[tinhTrang(p)] }}</span></td>
              <td>
                <div style="display:flex;gap:6px;align-items:center">
                  <button class="ui-btn icon" title="Xem chi tiết" @click="openDetail(p)"><iconify-icon icon="solar:eye-linear"></iconify-icon></button>
                  <label v-if="isAdmin" class="ui-switch" :title="p.trangThai ? 'Chuyển sang Ngừng hoạt động' : 'Chuyển sang Hoạt động'">
                    <input type="checkbox" :checked="p.trangThai" @change="toggle(p, $event)" /><span></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="ui-pager" v-if="filtered.length > pageSize">
        <span>Hiển thị {{ (page - 1) * pageSize + 1 }}–{{ Math.min(page * pageSize, filtered.length) }} / {{ filtered.length }}</span>
        <div class="pages">
          <button class="ui-btn sm" :disabled="page === 1" @click="page--">Trước</button>
          <button class="ui-btn sm" :disabled="page * pageSize >= filtered.length" @click="page++">Sau</button>
        </div>
      </div>
    </div>

    <!-- Chi tiết đầy đủ -->
    <div v-if="detail" class="ui-modal-overlay" @click.self="detail = null">
      <div class="ui-modal">
        <div class="ui-modal-head">Chi tiết phiếu giảm giá {{ detail.ma }}
          <button class="ui-btn icon" @click="detail = null"><iconify-icon icon="solar:close-square-linear"></iconify-icon></button>
        </div>
        <div class="ui-modal-body">
          <dl class="ui-dl">
            <dt>Mã phiếu</dt><dd>{{ detail.ma }}</dd>
            <dt>Tên phiếu</dt><dd>{{ detail.tenPhieuGiamGia }}</dd>
            <dt>Loại giảm</dt><dd>{{ isPhanTram(detail) ? 'Phần trăm' : 'Số tiền cố định' }}</dd>
            <dt>Mức giảm</dt><dd>{{ mucGiam(detail) }}</dd>
            <dt>Đơn tối thiểu</dt><dd>{{ formatMoney(detail.hoaDonToiThieu || 0) }}</dd>
            <dt>Áp dụng cho</dt><dd>{{ detail.loaiApDung === 'PHI_VAN_CHUYEN' ? 'Phí vận chuyển' : 'Tiền hàng' }}</dd>
            <dt>Số lượng phát hành</dt><dd>{{ detail.soLuongPhatHanh ?? detail.soLuongDung ?? 'Không giới hạn' }}</dd>
            <dt>Số lượt còn lại</dt><dd>{{ detail.soLuongDung ?? 'Không giới hạn' }}</dd>
            <dt>Đã được dùng</dt><dd>{{ detail.soLuotDaDung ?? detail.soLanDaDung ?? 0 }} lần</dd>
            <dt>Giới hạn mỗi khách</dt><dd>{{ detail.gioiHanMoiKhach ?? 'Không giới hạn' }}</dd>
            <dt>Ngày bắt đầu</dt><dd>{{ formatDate(detail.ngayBatDau) }}</dd>
            <dt>Ngày kết thúc</dt><dd>{{ formatDate(detail.ngayKetThuc) }}</dd>
            <dt>Trạng thái</dt><dd>{{ detail.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }} · {{ TINH_TRANG[tinhTrang(detail)] }}</dd>
            <dt>Phạm vi</dt><dd>{{ detail.riengTu ? 'Riêng tư (chỉ khách được tặng)' : 'Công khai' }}</dd>
            <dt>Mô tả</dt><dd style="font-weight:500">{{ detail.moTa || '—' }}</dd>
          </dl>
          <div v-if="detail.riengTu" style="margin-top:14px">
            <h4 class="ui-panel-title">Khách hàng được tặng ({{ detail.customers?.length || 0 }})</h4>
            <table class="ui-table">
              <thead><tr><th>Mã</th><th>Tên</th><th>Email</th><th>SĐT</th></tr></thead>
              <tbody>
                <tr v-for="c in detail.customers" :key="c.idKhachHang"><td>{{ c.ma }}</td><td>{{ c.ten }}</td><td>{{ c.email || '—' }}</td><td>{{ c.soDienThoai || '—' }}</td></tr>
                <tr v-if="!detail.customers?.length"><td colspan="4" class="ui-empty">Chưa có khách hàng</td></tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="ui-modal-foot"><button class="ui-btn" @click="detail = null">Đóng</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import axios from 'axios'
import authService from '@/services/authService.js'
import { useConfirm } from '@/composables/useConfirm.js'
import { errorMessage, formatDate, formatMoney } from '@/services/api/productAPI.js'

const API = 'http://localhost:8080/api/phieu-giam-gia'
const TINH_TRANG = { SAP_DIEN_RA: 'Sắp diễn ra', DANG_DIEN_RA: 'Đang diễn ra', DA_KET_THUC: 'Đã kết thúc', NGUNG: 'Ngừng hoạt động' }

const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()
const isAdmin = authService.isAdmin()

const items = ref([])
const loading = ref(false)
const detail = ref(null)
const filters = reactive({ keyword: '', tinhTrang: '', loai: '', phamVi: '' })
const page = ref(1)
const pageSize = 10

const isPhanTram = (p) => ['PHAN_TRAM', 'PERCENTAGE'].includes((p.loaiPhieuGiamGia || '').toUpperCase())
const soTienCoDinh = (p) => p.soTienGiamToiDa ?? p.phanTramGiamGia
const mucGiam = (p) => isPhanTram(p)
  ? `${p.phanTramGiamGia}%${p.soTienGiamToiDa ? ` (tối đa ${formatMoney(p.soTienGiamToiDa)})` : ''}`
  : formatMoney(soTienCoDinh(p))

const tinhTrang = (p) => {
  if (!p.trangThai) return 'NGUNG'
  const now = new Date()
  if (p.ngayBatDau && new Date(p.ngayBatDau) > now) return 'SAP_DIEN_RA'
  if (p.ngayKetThuc) {
    const end = new Date(p.ngayKetThuc)
    if (String(p.ngayKetThuc).length <= 10) end.setHours(23, 59, 59)
    if (end < now) return 'DA_KET_THUC'
  }
  return 'DANG_DIEN_RA'
}

const normalize = (s) => (s || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
const filtered = computed(() => items.value.filter((p) => {
  const kw = normalize(filters.keyword.trim())
  if (kw && !normalize(p.ma).includes(kw) && !normalize(p.tenPhieuGiamGia).includes(kw)) return false
  if (filters.tinhTrang && tinhTrang(p) !== filters.tinhTrang) return false
  if (filters.loai === 'PHAN_TRAM' && !isPhanTram(p)) return false
  if (filters.loai === 'TIEN' && isPhanTram(p)) return false
  if (filters.phamVi === 'cong-khai' && p.riengTu) return false
  if (filters.phamVi === 'rieng-tu' && !p.riengTu) return false
  return true
}))
const pageItems = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))
watch(filters, () => { page.value = 1 })
const resetFilters = () => Object.assign(filters, { keyword: '', tinhTrang: '', loai: '', phamVi: '' })

const load = async () => {
  loading.value = true
  try { items.value = (await axios.get(API)).data.filter((p) => !p.deleted) }
  catch (e) { toast.error(errorMessage(e, 'Không tải được phiếu giảm giá')) }
  finally { loading.value = false }
}

const openDetail = async (p) => {
  try { detail.value = (await axios.get(`${API}/detail/${p.id}`)).data }
  catch (e) { toast.error(errorMessage(e)) }
}

const toggle = async (p, event) => {
  const tat = p.trangThai
  const ok = await confirm({
    title: tat ? 'Ngừng hoạt động phiếu giảm giá?' : 'Chuyển phiếu sang Hoạt động?',
    message: tat ? 'Khách hàng và nhân viên sẽ không thể áp dụng phiếu này nữa.' : 'Phiếu sẽ áp dụng được trở lại (nếu còn trong thời gian hiệu lực).',
    details: [`${p.ma} - ${p.tenPhieuGiamGia}`],
    confirmText: tat ? 'Ngừng hoạt động' : 'Hoạt động',
    tone: tat ? 'warning' : 'info'
  })
  if (!ok) { event.target.checked = p.trangThai; return }
  try {
    await axios.put(`${API}/${p.id}/toggle-status`)
    toast.success('Đã cập nhật trạng thái phiếu giảm giá')
    load()
  } catch (e) {
    event.target.checked = p.trangThai
    toast.error(errorMessage(e))
  }
}

onMounted(load)
</script>
