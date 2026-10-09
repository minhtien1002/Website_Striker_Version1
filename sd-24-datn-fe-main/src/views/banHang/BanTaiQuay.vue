<template>
  <div class="ui-page pos">
    <div class="ui-header">
      <div>
        <h1 class="ui-title">Bán tại quầy</h1>
        <p class="ui-sub">Nhân viên: {{ nhanVienLabel }} · Hóa đơn chờ không giới hạn, tự động xóa lúc 0h hằng ngày.</p>
      </div>
      <button class="ui-btn primary" :disabled="busy" @click="createInvoice">
        <iconify-icon icon="solar:add-square-linear"></iconify-icon> Tạo hóa đơn chờ
      </button>
    </div>

    <!-- Hóa đơn chờ -->
    <div class="tabs">
      <div v-for="inv in invoices" :key="inv.id" class="tab" :class="{ active: inv.id === activeId }" @click="selectInvoice(inv.id)">
        <span>{{ inv.ma }}</span>
        <span class="ui-hint">{{ inv.tenKhachHang || 'Khách lẻ' }}</span>
        <button class="close" title="Hủy hóa đơn chờ" @click.stop="removeInvoice(inv)">×</button>
      </div>
      <span v-if="!invoices.length" class="ui-hint" style="padding:10px">Chưa có hóa đơn chờ. Bấm "Tạo hóa đơn chờ" để bắt đầu bán.</span>
    </div>

    <div v-if="activeInvoice" class="pos-grid">
      <!-- Cột trái: sản phẩm + giỏ hàng -->
      <div>
        <div class="ui-panel">
          <h3 class="ui-panel-title">Sản phẩm</h3>
          <div style="display:flex;gap:8px;margin-bottom:10px">
            <input v-model="productKeyword" class="ui-input" placeholder="Nhập / quét mã chi tiết, mã hoặc tên sản phẩm rồi Enter" @keyup.enter="onProductEnter" />
          </div>
          <div class="ui-table-wrap" style="max-height:330px;overflow:auto">
            <table class="ui-table">
              <thead><tr><th style="width:56px">Ảnh</th><th>Mã chi tiết</th><th>Sản phẩm</th><th>Màu / Size</th><th class="num">Giá</th><th class="num">Tồn</th><th style="width:80px"></th></tr></thead>
              <tbody>
                <tr v-if="loadingProducts"><td colspan="7" class="ui-empty">Đang tải...</td></tr>
                <tr v-else-if="!products.length"><td colspan="7" class="ui-empty">Không tìm thấy sản phẩm còn hàng</td></tr>
                <tr v-for="p in products" :key="p.id">
                  <td><img v-if="p.urlAnhSanPham" :src="imageUrl(p.urlAnhSanPham)" class="ui-thumb" style="width:44px;height:44px" alt="" /></td>
                  <td><strong>{{ p.ma }}</strong><div class="ui-hint">{{ p.maSanPham }}</div></td>
                  <td>{{ p.tenSanPham }}<div class="ui-hint">{{ p.tenThuongHieu }}</div></td>
                  <td>{{ p.tenMauSac }} / {{ p.tenKichCo }}</td>
                  <td class="num">
                    <span v-if="p.hasDiscount" class="ui-accent">{{ formatMoney(p.giaGiamGia) }}<div class="ui-hint" style="text-decoration:line-through">{{ formatMoney(p.giaBan) }}</div></span>
                    <span v-else>{{ formatMoney(p.giaBan) }}</span>
                  </td>
                  <td class="num">{{ p.soLuongTonKho }}</td>
                  <td><button class="ui-btn sm primary" @click="openAdd(p)">Thêm</button></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="ui-pager" v-if="productPages > 1">
            <span>Trang {{ productPage + 1 }} / {{ productPages }}</span>
            <div class="pages">
              <button class="ui-btn sm" :disabled="productPage === 0" @click="productPage--">Trước</button>
              <button class="ui-btn sm" :disabled="productPage + 1 >= productPages" @click="productPage++">Sau</button>
            </div>
          </div>
        </div>

        <div class="ui-panel">
          <h3 class="ui-panel-title">Giỏ hàng - {{ activeInvoice.ma }} ({{ totalQty }} sản phẩm)</h3>
          <div class="ui-table-wrap">
            <table class="ui-table">
              <thead><tr><th>Mã chi tiết</th><th>Sản phẩm</th><th>Màu / Size</th><th class="num">Đơn giá</th><th style="width:150px">Số lượng</th><th class="num">Thành tiền</th><th style="width:50px"></th></tr></thead>
              <tbody>
                <tr v-if="!cart.items.length"><td colspan="7" class="ui-empty">Giỏ hàng trống</td></tr>
                <tr v-for="it in cart.items" :key="it.id">
                  <td><strong>{{ it.maSanPham }}</strong><div class="ui-hint">{{ it.maSanPhamGoc }}</div></td>
                  <td>{{ it.tenSanPham }}</td>
                  <td>{{ it.tenMauSac }} / {{ it.tenKichCo }}</td>
                  <td class="num">{{ formatMoney(it.gia) }}</td>
                  <td>
                    <div style="display:flex;gap:4px;align-items:center">
                      <button class="ui-btn sm" :disabled="it.soLuong <= 1 || busy" @click="changeQty(it, it.soLuong - 1)">−</button>
                      <input :value="it.soLuong" type="number" min="1" :max="it.soLuongTonKho" class="ui-input" style="width:60px;padding:4px"
                             @change="changeQty(it, Number($event.target.value), $event)" />
                      <button class="ui-btn sm" :disabled="it.soLuong >= it.soLuongTonKho || busy" @click="changeQty(it, it.soLuong + 1)">+</button>
                    </div>
                    <div class="ui-hint">Tồn: {{ it.soLuongTonKho }}</div>
                  </td>
                  <td class="num">{{ formatMoney(it.thanhTien) }}</td>
                  <td><button class="ui-btn icon" title="Xóa khỏi giỏ" @click="removeItem(it)"><iconify-icon icon="solar:trash-bin-minimalistic-linear"></iconify-icon></button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Cột phải: khách hàng + thanh toán -->
      <div>
        <div class="ui-panel">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
            <h3 class="ui-panel-title" style="margin:0">Khách hàng</h3>
            <button class="ui-btn sm primary" @click="showQuickCustomer = true"><iconify-icon icon="solar:user-plus-linear"></iconify-icon> Thêm nhanh khách hàng</button>
          </div>
          <div class="ui-field" style="position:relative">
            <input v-model="customerKeyword" class="ui-input" placeholder="Tìm khách theo tên / SĐT / mã" @focus="customerOpen = true" @input="searchCustomers" />
            <div v-if="customerOpen && customers.length" class="dropdown">
              <div v-for="c in customers" :key="c.id" class="dropdown-item" @mousedown.prevent="chooseCustomer(c)">
                <strong>{{ c.ten }}</strong> · {{ c.soDienThoai || '—' }} <span class="ui-hint">{{ c.ma }}</span>
              </div>
            </div>
          </div>
          <div style="margin-top:10px;display:flex;justify-content:space-between;align-items:center">
            <div>
              <strong>{{ activeInvoice.tenKhachHang || 'Khách lẻ' }}</strong>
              <div class="ui-hint">{{ activeInvoice.soDienThoaiKhachHang || 'Không có số điện thoại' }}</div>
            </div>
            <button v-if="hasCustomer" class="ui-btn sm" @click="chooseCustomer(null)">Bỏ chọn</button>
          </div>
        </div>

        <div class="ui-panel">
          <h3 class="ui-panel-title">Phiếu giảm giá</h3>
          <select v-model="voucherId" class="ui-select" @change="manualVoucher = true">
            <option :value="null">Không dùng phiếu giảm giá</option>
            <option v-for="v in vouchers" :key="v.id" :value="v.id" :disabled="!v.hopLe">
              {{ v.ma }} - {{ v.tenPhieuGiamGia }} ({{ v.hopLe ? `giảm ${formatMoney(v.soTienGiam)}` : v.lyDo }})
            </option>
          </select>
          <p class="ui-hint" style="margin-top:6px">
            <template v-if="bestVoucher">Phiếu tốt nhất: {{ bestVoucher.ma }} (giảm {{ formatMoney(bestVoucher.soTienGiam) }}). </template>
            Hệ thống tự chọn phiếu tốt nhất khi giỏ hàng / khách hàng thay đổi.
          </p>
        </div>

        <div class="ui-panel">
          <h3 class="ui-panel-title">Thanh toán</h3>
          <div class="ui-field" style="margin-bottom:10px">
            <label>Phương thức</label>
            <div style="display:flex;gap:6px">
              <button v-for="m in METHODS" :key="m" class="ui-btn sm" :class="{ primary: method === m }" @click="method = m">{{ m }}</button>
            </div>
          </div>
          <div v-if="method === 'Kết hợp'" class="ui-field" style="margin-bottom:10px">
            <label>Tiền chuyển khoản <span class="req">*</span></label>
            <input v-model="tienChuyenKhoan" type="number" min="0" class="ui-input" />
          </div>
          <div v-if="method !== 'Chuyển khoản'" class="ui-field" style="margin-bottom:10px">
            <label>Tiền khách đưa (không bắt buộc)</label>
            <input v-model="tienKhachDua" type="number" min="0" class="ui-input" placeholder="Bỏ trống = khách đưa đủ" />
            <span v-if="paymentError" class="ui-error">{{ paymentError }}</span>
          </div>
          <div class="ui-field" style="margin-bottom:12px">
            <label>Ghi chú</label>
            <textarea v-model="ghiChu" class="ui-textarea" rows="2" maxlength="255"></textarea>
          </div>
          <dl class="ui-dl" style="grid-template-columns:1fr auto">
            <dt>Tiền hàng</dt><dd class="num">{{ formatMoney(cart.tongTien || 0) }}</dd>
            <dt>Giảm giá</dt><dd class="num">- {{ formatMoney(discount) }}</dd>
            <dt>Khách cần trả</dt><dd class="num ui-accent" style="font-size:1.25rem">{{ formatMoney(total) }}</dd>
            <template v-if="tienThua > 0"><dt>Tiền thừa trả khách</dt><dd class="num">{{ formatMoney(tienThua) }}</dd></template>
          </dl>
          <button class="ui-btn primary" style="width:100%;justify-content:center;margin-top:14px;padding:12px" :disabled="busy || !cart.items.length || !!paymentError" @click="pay">
            Thanh toán {{ formatMoney(total) }}
          </button>
        </div>
      </div>
    </div>

    <!-- Thêm sản phẩm -->
    <div v-if="adding" class="ui-modal-overlay" @click.self="adding = null">
      <div class="ui-modal">
        <div class="ui-modal-head">Thêm vào hóa đơn {{ activeInvoice?.ma }}</div>
        <div class="ui-modal-body">
          <dl class="ui-dl" style="margin-bottom:12px">
            <dt>Sản phẩm</dt><dd>{{ adding.tenSanPham }}</dd>
            <dt>Mã chi tiết</dt><dd>{{ adding.ma }} ({{ adding.maSanPham }})</dd>
            <dt>Màu / Size</dt><dd>{{ adding.tenMauSac }} / {{ adding.tenKichCo }}</dd>
            <dt>Đơn giá</dt><dd>{{ formatMoney(adding.hasDiscount ? adding.giaGiamGia : adding.giaBan) }}</dd>
            <dt>Tồn kho</dt><dd>{{ adding.soLuongTonKho }} (đã có {{ inCartQty(adding.id) }} trong giỏ)</dd>
          </dl>
          <div class="ui-field">
            <label>Số lượng <span class="req">*</span></label>
            <input v-model.number="addQty" type="number" min="1" :max="adding.soLuongTonKho - inCartQty(adding.id)" class="ui-input" @keyup.enter="confirmAdd" />
            <span v-if="addError" class="ui-error">{{ addError }}</span>
          </div>
        </div>
        <div class="ui-modal-foot">
          <button class="ui-btn" @click="adding = null">Hủy</button>
          <button class="ui-btn primary" :disabled="!!addError || busy" @click="confirmAdd">Xác nhận thêm</button>
        </div>
      </div>
    </div>

    <!-- Thêm nhanh khách hàng -->
    <div v-if="showQuickCustomer" class="ui-modal-overlay" @click.self="showQuickCustomer = false">
      <div class="ui-modal" style="max-width:460px">
        <div class="ui-modal-head">Thêm nhanh khách hàng</div>
        <div class="ui-modal-body">
          <div class="ui-field" style="margin-bottom:10px">
            <label>Họ tên <span class="req">*</span></label>
            <input v-model="quick.ten" class="ui-input" maxlength="100" />
            <span v-if="quickErr.ten" class="ui-error">{{ quickErr.ten }}</span>
          </div>
          <div class="ui-field">
            <label>Số điện thoại <span class="req">*</span></label>
            <input v-model="quick.soDienThoai" class="ui-input" maxlength="10" placeholder="VD 0912345678" />
            <span v-if="quickErr.soDienThoai" class="ui-error">{{ quickErr.soDienThoai }}</span>
          </div>
        </div>
        <div class="ui-modal-foot">
          <button class="ui-btn" @click="showQuickCustomer = false">Hủy</button>
          <button class="ui-btn primary" :disabled="busy" @click="saveQuickCustomer">Thêm và chọn</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useToast } from 'vue-toastification'
