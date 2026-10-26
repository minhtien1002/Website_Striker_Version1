<script setup>
import { ref, reactive, computed } from 'vue'
import { useNotification } from '@/composables/useNotification'

const { success, error } = useNotification()

// Thông tin liên hệ - đổi lại theo thông tin thật của shop
const HOTLINE = '1900 1234'
const SUPPORT_EMAIL = 'support@striker.vn'
const MAX_FILE_SIZE = 5 * 1024 * 1024

const activeTab = ref('feedback')
const historyFilter = ref('all')
const expandedFAQs = ref([])
const attachments = ref([])

const tabs = [
  { id: 'feedback', label: 'Gửi góp ý' },
  { id: 'history', label: 'Lịch sử phản hồi' },
  { id: 'support', label: 'Hỗ trợ' }
]

// Feedback Form
const feedbackForm = reactive({
  type: '',
  priority: 'medium',
  title: '',
  content: '',
  allowContact: true
})

const feedbackTypes = [
  { value: 'product', label: 'Góp ý về sản phẩm' },
  { value: 'size', label: 'Góp ý về size và form áo' },
  { value: 'service', label: 'Góp ý về dịch vụ' },
  { value: 'website', label: 'Góp ý về website' },
  { value: 'delivery', label: 'Góp ý về giao hàng' },
  { value: 'complaint', label: 'Khiếu nại' },
  { value: 'suggestion', label: 'Đề xuất cải thiện' },
  { value: 'other', label: 'Khác' }
]

const priorities = [
  { value: 'low', label: 'Thấp' },
  { value: 'medium', label: 'Trung bình' },
  { value: 'high', label: 'Cao' },
  { value: 'urgent', label: 'Khẩn cấp' }
]

const historyFilters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'pending', label: 'Đang xử lý' },
  { value: 'replied', label: 'Đã phản hồi' },
  { value: 'resolved', label: 'Đã giải quyết' }
]

// Góp ý đã gửi trong phiên này.
// Dữ liệu chỉ nằm trong bộ nhớ, tải lại trang sẽ mất.
const feedbackHistory = ref([])

const faqs = ref([
  {
    question: 'Chọn size áo thế nào cho đúng?',
    answer: 'Mỗi trang sản phẩm có size từ S đến 3XL. Nếu bạn ở giữa hai size, chọn size lớn hơn để áo thoải mái khi vận động. Lần đầu mua, STRIKER đổi size miễn phí.'
  },
  {
    question: 'Chính sách đổi trả như thế nào?',
    answer: 'Đổi trả trong 30 ngày kể từ ngày nhận hàng, với điều kiện áo còn nguyên tem mác, chưa giặt và chưa qua sử dụng.'
  },
  {
    question: 'Thời gian giao hàng là bao lâu?',
    answer: 'Nội thành 2-3 ngày làm việc, các tỉnh thành khác 3-5 ngày làm việc. Đơn từ 500.000đ được miễn phí vận chuyển.'
  },
  {
    question: 'Làm thế nào để theo dõi đơn hàng?',
    answer: 'Đăng nhập rồi vào mục Đơn hàng trong tài khoản. Nếu đặt hàng không cần tài khoản, dùng mã đơn được gửi qua email để tra cứu.'
  },
  {
    question: 'Giặt áo thể thao thế nào để giữ form?',
    answer: 'Giặt máy ở chế độ nhẹ với nước lạnh, lộn trái áo, không dùng chất tẩy và không sấy ở nhiệt độ cao. Phơi trong bóng râm để giữ màu.'
  }
])

const filteredFeedbackHistory = computed(() => {
  if (historyFilter.value === 'all') {
    return feedbackHistory.value
  }
  return feedbackHistory.value.filter(feedback => feedback.status === historyFilter.value)
})

const getTypeText = (type) => {
  const found = feedbackTypes.find(t => t.value === type)
  return found ? found.label : type
}

const STATUS_MAP = {
  pending: { text: 'Đang xử lý', tone: 'work' },
  replied: { text: 'Đã phản hồi', tone: 'ship' },
  resolved: { text: 'Đã giải quyết', tone: 'done' }
}

