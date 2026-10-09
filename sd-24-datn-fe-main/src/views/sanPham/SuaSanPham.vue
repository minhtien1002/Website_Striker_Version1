<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Sửa sản phẩm</h1>
        <p class="ui-sub" v-if="ma">Mã {{ ma }} · Biến thể (màu, kích cỡ, giá bán, số lượng) sửa ở màn chi tiết sản phẩm.</p>
      </div>
      <button class="ui-btn" @click="goBack"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại</button>
    </div>

    <div class="ui-panel" v-if="loaded">
      <SanPhamInfoForm ref="infoForm" :model="model" :exclude-id="route.params.id" />
      <div style="display:flex;gap:8px;margin-top:16px">
        <button class="ui-btn" @click="goBack">Hủy</button>
        <button class="ui-btn primary" :disabled="saving" @click="save">{{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}</button>
      </div>
    </div>
    <div v-else class="ui-panel ui-empty">Đang tải...</div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import SanPhamInfoForm from '@/components/sanPham/SanPhamInfoForm.vue'
import { productService, errorMessage } from '@/services/api/productAPI.js'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const model = reactive({ tenSanPham: '', idThuongHieu: '', idDanhMuc: '', idChatLieu: '', moTaSanPham: '', urlAnhDaiDien: '' })
const ma = ref('')
const loaded = ref(false)
const saving = ref(false)
const infoForm = ref(null)

const goBack = () => router.push(`/san-pham/chi-tiet/${route.params.id}`)

const save = async () => {
  if (!(await infoForm.value.validate())) { toast.error('Vui lòng kiểm tra lại thông tin'); return }
  const p = infoForm.value.payload()
  const ok = await confirm({ title: 'Lưu thay đổi sản phẩm?', details: [`${ma.value} - ${p.tenSanPham}`], confirmText: 'Lưu' })
  if (!ok) return
  saving.value = true
  try {
    await productService.updateProduct(route.params.id, p)
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
    const { data } = await productService.getProductById(route.params.id)
    Object.assign(model, {
      tenSanPham: data.tenSanPham, idThuongHieu: data.idThuongHieu || '', idDanhMuc: data.idDanhMuc || '',
      idChatLieu: data.idChatLieu || '', moTaSanPham: data.moTa || '', urlAnhDaiDien: data.urlAnhDaiDien || ''
    })
    ma.value = data.ma
    loaded.value = true
    await nextTick()
    infoForm.value.setInitial()
  } catch (e) {
    toast.error(errorMessage(e, 'Không tìm thấy sản phẩm'))
    router.push('/san-pham')
  }
})
</script>