import authService from '@/services/authService.js'
import cartAPI from '@/services/api/cartAPI.js'
import { downloadInvoicePDF } from '@/services/api/APIHoaDon/HoaDonAPI.js'
import { useConfirm } from '@/composables/useConfirm.js'
import { productService, errorMessage, formatMoney, imageUrl } from '@/services/api/productAPI.js'

const METHODS = ['Tiền mặt', 'Chuyển khoản', 'Kết hợp']
const toast = useToast()
const { confirm } = useConfirm()
const user = authService.getUser()
const nhanVienId = authService.getNhanVienId()
const nhanVienLabel = user?.tenNhanVien ? `${user.tenNhanVien} (${user.maNhanVien})` : (user?.username || '—')

const busy = ref(false)
const invoices = ref([])
const activeId = ref(null)
const activeInvoice = computed(() => invoices.value.find((i) => i.id === activeId.value) || null)
const cart = reactive({ items: [], tongTien: 0 })

// ---------- Hóa đơn chờ ----------
const loadInvoices = async () => {
  const { data } = await productService.getPendingInvoices()
  invoices.value = data.sort((a, b) => a.id - b.id)
  if (!invoices.value.find((i) => i.id === activeId.value)) activeId.value = invoices.value[0]?.id || null
}

const createInvoice = async () => {
  const ok = await confirm({ title: 'Tạo hóa đơn chờ mới?', message: 'Hóa đơn chờ chưa thanh toán sẽ tự động bị xóa lúc 0h hằng ngày.', confirmText: 'Tạo' })
  if (!ok) return
  busy.value = true
  try {
    const { data } = await productService.createPendingInvoice(nhanVienId)
    await loadInvoices()
    activeId.value = data.id
    toast.success(`Đã tạo hóa đơn ${data.ma}`)
  } catch (e) { toast.error(errorMessage(e)) } finally { busy.value = false }
}

