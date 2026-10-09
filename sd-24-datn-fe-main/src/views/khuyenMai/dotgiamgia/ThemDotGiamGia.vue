<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Thêm đợt giảm giá</h1>
        <p class="ui-sub">Sau khi tạo, đợt giảm giá không thể sửa hay xóa - chỉ đổi trạng thái.</p>
      </div>
      <button class="ui-btn" @click="router.push('/dot-giam-gia')"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại</button>
    </div>

    <div class="ui-panel">
      <div class="ui-grid-3">
        <div class="ui-field">
          <label>Tên đợt <span class="req">*</span></label>
          <input v-model="f.ten" class="ui-input" :class="{ invalid: e.ten }" maxlength="255" />
          <span v-if="e.ten" class="ui-error">{{ e.ten }}</span>
        </div>
        <div class="ui-field">
          <label>Loại giảm <span class="req">*</span></label>
          <select v-model="f.loai" class="ui-select">
            <option value="PHAN_TRAM">Phần trăm (%)</option>
            <option value="TIEN_MAT">Số tiền (đ)</option>
          </select>
        </div>
        <div class="ui-field">
          <label>{{ f.loai === 'PHAN_TRAM' ? 'Phần trăm giảm' : 'Số tiền giảm (đ)' }} <span class="req">*</span></label>
          <input v-model="f.giaTri" type="number" class="ui-input" :class="{ invalid: e.giaTri }" />
          <span v-if="e.giaTri" class="ui-error">{{ e.giaTri }}</span>
        </div>
        <div class="ui-field" v-if="f.loai === 'PHAN_TRAM'">
          <label>Giảm tối đa (đ) <span class="req">*</span></label>
          <input v-model="f.toiDa" type="number" min="1000" class="ui-input" :class="{ invalid: e.toiDa }" />
          <span v-if="e.toiDa" class="ui-error">{{ e.toiDa }}</span>
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
      </div>
    </div>

    <div class="ui-panel">
      <h3 class="ui-panel-title">Chọn chi tiết sản phẩm áp dụng ({{ selected.length }} đã chọn)</h3>
      <span v-if="e.sp" class="ui-error">{{ e.sp }}</span>
      <div class="ui-filters" style="margin:8px 0 12px">
        <div class="ui-field"><input v-model="kw" class="ui-input" placeholder="Tìm theo mã hoặc tên sản phẩm" /></div>
        <div class="ui-field"><label><input type="checkbox" :checked="allVisibleChecked" @change="toggleAll($event.target.checked)" /> Chọn tất cả đang hiển thị</label></div>
      </div>
      <div class="ui-table-wrap" style="max-height:480px;overflow:auto">
        <table class="ui-table">
          <thead><tr><th style="width:40px"></th><th>Mã SP</th><th>Mã chi tiết</th><th>Tên sản phẩm</th><th>Màu / Size</th><th class="num">Tồn kho</th><th class="num">Giá bán</th><th class="num">Giá sau giảm</th></tr></thead>
          <tbody>
            <tr v-for="v in visibleVariants" :key="v.id">
              <td><input type="checkbox" :value="v.id" v-model="selected" /></td>
              <td>{{ v.maSanPham }}</td><td>{{ v.ma }}</td><td>{{ v.tenSanPham }}</td><td>{{ v.tenMauSac }} / {{ v.tenKichCo }}</td>
              <td class="num">{{ v.soLuongTonKho }}</td>
              <td class="num">{{ formatMoney(v.giaBan) }}</td>
              <td class="num ui-accent">{{ formatMoney(giaSau(v.giaBan)) }}</td>
            </tr>
            <tr v-if="!visibleVariants.length"><td colspan="8" class="ui-empty">Không có sản phẩm</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <div style="display:flex;gap:8px">
      <button class="ui-btn" @click="router.push('/dot-giam-gia')">Hủy</button>
      <button class="ui-btn primary" :disabled="saving" @click="save">{{ saving ? 'Đang lưu...' : 'Thêm đợt giảm giá' }}</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import axios from 'axios'
import { useConfirm } from '@/composables/useConfirm.js'
import { productService, errorMessage, formatMoney } from '@/services/api/productAPI.js'

const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()
const today = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10)

const f = reactive({ ten: '', loai: 'PHAN_TRAM', giaTri: '', toiDa: '', batDau: today, ketThuc: '' })
const e = reactive({})
const variants = ref([])
const selected = ref([])
const kw = ref('')
const saving = ref(false)

