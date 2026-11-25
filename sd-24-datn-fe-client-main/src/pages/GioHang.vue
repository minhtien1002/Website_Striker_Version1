<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import axios from 'axios'
import { cartService, cartAPI } from '@/services/cartAPI.js'
import { customerAPI } from '@/api/customerAPI.js'
import authService from '@/services/authService.js'
import { imageUrl } from '@/services/productAPI.js'
import { useConfirm } from '@/composables/useConfirm.js'

/**
 * Giỏ hàng + thanh toán trên MỘT màn:
 *  - tích chọn sản phẩm muốn thanh toán (phần không chọn được giữ lại trong giỏ),
 *  - thông tin khách / địa chỉ mặc định hiện sẵn, đổi được ngay tại chỗ,
 *  - phí vận chuyển lấy từ API theo địa chỉ, phiếu giảm giá tốt nhất được chọn tự động,
 *  - chỉ thao tác rủi ro (xóa/chốt đơn) mới hỏi xác nhận.
 */
const API = 'http://localhost:8080/api'
// Cập nhật sang API v2 (2 cấp: Tỉnh/Thành phố -> Phường/Xã)
const PROVINCE_API = 'https://provinces.open-api.vn/api/v2' 
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const user = authService.getUser?.() || JSON.parse(localStorage.getItem('user_info') || 'null')
const isLoggedIn = !!(user && localStorage.getItem('is_logged_in') === 'true')
const customerId = user?.customerId || null

const money = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0)
const errMsg = (e, fb = 'Có lỗi xảy ra, vui lòng thử lại') => {
  const d = e?.response?.data
  return typeof d === 'string' && d ? d : d?.message || e?.message || fb
}

// ---------------- Giỏ hàng ----------------
const loading = ref(true)
const busy = ref(false)
const items = ref([])
const selected = ref([])