const removeInvoice = async (inv) => {
  const ok = await confirm({ title: `Hủy hóa đơn chờ ${inv.ma}?`, message: 'Giỏ hàng của hóa đơn này sẽ bị xóa. Tồn kho không thay đổi vì chưa thanh toán.', confirmText: 'Hủy hóa đơn', tone: 'danger' })
  if (!ok) return
  try {
    await productService.deletePendingInvoice(inv.id)
    toast.success(`Đã hủy hóa đơn chờ ${inv.ma}`)
    await loadInvoices()
  } catch (e) { toast.error(errorMessage(e)) }
}

const selectInvoice = (id) => { activeId.value = id }

// ---------- Giỏ hàng ----------
const loadCart = async () => {
  if (!activeId.value) { cart.items = []; cart.tongTien = 0; return }
  try {
    const data = await cartAPI.getCartByInvoiceId(activeId.value)
    cart.items = data.items || []
    cart.tongTien = Number(data.tongTien || 0)
  } catch (e) { toast.error(errorMessage(e, 'Không tải được giỏ hàng')) }
}
const totalQty = computed(() => cart.items.reduce((s, i) => s + i.soLuong, 0))
const inCartQty = (ctspId) => cart.items.find((i) => i.idChiTietSanPham === ctspId)?.soLuong || 0

