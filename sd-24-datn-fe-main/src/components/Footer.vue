<template>
  <footer class="modern-footer">
    <div class="footer-content">
      <div class="footer-main">
        <!-- Logo và mô tả -->
        <div class="footer-brand">
          <div class="logo-container">
            <img src="@/assets/images/logo.png" alt="STRIKER" class="footer-logo-img footer-logo-img-light" />
            <img src="@/assets/images/logo-trang.png" alt="STRIKER" class="footer-logo-img footer-logo-img-dark" />
          </div>
          <p class="brand-description">
            Nền tảng quản lý bán hàng hiệu quả được phát triển bởi STRIKER Team.
            Tối ưu hóa quy trình kinh doanh của bạn một cách dễ dàng và chuyên nghiệp.
          </p>
          <div class="social-links">
            <a href="#" class="social-link">
              <iconify-icon icon="ri:facebook-fill"></iconify-icon>
            </a>
            <a href="#" class="social-link">
              <iconify-icon icon="ri:instagram-line"></iconify-icon>
            </a>
            <a href="#" class="social-link">
              <iconify-icon icon="ri:twitter-x-line"></iconify-icon>
            </a>
            <a href="#" class="social-link">
              <iconify-icon icon="ri:linkedin-fill"></iconify-icon>
            </a>
          </div>
        </div>

        <!-- Liên kết nhanh -->
        <div class="footer-section">
          <h3 class="section-title">Liên kết nhanh</h3>
          <div class="section-line"></div>
          <ul class="footer-links">
            <li><router-link to="/thong-ke" class="footer-link">
              <iconify-icon icon="solar:home-smile-outline"></iconify-icon>
              Thống kê
            </router-link></li>
            <li><router-link to="/ban-tai-quay" class="footer-link">
              <iconify-icon icon="solar:shop-outline"></iconify-icon>
              Bán Tại Quầy
            </router-link></li>
            <li><router-link to="/san-pham" class="footer-link">
              <iconify-icon icon="solar:box-minimalistic-outline"></iconify-icon>
              Quản Lý Sản Phẩm
            </router-link></li>
            <li><router-link to="/hoa-don" class="footer-link">
              <iconify-icon icon="solar:document-outline"></iconify-icon>
              Hóa Đơn
            </router-link></li>
          </ul>
        </div>

        <!-- Quản lý -->
        <div class="footer-section">
          <h3 class="section-title">Quản lý</h3>
          <div class="section-line"></div>
          <ul class="footer-links">
            <li><router-link to="/nhan-vien" class="footer-link">
              <iconify-icon icon="solar:users-group-rounded-outline"></iconify-icon>
              Nhân Viên
            </router-link></li>
            <li><router-link to="/khach-hang" class="footer-link">
              <iconify-icon icon="solar:user-outline"></iconify-icon>
              Khách Hàng
            </router-link></li>
            <li><router-link to="/phieu-giam-gia" class="footer-link">
              <iconify-icon icon="solar:tag-horizontal-outline"></iconify-icon>
              Phiếu Giảm Giá
            </router-link></li>
            <li><router-link to="/dot-giam-gia" class="footer-link">
              <iconify-icon icon="solar:sale-outline"></iconify-icon>
              Đợt Giảm Giá
            </router-link></li>
          </ul>
        </div>

        <!-- Thông tin liên hệ -->
        <div class="footer-section">
          <h3 class="section-title">Thông tin liên hệ</h3>
          <div class="section-line"></div>
          <div class="contact-info">
            <div class="contact-item">
              <div class="contact-icon">
                <iconify-icon icon="solar:map-linear"></iconify-icon>
              </div>
              <div class="contact-details">
                <span class="contact-label">Địa chỉ</span>
                <span class="contact-value">268 Lý Thường Kiệt, Q.10, TP.HCM</span>
              </div>
            </div>
            <div class="contact-item">
              <div class="contact-icon">
                <iconify-icon icon="solar:phone-linear"></iconify-icon>
              </div>
              <div class="contact-details">
                <span class="contact-label">Điện thoại</span>
                <span class="contact-value">+84 123 456 789</span>
              </div>
            </div>
            <div class="contact-item">
              <div class="contact-icon">
                <iconify-icon icon="solar:letter-linear"></iconify-icon>
              </div>
              <div class="contact-details">
                <span class="contact-label">Email</span>
                <span class="contact-value">contact@striker.vn</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer bottom -->
      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <div class="copyright">
            <span>&copy; 2026 STRIKER Team. All Rights Reserved.</span>
          </div>
          <div class="footer-meta">
            <span class="version-info">Version 2.0.1</span>
            <span class="build-info">Build 2026</span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// Không cần script setup nào ở đây
</script>

