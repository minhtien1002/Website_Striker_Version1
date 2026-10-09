<template>
  <div class="ui-page" v-if="product">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">{{ product.tenSanPham }}</h1>
        <p class="ui-sub">Mã {{ product.ma }} · <span class="ui-badge" :class="{ on: product.trangThai }">{{ product.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}</span></p>
      </div>
      <div style="display:flex;gap:8px">
        <button class="ui-btn" @click="router.push('/san-pham')"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Danh sách</button>
        <button class="ui-btn" @click="router.push(`/san-pham/sua/${product.id}`)"><iconify-icon icon="solar:pen-linear"></iconify-icon> Sửa thông tin</button>
        <button class="ui-btn primary" @click="openAddVariants"><iconify-icon icon="solar:add-square-linear"></iconify-icon> Thêm biến thể</button>
      </div>
    </div>

    <!-- Thông tin đầy đủ -->
    <div class="ui-panel">
      <h3 class="ui-panel-title">Thông tin sản phẩm</h3>
      <div style="display:grid;grid-template-columns:220px 1fr;gap:20px">
        <div>
          <img v-if="product.urlAnhDaiDien" :src="imageUrl(product.urlAnhDaiDien)" alt="" style="width:220px;height:220px;object-fit:cover;border:1px solid var(--line)" />
          <div v-else class="ui-thumb" style="width:220px;height:220px"></div>
        </div>
        <dl class="ui-dl">
          <dt>Mã sản phẩm</dt><dd>{{ product.ma }}</dd>
          <dt>Tên sản phẩm</dt><dd>{{ product.tenSanPham }}</dd>
          <dt>Thương hiệu</dt><dd>{{ product.tenThuongHieu || '—' }}</dd>
          <dt>Thể loại</dt><dd>{{ product.tenDanhMuc || '—' }}</dd>
          <dt>Chất liệu</dt><dd>{{ product.tenChatLieu || '—' }}</dd>
          <dt>Màu sắc</dt><dd>{{ product.danhSachMauSac?.join(', ') || '—' }}</dd>
          <dt>Kích cỡ</dt><dd>{{ product.danhSachKichCo?.join(', ') || '—' }}</dd>
          <dt>Giá bán</dt><dd>{{ giaText }}</dd>
          <dt>Biến thể</dt><dd>{{ product.activeVariants }} đang bán / {{ product.totalVariants }} tổng · {{ product.soBienTheHetHang }} hết hàng</dd>
          <dt>Tổng tồn kho</dt><dd>{{ product.tongSoLuongTonKho }}</dd>
          <dt>Tổng đã bán</dt><dd>{{ product.tongSoLuongDaBan }}</dd>
          <dt>Ngày tạo</dt><dd>{{ formatDate(product.ngayTao, true) }}</dd>
          <dt>Cập nhật lần cuối</dt><dd>{{ formatDate(product.ngayCapNhat, true) }}</dd>
          <dt>Mô tả</dt><dd style="white-space:pre-line;font-weight:500">{{ product.moTa || '—' }}</dd>
        </dl>
      </div>
    </div>

    <!-- Biến thể -->
    <div class="ui-panel">
      <h3 class="ui-panel-title">Chi tiết sản phẩm ({{ totalElements }})</h3>
      <div class="ui-filters" style="margin-bottom:12px">
        <div class="ui-field">
          <label>Tìm kiếm</label>
          <input v-model="filters.search" class="ui-input" placeholder="Mã chi tiết sản phẩm" />
        </div>
        <div class="ui-field">
          <label>Màu sắc</label>
          <select v-model="filters.colorId" class="ui-select">
            <option value="">Tất cả</option>
            <option v-for="c in colorOptions" :key="c.id" :value="c.id">{{ c.ten }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Kích cỡ</label>
          <select v-model="filters.sizeId" class="ui-select">
            <option value="">Tất cả</option>
            <option v-for="s in sizeOptions" :key="s.id" :value="s.id">{{ s.ten }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Trạng thái</label>
          <select v-model="filters.status" class="ui-select">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
            <option value="out_of_stock">Hết hàng</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Giá bán từ (đ)</label>
          <input v-model="filters.minSellingPrice" type="number" min="0" class="ui-input" />
        </div>
        <div class="ui-field">
          <label>Giá bán đến (đ)</label>
          <input v-model="filters.maxSellingPrice" type="number" min="0" class="ui-input" />
          <span v-if="priceError" class="ui-error">{{ priceError }}</span>
        </div>
        <div class="ui-field">
          <button class="ui-btn" @click="resetFilters">Đặt lại bộ lọc</button>
        </div>
      </div>

      <div class="ui-table-wrap">
        <table class="ui-table">
          <thead>
            <tr>
              <th style="width:50px">STT</th>
              <th style="width:70px">Ảnh</th>
              <th>Mã chi tiết</th>
              <th>Màu sắc</th>
              <th>Kích cỡ</th>
              <th class="num">Giá bán</th>
              <th class="num">Tồn kho</th>
              <th class="num">Đã bán</th>
              <th>Trạng thái</th>
              <th style="width:110px">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loadingVariants"><td colspan="10" class="ui-empty">Đang tải...</td></tr>
            <tr v-else-if="!variants.length"><td colspan="10" class="ui-empty">Không có biến thể phù hợp.</td></tr>
            <tr v-for="(v, i) in variants" :key="v.id">
              <td>{{ page * size + i + 1 }}</td>
              <td><img v-if="v.urlAnhSanPham" :src="imageUrl(v.urlAnhSanPham)" class="ui-thumb" alt="" /><div v-else class="ui-thumb"></div></td>
              <td><strong>{{ v.ma }}</strong></td>
              <td>{{ v.tenMauSac }}</td>
              <td>{{ v.tenKichCo }}</td>
              <td class="num">{{ formatMoney(v.giaBan) }}</td>
              <td class="num">{{ v.soLuongTonKho }}<div v-if="v.soLuongTonKho === 0" class="ui-accent ui-hint">Hết hàng</div></td>
              <td class="num">{{ v.soLuongDaBan }}</td>
              <td><span class="ui-badge" :class="{ on: v.trangThai }">{{ v.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}</span></td>
              <td>
                <div style="display:flex;gap:6px;align-items:center">
                  <button class="ui-btn icon" title="Sửa" @click="router.push(`/san-pham/chi-tiet/sua/${v.id}`)"><iconify-icon icon="solar:pen-linear"></iconify-icon></button>
                  <label class="ui-switch" :title="v.trangThai ? 'Chuyển sang Ngừng hoạt động' : 'Chuyển sang Hoạt động'">
                    <input type="checkbox" :checked="v.trangThai" @change="toggleVariant(v, $event)" /><span></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="ui-pager" v-if="totalPages > 1">
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <div class="pages">
          <button class="ui-btn sm" :disabled="page === 0" @click="page--">Trước</button>
          <button class="ui-btn sm" :disabled="page + 1 >= totalPages" @click="page++">Sau</button>
        </div>
      </div>
    </div>

    <!-- Thêm biến thể -->
    <div v-if="showAdd" class="ui-modal-overlay" @click.self="showAdd = false">
      <div class="ui-modal lg">
        <div class="ui-modal-head">Thêm biến thể cho {{ product.tenSanPham }}
          <button class="ui-btn icon" @click="showAdd = false"><iconify-icon icon="solar:close-square-linear"></iconify-icon></button>
        </div>
        <div class="ui-modal-body">
          <BienTheEditor ref="editor" v-model="newVariants" :colors="activeColors" :sizes="activeSizes" :existing-pairs="existingPairs" />
        </div>
        <div class="ui-modal-foot">
          <button class="ui-btn" @click="showAdd = false">Hủy</button>
          <button class="ui-btn primary" :disabled="saving || !newVariants.length" @click="saveVariants">Thêm {{ newVariants.length }} biến thể</button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="ui-page"><div class="ui-panel ui-empty">{{ notFound ? 'Không tìm thấy sản phẩm.' : 'Đang tải...' }}</div></div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import BienTheEditor from '@/components/sanPham/BienTheEditor.vue'
import { productService, colorApi, sizeApi, errorMessage, imageUrl, formatMoney, formatDate } from '@/services/api/productAPI.js'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const product = ref(null)
const notFound = ref(false)
const variants = ref([])
const loadingVariants = ref(false)
const page = ref(0)
const size = 10
const totalPages = ref(0)
const totalElements = ref(0)
const filters = reactive({ search: '', colorId: '', sizeId: '', status: '', minSellingPrice: '', maxSellingPrice: '' })

const showAdd = ref(false)
const newVariants = ref([])
const activeColors = ref([])
const activeSizes = ref([])
const saving = ref(false)
const editor = ref(null)

const giaText = computed(() => {
  const p = product.value
  if (!p || p.giaBanThapNhat == null) return '—'
  return Number(p.giaBanThapNhat) === Number(p.giaBanCaoNhat) ? formatMoney(p.giaBanThapNhat) : `${formatMoney(p.giaBanThapNhat)} – ${formatMoney(p.giaBanCaoNhat)}`
})
const colorOptions = computed(() => uniq(product.value?.chiTietSanPhams || [], 'idMauSac', 'tenMauSac'))
const sizeOptions = computed(() => uniq(product.value?.chiTietSanPhams || [], 'idKichCo', 'tenKichCo'))
const existingPairs = computed(() => (product.value?.chiTietSanPhams || []).map((v) => `${v.idMauSac}-${v.idKichCo}`))
const priceError = computed(() => {
  const a = filters.minSellingPrice, b = filters.maxSellingPrice
  if ((a !== '' && Number(a) < 0) || (b !== '' && Number(b) < 0)) return 'Giá không được âm'
  if (a !== '' && b !== '' && Number(a) > Number(b)) return 'Giá từ phải nhỏ hơn hoặc bằng giá đến'
  return ''
})

function uniq(list, idKey, nameKey) {
  const m = new Map()
  list.forEach((v) => { if (v[idKey] && !m.has(v[idKey])) m.set(v[idKey], { id: v[idKey], ten: v[nameKey] }) })
  return [...m.values()]
}

const loadProduct = async () => {
  try {
    product.value = (await productService.getProductById(route.params.id)).data
  } catch (e) {
    notFound.value = true
    toast.error(errorMessage(e, 'Không tìm thấy sản phẩm'))
  }
}

const loadVariants = async () => {
  if (priceError.value) return
  loadingVariants.value = true
  try {
    const { data } = await productService.getProductDetailsPaged(route.params.id, { ...filters, search: filters.search.trim(), page: page.value, size })
    variants.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (e) {
    toast.error(errorMessage(e, 'Không tải được chi tiết sản phẩm'))
  } finally {
    loadingVariants.value = false
  }
}

let timer = null
watch(filters, () => { clearTimeout(timer); timer = setTimeout(() => { page.value = 0; loadVariants() }, 300) })
watch(page, loadVariants)
const resetFilters = () => Object.assign(filters, { search: '', colorId: '', sizeId: '', status: '', minSellingPrice: '', maxSellingPrice: '' })

const toggleVariant = async (v, event) => {
  const tat = v.trangThai
  const ok = await confirm({
    title: tat ? 'Ngừng hoạt động biến thể?' : 'Chuyển biến thể sang Hoạt động?',
    message: tat ? 'Biến thể sẽ không còn được bán.' : 'Biến thể sẽ được bán trở lại.',
    details: [`${v.ma} - ${v.tenMauSac} / ${v.tenKichCo}`],
    confirmText: tat ? 'Ngừng hoạt động' : 'Hoạt động',
    tone: tat ? 'warning' : 'info'
  })
  if (!ok) { event.target.checked = v.trangThai; return }
  try {
    const { data } = await productService.toggleProductDetailStatus(v.id)
    toast.success(data.message)
    await Promise.all([loadProduct(), loadVariants()])
  } catch (e) {
    event.target.checked = v.trangThai
    toast.error(errorMessage(e))
  }
}

const openAddVariants = async () => {
  try {
    const [c, s] = await Promise.all([colorApi.active(), sizeApi.active()])
    activeColors.value = c.data; activeSizes.value = s.data
    newVariants.value = []
    showAdd.value = true
  } catch (e) {
    toast.error(errorMessage(e))
  }
}

const saveVariants = async () => {
  if (!editor.value.validate()) { toast.error('Vui lòng nhập đúng giá bán và số lượng cho mọi biến thể'); return }
  const ok = await confirm({
    title: `Thêm ${newVariants.value.length} biến thể?`,
    message: 'Các biến thể mới sẽ ở trạng thái Hoạt động và bán được ngay.',
    details: newVariants.value.map((v) => `${v.tenMauSac} / ${v.tenKichCo}: ${formatMoney(v.giaBan)} × ${v.soLuongTonKho}`),
    confirmText: 'Thêm'
  })
  if (!ok) return
  saving.value = true
  try {
    await productService.addVariantsToProduct(product.value.id, newVariants.value.map(({ idMauSac, idKichCo, giaBan, soLuongTonKho }) => ({ idMauSac, idKichCo, giaBan, soLuongTonKho })))
    toast.success('Đã thêm biến thể')
    showAdd.value = false
    await Promise.all([loadProduct(), loadVariants()])
  } catch (e) {
    toast.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

onMounted(async () => { await loadProduct(); if (product.value) loadVariants() })
</script>