const adding = ref(null)
const addQty = ref(1)
const addError = computed(() => {
  if (!adding.value) return ''
  const max = adding.value.soLuongTonKho - inCartQty(adding.value.id)
  if (!Number.isInteger(addQty.value) || addQty.value < 1) return 'Số lượng phải là số nguyên ≥ 1'
  if (addQty.value > max) return max <= 0 ? 'Đã thêm hết số lượng tồn kho vào giỏ' : `Chỉ còn có thể thêm tối đa ${max}`
  return ''
})
const openAdd = (p) => { adding.value = p; addQty.value = 1 }
const confirmAdd = async () => {
  if (addError.value) return
  busy.value = true
  try {
    await cartAPI.addProductToCart(activeId.value, { idChiTietSanPham: adding.value.id, soLuong: addQty.value })
    toast.success(`Đã thêm ${addQty.value} × ${adding.value.ma}`)
    adding.value = null
    await loadCart()
  } catch (e) { toast.error(errorMessage(e)) } finally { busy.value = false }
}

const changeQty = async (it, qty, event) => {
  if (!Number.isInteger(qty) || qty < 1) { toast.error('Số lượng phải là số nguyên ≥ 1'); if (event) event.target.value = it.soLuong; return }
  if (qty > it.soLuongTonKho) { toast.error(`Chỉ còn ${it.soLuongTonKho} sản phẩm trong kho`); if (event) event.target.value = it.soLuong; return }
  const ok = await confirm({ title: 'Cập nhật số lượng?', details: [`${it.maSanPham} - ${it.tenSanPham}`, `${it.soLuong} → ${qty}`], confirmText: 'Cập nhật' })
  if (!ok) { if (event) event.target.value = it.soLuong; return }
  busy.value = true
  try { await cartAPI.updateCartItem(activeId.value, it.id, qty); await loadCart() }
  catch (e) { toast.error(errorMessage(e)); if (event) event.target.value = it.soLuong }
  finally { busy.value = false }
}

