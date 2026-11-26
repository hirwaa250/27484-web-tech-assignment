import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // send /api requests to the Spring Boot backend (avoids CORS errors)
      '/api': 'http://localhost:8081',
    },
  },
})
