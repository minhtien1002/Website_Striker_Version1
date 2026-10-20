<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Thêm phiếu giảm giá</h1>
        <p class="ui-sub">Lưu ý: sau khi tạo, phiếu không thể sửa hay xóa - chỉ đổi trạng thái. Hãy kiểm tra kỹ trước khi lưu.</p>
      </div>
      <button class="ui-btn" @click="router.push('/phieu-giam-gia')"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại</button>
    </div>

    <div class="ui-grid-2" style="align-items:start">
      <div class="ui-panel">
        <div class="ui-grid-2">
          <div class="ui-field">
            <label>Mã phiếu</label>
            <input v-model="f.ma" class="ui-input" :class="{ invalid: e.ma }" maxlength="30" placeholder="Bỏ trống để tự sinh" @input="f.ma = f.ma.toUpperCase()" />
            <span v-if="e.ma" class="ui-error">{{ e.ma }}</span>
            <span v-else class="ui-hint">Chữ in hoa, số, dấu gạch; 3–30 ký tự</span>
          </div>
          <div class="ui-field">
            <label>Tên phiếu <span class="req">*</span></label>
            <input v-model="f.ten" class="ui-input" :class="{ invalid: e.ten }" maxlength="255" />
            <span v-if="e.ten" class="ui-error">{{ e.ten }}</span>
          </div>
          <div class="ui-field">
            <label>Loại giảm <span class="req">*</span></label>
            <select v-model="f.loai" class="ui-select">
              <option value="PHAN_TRAM">Phần trăm (%)</option>
              <option value="SO_TIEN_CO_DINH">Số tiền cố định (đ)</option>
            </select>
          </div>
          <div class="ui-field">
            <label>Áp dụng cho <span class="req">*</span></label>
            <select v-model="f.loaiApDung" class="ui-select">
              <option value="DON_HANG">Tiền hàng</option>
              <option value="PHI_VAN_CHUYEN">Phí vận chuyển</option>
            </select>
          </div>
          <div class="ui-field">
            <label>{{ f.loai === 'PHAN_TRAM' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (đ)' }} <span class="req">*</span></label>
            <input v-model="f.giaTri" type="number" class="ui-input" :class="{ invalid: e.giaTri }" :min="f.loai === 'PHAN_TRAM' ? 1 : 1000" :max="f.loai === 'PHAN_TRAM' ? 100 : undefined" />
            <span v-if="e.giaTri" class="ui-error">{{ e.giaTri }}</span>
          </div>
          <div class="ui-field" v-if="f.loai === 'PHAN_TRAM'">
            <label>Giảm tối đa (đ) <span class="req">*</span></label>
            <input v-model="f.toiDa" type="number" min="1000" class="ui-input" :class="{ invalid: e.toiDa }" />
            <span v-if="e.toiDa" class="ui-error">{{ e.toiDa }}</span>
          </div>
          <div class="ui-field">
            <label>Đơn tối thiểu (đ) <span class="req">*</span></label>
            <input v-model="f.toiThieu" type="number" min="0" class="ui-input" :class="{ invalid: e.toiThieu }" />
            <span v-if="e.toiThieu" class="ui-error">{{ e.toiThieu }}</span>
          </div>
          <div class="ui-field">
            <label>Số lượng phát hành <span class="req">*</span></label>
            <input v-model="f.soLuong" type="number" min="1" class="ui-input" :class="{ invalid: e.soLuong }" />
            <span v-if="e.soLuong" class="ui-error">{{ e.soLuong }}</span>
          </div>
          <div class="ui-field">
            <label>Giới hạn mỗi khách</label>
            <input v-model="f.gioiHanMoiKhach" type="number" min="1" class="ui-input" placeholder="Để trống: không giới hạn" />
          </div>
          <div class="ui-field">
            <label>Ngày bắt đầu <span class="req">*</span></label>
            <input v-model="f.batDau" type="date" :min="today" class="ui-input" :class="{ invalid: e.batDau }" />
            <span v-if="e.batDau" class="ui-error">{{ e.batDau }}</span>
          </div>
          <div class="ui-field">
            <label>Ngày kết thúc <span class="req">*</span></label>
            <input v-model="f.ketThuc" type="date" :min="f.batDau || today" class="ui-input" :class="{ invalid: e.ketThuc }" />
            <span v-if="e.ketThuc" class="ui-error">{{ e.ketThuc }}</span>
          </div>
          <div class="ui-field" style="grid-column:1 / -1">
            <label>Mô tả</label>
            <textarea v-model="f.moTa" class="ui-textarea" rows="3" maxlength="500"></textarea>
          </div>
          <div class="ui-field" style="grid-column:1 / -1">
            <label><input type="checkbox" v-model="f.riengTu" /> Phiếu riêng tư (chỉ khách hàng được chọn mới dùng được)</label>
            <span v-if="e.khach" class="ui-error">{{ e.khach }}</span>
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-top:14px">
          <button class="ui-btn" @click="router.push('/phieu-giam-gia')">Hủy</button>
          <button class="ui-btn primary" :disabled="saving" @click="save">{{ saving ? 'Đang lưu...' : 'Thêm phiếu giảm giá' }}</button>
        </div>
      </div>

      <div class="ui-panel" :style="{ opacity: f.riengTu ? 1 : .5 }">
        <h3 class="ui-panel-title">Khách hàng áp dụng ({{ selected.length }} đã chọn)</h3>
        <input v-model="kw" class="ui-input" placeholder="Tìm theo tên, mã, SĐT, email" :disabled="!f.riengTu" style="margin-bottom:10px" />
        <div class="ui-table-wrap" style="max-height:480px;overflow:auto">
          <table class="ui-table">
            <thead><tr><th style="width:40px"></th><th>Mã</th><th>Tên</th><th>SĐT</th></tr></thead>
            <tbody>
              <tr v-for="c in filteredCustomers" :key="c.id">
                <td><input type="checkbox" :value="c.id" v-model="selected" :disabled="!f.riengTu" /></td>
                <td>{{ c.ma }}</td><td>{{ c.ten }}</td><td>{{ c.soDienThoai || '—' }}</td>
              </tr>
              <tr v-if="!filteredCustomers.length"><td colspan="4" class="ui-empty">Không có khách hàng</td></tr>
            </tbody>
          </table>
        </div>
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
import { errorMessage, formatMoney } from '@/services/api/productAPI.js'

const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const today = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10)
const f = reactive({ ma: '', ten: '', loai: 'PHAN_TRAM', loaiApDung: 'DON_HANG', giaTri: '', toiDa: '', toiThieu: 0, soLuong: '', gioiHanMoiKhach: '', batDau: today, ketThuc: '', moTa: '', riengTu: false })
const e = reactive({})
const customers = ref([])
const selected = ref([])
const kw = ref('')
const saving = ref(false)

const filteredCustomers = computed(() => {
  const k = kw.value.trim().toLowerCase()
  return customers.value.filter((c) => !k || [c.ma, c.ten, c.soDienThoai, c.email].some((v) => (v || '').toLowerCase().includes(k)))
})

const validate = () => {
  Object.keys(e).forEach((k) => delete e[k])
  if (f.ma && !/^[A-Z0-9_-]{3,30}$/.test(f.ma.trim())) e.ma = 'Mã chỉ gồm chữ in hoa, số, "-", "_" (3–30 ký tự)'
  if (!f.ten.trim()) e.ten = 'Vui lòng nhập tên phiếu'
  else if (f.ten.trim().length < 3) e.ten = 'Tên phiếu tối thiểu 3 ký tự'
  const gt = Number(f.giaTri)
  if (f.giaTri === '') e.giaTri = 'Vui lòng nhập mức giảm'
  else if (f.loai === 'PHAN_TRAM' && (!Number.isInteger(gt) || gt < 1 || gt > 100)) e.giaTri = 'Phần trăm là số nguyên từ 1 đến 100'
  else if (f.loai !== 'PHAN_TRAM' && (gt < 1000 || gt > 100000000)) e.giaTri = 'Số tiền giảm từ 1.000đ đến 100.000.000đ'
  if (f.loai === 'PHAN_TRAM') {
    if (f.toiDa === '' || Number(f.toiDa) < 1000) e.toiDa = 'Giảm tối đa tối thiểu 1.000đ'
  }
  const tt = Number(f.toiThieu)
  if (f.toiThieu === '' || tt < 0) e.toiThieu = 'Đơn tối thiểu phải ≥ 0'
  else if (f.loai !== 'PHAN_TRAM' && tt > 0 && gt >= tt) e.toiThieu = 'Đơn tối thiểu phải lớn hơn số tiền giảm'
  const sl = Number(f.soLuong)
  if (f.soLuong === '' || !Number.isInteger(sl) || sl < 1 || sl > 100000) e.soLuong = 'Số lượt từ 1 đến 100.000'
  if (f.gioiHanMoiKhach !== '' && (!Number.isInteger(Number(f.gioiHanMoiKhach)) || Number(f.gioiHanMoiKhach) < 1 || Number(f.gioiHanMoiKhach) > sl)) e.gioiHanMoiKhach = 'Giới hạn mỗi khách từ 1 đến số lượng phát hành'
  if (!f.batDau) e.batDau = 'Chọn ngày bắt đầu'
  else if (f.batDau < today) e.batDau = 'Ngày bắt đầu không được trước hôm nay'
  if (!f.ketThuc) e.ketThuc = 'Chọn ngày kết thúc'
  else if (f.batDau && f.ketThuc < f.batDau) e.ketThuc = 'Ngày kết thúc phải sau hoặc bằng ngày bắt đầu'
  if (f.riengTu && !selected.value.length) e.khach = 'Phiếu riêng tư cần chọn ít nhất một khách hàng'
  return Object.keys(e).length === 0
}

const save = async () => {
  if (!validate()) { toast.error('Vui lòng kiểm tra lại thông tin'); return }
  const muc = f.loai === 'PHAN_TRAM' ? `${f.giaTri}% (tối đa ${formatMoney(f.toiDa)})` : formatMoney(f.giaTri)
  const ok = await confirm({
    title: 'Thêm phiếu giảm giá?',
    message: 'Sau khi tạo, phiếu không thể sửa hay xóa.',
    details: [`Tên: ${f.ten.trim()}`, `Mức giảm: ${muc}`, `Áp dụng: ${f.loaiApDung === 'PHI_VAN_CHUYEN' ? 'Phí vận chuyển' : 'Tiền hàng'}`, `Đơn tối thiểu: ${formatMoney(f.toiThieu)}`, `Phát hành: ${f.soLuong}`,
      `Thời gian: ${f.batDau} → ${f.ketThuc}`, f.riengTu ? `Riêng tư - ${selected.value.length} khách hàng` : 'Công khai'],
    confirmText: 'Thêm'
  })
  if (!ok) return
  saving.value = true
  try {
    const phanTram = f.loai === 'PHAN_TRAM'
    await axios.post('http://localhost:8080/api/phieu-giam-gia/add', {
      ma: f.ma.trim() || null,
      tenPhieuGiamGia: f.ten.trim().replace(/\s+/g, ' '),
      loaiPhieuGiamGia: f.loai,
      phanTramGiamGia: phanTram ? Number(f.giaTri) : null,
      soTienGiamToiDa: phanTram ? Number(f.toiDa) : Number(f.giaTri),
      hoaDonToiThieu: Number(f.toiThieu),
      soLuongDung: Number(f.soLuong),
      loaiApDung: f.loaiApDung,
      gioiHanMoiKhach: f.gioiHanMoiKhach === '' ? null : Number(f.gioiHanMoiKhach),
      ngayBatDau: f.batDau,
      ngayKetThuc: f.ketThuc,
      riengTu: f.riengTu,
      moTa: f.moTa.trim() || null,
      khachHangIds: f.riengTu ? selected.value : []
    })
    toast.success('Đã thêm phiếu giảm giá')
    router.push('/phieu-giam-gia')
  } catch (err) {
    toast.error(errorMessage(err))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/khach-hang/home', { params: { page: 0, size: 1000 } })
    customers.value = (data.content || data || []).filter((c) => c.ma !== 'KHACH_LE')
  } catch { /* danh sách khách chỉ cần cho phiếu riêng tư */ }
})
</script>