<style lang="scss" scoped>
.modern-footer {
  background: linear-gradient(180deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.95) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 
    0 -25px 50px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);

  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .footer-main {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    gap: 48px;
    padding: 48px 0;

    @media (max-width: 1024px) {
      grid-template-columns: 1fr 1fr;
      gap: 32px;
    }

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
      gap: 32px;
      text-align: center;
    }
  }

  .footer-brand {
    .logo-container {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 20px;

      @media (max-width: 768px) {
        justify-content: center;
      }

      .footer-logo-img {
        height: 44px;
        width: auto;
      }

      .footer-logo-img-dark {
        display: none;
      }
    }

    .brand-description {
      font-size: 14px;
      line-height: 1.6;
      color: #64748b;
      margin-bottom: 24px;
      font-weight: 400;
    }

    .social-links {
      display: flex;
      gap: 12px;

      @media (max-width: 768px) {
        justify-content: center;
      }

      .social-link {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: rgba(255, 59, 24, 0.1);
        color: #ff3b18;
        text-decoration: none;
        transition: all 0.3s ease;
        font-size: 18px;

        &:hover {
          background: #ff3b18;
          color: white;
          transform: translateY(-2px);
          box-shadow: 0 8px 25px rgba(255, 59, 24, 0.3);
        }
      }
    }
  }

  .footer-section {
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 16px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .section-line {
      width: 40px;
      height: 2px;
      background: linear-gradient(90deg, #ff3b18 0%, #c92c10 100%);
      margin-bottom: 20px;
      border-radius: 1px;

      @media (max-width: 768px) {
        margin: 0 auto 20px auto;
      }
    }

    .footer-links {
      list-style: none;
      padding: 0;
      margin: 0;

      li {
        margin-bottom: 12px;
      }

      .footer-link {
        display: flex;
        align-items: center;
        gap: 10px;
        color: #64748b;
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;
        padding: 8px 0;

        @media (max-width: 768px) {
          justify-content: center;
        }

        iconify-icon {
          font-size: 16px;
          color: #94a3b8;
          transition: all 0.3s ease;
        }

        &:hover {
          color: #ff3b18;
          transform: translateX(4px);

          @media (max-width: 768px) {
            transform: translateY(-2px);
          }

          iconify-icon {
            color: #ff3b18;
          }
        }

        &.router-link-active {
          color: #ff3b18;
          font-weight: 600;

          iconify-icon {
            color: #ff3b18;
          }
        }
      }
    }

    .contact-info {
      .contact-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 16px;

        @media (max-width: 768px) {
          justify-content: center;
          text-align: left;
        }

        .contact-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 36px;
          height: 36px;
          border-radius: 8px;
          background: rgba(255, 59, 24, 0.1);
          color: #ff3b18;
          font-size: 16px;
          flex-shrink: 0;
        }

        .contact-details {
          display: flex;
          flex-direction: column;
          gap: 2px;

          .contact-label {
            font-size: 12px;
            font-weight: 600;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }

          .contact-value {
            font-size: 14px;
            color: #475569;
            font-weight: 500;
          }
        }
      }
    }
  }

  .footer-bottom {
    border-top: 1px solid rgba(226, 232, 240, 0.5);
    background: rgba(248, 250, 252, 0.8);
    padding: 0rem 1rem;

    .footer-bottom-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px 0;

      @media (max-width: 768px) {
        flex-direction: column;
        gap: 12px;
        text-align: center;
      }

      .copyright {
        span {
          font-size: 13px;
          color: #64748b;
          font-weight: 500;
        }
      }

      .footer-meta {
        display: flex;
        align-items: center;
        gap: 16px;

        .version-info {
          font-size: 12px;
          font-weight: 600;
          color: #475569;
        }

        .build-info {
          font-size: 10px;
          color: #94a3b8;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
      }
    }
  }
}

// Dark theme styles
.dark .modern-footer {
  background: linear-gradient(180deg, 
    rgba(15, 23, 42, 0.95) 0%, 
    rgba(30, 41, 59, 0.95) 100%);
  border-top: 1px solid rgba(71, 85, 105, 0.5);
  box-shadow: 
    0 -25px 50px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.05);

  .footer-brand {
    .logo-container {
      .footer-logo-img-light {
        display: none;
      }

      .footer-logo-img-dark {
        display: block;
      }
    }

    .brand-description {
      color: #94a3b8;
    }

    .social-links {
      .social-link {
        background: rgba(255, 59, 24, 0.2);
        color: #66b3ff;

        &:hover {
          background: #66b3ff;
          color: #0f172a;
        }
      }
    }
  }

  .footer-section {
    .section-title {
      color: #f1f5f9;
    }

    .footer-links {
      .footer-link {
        color: #94a3b8;

        iconify-icon {
          color: #64748b;
        }

        &:hover {
          color: #66b3ff;

          iconify-icon {
            color: #66b3ff;
          }
        }

        &.router-link-active {
          color: #66b3ff;

          iconify-icon {
            color: #66b3ff;
          }
        }
      }
    }

    .contact-info {
      .contact-item {
        .contact-icon {
          background: rgba(255, 59, 24, 0.2);
          color: #66b3ff;
        }

        .contact-details {
          .contact-label {
            color: #64748b;
          }

          .contact-value {
            color: #cbd5e1;
          }
        }
      }
    }
  }

  .footer-bottom {
    background: rgba(15, 23, 42, 0.8);
    border-top: 1px solid rgba(71, 85, 105, 0.3);

    .footer-bottom-content {
      .copyright span {
        color: #94a3b8;
      }

      .footer-meta {
        .version-info {
          color: #cbd5e1;
        }

        .build-info {
          color: #64748b;
        }
      }
    }
  }
}
</style>