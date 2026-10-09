<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Quản lý đợt giảm giá</h1>
        <p class="ui-sub">Đợt giảm giá không thể sửa hay xóa sau khi tạo - chỉ đổi trạng thái.</p>
      </div>
      <button class="ui-btn primary" @click="router.push('/them-dot-giam-gia')">
        <iconify-icon icon="solar:add-square-linear"></iconify-icon> Thêm đợt giảm giá
      </button>
    </div>

    <div class="ui-panel">
      <div class="ui-filters">
        <div class="ui-field">
          <label>Tìm kiếm</label>
          <input v-model="filters.keyword" class="ui-input" placeholder="Mã hoặc tên đợt" />
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
        <div class="ui-field"><button class="ui-btn" @click="Object.assign(filters, { keyword: '', tinhTrang: '' })">Đặt lại bộ lọc</button></div>
      </div>
    </div>

    <div class="ui-panel">
      <div class="ui-table-wrap">
        <table class="ui-table">
          <thead>
            <tr><th style="width:50px">STT</th><th>Mã</th><th>Tên đợt</th><th>Mức giảm</th><th>Thời gian</th><th>Tình trạng</th><th style="width:110px">Hành động</th></tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="ui-empty">Đang tải...</td></tr>
            <tr v-else-if="!filtered.length"><td colspan="7" class="ui-empty">Không có đợt giảm giá phù hợp.</td></tr>
            <tr v-for="(d, i) in filtered" :key="d.id">
              <td>{{ i + 1 }}</td>
              <td><strong>{{ d.ma || '—' }}</strong></td>
              <td>{{ d.tenDotGiamGia }}</td>
              <td>{{ mucGiam(d) }}</td>
              <td>{{ formatDate(d.ngayBatDau) }} – {{ formatDate(d.ngayKetThuc) }}</td>
              <td><span class="ui-badge" :class="{ on: tinhTrang(d) === 'DANG_DIEN_RA' }">{{ TINH_TRANG[tinhTrang(d)] }}</span></td>
              <td>
                <div style="display:flex;gap:6px;align-items:center">
                  <button class="ui-btn icon" title="Xem chi tiết" @click="openDetail(d)"><iconify-icon icon="solar:eye-linear"></iconify-icon></button>
                  <label class="ui-switch" :title="d.trangThai ? 'Chuyển sang Ngừng hoạt động' : 'Chuyển sang Hoạt động'">
                    <input type="checkbox" :checked="d.trangThai" @change="toggle(d, $event)" /><span></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="detail" class="ui-modal-overlay" @click.self="detail = null">
      <div class="ui-modal lg">
        <div class="ui-modal-head">Chi tiết đợt giảm giá {{ detail.ma }}
          <button class="ui-btn icon" @click="detail = null"><iconify-icon icon="solar:close-square-linear"></iconify-icon></button>
        </div>
        <div class="ui-modal-body">
          <dl class="ui-dl" style="margin-bottom:14px">
            <dt>Mã</dt><dd>{{ detail.ma }}</dd>
            <dt>Tên đợt</dt><dd>{{ detail.tenDotGiamGia }}</dd>
            <dt>Mức giảm</dt><dd>{{ mucGiam(detail) }}</dd>
            <dt>Thời gian</dt><dd>{{ formatDate(detail.ngayBatDau) }} – {{ formatDate(detail.ngayKetThuc) }}</dd>
            <dt>Trạng thái</dt><dd>{{ detail.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }} · {{ TINH_TRANG[tinhTrang(detail)] }}</dd>
            <dt>Số sản phẩm áp dụng</dt><dd>{{ detail.danhSachSanPham?.length || 0 }}</dd>
          </dl>
          <table class="ui-table">
            <thead><tr><th>Mã SP</th><th>Mã chi tiết</th><th>Tên sản phẩm</th><th>Màu / Size</th><th class="num">Giá gốc</th><th class="num">Giá sau giảm</th></tr></thead>
            <tbody>
              <tr v-for="s in detail.danhSachSanPham" :key="s.idSanPham">
                <td>{{ s.maSanPham }}</td><td>{{ s.maChiTiet }}</td><td>{{ s.tenSanPham }}</td><td>{{ s.mauSac }} / {{ s.kichCo }}</td>
                <td class="num">{{ formatMoney(s.giaBanDau) }}</td><td class="num ui-accent">{{ formatMoney(s.giaSauKhiGiam) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="ui-modal-foot"><button class="ui-btn" @click="detail = null">Đóng</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import axios from 'axios'
import { useConfirm } from '@/composables/useConfirm.js'
import { errorMessage, formatDate, formatMoney } from '@/services/api/productAPI.js'

const API = 'http://localhost:8080/api/dot-giam-gia'
const TINH_TRANG = { SAP_DIEN_RA: 'Sắp diễn ra', DANG_DIEN_RA: 'Đang diễn ra', DA_KET_THUC: 'Đã kết thúc', NGUNG: 'Ngừng hoạt động' }
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const items = ref([])
const loading = ref(false)
const detail = ref(null)
const filters = reactive({ keyword: '', tinhTrang: '' })

const mucGiam = (d) => d.loaiGiamGiaApDung === 'PHAN_TRAM'
  ? `${Number(d.giaTriGiamGia)}%${d.soTienGiamToiDa ? ` (tối đa ${formatMoney(d.soTienGiamToiDa)})` : ''}`
  : formatMoney(d.giaTriGiamGia)
const today = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10)
const tinhTrang = (d) => !d.trangThai ? 'NGUNG' : d.ngayBatDau > today ? 'SAP_DIEN_RA' : d.ngayKetThuc < today ? 'DA_KET_THUC' : 'DANG_DIEN_RA'
const filtered = computed(() => items.value.filter((d) => {
  const k = filters.keyword.trim().toLowerCase()
  if (k && !(d.ma || '').toLowerCase().includes(k) && !(d.tenDotGiamGia || '').toLowerCase().includes(k)) return false
  return !filters.tinhTrang || tinhTrang(d) === filters.tinhTrang
}))

const load = async () => {
  loading.value = true
  try { items.value = (await axios.get(API)).data.filter((d) => !d.deleted).sort((a, b) => b.id - a.id) }
  catch (e) { toast.error(errorMessage(e, 'Không tải được đợt giảm giá')) }
  finally { loading.value = false }
}

const openDetail = async (d) => {
  try { detail.value = (await axios.get(`${API}/detail/${d.id}`)).data } catch (e) { toast.error(errorMessage(e)) }
}

const toggle = async (d, event) => {
  const tat = d.trangThai
  const ok = await confirm({
    title: tat ? 'Ngừng hoạt động đợt giảm giá?' : 'Chuyển đợt giảm giá sang Hoạt động?',
    message: tat ? 'Giá các sản phẩm trong đợt sẽ trở về giá bán gốc.' : 'Giá giảm sẽ áp dụng lại cho các sản phẩm trong đợt (nếu còn trong thời gian hiệu lực).',
    details: [`${d.ma || ''} ${d.tenDotGiamGia}`],
    confirmText: tat ? 'Ngừng hoạt động' : 'Hoạt động',
    tone: tat ? 'warning' : 'info'
  })
  if (!ok) { event.target.checked = d.trangThai; return }
  try {
    await axios.put(`${API}/${d.id}/status`, { trangThai: !tat })
    toast.success('Đã cập nhật trạng thái đợt giảm giá')
    load()
  } catch (e) {
    event.target.checked = d.trangThai
    toast.error(errorMessage(e))
  }
}

onMounted(load)
</script>