const getStatusText = (status) => (STATUS_MAP[status] || { text: 'Không xác định' }).text
const getStatusClass = (status) => `st-status--${(STATUS_MAP[status] || { tone: 'wait' }).tone}`

const onFilesSelected = (event) => {
  const files = Array.from(event.target.files || [])
  const tooLarge = files.filter(file => file.size > MAX_FILE_SIZE)

  if (tooLarge.length > 0) {
    error(`File vượt quá 5MB: ${tooLarge.map(f => f.name).join(', ')}`)
  }

  attachments.value = files.filter(file => file.size <= MAX_FILE_SIZE)
}

const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

const submitFeedback = () => {
  const newFeedback = {
    id: Date.now(),
    ticketId: `FB${String(Date.now()).slice(-6)}`,
    title: feedbackForm.title,
    type: getTypeText(feedbackForm.type),
    priority: feedbackForm.priority,
    content: feedbackForm.content,
    attachmentCount: attachments.value.length,
    status: 'pending',
    date: new Date().toLocaleDateString('vi-VN'),
    reply: null
  }

  feedbackHistory.value.unshift(newFeedback)

  feedbackForm.type = ''
  feedbackForm.priority = 'medium'
  feedbackForm.title = ''
  feedbackForm.content = ''
  feedbackForm.allowContact = true
  attachments.value = []

  activeTab.value = 'history'

  success('Đã ghi nhận góp ý của bạn. STRIKER sẽ phản hồi sớm nhất có thể.')
}

const toggleFAQ = (index) => {
  const faqIndex = expandedFAQs.value.indexOf(index)
  if (faqIndex > -1) {
    expandedFAQs.value.splice(faqIndex, 1)
  } else {
    expandedFAQs.value.push(index)
  }
}
</script>

