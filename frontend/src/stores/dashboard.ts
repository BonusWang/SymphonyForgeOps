import { defineStore } from 'pinia'
import { getDashboardSnapshot, type DashboardSnapshot } from '../api/dashboard'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    loading: false,
    error: '',
    snapshot: null as DashboardSnapshot | null
  }),
  actions: {
    async load() {
      this.loading = true
      this.error = ''
      try {
        this.snapshot = await getDashboardSnapshot()
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'Failed to load dashboard'
      } finally {
        this.loading = false
      }
    }
  }
})