const removeItem = async (it) => {
  const ok = await confirm({ title: 'Xóa sản phẩm khỏi giỏ hàng?', details: [`${it.maSanPham} - ${it.tenSanPham} (${it.tenMauSac} / ${it.tenKichCo})`, `Số lượng: ${it.soLuong}`], confirmText: 'Xóa', tone: 'danger' })
  if (!ok) return
  try { await cartAPI.removeCartItem(activeId.value, it.id); await loadCart() } catch (e) { toast.error(errorMessage(e)) }
}

// ---------- Sản phẩm ----------
const products = ref([])
const loadingProducts = ref(false)
const productKeyword = ref('')
const productPage = ref(0)
const productPages = ref(0)
const loadProducts = async () => {
  loadingProducts.value = true
  try {
    const { data } = await productService.getSalesProductDetails({ keyword: productKeyword.value.trim(), page: productPage.value, size: 8 })
    products.value = data.content
    productPages.value = data.totalPages
  } catch (e) { toast.error(errorMessage(e, 'Không tải được sản phẩm')) } finally { loadingProducts.value = false }
}
let pTimer = null
watch(productKeyword, () => { clearTimeout(pTimer); pTimer = setTimeout(() => { productPage.value = 0; loadProducts() }, 300) })
watch(productPage, loadProducts)
const onProductEnter = async () => {
  await loadProducts()
  const k = productKeyword.value.trim().toUpperCase()
  const exact = products.value.find((p) => p.ma?.toUpperCase() === k)
  if (exact) openAdd(exact)
  else if (products.value.length === 1) openAdd(products.value[0])
}

// ---------- Khách hàng ----------
const customers = ref([])
const customerKeyword = ref('')
const customerOpen = ref(false)
const hasCustomer = computed(() => activeInvoice.value && activeInvoice.value.tenKhachHang && activeInvoice.value.tenKhachHang !== 'Khách lẻ')
let cTimer = null
const searchCustomers = () => {
  clearTimeout(cTimer)
  cTimer = setTimeout(async () => {
    try { customers.value = (await productService.getActiveCustomersForSales({ keyword: customerKeyword.value.trim(), size: 8 })).data.content } catch { customers.value = [] }
  }, 250)
}
document.addEventListener('click', (e) => { if (!e.target.closest?.('.ui-field')) customerOpen.value = false })

const chooseCustomer = async (c) => {
  customerOpen.value = false
  const ok = await confirm({
    title: c ? 'Chọn khách hàng cho hóa đơn?' : 'Bỏ chọn khách hàng?',
    details: c ? [`${c.ten} - ${c.soDienThoai || 'không có SĐT'} (${c.ma})`] : ['Hóa đơn sẽ chuyển về Khách lẻ'],
    confirmText: 'Xác nhận'
  })
  if (!ok) return
  try {
    await productService.updatePendingInvoiceCustomer(activeId.value, c
      ? { khachHangId: c.id, tenKhachHang: c.ten, soDienThoaiKhachHang: c.soDienThoai || '', email: c.email || null }
      : { tenKhachHang: 'Khách lẻ', soDienThoaiKhachHang: '', diaChiKhachHang: '' })
    customerKeyword.value = ''
    manualVoucher.value = false
    await loadInvoices()
  } catch (e) { toast.error(errorMessage(e)) }
}

