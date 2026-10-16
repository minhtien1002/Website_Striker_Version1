<template>
    <div class="breadcrumb-container">
      <div class="breadcrumb-content">
        <!-- Main Breadcrumb -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
          <ol class="breadcrumb-list">
            <li class="breadcrumb-item">
              <router-link to="/" class="breadcrumb-link home-link">
                <span class="breadcrumb-text">Trang chủ</span>
              </router-link>
            </li>
            <li v-for="(item, index) in breadcrumbItems" :key="index" class="breadcrumb-item">
              <router-link 
                v-if="item.path && index !== breadcrumbItems.length - 1"
                :to="item.path" 
                class="breadcrumb-link"
              >
                <span class="breadcrumb-text">{{ item.label }}</span>
              </router-link>
              <span v-else class="breadcrumb-current">
                <span class="breadcrumb-text">{{ item.label }}</span>
              </span>
            </li>
          </ol>
        </nav>
  
        <!-- Page Actions (optional) -->
        <div v-if="actions && actions.length > 0" class="breadcrumb-actions">
          <button
            v-for="(action, index) in actions"
            :key="index"
            @click="action.handler"
            :class="['action-btn', action.type || 'default']"
            :disabled="action.disabled"
          >
            <iconify-icon v-if="action.icon" :icon="action.icon" class="action-icon"></iconify-icon>
            <span class="action-text">{{ action.label }}</span>
          </button>
        </div>
      </div>
  
      <!-- Page Info (optional) -->
      <div v-if="showPageInfo" class="page-info">
        <div class="page-header">
          <div class="page-title-section">
            <h1 class="page-title">
              <iconify-icon v-if="pageIcon" :icon="pageIcon" class="page-title-icon"></iconify-icon>
              {{ pageTitle }}
            </h1>
            <p v-if="pageDescription" class="page-description">{{ pageDescription }}</p>
          </div>
          <div v-if="pageStats && pageStats.length > 0" class="page-stats">
            <div 
              v-for="(stat, index) in pageStats" 
              :key="index" 
              class="stat-item"
            >
              <iconify-icon v-if="stat.icon" :icon="stat.icon" class="stat-icon"></iconify-icon>
              <div class="stat-content">
                <span class="stat-value">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Breadcrumb',
    props: {
      // Breadcrumb items
      items: {
        type: Array,
        default: () => []
      },
      // Page actions (buttons)
      actions: {
        type: Array,
        default: () => []
      },
      // Show extended page info
      showPageInfo: {
        type: Boolean,
        default: false
      },
      // Page title
      pageTitle: {
        type: String,
        default: ''
      },
      // Page description
      pageDescription: {
        type: String,
        default: ''
      },
      // Page icon
      pageIcon: {
        type: String,
        default: ''
      },
      // Page statistics
      pageStats: {
        type: Array,
        default: () => []
      }
    },
    computed: {
      breadcrumbItems() {
        return this.items || []
      }
    }
  }
  </script>
  
  <style scoped>
  .breadcrumb-container {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-radius: 16px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 
      0 10px 25px rgba(0, 0, 0, 0.08),
      0 0 0 1px rgba(255, 255, 255, 0.1);
    margin-bottom: 24px;
    overflow: hidden;
  }
  
  .breadcrumb-content {
    padding: 16px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  /* Breadcrumb Navigation */
  .breadcrumb-nav {
    flex: 1;
    min-width: 0;
  }
  
  .breadcrumb-list {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin: 0;
    padding: 0;
    list-style: none;
  }
  
  .breadcrumb-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .breadcrumb-separator {
    color: #94a3b8;
    font-size: 14px;
    flex-shrink: 0;
  }
  
  .breadcrumb-link {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    border-radius: 8px;
    text-decoration: none;
    color: #64748b;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  .breadcrumb-link:hover {
    background: rgba(255, 59, 24, 0.08);
    color: #ff3b18;
    transform: translateY(-1px);
  }
  
  .breadcrumb-link.home-link {
    color: #ff3b18;
  }
  
  .breadcrumb-current {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    color: #1a202c;
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
  }
  
  .breadcrumb-text {
    flex-shrink: 0;
  }
  
  /* Actions */
  .breadcrumb-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    flex: 0 0 auto;
    justify-content: flex-end;
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
    white-space: nowrap;
    flex: 0 0 auto;
  }
  
  .action-btn.default {
    background: rgba(255, 59, 24, 0.1);
    color: #ff3b18;
    border: 1px solid rgba(255, 59, 24, 0.2);
  }
  
  .action-btn.default:hover:not(:disabled) {
    background: rgba(255, 59, 24, 0.15);
    transform: translateY(-1px);
  }
  
  .action-btn.primary {
    background: #ff3b18;
    color: white;
    border: 1px solid #ff3b18;
  }
  
  .action-btn.primary:hover:not(:disabled) {
    background: #c92c10;
    transform: translateY(-1px);
  }
  
  .action-btn.success {
    background: #28a745;
    color: white;
    border: 1px solid #28a745;
  }
  
  .action-btn.success:hover:not(:disabled) {
    background: #1e7e34;
    transform: translateY(-1px);
  }
  
  .action-btn.warning {
    background: #ffc107;
    color: #212529;
    border: 1px solid #ffc107;
  }
  
  .action-btn.warning:hover:not(:disabled) {
    background: #e0a800;
    transform: translateY(-1px);
  }
  
  .action-btn.danger {
    background: #dc3545;
    color: white;
    border: 1px solid #dc3545;
  }
  
  .action-btn.danger:hover:not(:disabled) {
    background: #c82333;
    transform: translateY(-1px);
  }
  
  .action-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .action-icon {
    font-size: 16px;
  }
  
  /* Page Info Section */
  .page-info {
    border-top: 1px solid rgba(226, 232, 240, 0.5);
    background: rgba(248, 250, 252, 0.5);
  }
  
  .page-header {
    padding: 20px 24px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
  }
  
  .page-title-section {
    flex: 1;
    min-width: 260px;
  }
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0 0 8px 0;
    font-size: 1.75rem;
    font-weight: 700;
    color: #1a202c;
  }
  
  .page-title-icon {
    font-size: 2rem;
    color: #ff3b18;
  }
  
  .page-description {
    margin: 0;
    color: #64748b;
    font-size: 1rem;
    line-height: 1.5;
  }
  
  .page-stats {
    display: flex;
    gap: 24px;
    flex-wrap: wrap;
    justify-content: flex-end;
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    border: 1px solid rgba(226, 232, 240, 0.5);
  }
  
  .stat-icon {
    font-size: 20px;
    color: #ff3b18;
  }
  
  .stat-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }
  
  .stat-value {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1a202c;
  }
  
  .stat-label {
    font-size: 0.8rem;
    color: #64748b;
    font-weight: 500;
  }
  
  /* Dark Theme */
  .dark .breadcrumb-container {
    background: linear-gradient(135deg, rgba(30, 41, 59, 0.95) 0%, rgba(15, 23, 42, 0.95) 100%);
    border: 1px solid rgba(71, 85, 105, 0.5);
    box-shadow: 
      0 10px 25px rgba(0, 0, 0, 0.3),
      0 0 0 1px rgba(255, 255, 255, 0.05);
  }
  
  .dark .breadcrumb-link {
    color: #94a3b8;
  }
  
  .dark .breadcrumb-link:hover {
    background: rgba(66, 179, 255, 0.1);
    color: #66b3ff;
  }
  
  .dark .breadcrumb-link.home-link {
    color: #66b3ff;
  }
  
  .dark .breadcrumb-current {
    color: #f1f5f9;
  }
  
  .dark .breadcrumb-separator {
    color: #64748b;
  }
  
  .dark .action-btn.default {
    background: rgba(66, 179, 255, 0.15);
    color: #66b3ff;
    border: 1px solid rgba(66, 179, 255, 0.3);
  }
  
  .dark .action-btn.default:hover:not(:disabled) {
    background: rgba(66, 179, 255, 0.2);
  }
  
  .dark .page-info {
    background: rgba(15, 23, 42, 0.5);
    border-top: 1px solid rgba(71, 85, 105, 0.3);
  }
  
  .dark .page-title {
    color: #f1f5f9;
  }
  
  .dark .page-title-icon {
    color: #66b3ff;
  }
  
  .dark .page-description {
    color: #94a3b8;
  }
  
  .dark .stat-item {
    background: rgba(30, 41, 59, 0.8);
    border: 1px solid rgba(71, 85, 105, 0.3);
  }
  
  .dark .stat-icon {
    color: #66b3ff;
  }
  
  .dark .stat-value {
    color: #f1f5f9;
  }
  
  .dark .stat-label {
    color: #94a3b8;
  }
  
  /* Responsive Design */
  @media (max-width: 1200px) {
    .breadcrumb-content {
      align-items: flex-start;
    }

    .breadcrumb-nav,
    .breadcrumb-actions {
      flex-basis: 100%;
    }

    .page-header {
      flex-direction: column;
    }

    .page-stats {
      width: 100%;
      justify-content: flex-start;
    }
  }

  @media (max-width: 768px) {
    .breadcrumb-content {
      padding: 12px 16px;
      flex-direction: column;
      align-items: stretch;
    }
  
    .breadcrumb-list {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
      padding: 4px 0;
    }
  
    .breadcrumb-list::-webkit-scrollbar {
      height: 2px;
    }
  
    .breadcrumb-list::-webkit-scrollbar-track {
      background: transparent;
    }
  
    .breadcrumb-list::-webkit-scrollbar-thumb {
      background: rgba(148, 163, 184, 0.3);
      border-radius: 1px;
    }
  
    .breadcrumb-actions {
      width: 100%;
      justify-content: flex-end;
    }
  
    .action-btn {
      padding: 6px 12px;
      font-size: 13px;
    }
  
    .page-header {
      padding: 16px;
      flex-direction: column;
      gap: 16px;
    }
  
    .page-title {
      font-size: 1.5rem;
    }
  
    .page-stats {
      align-self: stretch;
      justify-content: space-between;
    }
  
    .stat-item {
      flex: 1;
      min-width: 0;
      justify-content: center;
      text-align: center;
    }
  }
  
  @media (max-width: 480px) {
    .breadcrumb-content {
      padding: 10px 12px;
    }
  
    .breadcrumb-text {
      display: none;
    }
  
    .breadcrumb-link.home-link .breadcrumb-text,
    .breadcrumb-current .breadcrumb-text {
      display: inline;
    }
  
    .page-stats {
      flex-direction: column;
      gap: 8px;
    }
  
    .stat-item {
      min-width: auto;
    }
  
    .action-btn .action-text {
      display: none;
    }
  
    .action-btn {
      min-width: 40px;
      padding: 8px;
      justify-content: center;
    }
  }
  </style>
