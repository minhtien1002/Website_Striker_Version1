<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">10</span>
      <div class="st-container st-head__inner">
        <div>
          <div class="st-eyebrow">
            <span class="st-eyebrow__bar"></span>
            <span>Trung tâm hỗ trợ</span>
          </div>
          <h1 class="st-display">HỖ TRỢ</h1>
          <p class="st-lead">Chúng tôi luôn sẵn sàng đồng hành cùng bạn trên mọi đường chạy.</p>
        </div>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Quick Help Options -->
      <div class="st-quick-grid">
        <div class="st-quick-card">
          <div class="st-quick-card__icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h3 class="st-quick-card__title">FAQ</h3>
          <p class="st-quick-card__desc">Câu hỏi thường gặp</p>
        </div>

        <div class="st-quick-card">
          <div class="st-quick-card__icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
            </svg>
          </div>
          <h3 class="st-quick-card__title">Live Chat</h3>
          <p class="st-quick-card__desc">Trò chuyện trực tiếp</p>
        </div>

        <div class="st-quick-card">
          <div class="st-quick-card__icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
            </svg>
          </div>
          <h3 class="st-quick-card__title">Email</h3>
          <p class="st-quick-card__desc">Gửi email hỗ trợ</p>
        </div>

        <div class="st-quick-card">
          <div class="st-quick-card__icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
            </svg>
          </div>
          <h3 class="st-quick-card__title">Hotline</h3>
          <p class="st-quick-card__desc">1900 1234</p>
        </div>
      </div>

      <!-- Contact Form -->
      <div class="st-panel">
        <h2 class="st-panel__title">Gửi yêu cầu hỗ trợ</h2>

        <form class="st-form">
          <div class="st-form__row">
            <div class="st-form__field">
              <label class="st-label">Họ và tên *</label>
              <input type="text" class="st-input" placeholder="Nhập họ và tên">
            </div>

            <div class="st-form__field">
              <label class="st-label">Email *</label>
              <input type="email" class="st-input" placeholder="Nhập email">
            </div>
          </div>

          <div class="st-form__row">
            <div class="st-form__field">
              <label class="st-label">Số điện thoại</label>
              <input type="tel" class="st-input" placeholder="Nhập số điện thoại">
            </div>

            <div class="st-form__field">
              <label class="st-label">Loại yêu cầu *</label>
              <select class="st-select">
                <option value="">Chọn loại yêu cầu</option>
                <option value="order">Vấn đề về đơn hàng</option>
                <option value="product">Thông tin sản phẩm</option>
                <option value="payment">Vấn đề thanh toán</option>
                <option value="shipping">Vận chuyển</option>
                <option value="return">Đổi trả</option>
                <option value="other">Khác</option>
              </select>
            </div>
          </div>

          <div class="st-form__field">
            <label class="st-label">Tiêu đề *</label>
            <input type="text" class="st-input" placeholder="Nhập tiêu đề yêu cầu">
          </div>

          <div class="st-form__field">
            <label class="st-label">Nội dung *</label>
            <textarea rows="5" class="st-input st-textarea" placeholder="Mô tả chi tiết vấn đề của bạn..."></textarea>
          </div>

          <div class="st-form__field">
            <label class="st-label">Đính kèm file</label>
            <input type="file" multiple class="st-input">
            <p class="st-form__hint">Hỗ trợ: JPG, PNG, PDF (tối đa 5MB mỗi file)</p>
          </div>

          <div class="st-form__actions">
            <button type="submit" class="st-btn st-btn--primary">Gửi yêu cầu</button>
          </div>
        </form>
      </div>

      <!-- FAQ Section -->
      <div class="st-faq">
        <h2 class="st-faq__title">Câu hỏi thường gặp</h2>

        <div class="st-faq__list">
          <div class="st-faq__item">
            <button class="st-faq__question">
              <span>Làm thế nào để theo dõi đơn hàng?</span>
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>
            <div class="st-faq__answer">
              Bạn có thể theo dõi đơn hàng bằng cách đăng nhập vào tài khoản và truy cập mục "Đơn hàng" hoặc sử dụng mã đơn hàng được gửi qua email.
            </div>
          </div>

          <div class="st-faq__item">
            <button class="st-faq__question">
              <span>Chính sách đổi trả như thế nào?</span>
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>
          </div>

          <div class="st-faq__item">
            <button class="st-faq__question">
              <span>Thời gian giao hàng là bao lâu?</span>
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Trang hỗ trợ khách hàng STRIKER
</script>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-page {
  --ink: #10141a;
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --line: #dde3ea;
  --steel: #8c98a8;
  --slate: #4a5563;
  min-height: 100vh;
  background: var(--mist);
}

