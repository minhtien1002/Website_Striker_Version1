<template>
  <aside :class="['modern-sidebar', { 'sidebar-open': isSidebarActive, 'sidebar-closed': !isSidebarActive }]" aria-label="Main navigation">
    <div class="sidebar-backdrop" v-if="isSidebarActive" @click="$emit('toggle-sidebar')"></div>
    
    <div class="sidebar-content">
      <div class="sidebar-header">
        <div class="logo-container">
          <div class="logo-wrapper">
            <img src="@/assets/images/logo.png" alt="STRIKER" class="logo-img logo-img-light" />
            <img src="@/assets/images/logo-trang.png" alt="STRIKER" class="logo-img logo-img-dark" />
          </div>
        </div>
      </div>

      <div class="sidebar-navigation">
        <div class="nav-section">
          <div class="nav-section-header">
            <span class="section-title">ĐIỀU HƯỚNG CHÍNH</span>
            <div class="section-line"></div>
          </div>

          <nav class="nav-menu">
            <div v-for="item in filteredMenuItems" :key="item.path || item.label" class="menu-item-container">
              <router-link
                v-if="!item.children"
                :to="item.path"
                class="menu-item"
                :class="{ 'menu-item-active': isActive(item) }"
                @click="handleMenuClick"
              >
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <iconify-icon :icon="item.icon"></iconify-icon>
                  </div>
                  <span class="menu-label">{{ item.label }}</span>
                </div>
                <div class="menu-item-indicator"></div>
              </router-link>

              <div v-else class="menu-group">
                <button
                  class="menu-item menu-group-toggle"
                  :class="{ 'menu-item-active': isActive(item), 'menu-group-expanded': item.expanded }"
                  @click="toggleSubMenu(item)"
                >
                  <div class="menu-item-content">
                    <div class="menu-icon">
                      <iconify-icon :icon="item.icon"></iconify-icon>
                    </div>
                    <span class="menu-label">{{ item.label }}</span>
                  </div>
                  <div class="menu-expand-icon">
                    <iconify-icon 
                      :icon="item.expanded ? 'solar:alt-arrow-up-outline' : 'solar:alt-arrow-down-outline'"
                      class="expand-arrow"
                    ></iconify-icon>
                  </div>
                </button>

                <div class="submenu-container" :class="{ 'submenu-expanded': item.expanded }">
                  <div class="submenu-content">
                    <router-link
                      v-for="subItem in getFilteredChildren(item.children)"
                      :key="subItem.path"
                      :to="subItem.path"
                      class="submenu-item"
                      :class="{ 'submenu-item-active': $route.path === subItem.path }"
                      @click="handleMenuClick"
                    >
                      <div class="submenu-item-content">
                        <div class="submenu-dot"></div>
                        <div class="submenu-icon" v-if="subItem.icon">
                          <iconify-icon :icon="subItem.icon"></iconify-icon>
                        </div>
                        <span class="submenu-label">{{ subItem.label }}</span>
                      </div>
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </nav>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="footer-content">
          <div class="user-profile">
            <div class="user-avatar">
              <img src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80" alt="User Avatar" class="avatar-image">
              <div class="status-indicator"></div>
            </div>
            <div class="user-info">
              <div class="user-name">{{ currentUser?.username || 'Người dùng' }}</div>
              <div class="user-role">{{ getUserRoleText(currentUser?.tenQuyen) }}</div>
            </div>
            <div class="user-actions">
              <button class="action-btn logout-btn" title="Đăng xuất" @click="handleLogout">
                <iconify-icon icon="solar:logout-2-outline"></iconify-icon>
              </button>
            </div>
          </div>
          <div class="version-info">
            <span class="version-text">STRIKER v2.0.1</span>
          </div>
        </div>
      </div>
    </div>

  </aside>
</template>

<script>
import authService from '@/services/authService.js'

