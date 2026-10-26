<script setup>
import { ref, onMounted } from 'vue';

// Giả định dữ liệu footer được lấy từ một API
const footerData = ref({
  links: {
    'Về chúng tôi': [
      { text: 'Giới thiệu', url: '/about' },
      { text: 'Tuyển dụng', url: '/careers' },
      { text: 'Cửa hàng', url: '/stores' },
      { text: 'Chính sách bảo mật', url: '/privacy-policy' }
    ],
    'Hỗ trợ khách hàng': [
      { text: 'Chính sách đổi trả', url: '/return-policy' },
      { text: 'Hướng dẫn mua hàng', url: '/how-to-buy' },
      { text: 'Chính sách thanh toán', url: '/payment-policy' },
      { text: 'Chính sách vận chuyển', url: '/shipping-policy' }
    ]
  },
  contact: {
    address: 'Tòa nhà FPT Polytechnic., Cổng số 2, 13 P. Trịnh Văn Bô, Xuân Phương, Nam Từ Liêm, Hà Nội',
    phone: '0971502422',
    email: 'support@striker.vn'
  },
  socials: [
    { name: 'Facebook', url: 'https://www.facebook.com/striker.sportswear', icon: 'M22.675 0h-21.35C.598 0 0 .598 0 1.338v21.325c0 .74.598 1.337 1.325 1.337h11.338v-9.5h-3.375v-3.925h3.375v-2.85c0-3.325 2.037-5.138 4.987-5.138 1.425 0 2.65.1 3.012.15v3.413h-2.025c-1.6 0-1.925.75-1.925 1.888v2.475h3.837l-.5 3.925h-3.337v9.5h6.1C23.402 24 24 23.402 24 22.662V1.338C24 .598 23.402 0 22.675 0z' },
    { name: 'Instagram', url: 'https://www.instagram.com/striker.sportswear', icon: 'M12 0C8.74 0 8.358.01 7.152.06c-1.218.04-2.03.243-2.75.525-1.52.59-2.766 1.836-3.356 3.356C.293 5.122.09 5.935.05 7.15c-.048 1.208-.06 1.59-.06 4.852s.01 3.644.06 4.852c.04 1.215.242 2.028.525 2.75.59 1.52 1.836 2.766 3.356 3.356.72.283 1.53.485 2.75.525 1.208.048 1.59.06 4.852.06s3.644-.01 4.852-.06c1.215-.04 2.028-.242 2.75-.525 1.52-.59 2.766-1.836 3.356-3.356.283-.72.485-1.53.525-2.75.048-1.208.06-1.59.06-4.852s-.01-3.644-.06-4.852c-.04-1.215-.242-2.028-.525-2.75-1.424-3.6-5.83-4.59-8.498-4.59zM12 2.1c3.27 0 3.682.012 4.966.06c1.17.042 1.76.22 2.162.385.95.385 1.722 1.157 2.106 2.106.165.402.343.992.385 2.162.048 1.284.06 1.696.06 4.966s-.012 3.682-.06 4.966c-.042 1.17-.22 1.76-.385 2.162-.385.95-1.157 1.722-2.106 2.106-.402.165-.992.343-2.162.385-1.284.048-1.696.06-4.966.06s-3.682-.012-4.966-.06c-1.17-.042-1.76-.22-2.162-.385-.95-.385-1.722-1.157-2.106-2.106-.165-.402-.343-.992-.385-2.162-.048-1.284-.06-1.696-.06-4.966s.012-3.682.06-4.966c.042-1.17.22-1.76.385-2.162.385-.95 1.157-1.722 2.106-2.106.402-.165.992-.343 2.162-.385C8.318 2.112 8.73 2.1 12 2.1zM12 6.55c-3.003 0-5.45 2.447-5.45 5.45s2.447 5.45 5.45 5.45 5.45-2.447 5.45-5.45-2.447-5.45-5.45-5.45zM12 15.65c-1.85 0-3.35-1.5-3.35-3.35s1.5-3.35 3.35-3.35 3.35 1.5 3.35 3.35-1.5 3.35-3.35 3.35zM17.5 4.5c-.828 0-1.5.672-1.5 1.5s.672 1.5 1.5 1.5 1.5-.672 1.5-1.5-.672-1.5-1.5-1.5z' },
    { name: 'Youtube', url: 'https://www.youtube.com/@striker', icon: 'M23.498 6.186a3.02 3.02 0 0 0-2.13-2.135C19.7 3.5 12 3.5 12 3.5s-7.7 0-9.368.55C.797 4.051 0 4.848 0 6.186v11.628c0 1.338.797 2.135 2.13 2.135 1.668.55 9.368.55 9.368.55s7.7 0 9.368-.55a3.02 3.02 0 0 0 2.13-2.135V6.186zm-13.633 8.35v-4.88l4.43 2.44-4.43 2.44z' }
  ],
  logos: [
    { src: '/bo-cong-thuong.png', alt: 'Logo Bộ Công Thương' },
    { src: '/nca-chungnhan.png', alt: 'Logo chứng nhận NCA' },
    { src: '/dmca.png', alt: 'Logo DMCA' }
  ]
});

