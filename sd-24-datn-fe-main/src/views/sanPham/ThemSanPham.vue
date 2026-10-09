<template>
  <div class="ui-page">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Thêm sản phẩm</h1>
        <p class="ui-sub">Nhập thông tin chung rồi chọn màu sắc, kích cỡ để tạo các biến thể. Dự án chỉ dùng giá bán.</p>
      </div>
      <button class="ui-btn" @click="router.push('/san-pham')"><iconify-icon icon="solar:arrow-left-linear"></iconify-icon> Quay lại</button>
    </div>

    <div class="ui-panel">
      <h3 class="ui-panel-title">Thông tin chung</h3>
      <SanPhamInfoForm ref="infoForm" :model="model" />
    </div>

    <div class="ui-panel">
      <h3 class="ui-panel-title">Biến thể (màu sắc × kích cỡ)</h3>
      <BienTheEditor ref="editor" v-model="variants" :colors="colors" :sizes="sizes" />
    </div>

    <div style="display:flex;gap:8px">
      <button class="ui-btn" @click="router.push('/san-pham')">Hủy</button>
      <button class="ui-btn primary" :disabled="saving" @click="save">{{ saving ? 'Đang lưu...' : 'Thêm sản phẩm' }}</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { useConfirm } from '@/composables/useConfirm.js'
import SanPhamInfoForm from '@/components/sanPham/SanPhamInfoForm.vue'
import BienTheEditor from '@/components/sanPham/BienTheEditor.vue'
import { productService, colorApi, sizeApi, errorMessage, formatMoney } from '@/services/api/productAPI.js'

const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const model = reactive({ tenSanPham: '', idThuongHieu: '', idDanhMuc: '', idChatLieu: '', moTaSanPham: '', urlAnhDaiDien: '' })
const variants = ref([])
const colors = ref([])
const sizes = ref([])
const infoForm = ref(null)
const editor = ref(null)
const saving = ref(false)

const save = async () => {
  const infoOk = await infoForm.value.validate()
  const varOk = editor.value.validate()
  if (!infoOk) { toast.error('Vui lòng kiểm tra lại thông tin chung'); return }
  if (!variants.value.length) { toast.error('Vui lòng chọn ít nhất một màu sắc và một kích cỡ'); return }
  if (!varOk) { toast.error('Vui lòng nhập đúng giá bán và số lượng cho mọi biến thể'); return }

  const sp = infoForm.value.payload()
  const ok = await confirm({
    title: 'Thêm sản phẩm mới?',
    message: 'Sản phẩm và các biến thể sẽ ở trạng thái Hoạt động.',
    details: [`Tên: ${sp.tenSanPham}`, `Số biến thể: ${variants.value.length}`,
      ...variants.value.slice(0, 6).map((v) => `${v.tenMauSac} / ${v.tenKichCo}: ${formatMoney(v.giaBan)} × ${v.soLuongTonKho}`),
      ...(variants.value.length > 6 ? [`... và ${variants.value.length - 6} biến thể khác`] : [])],
    confirmText: 'Thêm'
  })
  if (!ok) return

  saving.value = true
  try {
    const { data } = await productService.createProductWithVariants({
      sanPham: sp,
      chiTietSanPhams: variants.value.map(({ idMauSac, idKichCo, giaBan, soLuongTonKho }) => ({ idMauSac, idKichCo, giaBan, soLuongTonKho }))
    })
    toast.success(`Đã thêm sản phẩm ${data.ma}`)
    router.push(`/san-pham/chi-tiet/${data.id}`)
  } catch (e) {
    toast.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [c, s] = await Promise.all([colorApi.active(), sizeApi.active()])
    colors.value = c.data; sizes.value = s.data
  } catch (e) { toast.error(errorMessage(e, 'Không tải được màu sắc / kích cỡ')) }
})
</script>