export default {
  props: {
    isSidebarActive: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['toggle-sidebar', 'show-logout-modal'],
  data() {
    return {
      currentUser: null,
      menuItems: [
        { path: '/thong-ke', label: 'Thống kê', icon: 'solar:home-smile-outline', adminOnly: true },
        { path: '/ban-tai-quay', label: 'Bán Tại Quầy', icon: 'solar:shop-outline', permission: 'ban-tai-quay' },
        { path: '/hoa-don', label: 'Quản Lý Hóa Đơn', icon: 'solar:document-outline', permission: 'hoa-don' },
        {
          path: '/san-pham',
          label: 'Quản Lý Sản Phẩm',
          icon: 'solar:box-minimalistic-outline',
          adminOnly: true
        },
        // Admin-only attribute management
        {
          label: 'Quản Lý Thuộc Tính',
          icon: 'solar:tag-outline',
          expanded: true,
          adminOnly: true,
          children: [
            { path: '/thuoc-tinh/thuong-hieu', label: 'Thương Hiệu', icon: 'mdi:factory', adminOnly: true },
            { path: '/thuoc-tinh/mau-sac', label: 'Màu Sắc', icon: 'solar:palette-outline', adminOnly: true },
            { path: '/thuoc-tinh/kich-co', label: 'Kích Cỡ', icon: 'solar:ruler-outline', adminOnly: true },
            { path: '/thuoc-tinh/chat-lieu', label: 'Chất Liệu', icon: 'mdi:tshirt-crew-outline', adminOnly: true },
            { path: '/thuoc-tinh/danh-muc', label: 'Thể Loại', icon: 'solar:folder-outline', adminOnly: true },
          ],
        },
        { path: '/phieu-giam-gia', label: 'Quản Lý Phiếu Giảm Giá', icon: 'solar:tag-horizontal-outline', permission: 'phieu-giam-gia' },
        { path: '/dot-giam-gia', label: 'Quản Lý Đợt Giảm Giá', icon: 'solar:sale-outline', adminOnly: true },
        { path: '/nhan-vien', label: 'Quản Lý Nhân Viên', icon: 'solar:users-group-rounded-outline', adminOnly: true },
        { path: '/khach-hang', label: 'Quản Lý Khách Hàng', icon: 'solar:user-outline', permission: 'khach-hang' },
      ],
    };
  },
  computed: {
    filteredMenuItems() {
      return this.menuItems.filter(item => this.hasAccessToMenuItem(item));
    }
  },
  methods: {
    hasAccessToMenuItem(item) {
      // Admin has access to everything
      if (authService.isAdmin()) {
        return true;
      }
      
      // Check if item is admin-only
      if (item.adminOnly) {
        return false;
      }
      
      // Check if item requires specific permission
      if (item.permission) {
        return authService.hasPermission(item.permission);
      }
      
      // If no restrictions, allow access
      return true;
    },

    getFilteredChildren(children) {
      if (!children) return [];
      return children.filter(child => this.hasAccessToMenuItem(child));
    },
    isActive(item) {
      // Kiểm tra nếu route hiện tại khớp với path của item hoặc bất kỳ path con nào
      return (
        this.$route.path === item.path ||
        (item.children && item.children.some(child => this.$route.path === child.path))
      );
    },
    handleMenuClick() {
      // Đóng sidebar trên mobile khi click vào menu
      if (window.innerWidth <= 768 && this.isSidebarActive) {
        this.$emit('toggle-sidebar');
      }
    },
    toggleSubMenu(item) {
      // Đảo ngược trạng thái mở rộng của submenu
      item.expanded = !item.expanded;
    },

    handleLogout() {
      // Emit event để AdminLayout xử lý modal
      this.$emit('show-logout-modal');
    },

    getUserRoleText(tenQuyen) {
      // Nếu không có thông tin quyền, trả về mặc định
      if (!tenQuyen) return 'Nhân viên';
      
      // Map các mã quyền từ database sang tên hiển thị
      const roleMap = {
        'ADMIN': 'Quản trị viên',
        'MANAGER': 'Quản lý', 
        'EMPLOYEE': 'Nhân viên',
        'NHANVIEN': 'Nhân viên',
        'QUANLY': 'Quản lý',
        'QUANTRI': 'Quản trị viên'
      };
      
      return roleMap[tenQuyen.toUpperCase()] || tenQuyen;
    },

    loadCurrentUser() {
      this.currentUser = authService.getUser();
      console.log('Current user loaded:', this.currentUser); // Debug log
    }
  },
  watch: {
    // Đóng tất cả các submenu khi route thay đổi
    '$route.path': {
      handler() {
        // Chỉ đóng submenu nếu nó không phải là submenu của mục hiện tại đang active
        this.menuItems.forEach(item => {
          if (item.children) {
            const isCurrentPathInSubmenu = item.children.some(child => this.$route.path.startsWith(child.path));
            if (item.expanded && !isCurrentPathInSubmenu) {
              item.expanded = false;
            }
          }
        });
      },
      immediate: true, // Chạy ngay khi component được tạo
    },
  },
  created() {
    // Load user info khi component được tạo
    this.loadCurrentUser();
    
    // Listen for storage changes (when user logs in/out in another tab)
    window.addEventListener('storage', (e) => {
      if (e.key === 'user_info' || e.key === 'is_logged_in') {
        this.loadCurrentUser();
      }
    });
  },
  mounted() {
    // Load thông tin user khi component được mount
    this.loadCurrentUser();
  }
};
</script>

<style lang="scss" scoped>
.modern-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 280px;
  z-index: 100;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  .sidebar-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(4px);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    z-index: -1;
  }

  .sidebar-content {
    height: 100%;
    width: 100%;
    background: linear-gradient(180deg, 
      rgba(255, 255, 255, 0.95) 0%, 
      rgba(248, 250, 252, 0.95) 100%);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-right: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 
      0 25px 50px rgba(0, 0, 0, 0.15),
      0 0 0 1px rgba(255, 255, 255, 0.05);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .sidebar-header {
    border-bottom: 1px solid rgba(226, 232, 240, 0.5);
    background: rgba(255, 255, 255, 0.8);

    .logo-container {
      display: flex;
      align-items: center;
      gap: 16px;

      .logo-wrapper {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        padding: 24px 0;

        .logo-img {
          height: 52px;
          width: auto;
        }

        .logo-img-dark {
          display: none;
        }
      }

    }
  }

  .sidebar-navigation {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(148, 163, 184, 0.3);
      border-radius: 2px;
    }

    .nav-section {
      padding: 0 20px;

      .nav-section-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;

        .section-title {
          font-size: 11px;
          font-weight: 600;
          color: #64748b;
          text-transform: uppercase;
          letter-spacing: 1px;
          white-space: nowrap;
        }

        .section-line {
          flex: 1;
          height: 1px;
          background: linear-gradient(90deg, 
            rgba(148, 163, 184, 0.3) 0%, 
            transparent 100%);
        }
      }

      .nav-menu {
        .menu-item-container {
          margin-bottom: 4px;

          .menu-item {
            position: relative;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 16px;
            border-radius: 12px;
            text-decoration: none;
            color: #475569;
            font-weight: 500;
            font-size: 14px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            border: none;
            background: transparent;
            width: 100%;

            .menu-item-content {
              display: flex;
              align-items: center;
              gap: 12px;
              flex: 1;

              .menu-icon {
                width: 20px;
                height: 20px;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #64748b;
                font-size: 18px;
                transition: all 0.3s ease;
              }

              .menu-label {
                font-weight: 500;
                transition: all 0.3s ease;
              }
            }

            .menu-item-indicator {
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              width: 3px;
              height: 0;
              background: linear-gradient(180deg, #ff3b18 0%, #c92c10 100%);
              border-radius: 0 2px 2px 0;
              transition: all 0.3s ease;
            }

            .menu-expand-icon {
              color: #94a3b8;
              font-size: 16px;
              transition: all 0.3s ease;

              .expand-arrow {
                transition: transform 0.3s ease;
              }
            }

            &:hover {
              background: rgba(255, 59, 24, 0.08);
              color: #ff3b18;
              transform: translateX(4px);

              .menu-icon {
                color: #ff3b18;
                transform: scale(1.1);
              }

              .menu-expand-icon {
                color: #ff3b18;
              }
            }

            &.menu-item-active {
              background: linear-gradient(135deg, 
                rgba(255, 59, 24, 0.15) 0%, 
                rgba(0, 86, 179, 0.1) 100%);
              color: #ff3b18;
              font-weight: 600;
              box-shadow: 0 4px 12px rgba(255, 59, 24, 0.2);

              .menu-icon {
                color: #ff3b18;
                transform: scale(1.1);
              }

              .menu-item-indicator {
                height: 20px;
              }

              .menu-expand-icon {
                color: #ff3b18;
              }
            }

            &.menu-group-expanded {
              .expand-arrow {
                transform: rotate(180deg);
              }
            }
          }

          .menu-group {
            .submenu-container {
              max-height: 0;
              overflow: hidden;
              transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
              margin-top: 4px;

              .submenu-content {
                padding: 8px 0 8px 12px;
                border-left: 2px solid rgba(255, 59, 24, 0.1);
                margin-left: 16px;

                .submenu-item {
                  display: flex;
                  align-items: center;
                  padding: 10px 16px;
                  border-radius: 8px;
                  text-decoration: none;
                  color: #64748b;
                  font-size: 13px;
                  font-weight: 500;
                  transition: all 0.3s ease;
                  margin-bottom: 2px;

                  .submenu-item-content {
                    display: flex;
                    align-items: center;
                    gap: 10px;

                    .submenu-dot {
                      width: 6px;
                      height: 6px;
                      border-radius: 50%;
                      background: #cbd5e1;
                      transition: all 0.3s ease;
                    }

                    .submenu-icon {
                      width: 16px;
                      height: 16px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      font-size: 14px;
                      color: #94a3b8;
                      transition: all 0.3s ease;
                    }

                    .submenu-label {
                      transition: all 0.3s ease;
                    }
                  }

                  &:hover {
                    background: rgba(255, 59, 24, 0.06);
                    color: #ff3b18;
                    transform: translateX(4px);

                    .submenu-dot {
                      background: #ff3b18;
                      transform: scale(1.3);
                    }

                    .submenu-icon {
                      color: #ff3b18;
                    }
                  }

                  &.submenu-item-active {
                    background: rgba(255, 59, 24, 0.1);
                    color: #ff3b18;
                    font-weight: 600;

                    .submenu-dot {
                      background: #ff3b18;
                      transform: scale(1.3);
                      box-shadow: 0 0 8px rgba(255, 59, 24, 0.4);
                    }

                    .submenu-icon {
                      color: #ff3b18;
                    }
                  }
                }
              }

              &.submenu-expanded {
                max-height: 400px;
              }
            }
          }
        }
      }
    }
  }

  .sidebar-footer {
    padding: 16px;
    border-top: 1px solid rgba(226, 232, 240, 0.5);
    background: rgba(248, 250, 252, 0.8);

    .footer-content {
      .user-profile {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background: rgba(255, 255, 255, 0.7);
        border-radius: 16px;
        border: 1px solid rgba(226, 232, 240, 0.6);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        transition: all 0.3s ease;
        margin-bottom: 12px;

        &:hover {
          background: rgba(255, 255, 255, 0.9);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
          transform: translateY(-1px);
        }

        .user-avatar {
          position: relative;
          flex-shrink: 0;

          .avatar-image {
            width: 40px;
            height: 40px;
            border-radius: 12px;
            object-fit: cover;
            border: 2px solid rgba(255, 59, 24, 0.2);
            transition: all 0.3s ease;
          }

          .status-indicator {
            position: absolute;
            bottom: -2px;
            right: -2px;
            width: 12px;
            height: 12px;
            background: #10b981;
            border: 2px solid white;
            border-radius: 50%;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          }
        }

        .user-info {
          flex: 1;
          min-width: 0;

          .user-name {
            font-size: 14px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 2px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .user-role {
            font-size: 12px;
            color: #64748b;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .user-actions {
          display: flex;
          gap: 4px;

          .action-btn {
            width: 32px;
            height: 32px;
            border: none;
            background: rgba(148, 163, 184, 0.1);
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
            color: #64748b;
            font-size: 16px;

            &:hover {
              background: rgba(255, 59, 24, 0.1);
              color: #ff3b18;
              transform: scale(1.05);
            }

            &.logout-btn:hover {
              background: rgba(239, 68, 68, 0.1);
              color: #ef4444;
            }
          }
        }
      }

      .version-info {
        text-align: center;
        padding: 8px 0;

        .version-text {
          font-size: 11px;
          font-weight: 500;
          color: #94a3b8;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
      }
    }
  }

  // Closed state
  &.sidebar-closed {
    transform: translateX(-100%);

    @media (max-width: 768px) {
      .sidebar-backdrop {
        opacity: 0;
        visibility: hidden;
      }
    }
  }

  // Open state
  &.sidebar-open {
    transform: translateX(0);

    @media (max-width: 768px) {
      .sidebar-backdrop {
        opacity: 1;
        visibility: visible;
      }
    }
  }
}

// Dark theme styles
.dark .modern-sidebar {
  .sidebar-content {
    background: linear-gradient(180deg, 
      rgba(15, 23, 42, 0.95) 0%, 
      rgba(30, 41, 59, 0.95) 100%);
    border-right: 1px solid rgba(71, 85, 105, 0.5);
    box-shadow: 
      0 25px 50px rgba(0, 0, 0, 0.5),
      0 0 0 1px rgba(255, 255, 255, 0.05);
  }

  .sidebar-header {
    background: rgba(15, 23, 42, 0.8);
    border-bottom: 1px solid rgba(71, 85, 105, 0.3);

    .logo-container {
      .logo-wrapper {
        .logo-img-light {
          display: none;
        }

        .logo-img-dark {
          display: block;
        }
      }
    }
  }

  .sidebar-navigation {
    .nav-section {
      .nav-section-header {
        .section-title {
          color: #94a3b8;
        }

        .section-line {
          background: linear-gradient(90deg, 
            rgba(148, 163, 184, 0.2) 0%, 
            transparent 100%);
        }
      }

      .nav-menu {
        .menu-item-container {
          .menu-item {
            color: #cbd5e1;

            .menu-item-content {
              .menu-icon {
                color: #94a3b8;
              }
            }

            .menu-expand-icon {
              color: #64748b;
            }

            &:hover {
              background: rgba(255, 59, 24, 0.15);
              color: #ff8a6e;

              .menu-icon {
                color: #ff8a6e;
              }

              .menu-expand-icon {
                color: #ff8a6e;
              }
            }

            &.menu-item-active {
              background: linear-gradient(135deg, 
                rgba(255, 59, 24, 0.2) 0%, 
                rgba(0, 86, 179, 0.15) 100%);
              color: #ff8a6e;

              .menu-icon {
                color: #ff8a6e;
              }

              .menu-expand-icon {
                color: #ff8a6e;
              }
            }
          }

          .menu-group {
            .submenu-container {
              .submenu-content {
                border-left: 2px solid rgba(255, 59, 24, 0.2);

                .submenu-item {
                  color: #94a3b8;

                  .submenu-item-content {
                    .submenu-dot {
                      background: #64748b;
                    }

                    .submenu-icon {
                      color: #64748b;
                    }
                  }

                  &:hover {
                    background: rgba(255, 59, 24, 0.1);
                    color: #ff8a6e;

                    .submenu-dot {
                      background: #ff8a6e;
                    }

                    .submenu-icon {
                      color: #ff8a6e;
                    }
                  }

                  &.submenu-item-active {
                    background: rgba(255, 59, 24, 0.15);
                    color: #ff8a6e;

                    .submenu-dot {
                      background: #ff8a6e;
                    }

                    .submenu-icon {
                      color: #ff8a6e;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  .sidebar-footer {
    background: rgba(15, 23, 42, 0.8);
    border-top: 1px solid rgba(71, 85, 105, 0.3);

    .footer-content {
      .user-profile {
        background: rgba(30, 41, 59, 0.7);
        border: 1px solid rgba(71, 85, 105, 0.4);

        &:hover {
          background: rgba(30, 41, 59, 0.9);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
        }

        .user-avatar {
          .avatar-image {
            border: 2px solid rgba(102, 179, 255, 0.3);
          }

          .status-indicator {
            background: #10b981;
            border: 2px solid #1e293b;
          }
        }

        .user-info {
          .user-name {
            color: #f1f5f9;
          }

          .user-role {
            color: #94a3b8;
          }
        }

        .user-actions {
          .action-btn {
            background: rgba(71, 85, 105, 0.3);
            color: #94a3b8;

            &:hover {
              background: rgba(102, 179, 255, 0.2);
              color: #ff8a6e;
            }

            &.logout-btn:hover {
              background: rgba(239, 68, 68, 0.2);
              color: #fca5a5;
            }
          }
        }
      }

      .version-info {
        .version-text {
          color: #64748b;
        }
      }
    }
  }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: slideUp 0.3s ease;
  max-height: 90vh;
  overflow-y: auto;
}

.logout-confirm-modal {
  max-width: 450px;
  width: 90%;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.logout-icon {
  color: #f59e0b;
  font-size: 20px;
}

.close-modal {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  font-size: 20px;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-modal:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 24px;
}

.confirm-message {
  font-size: 16px;
  color: #374151;
  margin-bottom: 16px;
  line-height: 1.5;
}

.warning-note {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 8px;
  font-size: 14px;
  color: #92400e;
}

.warning-note iconify-icon {
  color: #f59e0b;
  font-size: 16px;
  flex-shrink: 0;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 24px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
  justify-content: center;
}

.btn.secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn.secondary:hover {
  background: #e5e7eb;
  color: #1f2937;
}

.btn.danger {
  background: #dc2626;
  color: white;
}

.btn.danger:hover {
  background: #b91c1c;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Dark theme for modal
.dark .modal-content {
  background: #1f2937;
  color: #f9fafb;
}

.dark .modal-header {
  background: #111827;
  border-bottom: 1px solid #374151;
}

.dark .modal-title {
  color: #f9fafb;
}

.dark .close-modal {
  color: #9ca3af;
}

.dark .close-modal:hover {
  background: #374151;
  color: #f3f4f6;
}

.dark .confirm-message {
  color: #d1d5db;
}

.dark .warning-note {
  background: #451a03;
  border: 1px solid #92400e;
  color: #fbbf24;
}

.dark .modal-footer {
  background: #111827;
  border-top: 1px solid #374151;
}

.dark .btn.secondary {
  background: #374151;
  color: #d1d5db;
  border: 1px solid #4b5563;
}

.dark .btn.secondary:hover {
  background: #4b5563;
  color: #f9fafb;
}

// Responsive design
@media (max-width: 768px) {
  .modern-sidebar {
    width: 100%;
    max-width: 320px;

    &.sidebar-closed {
      transform: translateX(-100%);
    }

    &.sidebar-open {
      transform: translateX(0);
    }
  }

  .modal-content {
    margin: 20px;
    width: calc(100% - 40px);
  }

  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
