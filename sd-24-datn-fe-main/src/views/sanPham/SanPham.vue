<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Quản lý sản phẩm</h1>
        <p class="ui-sub">{{ totalElements }} sản phẩm</p>
      </div>
      <button class="ui-btn primary" @click="router.push('/san-pham/them')">
        <iconify-icon icon="solar:add-square-linear"></iconify-icon> Thêm sản phẩm
      </button>
    </div>

    <div class="ui-panel">
      <div class="ui-filters">
        <div class="ui-field">
          <label>Tìm kiếm</label>
          <input v-model="filters.keyword" class="ui-input" placeholder="Mã hoặc tên sản phẩm" />
        </div>
        <div class="ui-field">
          <label>Thương hiệu</label>
          <select v-model="filters.idThuongHieu" class="ui-select">
            <option value="">Tất cả</option>
            <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.tenThuongHieu }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Thể loại</label>
          <select v-model="filters.idDanhMuc" class="ui-select">
            <option value="">Tất cả</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.tenDanhMuc }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Chất liệu</label>
          <select v-model="filters.idChatLieu" class="ui-select">
            <option value="">Tất cả</option>
            <option v-for="m in materials" :key="m.id" :value="m.id">{{ m.tenChatLieu }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Trạng thái</label>
          <select v-model="filters.trangThai" class="ui-select">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Khoảng giá</label>
          <select v-model="khoangGia" class="ui-select" @change="applyKhoangGia">
            <option value="">Tất cả</option>
            <option v-for="k in KHOANG_GIA" :key="k.value" :value="k.value">{{ k.label }}</option>
            <option value="custom">Tự nhập...</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Giá từ (đ)</label>
          <input v-model="filters.minGia" type="number" min="0" step="10000" class="ui-input" :class="{ invalid: giaError }" placeholder="0" @input="khoangGia = 'custom'" />
        </div>
        <div class="ui-field">
          <label>Giá đến (đ)</label>
          <input v-model="filters.maxGia" type="number" min="0" step="10000" class="ui-input" :class="{ invalid: giaError }" placeholder="Không giới hạn" @input="khoangGia = 'custom'" />
          <span v-if="giaError" class="ui-error">{{ giaError }}</span>
        </div>
        <div class="ui-field">
          <button class="ui-btn" @click="resetFilters">Đặt lại bộ lọc</button>
        </div>
      </div>
    </div>

    <div class="ui-panel">
      <div class="ui-table-wrap">
        <table class="ui-table">
          <thead>
            <tr>
              <th style="width:50px">STT</th>
              <th style="width:70px">Ảnh</th>
              <th style="width:90px">Mã</th>
              <th style="min-width:240px">Tên sản phẩm</th>
              <th>Thương hiệu</th>
              <th>Thể loại</th>
              <th class="num">Giá bán</th>
              <th class="num">Tồn kho</th>
              <th class="num">Đã bán</th>
              <th>Trạng thái</th>
              <th style="width:150px">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="11" class="ui-empty">Đang tải...</td></tr>
            <tr v-else-if="!products.length"><td colspan="11" class="ui-empty">Không có sản phẩm phù hợp.</td></tr>
            <tr v-for="(p, i) in products" :key="p.id">
              <td>{{ page * size + i + 1 }}</td>
              <td><img v-if="p.urlAnhDaiDien" :src="imageUrl(p.urlAnhDaiDien)" class="ui-thumb" alt="" @error="onImgError" /><div v-else class="ui-thumb"></div></td>
              <td>{{ p.ma }}</td>
              <td>
                <span class="ui-link" @click="openDetail(p)">{{ p.tenSanPham }}</span>
                <div class="ui-hint">{{ p.activeVariants }}/{{ p.totalVariants }} biến thể đang bán<span v-if="p.soBienTheHetHang"> · {{ p.soBienTheHetHang }} hết hàng</span></div>
              </td>
              <td>{{ p.tenThuongHieu || '—' }}</td>
              <td>{{ p.tenDanhMuc || '—' }}</td>
              <td class="num">{{ giaText(p) }}</td>
              <td class="num">{{ p.tongSoLuongTonKho }}</td>
              <td class="num">{{ p.tongSoLuongDaBan }}</td>
              <td><span class="ui-badge" :class="{ on: p.trangThai }">{{ p.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}</span></td>
              <td>
                <div style="display:flex;gap:6px;align-items:center">
                  <button class="ui-btn icon" title="Xem chi tiết" @click="openDetail(p)"><iconify-icon icon="solar:eye-linear"></iconify-icon></button>
                  <button class="ui-btn icon" title="Sửa" @click="router.push(`/san-pham/sua/${p.id}`)"><iconify-icon icon="solar:pen-linear"></iconify-icon></button>
                  <label class="ui-switch" :title="p.trangThai ? 'Chuyển sang Ngừng hoạt động' : 'Chuyển sang Hoạt động'">
                    <input type="checkbox" :checked="p.trangThai" @change="toggle(p, $event)" /><span></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="ui-pager">
        <span>Trang {{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
        <div class="pages">
          <select v-model.number="size" class="ui-select" style="width:auto">
            <option :value="10">10 / trang</option>
            <option :value="20">20 / trang</option>
            <option :value="50">50 / trang</option>
          </select>
          <button class="ui-btn sm" :disabled="page === 0" @click="page--">Trước</button>
          <button class="ui-btn sm" :disabled="page + 1 >= totalPages" @click="page++">Sau</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import { productService, brandApi, categoryApi, materialApi, errorMessage, imageUrl, formatMoney } from '@/services/api/productAPI.js'

const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const products = ref([])
const loading = ref(false)
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const filters = reactive({ keyword: '', idThuongHieu: '', idDanhMuc: '', idChatLieu: '', trangThai: '', minGia: '', maxGia: '' })
const KHOANG_GIA = [
  { value: '0-300000', label: 'Dưới 300.000đ' },
  { value: '300000-500000', label: '300.000đ – 500.000đ' },
  { value: '500000-800000', label: '500.000đ – 800.000đ' },
  { value: '800000-1200000', label: '800.000đ – 1.200.000đ' },
  { value: '1200000-', label: 'Trên 1.200.000đ' }
]
const khoangGia = ref('')
const applyKhoangGia = () => {
  if (khoangGia.value === 'custom') return
  const [min, max] = (khoangGia.value || '-').split('-')
  filters.minGia = min || ''
  filters.maxGia = max || ''
}
const giaError = computed(() => {
  const a = filters.minGia, b = filters.maxGia
  if ((a !== '' && Number(a) < 0) || (b !== '' && Number(b) < 0)) return 'Giá không được âm'
  if (a !== '' && b !== '' && Number(a) > Number(b)) return 'Giá từ phải nhỏ hơn hoặc bằng giá đến'
  return ''
})
const brands = ref([])
const categories = ref([])
const materials = ref([])

const giaText = (p) => {
  if (p.giaBanThapNhat == null) return '—'
  return Number(p.giaBanThapNhat) === Number(p.giaBanCaoNhat)
    ? formatMoney(p.giaBanThapNhat)
    : `${formatMoney(p.giaBanThapNhat)} – ${formatMoney(p.giaBanCaoNhat)}`
}

const load = async () => {
  if (giaError.value) return
  loading.value = true
  try {
    const { data } = await productService.searchProducts({ ...filters, keyword: filters.keyword.trim(), page: page.value, size: size.value })
    products.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (e) {
    toast.error(errorMessage(e, 'Không tải được danh sách sản phẩm'))
  } finally {
    loading.value = false
  }
}

let timer = null
watch(filters, () => { clearTimeout(timer); timer = setTimeout(() => { page.value = 0; load() }, 300) })
watch(page, load)
watch(size, () => { if (page.value === 0) load(); else page.value = 0 })

const resetFilters = () => { khoangGia.value = ''; Object.assign(filters, { keyword: '', idThuongHieu: '', idDanhMuc: '', idChatLieu: '', trangThai: '', minGia: '', maxGia: '' }) }
const openDetail = (p) => router.push(`/san-pham/chi-tiet/${p.id}`)
const onImgError = (e) => { e.target.style.visibility = 'hidden' }

const toggle = async (p, event) => {
  const tat = p.trangThai
  const ok = await confirm({
    title: tat ? 'Ngừng hoạt động sản phẩm?' : 'Chuyển sản phẩm sang Hoạt động?',
    message: tat
      ? 'Sản phẩm và tất cả biến thể sẽ không còn bán tại quầy lẫn trên trang khách hàng.'
      : 'Sản phẩm và tất cả biến thể sẽ được bán trở lại.',
    details: [`${p.ma} - ${p.tenSanPham}`, `Số biến thể: ${p.totalVariants}`],
    confirmText: tat ? 'Ngừng hoạt động' : 'Hoạt động',
    tone: tat ? 'warning' : 'info'
  })
  if (!ok) { event.target.checked = p.trangThai; return }
  try {
    const { data } = await productService.toggleProductStatus(p.id)
    toast.success(data.message)
    load()
  } catch (e) {
    event.target.checked = p.trangThai
    toast.error(errorMessage(e))
  }
}

onMounted(async () => {
  load()
  try {
    const [b, c, m] = await Promise.all([brandApi.all(), categoryApi.all(), materialApi.all()])
    brands.value = b.data; categories.value = c.data; materials.value = m.data
  } catch { /* bộ lọc thuộc tính không bắt buộc */ }
})
</script>