<template>
  <div class="st-card">
    <!-- Header -->
    <header class="st-head">
      <div class="st-eyebrow">
        <span class="st-eyebrow__bar"></span>
        <span>Chăm sóc khách hàng</span>
      </div>
      <h1 class="st-title">Góp ý và phản hồi</h1>
      <p class="st-lead">Cho STRIKER biết bạn nghĩ gì về sản phẩm và dịch vụ.</p>
    </header>

    <!-- Tabs -->
    <nav class="st-tabs">
      <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
        :class="['st-tab', { 'is-active': activeTab === tab.id }]">
        {{ tab.label }}
      </button>
    </nav>

    <!-- Gửi góp ý -->
    <div v-if="activeTab === 'feedback'">
      <form @submit.prevent="submitFeedback" class="st-form">
        <div class="st-grid">
          <div class="st-group">
            <label class="st-label" for="fb-type">Loại góp ý *</label>
            <div class="st-select-wrap">
              <select id="fb-type" v-model="feedbackForm.type" required class="st-select">
                <option value="">Chọn loại góp ý</option>
                <option v-for="type in feedbackTypes" :key="type.value" :value="type.value">
                  {{ type.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="st-group">
            <label class="st-label" for="fb-priority">Mức độ ưu tiên</label>
            <div class="st-select-wrap">
              <select id="fb-priority" v-model="feedbackForm.priority" class="st-select">
                <option v-for="p in priorities" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
            </div>
          </div>
        </div>

        <div class="st-group">
          <label class="st-label" for="fb-title">Tiêu đề *</label>
          <input id="fb-title" v-model="feedbackForm.title" type="text" required class="st-input"
            placeholder="Tóm tắt ngắn gọn điều bạn muốn nói" />
        </div>

        <div class="st-group">
          <label class="st-label" for="fb-content">Nội dung chi tiết *</label>
          <textarea id="fb-content" v-model="feedbackForm.content" required rows="6" class="st-input st-textarea"
            placeholder="Mô tả càng cụ thể càng giúp chúng tôi xử lý nhanh hơn"></textarea>
        </div>

        <div class="st-group">
          <label class="st-label" for="fb-files">Đính kèm file</label>
          <input id="fb-files" type="file" multiple accept="image/*,.pdf,.doc,.docx" class="st-file"
            @change="onFilesSelected" />
          <p class="st-hint">Nhận JPG, PNG, PDF, DOC. Mỗi file tối đa 5MB.</p>

          <ul v-if="attachments.length" class="st-files">
            <li v-for="(file, index) in attachments" :key="index">
              <span class="st-files__name">{{ file.name }}</span>
              <span class="st-files__size">{{ formatFileSize(file.size) }}</span>
              <button type="button" @click="removeAttachment(index)" :aria-label="`Bỏ file ${file.name}`">×</button>
            </li>
          </ul>
        </div>

        <label class="st-check">
          <input v-model="feedbackForm.allowContact" type="checkbox" />
          <span>Cho phép STRIKER liên hệ qua email để hỗ trợ tốt hơn</span>
        </label>

        <div class="st-form__foot">
          <button type="submit" class="st-btn st-btn--primary">Gửi góp ý</button>
        </div>
      </form>
    </div>

    <!-- Lịch sử -->
    <div v-if="activeTab === 'history'">
      <div class="st-history-head">
        <h2 class="st-section__title">Góp ý bạn đã gửi</h2>
        <div class="st-select-wrap st-select-wrap--sm">
          <select v-model="historyFilter" class="st-select" aria-label="Lọc theo trạng thái">
            <option v-for="f in historyFilters" :key="f.value" :value="f.value">{{ f.label }}</option>
          </select>
        </div>
      </div>

      <div v-if="filteredFeedbackHistory.length === 0" class="st-empty">
        <p class="st-empty__title">Chưa có góp ý nào</p>
        <p class="st-empty__hint">Góp ý bạn gửi sẽ hiện ở đây để tiện theo dõi trạng thái xử lý.</p>
        <button @click="activeTab = 'feedback'" class="st-btn st-btn--primary">Gửi góp ý đầu tiên</button>
      </div>

      <div v-else class="st-list">
        <article v-for="feedback in filteredFeedbackHistory" :key="feedback.id" class="st-entry">
          <div class="st-entry__top">
            <div>
              <h3 class="st-entry__title">{{ feedback.title }}</h3>
              <p class="st-entry__meta">{{ feedback.type }} · {{ feedback.date }}</p>
            </div>
            <span :class="['st-status', getStatusClass(feedback.status)]">
              {{ getStatusText(feedback.status) }}
            </span>
          </div>

          <p class="st-entry__content">{{ feedback.content }}</p>

          <p v-if="feedback.attachmentCount" class="st-entry__files">
            {{ feedback.attachmentCount }} file đính kèm
          </p>

          <div v-if="feedback.reply" class="st-reply">
            <p class="st-reply__from">Phản hồi từ STRIKER</p>
            <p class="st-reply__body">{{ feedback.reply.content }}</p>
            <p class="st-reply__meta">{{ feedback.reply.date }} · {{ feedback.reply.author }}</p>
          </div>

          <p class="st-entry__code">Mã: {{ feedback.ticketId }}</p>
        </article>
      </div>
    </div>

    <!-- Hỗ trợ -->
    <div v-if="activeTab === 'support'">
      <h2 class="st-section__title">Liên hệ trực tiếp</h2>
      <div class="st-channels">
        <a :href="`tel:${HOTLINE.replace(/\s/g, '')}`" class="st-channel">
          <span class="st-channel__label">Hotline</span>
          <span class="st-channel__value">{{ HOTLINE }}</span>
          <span class="st-channel__desc">8:00 – 21:00 mỗi ngày</span>
        </a>

        <a :href="`mailto:${SUPPORT_EMAIL}`" class="st-channel">
          <span class="st-channel__label">Email</span>
          <span class="st-channel__value">{{ SUPPORT_EMAIL }}</span>
          <span class="st-channel__desc">Phản hồi trong 24 giờ</span>
        </a>

        <button @click="activeTab = 'feedback'" class="st-channel st-channel--btn">
          <span class="st-channel__label">Gửi góp ý</span>
          <span class="st-channel__value">Điền biểu mẫu</span>
          <span class="st-channel__desc">Theo dõi được trạng thái xử lý</span>
        </button>
      </div>

      <h2 class="st-section__title">Câu hỏi thường gặp</h2>
      <div class="st-faqs">
        <div v-for="(faq, index) in faqs" :key="index" class="st-faq">
          <button @click="toggleFAQ(index)" class="st-faq__q" :aria-expanded="expandedFAQs.includes(index)">
            <span>{{ faq.question }}</span>
            <span class="st-faq__sign">{{ expandedFAQs.includes(index) ? '−' : '+' }}</span>
          </button>
          <div v-if="expandedFAQs.includes(index)" class="st-faq__a">{{ faq.answer }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-card {
  --ink: #10141a;
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --line: #dde3ea;
  --steel: #8c98a8;
  --slate: #4a5563;
  --done: #0f7a3d;
  --work: #b45309;
  --ship: #1b3bff;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-top: 4px solid var(--ink);
  padding: 1.75rem;
}

/* ===== Header ===== */
.st-head { margin-bottom: 1.5rem; }

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  margin-bottom: 0.7rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.8rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--strike);
}

.st-eyebrow__bar {
  width: 2.2rem;
  height: 3px;
  background: var(--strike);
}

.st-title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 2rem;
  line-height: 1;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.45rem;
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.95rem;
  color: var(--slate);
  margin: 0;
}

