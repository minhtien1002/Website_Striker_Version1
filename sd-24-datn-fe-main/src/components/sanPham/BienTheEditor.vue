<template>
  <div>
    <div class="ui-grid-2" style="margin-bottom:12px">
      <div class="ui-field">
        <label>Màu sắc <span class="req">*</span></label>
        <div class="chips">
          <label v-for="c in colors" :key="c.id" class="chip" :class="{ on: selectedColors.includes(c.id) }">
            <input type="checkbox" :value="c.id" v-model="selectedColors" /> {{ c.tenMauSac }}
          </label>
          <span v-if="!colors.length" class="ui-hint">Chưa có màu sắc đang hoạt động</span>
        </div>
      </div>
      <div class="ui-field">
        <label>Kích cỡ <span class="req">*</span></label>
        <div class="chips">
          <label v-for="s in sizes" :key="s.id" class="chip" :class="{ on: selectedSizes.includes(s.id) }">
            <input type="checkbox" :value="s.id" v-model="selectedSizes" /> {{ s.tenKichCo }}
          </label>
          <span v-if="!sizes.length" class="ui-hint">Chưa có kích cỡ đang hoạt động</span>
        </div>
      </div>
    </div>

    <div v-if="rows.length" class="ui-filters" style="margin-bottom:10px">
      <div class="ui-field">
        <label>Giá bán chung (đ)</label>
        <input v-model="bulk.giaBan" type="number" min="1000" step="1000" class="ui-input" placeholder="VD 250000" />
      </div>
      <div class="ui-field">
        <label>Số lượng chung</label>
        <input v-model="bulk.soLuong" type="number" min="0" step="1" class="ui-input" placeholder="VD 20" />
      </div>
      <div class="ui-field">
        <button type="button" class="ui-btn" @click="applyBulk">Áp dụng cho tất cả</button>
      </div>
    </div>

    <div class="ui-table-wrap" v-if="rows.length">
      <table class="ui-table">
        <thead>
          <tr>
            <th>Màu sắc</th>
            <th>Kích cỡ</th>
            <th style="width:200px">Giá bán (đ) *</th>
            <th style="width:160px">Số lượng *</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.key">
            <td>{{ r.tenMauSac }}</td>
            <td>{{ r.tenKichCo }}</td>
            <td>
              <input v-model="r.giaBan" type="number" min="1000" step="1000" class="ui-input" :class="{ invalid: r.errGia }" @input="validateRow(r)" />
              <span v-if="r.errGia" class="ui-error">{{ r.errGia }}</span>
            </td>
            <td>
              <input v-model="r.soLuongTonKho" type="number" min="0" step="1" class="ui-input" :class="{ invalid: r.errSl }" @input="validateRow(r)" />
              <span v-if="r.errSl" class="ui-error">{{ r.errSl }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="ui-hint">Chọn ít nhất một màu sắc và một kích cỡ để tạo biến thể.</p>
    <p v-if="skipped" class="ui-hint">Đã bỏ qua {{ skipped }} cặp màu / kích cỡ mà sản phẩm đã có.</p>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'

/**
 * Sinh danh sách biến thể = màu sắc × kích cỡ đã chọn. Chỉ có giá bán (dự án không dùng giá nhập).
 * Cặp màu/kích cỡ đã tồn tại trong sản phẩm (existingPairs) được bỏ qua.
 */
const props = defineProps({
  colors: { type: Array, default: () => [] },
  sizes: { type: Array, default: () => [] },
  existingPairs: { type: Array, default: () => [] } // ["idMau-idCo"]
})
const emit = defineEmits(['update:modelValue'])

const selectedColors = ref([])
const selectedSizes = ref([])
const rows = ref([])
const bulk = reactive({ giaBan: '', soLuong: '' })

const skipped = computed(() => {
  let n = 0
  for (const c of selectedColors.value) for (const s of selectedSizes.value) if (props.existingPairs.includes(`${c}-${s}`)) n++
  return n
})

watch([selectedColors, selectedSizes], () => {
  const old = Object.fromEntries(rows.value.map((r) => [r.key, r]))
  const next = []
  for (const cId of selectedColors.value) {
    for (const sId of selectedSizes.value) {
      const key = `${cId}-${sId}`
      if (props.existingPairs.includes(key)) continue
      const c = props.colors.find((x) => x.id === cId)
      const s = props.sizes.find((x) => x.id === sId)
      next.push(old[key] || {
        key, idMauSac: cId, idKichCo: sId, tenMauSac: c?.tenMauSac, tenKichCo: s?.tenKichCo,
        giaBan: bulk.giaBan || '', soLuongTonKho: bulk.soLuong || '', errGia: '', errSl: ''
      })
    }
  }
  rows.value = next
  emitValue()
}, { deep: true })

const validateRow = (r) => {
  const gia = Number(r.giaBan)
  const sl = Number(r.soLuongTonKho)
  r.errGia = r.giaBan === '' ? 'Nhập giá bán' : !Number.isFinite(gia) || gia < 1000 ? 'Tối thiểu 1.000đ' : gia > 100000000 ? 'Tối đa 100.000.000đ' : ''
  r.errSl = r.soLuongTonKho === '' ? 'Nhập số lượng' : !Number.isInteger(sl) || sl < 0 ? 'Số nguyên ≥ 0' : sl > 100000 ? 'Tối đa 100.000' : ''
  emitValue()
}

const applyBulk = () => {
  rows.value.forEach((r) => {
    if (bulk.giaBan !== '') r.giaBan = bulk.giaBan
    if (bulk.soLuong !== '') r.soLuongTonKho = bulk.soLuong
    validateRow(r)
  })
}

const emitValue = () => {
  emit('update:modelValue', rows.value.map((r) => ({
    idMauSac: r.idMauSac, idKichCo: r.idKichCo, tenMauSac: r.tenMauSac, tenKichCo: r.tenKichCo,
    giaBan: r.giaBan === '' ? null : Number(r.giaBan),
    soLuongTonKho: r.soLuongTonKho === '' ? null : Number(r.soLuongTonKho)
  })))
}

/** Gọi trước khi lưu: trả true nếu mọi dòng hợp lệ. */
const validate = () => {
  rows.value.forEach(validateRow)
  return rows.value.length > 0 && rows.value.every((r) => !r.errGia && !r.errSl)
}

const reset = () => { selectedColors.value = []; selectedSizes.value = []; bulk.giaBan = ''; bulk.soLuong = '' }

defineExpose({ validate, reset })
</script>

<style scoped>
.chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border: 1px solid var(--line); background: #fff; cursor: pointer; font-size: .85rem; font-weight: 600; }
.chip input { display: none; }
.chip.on { background: var(--accent); border-color: var(--accent); color: #fff; }
</style>
