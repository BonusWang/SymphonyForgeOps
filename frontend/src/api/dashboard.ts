import axios from 'axios'

export interface ManagedProject {
  id: string
  name: string
  repoUrl: string
  localPath: string
  defaultBranch: string
  stack: string
  status: string
  commandTemplates: string[]
}

export interface WorkOrder {
  id: string
  projectId: string
  title: string
  status: string
  implementationAgent: string
  testCommand: string
  reviewCommand: string
  humanApprovalRequired: boolean
}

export interface AgentRun {
  id: string
  workOrderId: string
  agentName: string
  status: string
  summary: string
  startedAt: string
}

export interface ReviewItem {
  id: string
  workOrderId: string
  type: string
  status: string
  riskLevel: string
  summary: string
}

export interface DashboardSnapshot {
  projectCount: number
  openWorkOrderCount: number
  runningAgentCount: number
  pendingReviewCount: number
  projects: ManagedProject[]
  workOrders: WorkOrder[]
  agentRuns: AgentRun[]
  reviewItems: ReviewItem[]
}

export async function getDashboardSnapshot(): Promise<DashboardSnapshot> {
  const { data } = await axios.get<DashboardSnapshot>('/api/v1/dashboard')
  return data
}

