import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: process.env.VITE_FORGEOPS_API_TARGET || 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})
