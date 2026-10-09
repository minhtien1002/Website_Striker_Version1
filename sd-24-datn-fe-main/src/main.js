import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { autoAnimatePlugin } from '@formkit/auto-animate/vue'
import Toast from 'vue-toastification'
import { Icon } from '@iconify/vue'
import CoreUI from '@coreui/vue'
import CIcon from '@coreui/icons-vue'
import * as icons from '@coreui/icons'

import App from './App.vue'
import router from './router.js'

// Styles
import '@coreui/coreui/dist/css/coreui.min.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'vue-toastification/dist/index.css'
import './assets/main.scss'
import './assets/style.css'
import './assets/theme.css' // giao diện phẳng dùng chung - phải nạp cuối cùng

// Create app instance
const app = createApp(App)
const pinia = createPinia()

// Register plugins
app.use(pinia)
app.use(router)
app.use(CoreUI)
app.use(autoAnimatePlugin)

// Configure Toast notifications
app.use(Toast, {
  transition: 'Vue-Toastification__bounce',
  maxToasts: 20,
  newestOnTop: true,
  position: 'top-right',
  timeout: 3000,
  closeOnClick: true,
  pauseOnFocusLoss: true,
  pauseOnHover: true,
  toastDefaults: {
    success: { timeout: 3000, playSound: false },
    error: { timeout: 5000, playSound: false },
    warning: { timeout: 4000, playSound: false }
  }
})

// Register global components
app.component('CIcon', CIcon)
app.component('iconify-icon', Icon)

// Provide global icons
app.provide('icons', icons)

// Validate DOM element exists before mounting
if (!document.getElementById('app')) {
  console.error('Element #app not found in index.html')
}

// Mount the application
app.mount('#app')