// Giả lập việc fetch data từ API
const fetchData = async () => {
  try {
    // const response = await axios.get('/api/footer-data');
    // footerData.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu footer:', error);
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <footer class="st-footer">
    <div class="st-container">
      <div class="st-footer__grid">
        <!-- Cột 1: Giới thiệu chung -->
        <div class="st-footer__col">
          <div class="st-footer__brand">
            <img src="@/assets/logo-trang.png" alt="STRIKER" class="st-footer__logo" />
          </div>
          <p class="st-footer__desc">
            Áo thể thao chính hãng, độc đáo và chuẩn phong độ. Cam kết chất lượng vải và dịch vụ tốt nhất cho khách hàng.
          </p>
        </div>

        <!-- Cột 2 & 3: Các liên kết hữu ích -->
        <div v-for="(links, title) in footerData.links" :key="title" class="st-footer__col">
          <h3 class="st-footer__heading">{{ title }}</h3>
          <ul class="st-footer__list">
            <li v-for="(link, index) in links" :key="index">
              <a :href="link.url" class="st-footer__link">{{ link.text }}</a>
            </li>
          </ul>
        </div>

        <!-- Cột 4: Thông tin liên hệ và mạng xã hội -->
        <div class="st-footer__col">
          <h3 class="st-footer__heading">Liên hệ</h3>
          <ul class="st-footer__list">
            <li><span class="st-footer__label">Địa chỉ:</span> {{ footerData.contact.address }}</li>
            <li><span class="st-footer__label">Điện thoại:</span> <a :href="`tel:${footerData.contact.phone}`" class="st-footer__link">{{ footerData.contact.phone }}</a></li>
            <li><span class="st-footer__label">Email:</span> <a :href="`mailto:${footerData.contact.email}`" class="st-footer__link">{{ footerData.contact.email }}</a></li>
          </ul>

          <div class="st-footer__socials">
            <a v-for="(social, index) in footerData.socials" :key="index" :href="social.url" target="_blank" rel="noopener noreferrer" class="st-footer__social">
              <svg fill="currentColor" viewBox="0 0 24 24"><path :d="social.icon" /></svg>
            </a>
          </div>
        </div>
      </div>

      <!-- Bản quyền và dòng cuối chân trang -->
      <div class="st-footer__bottom">
        <p class="st-footer__copyright">&copy; {{ new Date().getFullYear() }} STRIKER SPORTSWEAR. All Rights Reserved.</p>
        <div class="st-footer__logos">
          <img
            v-for="(logo, index) in footerData.logos"
            :key="index"
            :src="logo.src"
            :alt="logo.alt"
            loading="lazy"
            onerror="this.src='https://placehold.co/100x40/d1d5db/374151?text=Logo';"
          />
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-footer {
  --ink: #10141a;
  --strike: #ff3b18;
  --steel: #8c98a8;
  --line: rgba(255, 255, 255, 0.1);
  background: var(--ink);
  color: #b7c0cc;
  padding: 3.5rem 0 2rem;
}

.st-container { width: 100%; max-width: 1280px; margin: 0 auto; padding: 0 1.25rem; }

.st-footer__grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: 2.5rem; }
.st-footer__col { display: flex; flex-direction: column; gap: 1rem; }

.st-footer__brand { display: flex; align-items: center; gap: 0.6rem; }
.st-footer__logo { height: 2.6rem; width: auto; }
.st-footer__desc { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.88rem; line-height: 1.6; margin: 0; }

.st-footer__heading { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 1.05rem; letter-spacing: 0.03em; text-transform: uppercase; color: #fff; margin: 0; }
.st-footer__list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.6rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.88rem; }
.st-footer__label { font-weight: 600; color: #d7dce3; }
.st-footer__link { color: inherit; text-decoration: none; transition: color 0.2s ease; }
.st-footer__link:hover { color: #fff; }

.st-footer__socials { display: flex; gap: 1rem; margin-top: 0.4rem; }
.st-footer__social { color: var(--steel); transition: color 0.2s ease; }
.st-footer__social svg { width: 1.5rem; height: 1.5rem; }
.st-footer__social:hover { color: var(--strike); }

.st-footer__bottom { border-top: 1px solid var(--line); margin-top: 2.5rem; padding-top: 1.5rem; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem; }
.st-footer__copyright { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; margin: 0; }
.st-footer__logos { display: flex; align-items: center; gap: 1rem; }
.st-footer__logos img { height: 2.5rem; object-fit: contain; }
</style>
