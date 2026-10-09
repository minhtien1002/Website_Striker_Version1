<template>
  <div>
    <div class="ui-grid-2">
      <div class="ui-field" style="grid-column:1 / -1">
        <label>Tên sản phẩm <span class="req">*</span></label>
        <input v-model="model.tenSanPham" class="ui-input" :class="{ invalid: errors.tenSanPham }" maxlength="255"
               placeholder="VD: Áo bóng đá Nike Academy 23" @input="onNameInput" />
        <span v-if="errors.tenSanPham" class="ui-error">{{ errors.tenSanPham }}</span>
        <span v-else class="ui-hint">3–255 ký tự, không trùng tên sản phẩm đã có (không phân biệt hoa thường).</span>
      </div>
      <div class="ui-field">
        <label>Thương hiệu <span class="req">*</span></label>
        <select v-model="model.idThuongHieu" class="ui-select" :class="{ invalid: errors.idThuongHieu }" @change="errors.idThuongHieu = ''">
          <option value="">-- Chọn thương hiệu --</option>
          <option v-for="b in brandOptions" :key="b.id" :value="b.id">{{ b.tenThuongHieu }}{{ b.deleted ? ' (ngừng hoạt động)' : '' }}</option>
        </select>
        <span v-if="errors.idThuongHieu" class="ui-error">{{ errors.idThuongHieu }}</span>
      </div>
      <div class="ui-field">
        <label>Thể loại <span class="req">*</span></label>
        <select v-model="model.idDanhMuc" class="ui-select" :class="{ invalid: errors.idDanhMuc }" @change="errors.idDanhMuc = ''">
          <option value="">-- Chọn thể loại --</option>
          <option v-for="c in categoryOptions" :key="c.id" :value="c.id">{{ c.tenDanhMuc }}{{ c.deleted ? ' (ngừng hoạt động)' : '' }}</option>
        </select>
        <span v-if="errors.idDanhMuc" class="ui-error">{{ errors.idDanhMuc }}</span>
      </div>
      <div class="ui-field">
        <label>Chất liệu <span class="req">*</span></label>
        <select v-model="model.idChatLieu" class="ui-select" :class="{ invalid: errors.idChatLieu }" @change="errors.idChatLieu = ''">
          <option value="">-- Chọn chất liệu --</option>
          <option v-for="m in materialOptions" :key="m.id" :value="m.id">{{ m.tenChatLieu }}{{ m.deleted ? ' (ngừng hoạt động)' : '' }}</option>
        </select>
        <span v-if="errors.idChatLieu" class="ui-error">{{ errors.idChatLieu }}</span>
      </div>
      <div class="ui-field">
        <label>Ảnh đại diện</label>
        <div style="display:flex;gap:10px;align-items:center">
          <img v-if="model.urlAnhDaiDien" :src="imageUrl(model.urlAnhDaiDien)" class="ui-thumb" style="width:72px;height:72px" alt="" />
          <div v-else class="ui-thumb" style="width:72px;height:72px"></div>
          <label class="ui-btn">
            <input type="file" accept="image/png,image/jpeg,image/webp" style="display:none" @change="onFile" />
            {{ uploading ? 'Đang tải ảnh...' : 'Chọn ảnh' }}
          </label>
        </div>
        <span v-if="errors.anh" class="ui-error">{{ errors.anh }}</span>
        <span v-else class="ui-hint">JPG, PNG, WEBP · tối đa 5MB</span>
      </div>
      <div class="ui-field" style="grid-column:1 / -1">
        <label>Mô tả</label>
        <textarea v-model="model.moTaSanPham" class="ui-textarea" rows="4" maxlength="2000" placeholder="Mô tả sản phẩm (không bắt buộc)"></textarea>
        <span class="ui-hint">{{ (model.moTaSanPham || '').length }}/2000 ký tự</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { productService, brandApi, categoryApi, materialApi, imageUrl } from '@/services/api/productAPI.js'

