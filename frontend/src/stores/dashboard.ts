import { defineStore } from 'pinia'
import {
  getDashboardSnapshot,
  getGithubLinks,
  getReviews,
  getWorkers,
  type DashboardSnapshot,
  type GithubLink,
  type ReviewDashboard,
  type WorkerHost
} from '../api/dashboard'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    loading: false,
    error: '',
    snapshot: null as DashboardSnapshot | null,
    workers: [] as WorkerHost[],
    githubLinks: [] as GithubLink[],
    reviews: null as ReviewDashboard | null
  }),
  actions: {
    async load() {
      this.loading = true
      this.error = ''
      try {
        const [snapshot, workers, githubLinks, reviews] = await Promise.all([
          getDashboardSnapshot(),
          getWorkers(),
          getGithubLinks(),
          getReviews()
        ])
        this.snapshot = snapshot
        this.workers = workers
        this.githubLinks = githubLinks
        this.reviews = reviews
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'Failed to load dashboard'
      } finally {
        this.loading = false
      }
    }
  }
})