/* ===== Tabs ===== */
.st-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  border-bottom: 2px solid var(--ink);
  margin-bottom: 1.75rem;
}

.st-tab {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.92rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--steel);
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 0.6rem 0.9rem;
  margin-bottom: -2px;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.st-tab:hover { color: var(--ink); }

.st-tab.is-active {
  color: var(--ink);
  border-bottom-color: var(--strike);
}

/* ===== Form ===== */
.st-form { display: flex; flex-direction: column; gap: 1.25rem; }

.st-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr));
  gap: 1.25rem;
}

.st-group { display: flex; flex-direction: column; }

.st-label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.82rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ink);
  margin-bottom: 0.5rem;
}

.st-input,
.st-select {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.94rem;
  color: var(--ink);
  background: var(--mist);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.7rem 0.85rem;
}

.st-textarea { resize: vertical; min-height: 8rem; line-height: 1.55; }

.st-input:focus,
.st-select:focus {
  outline: none;
  border-color: var(--strike);
  background: var(--chalk);
}

.st-select-wrap { position: relative; }

.st-select-wrap::after {
  content: '';
  position: absolute;
  right: 0.9rem;
  top: 50%;
  width: 0.5rem;
  height: 0.5rem;
  border-right: 2px solid var(--strike);
  border-bottom: 2px solid var(--strike);
  transform: translateY(-70%) rotate(45deg);
  pointer-events: none;
}

.st-select {
  appearance: none;
  padding-right: 2.25rem;
  cursor: pointer;
}

.st-select-wrap--sm { min-width: 11rem; }

.st-file {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.88rem;
  color: var(--slate);
  background: var(--mist);
  border: 1px dashed var(--line);
  border-radius: 2px;
  padding: 0.7rem 0.85rem;
  cursor: pointer;
  width: 100%;
}

.st-hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.78rem;
  color: var(--steel);
  margin: 0.4rem 0 0;
}

.st-files {
  list-style: none;
  margin: 0.7rem 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.st-files li {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--ink);
  background: var(--mist);
  border-left: 3px solid var(--strike);
  padding: 0.4rem 0.6rem;
}

.st-files__name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.st-files__size { color: var(--steel); font-size: 0.78rem; }

.st-files button {
  background: none;
  border: none;
  color: var(--strike);
  font-size: 1.1rem;
  line-height: 1;
  cursor: pointer;
  padding: 0 0.2rem;
}

.st-check {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
  color: var(--slate);
  cursor: pointer;
}

.st-check input {
  width: 1rem;
  height: 1rem;
  accent-color: var(--strike);
  cursor: pointer;
  flex-shrink: 0;
}

.st-form__foot { display: flex; justify-content: flex-end; }

