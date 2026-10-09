<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Quản lý {{ config.label }}</h1>
        <p class="ui-sub">{{ items.length }} {{ config.label }} · {{ soHoatDong }} đang hoạt động</p>
      </div>
      <button class="ui-btn primary" @click="router.push(`${config.route}/them`)">
        <iconify-icon icon="solar:add-square-linear"></iconify-icon> Thêm {{ config.label }}
      </button>
    </div>

    <div class="ui-panel">
      <div class="ui-filters">
        <div class="ui-field">
          <label>Tìm kiếm</label>
          <input v-model="filters.keyword" class="ui-input" :placeholder="`Mã, tên hoặc chú thích ${config.label}`" />
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
          <button class="ui-btn" @click="resetFilters">Đặt lại bộ lọc</button>
        </div>
      </div>
    </div>

    <div class="ui-panel">
      <div class="ui-table-wrap">
        <table class="ui-table">
          <thead>
            <tr>
              <th style="width:60px">STT</th>
              <th style="width:110px">Mã</th>
              <th>Tên {{ config.label }}</th>
              <th>Chú thích</th>
              <th style="width:120px">Ngày tạo</th>
              <th style="width:150px">Trạng thái</th>
              <th style="width:120px">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="ui-empty">Đang tải...</td></tr>
            <tr v-else-if="!pageItems.length"><td colspan="7" class="ui-empty">Không có {{ config.label }} nào.</td></tr>
            <tr v-for="(item, i) in pageItems" :key="item.id">
              <td>{{ (page - 1) * pageSize + i + 1 }}</td>
              <td>{{ item.ma }}</td>
              <td><strong>{{ item.ten }}</strong></td>
              <td class="ui-muted">{{ item.moTa || '—' }}</td>
              <td>{{ formatDate(item.ngayTao) }}</td>
              <td>
                <span class="ui-badge" :class="{ on: item.hoatDong }">{{ item.hoatDong ? 'Hoạt động' : 'Ngừng hoạt động' }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;align-items:center">
                  <button class="ui-btn icon" title="Chỉnh sửa" @click="router.push(`${config.route}/sua/${item.id}`)">
                    <iconify-icon icon="solar:pen-linear"></iconify-icon>
                  </button>
                  <label class="ui-switch" :title="item.hoatDong ? 'Chuyển sang Ngừng hoạt động' : 'Chuyển sang Hoạt động'">
                    <input type="checkbox" :checked="item.hoatDong" @change="toggle(item, $event)" />
                    <span></span>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import { errorMessage, formatDate } from '@/services/api/productAPI.js'

/**
 * Màn danh sách dùng chung cho mọi thuộc tính sản phẩm.
 * config: { label: 'thương hiệu', route: '/thuoc-tinh/thuong-hieu', api: thuocTinhApi(...), tenField, maField }
 */
const props = defineProps({ config: { type: Object, required: true } })
const config = props.config
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const items = ref([])
const loading = ref(false)
const filters = reactive({ keyword: '', trangThai: '' })
const page = ref(1)
const pageSize = 10

const normalize = (s) => (s || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase().trim()

const filtered = computed(() => {
  const kw = normalize(filters.keyword)
  return items.value.filter((it) => {
    if (filters.trangThai === 'active' && !it.hoatDong) return false
    if (filters.trangThai === 'inactive' && it.hoatDong) return false
    if (!kw) return true
    return [it.ma, it.ten, it.moTa].some((v) => normalize(v).includes(kw))
  })
})
const pageItems = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))
const soHoatDong = computed(() => items.value.filter((i) => i.hoatDong).length)
watch(filters, () => { page.value = 1 })

const load = async () => {
  loading.value = true
  try {
    const { data } = await config.api.all()
    items.value = data.map((d) => ({
      id: d.id,
      ma: d[config.maField],
      ten: d[config.tenField],
      moTa: d.moTa,
      ngayTao: d.ngayTao,
      hoatDong: !d.deleted
    }))
  } catch (e) {
    toast.error(errorMessage(e, `Không tải được danh sách ${config.label}`))
  } finally {
    loading.value = false
  }
}

const resetFilters = () => { filters.keyword = ''; filters.trangThai = '' }

const toggle = async (item, event) => {
  const tat = item.hoatDong
  const details = [`${config.label[0].toUpperCase() + config.label.slice(1)}: ${item.ten} (${item.ma})`]
  let message
  if (tat) {
    let soSanPham = 0
    try { soSanPham = (await config.api.usage(item.id)).data.soSanPham } catch { /* vẫn cho hỏi xác nhận */ }
    message = soSanPham > 0
      ? `Đang có ${soSanPham} sản phẩm sử dụng ${config.label} này. Sau khi ngừng hoạt động, ${config.label} sẽ không thể chọn cho sản phẩm mới; các sản phẩm hiện có vẫn giữ nguyên.`
      : `${config.label[0].toUpperCase() + config.label.slice(1)} sẽ không thể chọn khi tạo / sửa sản phẩm.`
    if (soSanPham > 0) details.push(`Số sản phẩm đang sử dụng: ${soSanPham}`)
  } else {
    message = `${config.label[0].toUpperCase() + config.label.slice(1)} sẽ được chọn lại được khi tạo / sửa sản phẩm.`
  }
  const ok = await confirm({
    title: tat ? `Ngừng hoạt động ${config.label}?` : `Chuyển ${config.label} sang Hoạt động?`,
    message,
    details,
    confirmText: tat ? 'Ngừng hoạt động' : 'Hoạt động',
    tone: tat ? 'warning' : 'info'
  })
  if (!ok) { event.target.checked = item.hoatDong; return }
  try {
    const { data } = await config.api.toggleStatus(item.id)
    toast.success(data.message || 'Đã cập nhật trạng thái')
    await load()
  } catch (e) {
    event.target.checked = item.hoatDong
    toast.error(errorMessage(e))
  }
}

onMounted(load)
</script>
