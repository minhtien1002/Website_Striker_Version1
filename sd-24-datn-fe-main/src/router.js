import { createRouter, createWebHistory } from 'vue-router'
import authService from './services/authService.js'

// Auth views
import Login from './views/auth/Login.vue'

// Main views
import ThongKe from './views/tongKet/ThongKe.vue'
import HoaDon from './views/hoaDon/HoaDon.vue'
import PhieuGiamGia from './views/khuyenMai/phieugiamgia/PhieuGiamGia.vue'
import DotGiamGia from './views/khuyenMai/dotgiamgia/DotGiamGia.vue'
import BanTaiQuay from './views/banHang/BanTaiQuay.vue'
import SanPham from './views/sanPham/SanPham.vue'
import ChiTietSanPham from './views/sanPham/ChiTietSanPham.vue'
import NhanVien from './views/taiKhoan/nhanvien/NhanVien.vue'
import KhachHang from './views/taiKhoan/khachhang/KhachHang.vue'
import HoaDonChiTiet from './views/hoaDon/HoaDonChiTiet.vue'

// Attribute management views
import ThuongHieuPage from './views/thuocTinhSanPham/ThuongHieu.vue'
import MauSacPage from './views/thuocTinhSanPham/MauSac.vue'
import KichCoPage from './views/thuocTinhSanPham/KichCo.vue'
import ChatLieuPage from './views/thuocTinhSanPham/ChatLieu.vue'
import DanhMucPage from './views/thuocTinhSanPham/DanhMuc.vue'

import ThemPhieuGiamGia from './views/khuyenMai/phieugiamgia/ThemPhieuGiamGia.vue'
import ThemDotGiamGia from './views/khuyenMai/dotgiamgia/ThemDotGiamGia.vue'
import ThemNhanVien from './views/taiKhoan/nhanvien/ThemNhanVien.vue'
import EditNhanVien from './views/taiKhoan/nhanvien/EditNhanVien.vue'
import ThemKhachHang from './views/taiKhoan/khachhang/ThemKhachHang.vue'
import EditKhachHang from './views/taiKhoan/khachhang/EditKhachHang.vue'

// Attribute form pages
import ThemSuaChatLieu from './views/thuocTinhSanPham/themVaChinhSuaThuocTinh/ThemSuaChatLieu.vue'
import ThemSuaDanhMuc from './views/thuocTinhSanPham/themVaChinhSuaThuocTinh/ThemSuaDanhMuc.vue'
import ThemSuaMauSac from './views/thuocTinhSanPham/themVaChinhSuaThuocTinh/ThemSuaMauSac.vue'
import ThemSuaKichCo from './views/thuocTinhSanPham/themVaChinhSuaThuocTinh/ThemSuaKichCo.vue'
import ThemSuaThuongHieu from './views/thuocTinhSanPham/themVaChinhSuaThuocTinh/ThemSuaThuongHieu.vue'
import ThemSanPham from '@/views/sanPham/ThemSanPham.vue';
import SuaSanPham from '@/views/sanPham/SuaSanPham.vue';
import SuaChiTietSanPham from '@/views/sanPham/SuaChiTietSanPham.vue';

