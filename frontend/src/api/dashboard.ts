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

export interface WorkerHost {
  workerKey: string
  displayName: string
  protocol: string
  host: string
  capacity: number
  status: string
  availableCapacity: number
  currentRuns: number
  lastError: string | null
}

export interface GithubLink {
  workOrderId: string
  issueUrl: string
  prUrl: string
  checkStatus: string
}

export interface ReviewFinding {
  workOrderId: string
  source: string
  severity: string
  summary: string
  status: string
}

export interface HumanDecision {
  workOrderId: string
  decision: string
  reason: string
  decider: string
}

export interface ReviewDashboard {
  findings: ReviewFinding[]
  humanDecisions: HumanDecision[]
}

export async function getDashboardSnapshot(): Promise<DashboardSnapshot> {
  return request.get<DashboardSnapshot, DashboardSnapshot>('/v1/dashboard')
}

export async function getWorkers(): Promise<WorkerHost[]> {
  return request.get<WorkerHost[], WorkerHost[]>('/v1/workers')
}

export async function getGithubLinks(): Promise<GithubLink[]> {
  return request.get<GithubLink[], GithubLink[]>('/v1/github-links')
}

export async function getReviews(): Promise<ReviewDashboard> {
  return request.get<ReviewDashboard, ReviewDashboard>('/v1/reviews')
}
