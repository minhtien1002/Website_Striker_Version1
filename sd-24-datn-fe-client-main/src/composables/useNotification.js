import { createApp } from 'vue'
import { Icon } from '@iconify/vue'
import NotificationToast from '../components/NotificationToast.vue'

class NotificationService {
  constructor() {
    this.notifications = []
  }

  show(options) {
    const {
      message,
      title = '',
      type = 'info',
      duration = 3000,
      position = 'top-right',
      closable = true
    } = options

    // Create a container for the notification
    const container = document.createElement('div')
    document.body.appendChild(container)

    // Create Vue app instance for the notification
    const app = createApp(NotificationToast, {
      message,
      title,
      type,
      duration,
      position,
      closable,
      onClose: () => {
        app.unmount()
        document.body.removeChild(container)
      }
    })

    // Toast này chạy trong 1 Vue app riêng (tách khỏi app chính), nên phải đăng
    // ký lại iconify-icon ở đây — nếu không icon trong toast sẽ không hiện ra.
    app.component('iconify-icon', Icon)

    // Mount the notification
    app.mount(container)

    return app
  }

  success(message, options = {}) {
    return this.show({
      message,
      type: 'success',
      title: options.title || 'Thành công',
      ...options
    })
  }

  error(message, options = {}) {
    return this.show({
      message,
      type: 'error',
      title: options.title || 'Lỗi',
      ...options
    })
  }

  warning(message, options = {}) {
    return this.show({
      message,
      type: 'warning',
      title: options.title || 'Cảnh báo',
      ...options
    })
  }

  info(message, options = {}) {
    return this.show({
      message,
      type: 'info',
      title: options.title || 'Thông báo',
      ...options
    })
  }
}

// Create singleton instance
const notificationService = new NotificationService()

// Composable function
export function useNotification() {
  return {
    notify: notificationService.show.bind(notificationService),
    success: notificationService.success.bind(notificationService),
    error: notificationService.error.bind(notificationService),
    warning: notificationService.warning.bind(notificationService),
    info: notificationService.info.bind(notificationService)
  }
}

// Export service for direct use
export default notificationService
