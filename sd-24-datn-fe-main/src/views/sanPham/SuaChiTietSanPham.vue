<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Sửa chi tiết sản phẩm</h1>
        <p class="ui-sub" v-if="detail">{{ detail.ma }} · {{ detail.maSanPham }} - {{ detail.tenSanPham }}</p>
      </div>
      <button class="ui-btn" @click="goBack"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại</button>
    </div>

    <div class="ui-panel" v-if="detail" style="max-width:820px">
      <dl class="ui-dl" style="margin-bottom:16px">
        <dt>Mã chi tiết</dt><dd>{{ detail.ma }}</dd>
        <dt>Đã bán</dt><dd>{{ detail.soLuongDaBan }}</dd>
        <dt>Trạng thái</dt><dd><span class="ui-badge" :class="{ on: detail.trangThai }">{{ detail.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}</span></dd>
      </dl>
      <div class="ui-grid-2">
        <div class="ui-field">
          <label>Màu sắc <span class="req">*</span></label>
          <select v-model="form.idMauSac" class="ui-select">
            <option v-for="c in colorOptions" :key="c.id" :value="c.id">{{ c.tenMauSac }}{{ c.deleted ? ' (ngừng hoạt động)' : '' }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Kích cỡ <span class="req">*</span></label>
          <select v-model="form.idKichCo" class="ui-select">
            <option v-for="s in sizeOptions" :key="s.id" :value="s.id">{{ s.tenKichCo }}{{ s.deleted ? ' (ngừng hoạt động)' : '' }}</option>
          </select>
        </div>
        <div class="ui-field">
          <label>Giá bán (đ) <span class="req">*</span></label>
          <input v-model="form.giaBan" type="number" min="1000" step="1000" class="ui-input" :class="{ invalid: errors.giaBan }" />
          <span v-if="errors.giaBan" class="ui-error">{{ errors.giaBan }}</span>
        </div>
        <div class="ui-field">
          <label>Số lượng tồn kho <span class="req">*</span></label>
          <input v-model="form.soLuongTonKho" type="number" min="0" step="1" class="ui-input" :class="{ invalid: errors.soLuongTonKho }" />
          <span v-if="errors.soLuongTonKho" class="ui-error">{{ errors.soLuongTonKho }}</span>
        </div>
        <div class="ui-field">
          <label>Ảnh riêng của biến thể</label>
          <div style="display:flex;gap:10px;align-items:center">
            <img v-if="form.urlAnhSanPham" :src="imageUrl(form.urlAnhSanPham)" class="ui-thumb" style="width:72px;height:72px" alt="" />
            <div v-else class="ui-thumb" style="width:72px;height:72px"></div>
            <label class="ui-btn"><input type="file" accept="image/png,image/jpeg,image/webp" style="display:none" @change="onFile" />{{ uploading ? 'Đang tải...' : 'Chọn ảnh' }}</label>
          </div>
          <span v-if="errors.anh" class="ui-error">{{ errors.anh }}</span>
          <span v-else class="ui-hint">Bỏ trống thì dùng ảnh đại diện của sản phẩm</span>
        </div>
        <div class="ui-field" style="grid-column:1 / -1">
          <label>Mô tả chi tiết</label>
          <textarea v-model="form.moTaChiTiet" class="ui-textarea" rows="3" maxlength="500"></textarea>
        </div>
      </div>
      <div style="display:flex;gap:8px;margin-top:16px">
        <button class="ui-btn" @click="goBack">Hủy</button>
        <button class="ui-btn primary" :disabled="saving || uploading" @click="save">{{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}</button>
      </div>
    </div>
    <div v-else class="ui-panel ui-empty">Đang tải...</div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import { productService, colorApi, sizeApi, errorMessage, imageUrl, formatMoney } from '@/services/api/productAPI.js'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const detail = ref(null)
const form = reactive({ idMauSac: null, idKichCo: null, giaBan: '', soLuongTonKho: '', moTaChiTiet: '', urlAnhSanPham: '' })
const errors = reactive({ giaBan: '', soLuongTonKho: '', anh: '' })
const colors = ref([])
const sizes = ref([])
const saving = ref(false)
const uploading = ref(false)

const colorOptions = computed(() => colors.value.filter((c) => !c.deleted || c.id === detail.value?.idMauSac))
const sizeOptions = computed(() => sizes.value.filter((s) => !s.deleted || s.id === detail.value?.idKichCo))
const goBack = () => router.push(detail.value ? `/san-pham/chi-tiet/${detail.value.idSanPham}` : '/san-pham')

const validate = () => {
  const g = Number(form.giaBan), s = Number(form.soLuongTonKho)
  errors.giaBan = form.giaBan === '' ? 'Nhập giá bán' : g < 1000 ? 'Giá bán tối thiểu 1.000đ' : g > 100000000 ? 'Tối đa 100.000.000đ' : ''
  errors.soLuongTonKho = form.soLuongTonKho === '' ? 'Nhập số lượng' : !Number.isInteger(s) || s < 0 ? 'Số nguyên ≥ 0' : s > 100000 ? 'Tối đa 100.000' : ''
  return !errors.giaBan && !errors.soLuongTonKho
}

const onFile = async (e) => {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (!/^image\/(png|jpeg|webp)$/.test(file.type) || file.size > 5 * 1024 * 1024) { errors.anh = 'Ảnh JPG/PNG/WEBP, tối đa 5MB'; return }
  errors.anh = ''
  uploading.value = true
  try { form.urlAnhSanPham = await productService.uploadImage(file) } catch { errors.anh = 'Tải ảnh thất bại' } finally { uploading.value = false }
}

const save = async () => {
  if (!validate()) return
  const mau = colors.value.find((c) => c.id === form.idMauSac)?.tenMauSac
  const co = sizes.value.find((s) => s.id === form.idKichCo)?.tenKichCo
  const ok = await confirm({
    title: 'Lưu thay đổi biến thể?',
    details: [`${detail.value.ma}: ${mau} / ${co}`, `Giá bán: ${formatMoney(form.giaBan)}`, `Tồn kho: ${form.soLuongTonKho}`],
    confirmText: 'Lưu'
  })
  if (!ok) return
  saving.value = true
  try {
    await productService.updateProductDetail(route.params.id, {
      idMauSac: form.idMauSac, idKichCo: form.idKichCo, giaBan: Number(form.giaBan),
      soLuongTonKho: Number(form.soLuongTonKho), moTaChiTiet: form.moTaChiTiet?.trim() || null,
      urlAnhSanPham: form.urlAnhSanPham || null
    })
    toast.success('Đã lưu thay đổi')
    goBack()
  } catch (e) {
    toast.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [d, c, s] = await Promise.all([productService.getProductDetailById(route.params.id), colorApi.all(), sizeApi.all()])
    detail.value = d.data; colors.value = c.data; sizes.value = s.data
    Object.assign(form, {
      idMauSac: d.data.idMauSac, idKichCo: d.data.idKichCo, giaBan: d.data.giaBan, soLuongTonKho: d.data.soLuongTonKho,
      moTaChiTiet: d.data.moTaChiTiet || '', urlAnhSanPham: d.data.urlAnhSanPham && d.data.urlAnhSanPham !== '' ? d.data.urlAnhSanPham : ''
    })
  } catch (e) {
    toast.error(errorMessage(e, 'Không tìm thấy chi tiết sản phẩm'))
    router.push('/san-pham')
  }
})
</script>