const visibleVariants = computed(() => {
  const k = kw.value.trim().toLowerCase()
  return variants.value.filter((v) => !k || [v.ma, v.maSanPham, v.tenSanPham].some((x) => (x || '').toLowerCase().includes(k)))
})
const allVisibleChecked = computed(() => visibleVariants.value.length > 0 && visibleVariants.value.every((v) => selected.value.includes(v.id)))
const toggleAll = (on) => {
  const ids = visibleVariants.value.map((v) => v.id)
  selected.value = on ? [...new Set([...selected.value, ...ids])] : selected.value.filter((id) => !ids.includes(id))
}

const giaSau = (gia) => {
  const g = Number(gia), v = Number(f.giaTri) || 0
  let giam = f.loai === 'PHAN_TRAM' ? (g * v) / 100 : v
  if (f.loai === 'PHAN_TRAM' && f.toiDa !== '' && giam > Number(f.toiDa)) giam = Number(f.toiDa)
  return Math.max(0, Math.round(g - giam))
}

const validate = () => {
  Object.keys(e).forEach((k) => delete e[k])
  if (f.ten.trim().length < 3) e.ten = 'Tên đợt tối thiểu 3 ký tự'
  const v = Number(f.giaTri)
  if (f.giaTri === '') e.giaTri = 'Vui lòng nhập mức giảm'
  else if (f.loai === 'PHAN_TRAM' && (!Number.isInteger(v) || v < 1 || v > 100)) e.giaTri = 'Phần trăm là số nguyên 1–100'
  else if (f.loai === 'TIEN_MAT' && (v < 1000 || v > 100000000)) e.giaTri = 'Số tiền từ 1.000đ đến 100.000.000đ'
  if (f.loai === 'PHAN_TRAM' && (f.toiDa === '' || Number(f.toiDa) < 1000)) e.toiDa = 'Giảm tối đa tối thiểu 1.000đ'
  if (!f.batDau || f.batDau < today) e.batDau = 'Ngày bắt đầu không được trước hôm nay'
  if (!f.ketThuc) e.ketThuc = 'Chọn ngày kết thúc'
  else if (f.ketThuc < f.batDau) e.ketThuc = 'Ngày kết thúc phải sau hoặc bằng ngày bắt đầu'
  if (!selected.value.length) e.sp = 'Vui lòng chọn ít nhất một chi tiết sản phẩm'
  if (f.loai === 'TIEN_MAT' && !e.giaTri) {
    const bad = variants.value.filter((x) => selected.value.includes(x.id) && Number(x.giaBan) <= v)
    if (bad.length) e.giaTri = `Số tiền giảm lớn hơn hoặc bằng giá bán của ${bad.length} sản phẩm (VD ${bad[0].ma})`
  }
  return Object.keys(e).length === 0
}

const save = async () => {
  if (!validate()) { toast.error('Vui lòng kiểm tra lại thông tin'); return }
  const ok = await confirm({
    title: 'Thêm đợt giảm giá?',
    message: 'Sau khi tạo, đợt giảm giá không thể sửa hay xóa.',
    details: [`Tên: ${f.ten.trim()}`, `Mức giảm: ${f.loai === 'PHAN_TRAM' ? `${f.giaTri}% (tối đa ${formatMoney(f.toiDa)})` : formatMoney(f.giaTri)}`,
      `Thời gian: ${f.batDau} → ${f.ketThuc}`, `Số chi tiết sản phẩm: ${selected.value.length}`],
    confirmText: 'Thêm'
  })
  if (!ok) return
  saving.value = true
  try {
    await axios.post('http://localhost:8080/api/dot-giam-gia/add', {
      tenDotGiamGia: f.ten.trim(),
      loaiGiamGiaApDung: f.loai,
      giaTriGiamGia: Number(f.giaTri),
      soTienGiamToiDa: f.loai === 'PHAN_TRAM' ? Number(f.toiDa) : null,
      ngayBatDau: f.batDau,
      ngayKetThuc: f.ketThuc,
      listSanPhamId: selected.value
    })
    toast.success('Đã thêm đợt giảm giá')
    router.push('/dot-giam-gia')
  } catch (err) {
    toast.error(errorMessage(err))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await productService.getAllProductDetails({ status: 'active', page: 0, size: 2000 })
    variants.value = data.content.filter((v) => v.soLuongTonKho > 0)
  } catch (err) { toast.error(errorMessage(err, 'Không tải được sản phẩm')) }
})
</script>