const routes = [
  // Auth routes
  { path: '/login', name: 'Login', component: Login, meta: { requiresGuest: true } },
  
  // Main routes
  { path: '/', name: 'Home', redirect: '/ban-tai-quay' }, // Redirect to sales counter for all users
  { path: '/thong-ke', name: 'ThongKe', component: ThongKe, meta: { requiresAuth: true, adminOnly: true } },

  // Sales counter - accessible by both admin and employees
  { path: '/ban-tai-quay', name: 'BanTaiQuay', component: BanTaiQuay, meta: { requiresAuth: true, permission: 'ban-tai-quay' } },

  // Invoice management - accessible by both admin and employees
  { path: '/hoa-don', name: 'HoaDon', component: HoaDon, meta: { requiresAuth: true, permission: 'hoa-don' } },
  { path: '/hoa-don/chi-tiet/:id', name: 'HoaDonChiTiet', component: HoaDonChiTiet, meta: { requiresAuth: true, permission: 'hoa-don' } },

  // Customer management - accessible by both admin and employees
  { path: '/khach-hang', name: 'KhachHang', component: KhachHang, meta: { requiresAuth: true, permission: 'khach-hang' } },
  { path: '/them-khach-hang', name: 'ThemKhachHang', component: ThemKhachHang, meta: { requiresAuth: true, permission: 'khach-hang' } },
  { path: '/khach-hang/edit/:id', name: 'EditKhachHang', component: EditKhachHang, meta: { requiresAuth: true, permission: 'khach-hang' } },

  // Voucher management - view accessible by both admin and employees, add/edit admin only
  { path: '/phieu-giam-gia', name: 'PhieuGiamGia', component: PhieuGiamGia, meta: { requiresAuth: true, permission: 'phieu-giam-gia' } },
  { path: '/them-phieu-giam-gia', name: 'ThemPhieuGiamGia', component: ThemPhieuGiamGia, meta: { requiresAuth: true, adminOnly: true } },

  // Admin-only routes - Product management
  {
    path: '/san-pham',
    name: 'SanPham',
    component: SanPham,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/san-pham/chi-tiet/:id',
    name: 'ChiTietSanPham',
    component: ChiTietSanPham,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/san-pham/them',
    name: 'ThemSanPham',
    component: ThemSanPham,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/san-pham/sua/:id',
    name: 'SuaSanPham',
    component: SuaSanPham,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/san-pham/chi-tiet/sua/:id',
    name: 'SuaChiTietSanPham',
    component: SuaChiTietSanPham,
    meta: { requiresAuth: true, adminOnly: true }
  },

  // Admin-only routes - Discount campaigns
  { path: '/dot-giam-gia', name: 'DotGiamGia', component: DotGiamGia, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/them-dot-giam-gia', name: 'ThemDotGiamGia', component: ThemDotGiamGia, meta: { requiresAuth: true, adminOnly: true } },

  // Admin-only routes - Employee management
  { path: '/nhan-vien', name: 'NhanVien', component: NhanVien, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/them-nhan-vien', name: 'ThemNhanVien', component: ThemNhanVien, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/nhan-vien/edit/:id', name: 'EditNhanVien', component: EditNhanVien, meta: { requiresAuth: true, adminOnly: true } },

  // Admin-only routes - Attribute management
  { path: '/thuoc-tinh/thuong-hieu', name: 'ThuongHieu', component: ThuongHieuPage, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/thuong-hieu/them', name: 'ThemThuongHieu', component: ThemSuaThuongHieu, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/thuong-hieu/sua/:id', name: 'EditThuongHieu', component: ThemSuaThuongHieu, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/mau-sac', name: 'MauSac', component: MauSacPage, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/mau-sac/them', name: 'ThemMauSac', component: ThemSuaMauSac, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/mau-sac/sua/:id', name: 'EditMauSac', component: ThemSuaMauSac, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/kich-co', name: 'KichCo', component: KichCoPage, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/kich-co/them', name: 'ThemKichCo', component: ThemSuaKichCo, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/kich-co/sua/:id', name: 'EditKichCo', component: ThemSuaKichCo, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/chat-lieu', name: 'ChatLieu', component: ChatLieuPage, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/chat-lieu/them', name: 'ThemChatLieu', component: ThemSuaChatLieu, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/chat-lieu/sua/:id', name: 'EditChatLieu', component: ThemSuaChatLieu, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/danh-muc', name: 'DanhMuc', component: DanhMucPage, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/danh-muc/them', name: 'ThemDanhMuc', component: ThemSuaDanhMuc, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/thuoc-tinh/danh-muc/sua/:id', name: 'EditDanhMuc', component: ThemSuaDanhMuc, meta: { requiresAuth: true, adminOnly: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated()
  
  // Check if route requires authentication
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      // Redirect to login if not authenticated
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check role-based permissions
    const requiresAdmin = to.matched.some(record => record.meta.adminOnly)
    const requiredPermission = to.matched.find(record => record.meta.permission)?.meta.permission
    
    if (requiresAdmin && !authService.isAdmin()) {
      // Admin-only route but user is not admin
      next({ name: 'BanTaiQuay' }) // Redirect to sales counter
      return
    }
    
    if (requiredPermission && !authService.hasPermission(requiredPermission)) {
      // User doesn't have required permission
      next({ name: 'BanTaiQuay' }) // Redirect to sales counter
      return
    }
  }
  
  // Check if route requires guest (not authenticated)
  if (to.matched.some(record => record.meta.requiresGuest)) {
    if (isAuthenticated) {
      // Redirect based on user role
      if (authService.isAdmin()) {
        next({ name: 'ThongKe' }) // Admin goes to dashboard
      } else {
        next({ name: 'BanTaiQuay' }) // Employee goes to sales counter
      }
      return
    }
  }
  
  next()
})

export default router
