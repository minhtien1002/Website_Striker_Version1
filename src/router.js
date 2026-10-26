import { createRouter, createWebHistory } from 'vue-router'
import authService from './services/authService.js'
import TrangChu from './pages/TrangChu.vue'
import SanPham from './pages/SanPham.vue'
import GiamGia from './pages/GiamGia.vue'
import TraCuuDonHang from './pages/TraCuuDonHang.vue'
import HoTro from './pages/HoTro.vue'
// import FavoritesPage from './pages/Favorites.vue'
// import ProfilePage from './pages/ProfilePage.vue' // Đảm bảo tên file đúng
import AuthForm from './components/AuthForm.vue'
import CheckoutPage from './pages/CheckoutPage.vue'

// Account Dashboard imports
import AccountDashboard from './components/AccountDashboard.vue'
import TongQuan from './pages/TongQuan.vue'
import LichSuMuaHang from './pages/LichSuMuaHang.vue'
import ThongTinTaiKhoan from './pages/ThongTinTaiKhoan.vue'
import GopYPhanHoi from './pages/GopYPhanHoi.vue'
import ChiTietSanPham from './pages/ChiTietSanPham.vue'
import GioHang from './pages/GioHang.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'home', component: TrangChu },
  { path: '/trang-chu', redirect: '/home' },
  { path: '/products', name: 'products', component: SanPham },
  { path: '/discounts', name: 'discounts', component: GiamGia },
  { path: '/orders', name: 'orders', component: TraCuuDonHang },
  { path: '/support', name: 'support', component: HoTro },
  // { path: '/favorites', name: 'favorites', component: FavoritesPage },
  // { path: '/profile', name: 'profile', component: ProfilePage },
  { path: '/product/:id', name: 'product-detail', component: ChiTietSanPham },
  { path: '/cart', name: 'cart', component: GioHang },
  { path: '/auth', name: 'auth', component: AuthForm },
  { path: '/checkout', name: 'checkout', component: CheckoutPage }, // <<< Đã thêm route

  // Account Dashboard routes (requires authentication)
  {
    path: '/account',
    component: AccountDashboard,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/account/overview' },
      { path: 'overview', name: 'account-overview', component: TongQuan, meta: { requiresAuth: true } },
      { path: 'purchase-history', name: 'account-purchase-history', component: LichSuMuaHang, meta: { requiresAuth: true } },
      { path: 'profile', name: 'account-profile', component: ThongTinTaiKhoan, meta: { requiresAuth: true } },
      { path: 'feedback', name: 'account-feedback', component: GopYPhanHoi, meta: { requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const isAuthenticated = authService.isAuthenticated()

  if (requiresAuth && !isAuthenticated) {
    // Redirect to auth page if route requires authentication and user is not logged in
    next('/auth')
  } else if (to.path === '/auth' && isAuthenticated) {
    // Redirect authenticated users away from auth page
    next('/')
  } else {
    next()
  }
})

export default router