/* ===== Nút ===== */
.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.7rem 1.6rem;
  cursor: pointer;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}

.st-btn--primary {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-btn--primary:hover {
  background: var(--strike);
  border-color: var(--strike);
}

/* ===== Lịch sử ===== */
.st-history-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.st-section__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 1.1rem;
  padding-bottom: 0.6rem;
  border-bottom: 2px solid var(--ink);
}

.st-history-head .st-section__title {
  margin: 0;
  padding: 0;
  border: none;
}

.st-list { display: flex; flex-direction: column; gap: 0.9rem; }

.st-entry {
  background: var(--chalk);
  border: 1px solid var(--line);
  border-left: 4px solid var(--ink);
  padding: 1.1rem 1.25rem;
}

.st-entry__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.7rem;
}

.st-entry__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 1.05rem;
  color: var(--ink);
  margin: 0 0 0.2rem;
}

.st-entry__meta {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.8rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0;
}

.st-status {
  display: inline-block;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  padding: 0.25rem 0.6rem;
  white-space: nowrap;
  color: var(--chalk);
  background: var(--steel);
}

.st-status--done { background: var(--done); }
.st-status--ship { background: var(--ship); }
.st-status--work { background: var(--work); }
.st-status--wait { background: var(--steel); }

.st-entry__content {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  line-height: 1.6;
  color: var(--slate);
  margin: 0 0 0.6rem;
  white-space: pre-line;
}

.st-entry__files {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.82rem;
  color: var(--steel);
  margin: 0 0 0.6rem;
}

.st-reply {
  background: var(--mist);
  border-left: 3px solid var(--strike);
  padding: 0.85rem 1rem;
  margin: 0.8rem 0;
}

.st-reply__from {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--strike);
  margin: 0 0 0.4rem;
}

.st-reply__body {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.st-reply__meta {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.78rem;
  color: var(--steel);
  margin: 0;
}

.st-entry__code {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.8rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0.8rem 0 0;
  padding-top: 0.7rem;
  border-top: 1px solid var(--line);
}

/* ===== Kênh hỗ trợ ===== */
.st-channels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr));
  gap: 0.9rem;
  margin-bottom: 2.25rem;
}

.st-channel {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  text-align: left;
  background: var(--mist);
  border: 1px solid var(--line);
  border-top: 4px solid var(--ink);
  padding: 1.1rem 1.2rem;
  text-decoration: none;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
  font: inherit;
}

.st-channel:hover {
  border-top-color: var(--strike);
  transform: translateY(-2px);
}

.st-channel__label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--strike);
}

.st-channel__value {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.2rem;
  color: var(--ink);
}

.st-channel__desc {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.82rem;
  color: var(--slate);
}

/* ===== FAQ ===== */
.st-faqs { display: flex; flex-direction: column; gap: 0.4rem; }

.st-faq { border: 1px solid var(--line); }

.st-faq__q {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  text-align: left;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--ink);
  background: var(--chalk);
  border: none;
  padding: 0.9rem 1rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.st-faq__q:hover { background: var(--mist); }

.st-faq__sign {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  line-height: 1;
  color: var(--strike);
  flex-shrink: 0;
}

.st-faq__a {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
  line-height: 1.65;
  color: var(--slate);
  background: var(--mist);
  border-top: 1px solid var(--line);
  padding: 0.9rem 1rem;
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  border: 1px dashed var(--line);
  padding: 3.5rem 1.5rem;
}

.st-empty__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.5rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.st-empty__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
  max-width: 26rem;
  margin: 0 auto 1.4rem;
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-tab:focus-visible,
.st-input:focus-visible,
.st-select:focus-visible,
.st-file:focus-visible,
.st-channel:focus-visible,
.st-faq__q:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 560px) {
  .st-card { padding: 1.25rem 1rem; }
  .st-entry__top { flex-direction: column; }
  .st-tab { padding: 0.55rem 0.6rem; font-size: 0.85rem; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-tab, .st-btn, .st-channel, .st-faq__q { transition: none; }
  .st-channel:hover { transform: none; }
}
</style>