/**
 * Form thông tin chung của sản phẩm. Combobox chỉ liệt kê thuộc tính đang hoạt động; khi sửa,
 * thuộc tính đã ngừng nhưng sản phẩm đang dùng vẫn hiện (kèm ghi chú) để không mất dữ liệu.
 */
const props = defineProps({
  model: { type: Object, required: true }, // { tenSanPham, idThuongHieu, idDanhMuc, idChatLieu, moTaSanPham, urlAnhDaiDien }
  excludeId: { type: [Number, String], default: null }
})
const model = props.model
const errors = reactive({ tenSanPham: '', idThuongHieu: '', idDanhMuc: '', idChatLieu: '', anh: '' })
const all = reactive({ brands: [], categories: [], materials: [] })
const uploading = ref(false)
const initial = reactive({ idThuongHieu: null, idDanhMuc: null, idChatLieu: null })

const keep = (list, currentId) => list.filter((x) => !x.deleted || x.id === currentId)
const brandOptions = computed(() => keep(all.brands, initial.idThuongHieu))
const categoryOptions = computed(() => keep(all.categories, initial.idDanhMuc))
const materialOptions = computed(() => keep(all.materials, initial.idChatLieu))

const chuanHoa = (s) => (s || '').trim().replace(/\s+/g, ' ')
let timer = null

const validateTen = () => {
  const t = chuanHoa(model.tenSanPham)
  errors.tenSanPham = !t ? 'Vui lòng nhập tên sản phẩm' : t.length < 3 ? 'Tên sản phẩm phải có ít nhất 3 ký tự' : t.length > 255 ? 'Tối đa 255 ký tự' : ''
  return !errors.tenSanPham
}

const checkTrung = async () => {
  if (!validateTen()) return false
  try {
    const { data } = await productService.checkProductName(chuanHoa(model.tenSanPham), props.excludeId)
    if (data.exists) { errors.tenSanPham = 'Tên sản phẩm đã tồn tại (không phân biệt chữ hoa, chữ thường)'; return true }
  } catch { /* backend kiểm tra lại khi lưu */ }
  return false
}

const onNameInput = () => { validateTen(); clearTimeout(timer); timer = setTimeout(checkTrung, 400) }

const onFile = async (e) => {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (!/^image\/(png|jpeg|webp)$/.test(file.type)) { errors.anh = 'Chỉ chấp nhận ảnh JPG, PNG, WEBP'; return }
  if (file.size > 5 * 1024 * 1024) { errors.anh = 'Ảnh tối đa 5MB'; return }
  errors.anh = ''
  uploading.value = true
  try { model.urlAnhDaiDien = await productService.uploadImage(file) }
  catch { errors.anh = 'Tải ảnh thất bại, vui lòng thử lại' }
  finally { uploading.value = false }
}

/** Kiểm tra toàn bộ form. Trả true nếu hợp lệ. */
const validate = async () => {
  validateTen()
  errors.idThuongHieu = model.idThuongHieu ? '' : 'Vui lòng chọn thương hiệu'
  errors.idDanhMuc = model.idDanhMuc ? '' : 'Vui lòng chọn thể loại'
  errors.idChatLieu = model.idChatLieu ? '' : 'Vui lòng chọn chất liệu'
  if (Object.values(errors).some(Boolean) || uploading.value) return false
  return !(await checkTrung())
}

const payload = () => ({
  tenSanPham: chuanHoa(model.tenSanPham),
  idThuongHieu: Number(model.idThuongHieu),
  idDanhMuc: Number(model.idDanhMuc),
  idChatLieu: Number(model.idChatLieu),
  moTaSanPham: model.moTaSanPham?.trim() || null,
  urlAnhDaiDien: model.urlAnhDaiDien || null
})

const setInitial = () => Object.assign(initial, { idThuongHieu: model.idThuongHieu, idDanhMuc: model.idDanhMuc, idChatLieu: model.idChatLieu })

defineExpose({ validate, payload, setInitial, errors })

onMounted(async () => {
  const [b, c, m] = await Promise.all([brandApi.all(), categoryApi.all(), materialApi.all()])
  all.brands = b.data; all.categories = c.data; all.materials = m.data
})
</script>
