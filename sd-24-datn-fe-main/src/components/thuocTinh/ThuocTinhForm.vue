<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">{{ isEdit ? `Sửa ${config.label}` : `Thêm ${config.label}` }}</h1>
        <p class="ui-sub" v-if="isEdit && original">Mã: {{ original.ma }} · Trạng thái: {{ original.hoatDong ? 'Hoạt động' : 'Ngừng hoạt động' }}</p>
      </div>
      <button class="ui-btn" @click="goBack">
        <iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại
      </button>
    </div>

    <form class="ui-panel" style="max-width:720px" @submit.prevent="save" novalidate>
      <div class="ui-field" style="margin-bottom:14px">
        <label>Tên {{ config.label }} <span class="req">*</span></label>
        <input v-model="form.ten" class="ui-input" :class="{ invalid: errors.ten }" :maxlength="maxLength"
               :placeholder="`Nhập tên ${config.label}`" @input="onNameInput" />
        <span v-if="errors.ten" class="ui-error">{{ errors.ten }}</span>
        <span v-else class="ui-hint">Không được trùng tên đã có (không phân biệt chữ hoa, chữ thường; VD "{{ config.example }}"). Tối đa {{ maxLength }} ký tự.</span>
      </div>

      <div class="ui-field" style="margin-bottom:18px">
        <label>Chú thích</label>
        <textarea v-model="form.moTa" class="ui-textarea" :class="{ invalid: errors.moTa }" rows="4" maxlength="500"
                  :placeholder="`Ghi chú thêm về ${config.label} (không bắt buộc)`"></textarea>
        <span v-if="errors.moTa" class="ui-error">{{ errors.moTa }}</span>
        <span v-else class="ui-hint">{{ (form.moTa || '').length }}/500 ký tự</span>
      </div>

      <div style="display:flex;gap:8px">
        <button type="button" class="ui-btn" @click="goBack">Hủy</button>
        <button type="submit" class="ui-btn primary" :disabled="saving">
          {{ saving ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : `Thêm ${config.label}` }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import { errorMessage } from '@/services/api/productAPI.js'

const props = defineProps({ config: { type: Object, required: true } })
const config = props.config
const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const isEdit = computed(() => !!route.params.id)
const maxLength = config.maxLength || 100
const form = reactive({ ten: '', moTa: '' })
const errors = reactive({ ten: '', moTa: '' })
const original = ref(null)
const saving = ref(false)

const chuanHoa = (s) => (s || '').trim().replace(/\s+/g, ' ')
let checkTimer = null

const validateTen = () => {
  const ten = chuanHoa(form.ten)
  if (!ten) return (errors.ten = `Vui lòng nhập tên ${config.label}`)
  if (ten.length > maxLength) return (errors.ten = `Tên tối đa ${maxLength} ký tự`)
  if (!/^[\p{L}\p{N} .,&()'/+%-]+$/u.test(ten)) return (errors.ten = 'Tên chứa ký tự không hợp lệ')
  errors.ten = ''
  return ''
}

const checkTrung = async () => {
  const ten = chuanHoa(form.ten)
  if (!ten || validateTen()) return false
  try {
    const { data } = await config.api.checkName(ten, isEdit.value ? route.params.id : null)
    if (data.exists) {
      errors.ten = `Tên "${ten}" đã tồn tại (không phân biệt chữ hoa, chữ thường)`
      return true
    }
  } catch { /* backend vẫn kiểm tra lại khi lưu */ }
  return false
}

const onNameInput = () => {
  validateTen()
  clearTimeout(checkTimer)
  checkTimer = setTimeout(checkTrung, 400)
}

const save = async () => {
  validateTen()
  errors.moTa = (form.moTa || '').length > 500 ? 'Chú thích tối đa 500 ký tự' : ''
  if (errors.ten || errors.moTa) return
  if (await checkTrung()) return

  const ten = chuanHoa(form.ten)
  const ok = await confirm({
    title: isEdit.value ? `Lưu thay đổi ${config.label}?` : `Thêm ${config.label} mới?`,
    message: isEdit.value ? 'Thông tin mới sẽ áp dụng cho mọi sản phẩm đang dùng.' : `${config.label[0].toUpperCase() + config.label.slice(1)} mới sẽ ở trạng thái Hoạt động.`,
    details: [`Tên: ${ten}`, ...(form.moTa?.trim() ? [`Chú thích: ${form.moTa.trim()}`] : [])],
    confirmText: isEdit.value ? 'Lưu' : 'Thêm'
  })
  if (!ok) return

  saving.value = true
  try {
    const payload = { ten, moTa: form.moTa?.trim() || null }
    if (isEdit.value) await config.api.update(route.params.id, payload)
    else await config.api.create(payload)
    toast.success(isEdit.value ? 'Đã lưu thay đổi' : `Đã thêm ${config.label} "${ten}"`)
    router.push(config.route)
  } catch (e) {
    const msg = errorMessage(e)
    if (e?.response?.status === 409) errors.ten = msg
    toast.error(msg)
  } finally {
    saving.value = false
  }
}

const goBack = () => router.push(config.route)

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const { data } = await config.api.getById(route.params.id)
    form.ten = data[config.tenField] || ''
    form.moTa = data.moTa || ''
    original.value = { ma: data[config.maField], hoatDong: !data.deleted }
  } catch (e) {
    toast.error(errorMessage(e, `Không tìm thấy ${config.label}`))
    goBack()
  }
})
</script>