.st-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Header ===== */
.st-head {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 3rem 0 2.5rem;
}

.st-head__jersey {
  position: absolute;
  top: 50%;
  right: 3rem;
  transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(7rem, 16vw, 13rem);
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08);
  pointer-events: none;
  user-select: none;
}

.st-head__inner {
  position: relative;
  z-index: 1;
}

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.8rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--strike);
}

.st-eyebrow__bar {
  width: 2.5rem;
  height: 3px;
  background: var(--strike);
}

.st-display {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  line-height: 0.95;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.6rem;
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1rem;
  color: var(--steel);
  margin: 0;
  max-width: 32rem;
}

/* ===== Body ===== */
.st-body { padding: 2.5rem 1.25rem 4rem; }

/* Quick help grid */
.st-quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.st-quick-card {
  background: var(--chalk);
  border: 1px solid var(--line);
  padding: 1.75rem 1.25rem;
  text-align: center;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.st-quick-card:hover {
  border-color: var(--strike);
  transform: translateY(-2px);
}

.st-quick-card__icon {
  width: 3rem;
  height: 3rem;
  margin: 0 auto 0.9rem;
  display: grid;
  place-items: center;
  background: var(--mist);
  border-radius: 50%;
  color: var(--strike);
}

.st-quick-card__icon svg { width: 1.5rem; height: 1.5rem; }

.st-quick-card__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.3rem;
}

.st-quick-card__desc {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
  margin: 0;
}

/* Panel / form */
.st-panel {
  background: var(--chalk);
  border: 1px solid var(--line);
  padding: 2rem 1.75rem;
  margin-bottom: 2.5rem;
}

.st-panel__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.6rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 1.5rem;
}

.st-form { display: flex; flex-direction: column; gap: 1.25rem; }

.st-form__row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr));
  gap: 1.25rem;
}

.st-form__field { display: flex; flex-direction: column; gap: 0.5rem; }

.st-label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.82rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--slate);
}

.st-input,
.st-select {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.7rem 0.8rem;
}

.st-textarea { resize: vertical; }

.st-input:focus,
.st-select:focus {
  outline: none;
  border-color: var(--strike);
}

.st-form__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.78rem;
  color: var(--steel);
  margin: 0.35rem 0 0;
}

.st-form__actions { display: flex; justify-content: flex-end; }

.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.75rem 1.75rem;
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

/* FAQ */
.st-faq__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.6rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 1.25rem;
}

.st-faq__list { display: flex; flex-direction: column; gap: 0.75rem; }

.st-faq__item {
  background: var(--chalk);
  border: 1px solid var(--line);
}

.st-faq__question {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.1rem 1.25rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--ink);
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
}

.st-faq__question:hover { background: var(--mist); }

.st-faq__question svg { width: 1.15rem; height: 1.15rem; color: var(--steel); flex-shrink: 0; }

.st-faq__answer {
  padding: 0 1.25rem 1.1rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
  color: var(--slate);
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-input:focus-visible,
.st-select:focus-visible,
.st-faq__question:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}
</style>
