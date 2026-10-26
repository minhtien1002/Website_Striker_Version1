import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite' // <-- THÊM DÒNG NÀY

export default defineConfig({
  plugins: [
    vue(),
    tailwindcss() // <-- THÊM DÒNG NÀY
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    port: 3000,
    open: '/home',
    proxy: {
      '/api/mokky': {
        target: 'https://a5687b208ca7ac57.mokky.dev',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/api\/mokky/, '')
      },
      '/api/viettel-post': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true
      }
    }
  }
});