const showQuickCustomer = ref(false)
const quick = reactive({ ten: '', soDienThoai: '' })
const quickErr = reactive({ ten: '', soDienThoai: '' })
const saveQuickCustomer = async () => {
  const ten = quick.ten.trim().replace(/\s+/g, ' ')
  quickErr.ten = ten.length < 2 ? 'Họ tên tối thiểu 2 ký tự' : !/^[\p{L} ]+$/u.test(ten) ? 'Họ tên chỉ gồm chữ cái' : ''
  quickErr.soDienThoai = /^0(3|5|7|8|9)\d{8}$/.test(quick.soDienThoai.trim()) ? '' : 'Số điện thoại gồm 10 số, bắt đầu bằng 03/05/07/08/09'
  if (quickErr.ten || quickErr.soDienThoai) return
  const ok = await confirm({ title: 'Thêm khách hàng mới?', details: [`${ten} - ${quick.soDienThoai.trim()}`], message: 'Khách hàng sẽ được chọn cho hóa đơn hiện tại.', confirmText: 'Thêm' })
  if (!ok) return
  busy.value = true
  try {
    const { data } = await productService.quickCreateCustomer({ ten, soDienThoai: quick.soDienThoai.trim() })
    await productService.updatePendingInvoiceCustomer(activeId.value, { khachHangId: data.id, tenKhachHang: ten, soDienThoaiKhachHang: quick.soDienThoai.trim() })
    toast.success(`Đã thêm khách hàng ${ten}`)
    showQuickCustomer.value = false
    Object.assign(quick, { ten: '', soDienThoai: '' })
    manualVoucher.value = false
    await loadInvoices()
  } catch (e) { toast.error(errorMessage(e)) } finally { busy.value = false }
}

// ---------- Phiếu giảm giá ----------
const vouchers = ref([])
const voucherId = ref(null)
const manualVoucher = ref(false)
const bestVoucher = computed(() => vouchers.value.find((v) => v.hopLe) || null)
const selectedVoucher = computed(() => vouchers.value.find((v) => v.id === voucherId.value && v.hopLe) || null)
const discount = computed(() => Number(selectedVoucher.value?.soTienGiam || 0))
const total = computed(() => Math.max(0, Number(cart.tongTien || 0) - discount.value))

const loadVouchers = async () => {
  if (!cart.items.length) { vouchers.value = []; voucherId.value = null; return }
  try {
    const { data } = await productService.getAvailableVouchers(cart.tongTien, activeInvoice.value?.idKhachHang?.id)
    vouchers.value = data
    // Tự chọn phiếu tốt nhất, trừ khi nhân viên đã chủ động chọn và phiếu đó vẫn hợp lệ
    if (!manualVoucher.value || !data.find((v) => v.id === voucherId.value && v.hopLe)) {
      voucherId.value = data.find((v) => v.hopLe)?.id ?? null
      manualVoucher.value = false
    }
  } catch { vouchers.value = [] }
}

// ---------- Thanh toán ----------
const method = ref('Tiền mặt')
const tienKhachDua = ref('')
const tienChuyenKhoan = ref('')
const ghiChu = ref('')
const canTraTienMat = computed(() => method.value === 'Kết hợp' ? Math.max(0, total.value - Number(tienChuyenKhoan.value || 0)) : total.value)
const tienThua = computed(() => tienKhachDua.value === '' || method.value === 'Chuyển khoản' ? 0 : Math.max(0, Number(tienKhachDua.value) - canTraTienMat.value))
const paymentError = computed(() => {
  if (method.value === 'Kết hợp') {
    const ck = Number(tienChuyenKhoan.value)
    if (tienChuyenKhoan.value === '' || ck <= 0) return 'Nhập số tiền chuyển khoản'
    if (ck > total.value) return 'Tiền chuyển khoản vượt quá số tiền cần trả'
  }
  if (method.value !== 'Chuyển khoản' && tienKhachDua.value !== '' && Number(tienKhachDua.value) < canTraTienMat.value) {
    return `Tiền khách đưa chưa đủ (cần ${formatMoney(canTraTienMat.value)})`
  }
  return ''
})