const loadCart = async () => {
  try {
    const cart = await cartService.getCart()
    items.value = cart?.items || []
    // Mặc định chọn tất cả, giữ lựa chọn cũ nếu đã có
    const ids = items.value.map((i) => i.id)
    selected.value = selected.value.length ? selected.value.filter((id) => ids.includes(id)) : ids
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

const selectedItems = computed(() => items.value.filter((i) => selected.value.includes(i.id)))
const allChecked = computed(() => items.value.length > 0 && selected.value.length === items.value.length)
const toggleAll = (on) => { selected.value = on ? items.value.map((i) => i.id) : [] }
const subtotal = computed(() => selectedItems.value.reduce((s, i) => s + Number(i.gia) * i.soLuong, 0))
const totalQty = computed(() => selectedItems.value.reduce((s, i) => s + i.soLuong, 0))

const changeQty = async (item, qty, ev) => {
  if (!Number.isInteger(qty) || qty < 1) { toast.error('Số lượng phải lớn hơn 0'); if (ev) ev.target.value = item.soLuong; return }
  if (qty > item.soLuongTonKho) { toast.error(`Chỉ còn ${item.soLuongTonKho} sản phẩm`); if (ev) ev.target.value = item.soLuong; return }
  busy.value = true
  try { await cartService.updateQuantity(item.id, qty); await loadCart() }
  catch (e) { toast.error(errMsg(e)); if (ev) ev.target.value = item.soLuong }
  finally { busy.value = false }
}

const removeItem = async (item) => {
  const ok = await confirm({ title: 'Xóa sản phẩm khỏi giỏ hàng?', details: [`${item.tenSanPham} (${item.tenMauSac} / ${item.tenKichCo}) × ${item.soLuong}`], confirmText: 'Xóa', tone: 'danger' })
  if (!ok) return
  busy.value = true
  try { await cartService.removeItem(item.id); await loadCart(); toast.info('Đã xóa sản phẩm khỏi giỏ hàng') }
  catch (e) { toast.error(errMsg(e)) }
  finally { busy.value = false }
}

// ---------------- Thông tin nhận hàng ----------------
const form = reactive({ ten: '', soDienThoai: '', email: '', ghiChu: '' })
const errors = reactive({})
const addresses = ref([])
const addressMode = ref('saved') // saved | new
const selectedAddressId = ref(null)
const newAddr = reactive({ tinh: '', phuong: '', chiTiet: '' })
const provinces = ref([])
const wards = ref([])

const savedAddress = computed(() => addresses.value.find((a) => a.id === selectedAddressId.value) || null)
const fullAddress = computed(() => {
  if (addressMode.value === 'saved' && savedAddress.value) {
    const a = savedAddress.value
    return [a.diaChiCuThe, a.phuong, a.quan, a.thanhPho].filter(Boolean).join(', ')
  }
  if (newAddr.chiTiet.trim() && newAddr.phuong && newAddr.tinh) {
    return `${newAddr.chiTiet.trim()}, ${newAddr.phuong}, ${newAddr.tinh}`
  }
  return ''
})

const loadProfile = async () => {
  if (!isLoggedIn) { addressMode.value = 'new'; return }
  try {
    const p = (await customerAPI.getProfile()).data
    form.ten = p?.ten || ''
    form.soDienThoai = p?.soDienThoai || ''
    form.email = p?.email || ''
  } catch { /* vẫn cho nhập tay */ }
  try {
    addresses.value = (await customerAPI.getAddresses()).data || []
    const macDinh = addresses.value.find((a) => a.macDinh) || addresses.value[0]
    if (macDinh) selectedAddressId.value = macDinh.id
    else addressMode.value = 'new'
  } catch { addressMode.value = 'new' }
}

const loadProvinces = async () => {
  try { 
    provinces.value = (await axios.get(`${PROVINCE_API}/p/`)).data 
  } catch { provinces.value = [] }
}

watch(() => newAddr.tinh, async (name) => {
  newAddr.phuong = ''; wards.value = []
  const p = provinces.value.find((x) => x.name === name)
  if (p) {
    try { 
      const res = await axios.get(`${PROVINCE_API}/p/${p.code}?depth=2`)
      wards.value = res.data.wards || [] 
    } catch { /* bỏ qua */ }
  }
})

// ---------------- Phí vận chuyển (API) ----------------
const shipping = reactive({ fee: null, loading: false, error: '', source: '' })
let shipTimer = null
watch([fullAddress, totalQty], () => {
  clearTimeout(shipTimer)
  shipping.fee = null; shipping.error = ''
  if (!fullAddress.value || !totalQty.value) return
  shipping.loading = true
  shipTimer = setTimeout(async () => {
    try {
      const { data } = await axios.get(`${API}/ban-hang-client/phi-van-chuyen`, { params: { diaChi: fullAddress.value, soLuong: totalQty.value } })
      shipping.fee = Number(data.phiVanChuyen); shipping.source = data.nguon
    } catch (e) { shipping.error = errMsg(e, 'Không tính được phí vận chuyển') }
    finally { shipping.loading = false }
  }, 350)
})

// ---------------- Phiếu giảm giá ----------------
const vouchers = ref([])
const voucherId = ref(null)
const manualVoucher = ref(false)
const voucherCode = ref('')
const voucherCodeMessage = ref('')
const selectedVoucher = computed(() => vouchers.value.find((v) => v.id === voucherId.value && v.hopLe) || null)
const discount = computed(() => Number(selectedVoucher.value?.soTienGiam || 0))
let vTimer = null
watch([subtotal, () => shipping.fee], () => {
  clearTimeout(vTimer)
  vTimer = setTimeout(async () => {
    if (!subtotal.value) { vouchers.value = []; voucherId.value = null; return }
    try {
      const { data } = await axios.get(`${API}/phieu-giam-gia/kha-dung`, { params: { tongTien: subtotal.value, phiVanChuyen: shipping.fee || 0, ...(customerId ? { khachHangId: customerId } : {}) } })
      vouchers.value = data
      if (!manualVoucher.value || !data.find((v) => v.id === voucherId.value && v.hopLe)) {
        voucherId.value = data.find((v) => v.hopLe)?.id ?? null
        manualVoucher.value = false
      }
    } catch { vouchers.value = [] }
  }, 300)
}, { immediate: true })

const applyVoucherCode = () => {
  const code = voucherCode.value.trim().toUpperCase()
  voucherCodeMessage.value = ''
  if (!code) { voucherCodeMessage.value = 'Vui lòng nhập mã phiếu giảm giá'; return }
  const voucher = vouchers.value.find((v) => String(v.ma || '').trim().toUpperCase() === code)
  if (!voucher) { voucherCodeMessage.value = 'Mã không tồn tại hoặc không dành cho tài khoản này'; return }
  if (!voucher.hopLe) { voucherCodeMessage.value = voucher.lyDo || 'Đơn hàng chưa đủ điều kiện áp dụng'; return }
  voucherId.value = voucher.id
  manualVoucher.value = true
  voucherCode.value = voucher.ma
  voucherCodeMessage.value = `Đã áp dụng ${voucher.ma}, giảm ${money(voucher.soTienGiam)}`
}

// ---------------- Thanh toán ----------------
const paymentMethod = ref('COD')
const total = computed(() => Math.max(0, subtotal.value + (shipping.fee || 0) - discount.value))

const validate = () => {
  Object.keys(errors).forEach((k) => delete errors[k])
  const ten = form.ten.trim().replace(/\s+/g, ' ')
  if (ten.length < 2 || !/^[\p{L} ]+$/u.test(ten)) errors.ten = 'Họ tên tối thiểu 2 ký tự, chỉ gồm chữ cái'
  if (!/^0(3|5|7|8|9)\d{8}$/.test(form.soDienThoai.trim())) errors.soDienThoai = 'Số điện thoại 10 số, bắt đầu bằng 03/05/07/08/09'
  if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(form.email.trim())) errors.email = 'Email không đúng định dạng'
  if (!isLoggedIn && !form.email.trim()) errors.email = 'Vui lòng nhập email để nhận thông tin đơn hàng'
  if (!fullAddress.value) errors.diaChi = 'Vui lòng chọn hoặc nhập đầy đủ địa chỉ giao hàng'
  else if (addressMode.value === 'new' && newAddr.chiTiet.trim().length < 5) errors.diaChi = 'Địa chỉ cụ thể tối thiểu 5 ký tự'
  if (!selectedItems.value.length) errors.items = 'Vui lòng tích chọn ít nhất một sản phẩm'
  const vuot = selectedItems.value.find((i) => i.soLuong > i.soLuongTonKho)
  if (vuot) errors.items = `"${vuot.tenSanPham}" chỉ còn ${vuot.soLuongTonKho} sản phẩm`
  return Object.keys(errors).length === 0
}

const placeOrder = async () => {
  if (!validate()) { toast.error(Object.values(errors)[0]); return }
  if (shipping.loading || shipping.fee === null) { toast.error(shipping.error || 'Đang tính phí vận chuyển, vui lòng đợi'); return }
  const ok = await confirm({
    title: 'Xác nhận đặt hàng?',
    message: paymentMethod.value === 'VNPAY' ? 'Bạn sẽ được chuyển sang VNPay để thanh toán.' : 'Bạn thanh toán khi nhận hàng (COD).',
    details: [
      `Người nhận: ${form.ten.trim()} - ${form.soDienThoai.trim()}`,
      `Địa chỉ: ${fullAddress.value}`,
      `Sản phẩm: ${totalQty.value} (${selectedItems.value.length} loại)`,
      `Tiền hàng: ${money(subtotal.value)}`,
      `Phí vận chuyển: ${money(shipping.fee)}`,
      ...(selectedVoucher.value ? [`Phiếu ${selectedVoucher.value.ma}: - ${money(discount.value)}`] : []),
      `Tổng thanh toán: ${money(total.value)}`
    ],
    confirmText: paymentMethod.value === 'VNPAY' ? 'Thanh toán VNPay' : 'Đặt hàng'
  })
  if (!ok) return

  busy.value = true
  const invoiceId = localStorage.getItem('currentInvoiceId')
  let remainderId = null
  try {
    // SỬA LỖI TẠI ĐÂY: Đường dẫn API tách giỏ hàng
    if (selectedItems.value.length < items.value.length) {
      const { data } = await axios.post(`${API}/ban-hang-client/gio-hang/${invoiceId}/tach`, { cartItemIds: selected.value })
      remainderId = data.hoaDonConLai
    }
    const body = {
      tenKhachHang: form.ten.trim().replace(/\s+/g, ' '),
      soDienThoai: form.soDienThoai.trim(),
      email: form.email.trim() || null,
      diaChi: fullAddress.value,
      phuongThucThanhToan: paymentMethod.value,
      ghiChu: form.ghiChu.trim() || '',
      voucherId: selectedVoucher.value?.id ?? null
    }
    if (paymentMethod.value === 'VNPAY') {
      if (remainderId) localStorage.setItem('pendingRemainderInvoiceId', remainderId)
      const { data } = await axios.post(`${API}/client-payment/vnpay/create/${invoiceId}`, body)
      if (!data.paymentUrl) throw new Error('Không tạo được liên kết thanh toán VNPay')
      window.location.href = data.paymentUrl
      return
    }
    const { data: order } = await axios.post(`${API}/ban-hang-client/thanh-toan/${invoiceId}`, body)
    if (remainderId) localStorage.setItem('currentInvoiceId', remainderId)
    else localStorage.removeItem('currentInvoiceId')
    toast.success(`Đặt hàng thành công! Mã đơn ${order.maHoaDon || order.ma || ''}`)
    router.push(isLoggedIn
      ? { path: '/account/purchase-history' }
      : { path: '/orders', query: { orderCode: order.maHoaDon || order.ma, autoSearch: 'true' } })
  } catch (e) {
    // SỬA LỖI TẠI ĐÂY: Đường dẫn API gộp lại giỏ hàng nếu đặt hàng thất bại
    if (remainderId) {
      try { await axios.post(`${API}/ban-hang-client/gio-hang/${remainderId}/gop-vao/${invoiceId}`) } catch { /* bỏ qua */ }
      localStorage.removeItem('pendingRemainderInvoiceId')
    }
    toast.error(errMsg(e, 'Đặt hàng chưa thành công'))
    await loadCart()
  } finally {
    busy.value = false
  }
}

onMounted(async () => {
  const q = router.currentRoute.value.query
  if (q.payment === 'vnpay' && q.status && q.status !== 'success') {
    toast.error('Thanh toán VNPay chưa thành công. Sản phẩm vẫn được giữ trong giỏ hàng, bạn có thể thử lại.')
    await new Promise((r) => setTimeout(r, 400))
  }
  await Promise.all([loadCart(), loadProfile(), loadProvinces()])
})
</script>

<template>
  <div class="cx-page">
    <h1 class="cx-title">Giỏ hàng & thanh toán</h1>
    <p class="cx-sub">Tích chọn sản phẩm muốn mua, kiểm tra thông tin nhận hàng rồi đặt hàng ngay trên trang này.</p>

    <div v-if="loading" class="cx-panel cx-empty">Đang tải giỏ hàng...</div>
    <div v-else-if="!items.length" class="cx-panel cx-empty">
      Giỏ hàng đang trống.
      <div style="margin-top:12px"><router-link to="/products" class="cx-btn primary">Xem sản phẩm</router-link></div>
    </div>

    <div v-else class="cx-grid">
      <div>
        <!-- Sản phẩm -->
        <div class="cx-panel">
          <h3>Sản phẩm ({{ selectedItems.length }}/{{ items.length }} đã chọn)</h3>
          <table class="cx-table">
            <thead>
              <tr>
                <th style="width:36px"><input type="checkbox" :checked="allChecked" @change="toggleAll($event.target.checked)" /></th>
                <th>Sản phẩm</th>
                <th class="cx-num">Đơn giá</th>
                <th style="width:150px">Số lượng</th>
                <th class="cx-num">Thành tiền</th>
                <th style="width:40px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in items" :key="it.id">
                <td><input type="checkbox" :value="it.id" v-model="selected" /></td>
                <td>
                  <div style="display:flex;gap:10px;align-items:center">
                    <img :src="imageUrl(it.urlAnhSanPham)" class="cx-thumb" alt="" />
                    <div>
                      <strong>{{ it.tenSanPham }}</strong>
                      <div class="cx-hint">{{ it.tenMauSac }} / {{ it.tenKichCo }} · Mã {{ it.maSanPham }}</div>
                      <div v-if="it.soLuong > it.soLuongTonKho" class="cx-error">Chỉ còn {{ it.soLuongTonKho }} sản phẩm</div>
                    </div>
                  </div>
                </td>
                <td class="cx-num">{{ money(it.gia) }}</td>
                <td>
                  <div style="display:flex">
                    <button class="cx-btn sm" :disabled="it.soLuong <= 1 || busy" @click="changeQty(it, it.soLuong - 1)">−</button>
                    <input :value="it.soLuong" type="number" min="1" :max="it.soLuongTonKho" class="cx-input" style="width:56px;text-align:center;padding:4px"
                           @change="changeQty(it, Number($event.target.value), $event)" />
                    <button class="cx-btn sm" :disabled="it.soLuong >= it.soLuongTonKho || busy" @click="changeQty(it, it.soLuong + 1)">+</button>
                  </div>
                </td>
                <td class="cx-num">{{ money(Number(it.gia) * it.soLuong) }}</td>
                <td><button class="cx-btn sm" title="Xóa khỏi giỏ" :disabled="busy" @click="removeItem(it)">×</button></td>
              </tr>
            </tbody>
          </table>
          <p v-if="errors.items" class="cx-error">{{ errors.items }}</p>
        </div>

        <!-- Thông tin nhận hàng -->
        <div class="cx-panel">
          <h3>Thông tin nhận hàng</h3>
          <div class="cx-row">
            <div class="cx-field">
              <label>Họ tên người nhận <span class="req">*</span></label>
              <input v-model="form.ten" class="cx-input" :class="{ invalid: errors.ten }" maxlength="100" />
              <span v-if="errors.ten" class="cx-error">{{ errors.ten }}</span>
            </div>
            <div class="cx-field">
              <label>Số điện thoại <span class="req">*</span></label>
              <input v-model="form.soDienThoai" class="cx-input" :class="{ invalid: errors.soDienThoai }" maxlength="10" />
              <span v-if="errors.soDienThoai" class="cx-error">{{ errors.soDienThoai }}</span>
            </div>
          </div>
          <div class="cx-field">
            <label>Email <span v-if="!isLoggedIn" class="req">*</span></label>
            <input v-model="form.email" class="cx-input" :class="{ invalid: errors.email }" maxlength="100" placeholder="Nhận thông tin đơn hàng qua email" />
            <span v-if="errors.email" class="cx-error">{{ errors.email }}</span>
          </div>

          <div class="cx-field">
            <label>Địa chỉ giao hàng <span class="req">*</span></label>
            <div v-if="addresses.length" style="display:flex;gap:6px;margin-bottom:8px">
              <button class="cx-chip" :class="{ on: addressMode === 'saved' }" @click="addressMode = 'saved'">Địa chỉ đã lưu</button>
              <button class="cx-chip" :class="{ on: addressMode === 'new' }" @click="addressMode = 'new'">Địa chỉ khác</button>
            </div>
            <template v-if="addressMode === 'saved' && addresses.length">
              <label v-for="a in addresses" :key="a.id" style="display:flex;gap:8px;align-items:flex-start;font-weight:500;padding:8px;border:1px solid var(--c-line);margin-bottom:6px;cursor:pointer">
                <input type="radio" :value="a.id" v-model="selectedAddressId" />
                <span>
                  {{ [a.diaChiCuThe, a.phuong, a.quan, a.thanhPho].filter(Boolean).join(', ') }} 
                  <span v-if="a.macDinh" class="cx-badge on">Mặc định</span>
                </span>
              </label>
            </template>
            <template v-else>
              <div class="cx-row">
                <select v-model="newAddr.tinh" class="cx-select">
                  <option value="">Tỉnh / Thành phố</option>
                  <option v-for="p in provinces" :key="p.code" :value="p.name">{{ p.name }}</option>
                </select>
                
                <select v-model="newAddr.phuong" class="cx-select" :disabled="!newAddr.tinh">
                  <option value="">Phường / Xã</option>
                  <option v-for="w in wards" :key="w.code" :value="w.name">{{ w.name }}</option>
                </select>
                
                <input v-model="newAddr.chiTiet" class="cx-input" maxlength="200" placeholder="Số nhà, tên đường" />
              </div>
            </template>
            <span v-if="errors.diaChi" class="cx-error">{{ errors.diaChi }}</span>
          </div>
          <div class="cx-field">
            <label>Ghi chú</label>
            <textarea v-model="form.ghiChu" class="cx-textarea" rows="2" maxlength="255"></textarea>
          </div>
        </div>
      </div>

      <!-- Tóm tắt đơn hàng -->
      <div>
        <div class="cx-panel">
          <h3>Tóm tắt đơn hàng</h3>
          <p style="margin:0 0 4px"><strong>{{ form.ten || 'Chưa có tên người nhận' }}</strong> · {{ form.soDienThoai || 'Chưa có SĐT' }}</p>
          <p class="cx-hint" style="margin:0 0 12px">{{ fullAddress || 'Chưa có địa chỉ giao hàng' }}</p>

          <div class="cx-field">
            <label>Phiếu giảm giá</label>
            <div style="display:flex;gap:6px;margin-bottom:8px">
              <input v-model="voucherCode" class="cx-input" placeholder="Nhập mã giảm giá" @keyup.enter="applyVoucherCode" />
              <button class="cx-chip" type="button" @click="applyVoucherCode">Áp dụng</button>
            </div>
            <span v-if="voucherCodeMessage" class="cx-hint">{{ voucherCodeMessage }}</span>
            <select v-model="voucherId" class="cx-select" @change="manualVoucher = true">
              <option :value="null">Không dùng phiếu giảm giá</option>
              <option v-for="v in vouchers" :key="v.id" :value="v.id" :disabled="!v.hopLe">
                {{ v.ma }} - {{ v.hopLe ? `giảm ${money(v.soTienGiam)}` : v.lyDo }}
              </option>
            </select>
            <span class="cx-hint">Phiếu giảm nhiều nhất được chọn tự động.</span>
          </div>

          <div class="cx-field">
            <label>Thanh toán</label>
            <div style="display:flex;gap:6px">
              <button class="cx-chip" :class="{ on: paymentMethod === 'COD' }" @click="paymentMethod = 'COD'">Khi nhận hàng (COD)</button>
              <button class="cx-chip" :class="{ on: paymentMethod === 'VNPAY' }" @click="paymentMethod = 'VNPAY'">VNPay</button>
            </div>
          </div>

          <dl class="cx-sum" style="margin-top:12px">
            <dt>Tiền hàng ({{ totalQty }} sản phẩm)</dt><dd>{{ money(subtotal) }}</dd>
            <dt>Phí vận chuyển</dt>
            <dd>
              <span v-if="shipping.loading">Đang tính...</span>
              <span v-else-if="shipping.error" class="cx-error">{{ shipping.error }}</span>
              <span v-else-if="shipping.fee !== null">{{ money(shipping.fee) }}</span>
              <span v-else class="cx-muted">Chọn địa chỉ</span>
            </dd>
            <dt>Giảm giá</dt><dd>- {{ money(discount) }}</dd>
            <dt style="font-weight:800;color:var(--c-ink)">Tổng thanh toán</dt><dd class="cx-accent" style="font-size:1.3rem">{{ money(total) }}</dd>
          </dl>
          <p v-if="shipping.source" class="cx-hint">Phí vận chuyển: {{ shipping.source }}</p>

          <button class="cx-btn primary block" style="margin-top:14px;padding:13px" :disabled="busy || !selectedItems.length" @click="placeOrder">
            {{ paymentMethod === 'VNPAY' ? 'Thanh toán VNPay' : 'Đặt hàng' }} · {{ money(total) }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>