import request from '../utils/request'

export interface ManagedProject {
  id: string
  name: string
  repoUrl: string
  localPath: string
  defaultBranch: string
  workflowPath: string
  workspaceRoot: string
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
  retryQueueCount: number
  workflowContractCount: number
  orchestratorStatus: string
  workspaceRoot: string
  projects: ManagedProject[]
  workOrders: WorkOrder[]
  agentRuns: AgentRun[]
  reviewItems: ReviewItem[]
}

export async function getDashboardSnapshot(): Promise<DashboardSnapshot> {
  return request.get<DashboardSnapshot, DashboardSnapshot>('/v1/dashboard')
}