const pay = async () => {
  if (!nhanVienId) { toast.error('Tài khoản chưa gắn nhân viên. Vui lòng đăng xuất và đăng nhập lại.'); return }
  if (paymentError.value) return
  const inv = activeInvoice.value
  const ok = await confirm({
    title: `Thanh toán hóa đơn ${inv.ma}?`,
    message: 'Hóa đơn sẽ được tự động xác nhận và hoàn thành, tồn kho bị trừ ngay.',
    details: [
      `Khách hàng: ${inv.tenKhachHang || 'Khách lẻ'}`,
      `Số sản phẩm: ${totalQty.value}`,
      `Tiền hàng: ${formatMoney(cart.tongTien)}`,
      ...(selectedVoucher.value ? [`Phiếu ${selectedVoucher.value.ma}: - ${formatMoney(discount.value)}`] : []),
      `Khách cần trả: ${formatMoney(total.value)} (${method.value})`,
      ...(tienThua.value > 0 ? [`Tiền thừa: ${formatMoney(tienThua.value)}`] : [])
    ],
    confirmText: 'Thanh toán'
  })
  if (!ok) return
  busy.value = true
  try {
    const { data } = await productService.processPayment({
      hoaDonId: inv.id,
      nhanVienId,
      phuongThucThanhToan: method.value,
      tongTien: Number(cart.tongTien),
      tongTienSauGiam: total.value,
      tienMat: method.value === 'Chuyển khoản' ? 0 : Number(tienKhachDua.value || 0),
      tienChuyenKhoan: method.value === 'Kết hợp' ? Number(tienChuyenKhoan.value) : 0,
      phieuGiamGiaId: selectedVoucher.value?.id ?? null,
      ghiChu: ghiChu.value.trim() || null
    })
    if (data && data.success === false) throw new Error(data.message)
    toast.success(`Thanh toán thành công hóa đơn ${inv.ma}`)
    const paidId = inv.id
    tienKhachDua.value = ''; tienChuyenKhoan.value = ''; ghiChu.value = ''; method.value = 'Tiền mặt'; manualVoucher.value = false
    await loadInvoices()
    loadProducts()
    if (await confirm({ title: 'In hóa đơn?', message: `Hóa đơn ${inv.ma} đã thanh toán.`, confirmText: 'In hóa đơn', cancelText: 'Không in' })) {
      try {
        const blob = await downloadInvoicePDF(paidId)
        window.open(URL.createObjectURL(new Blob([blob], { type: 'application/pdf' })), '_blank')
      } catch (e) { toast.error(errorMessage(e, 'Không in được hóa đơn')) }
    }
  } catch (e) {
    toast.error(e?.response ? (e.response.data?.message || errorMessage(e)) : e.message)
  } finally {
    busy.value = false
  }
}

watch(activeId, async () => { manualVoucher.value = false; await loadCart() })
watch(() => [cart.tongTien, activeInvoice.value?.idKhachHang?.id], loadVouchers)

onMounted(async () => {
  try { await loadInvoices() } catch (e) { toast.error(errorMessage(e, 'Không tải được hóa đơn chờ')) }
  await loadCart()
  loadProducts()
  searchCustomers()
})
</script>

<style scoped>
.pos-grid { display: grid; grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr); gap: 16px; align-items: start; }
@media (max-width: 1100px) { .pos-grid { grid-template-columns: 1fr; } }
.tabs { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 12px; background: #fff; border: 1px solid var(--line); padding: 6px; }
.tab { display: flex; align-items: center; gap: 8px; padding: 6px 10px; border: 1px solid var(--line); cursor: pointer; font-weight: 700; background: var(--muted); }
.tab.active { background: var(--accent); border-color: var(--accent); color: #fff; }
.tab.active .ui-hint { color: #fff; }
.tab .close { border: none; background: transparent; font-size: 1.1rem; line-height: 1; cursor: pointer; color: inherit; }
.dropdown { position: absolute; top: 100%; left: 0; right: 0; background: #fff; border: 1px solid var(--line); z-index: 20; max-height: 260px; overflow-y: auto; }
.dropdown-item { padding: 8px 10px; cursor: pointer; border-bottom: 1px solid var(--muted); }
.dropdown-item:hover { background: var(--muted); }
</style>
